"""Real Firefox regression checks. Requires app at localhost:7000 and Firefox."""
import base64
import json
import os
import socket
import subprocess
import tempfile
import time

class Browser:
    def __init__(self):
        self.profile = tempfile.TemporaryDirectory(prefix='cq-firefox-')
        self.process = subprocess.Popen(['firefox', '--headless', '--no-remote', '--profile', self.profile.name, '--marionette'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        for _ in range(100):
            try:
                self.socket = socket.create_connection(('127.0.0.1', 2828), timeout=2)
                break
            except OSError:
                time.sleep(.1)
        else:
            raise RuntimeError('Firefox Marionette unavailable')
        self.socket.settimeout(20)
        self.receive()
        self.sequence = 0
        self.call('WebDriver:NewSession', {'capabilities': {}})
    def receive(self):
        size = b''
        while not size.endswith(b':'):
            size += self.socket.recv(1)
        data = b''
        while len(data) < int(size[:-1]):
            data += self.socket.recv(int(size[:-1]) - len(data))
        return json.loads(data)
    def call(self, name, args):
        self.sequence += 1
        packet = json.dumps([0, self.sequence, name, args]).encode()
        self.socket.sendall(str(len(packet)).encode() + b':' + packet)
        reply = self.receive()
        if reply[2]:
            raise RuntimeError(reply[2])
        return reply[3]
    def script(self, script):
        result = self.call('WebDriver:ExecuteScript', {'script': script, 'args': [], 'newSandbox': False, 'sandbox': 'default'})
        return result['value']
    def navigate(self, path):
        self.call('WebDriver:Navigate', {'url': os.environ.get('CODEQUEST_TEST_URL', 'http://localhost:7001') + path})
    def close(self):
        self.socket.close()
        self.process.terminate()
        self.process.wait(timeout=10)
        self.profile.cleanup()

browser = Browser()
try:
    browser.navigate('/')
    assert browser.script("return !!document.querySelector('.cq-guide img')"), 'Home guide missing'
    assert browser.script("return document.querySelectorAll('.cq-tour-pane').length") == 4
    browser.script("localStorage.setItem('cq-guide-v2:visitante', JSON.stringify({skipped:true})); location.reload(); return true;")
    browser.navigate('/')
    for mode in ['dark', 'light']:
        browser.script("localStorage.setItem('cq-theme', '" + mode + "'); return true;")
        for path, selector in [('/', '.terminal'), ('/modulos', '.module-card'), ('/modulos/1', '.phase-map-card'), ('/login', '.auth-card'), ('/admin/modulos', '.admin-table')]:
            browser.navigate(path)
            result = browser.script("const e = document.querySelector('" + selector + "'); return e ? {bg:getComputedStyle(e).backgroundColor, color:getComputedStyle(e).color} : null;")
            if result is None:
                raise AssertionError('Missing ' + selector)
            assert result['bg'] != result['color'], (path, mode, result)
            if mode == 'dark':
                assert result['bg'] != 'rgb(255, 255, 255)', (path, result)
            print('PASS', mode, path, result)
    browser.navigate('/')
    browser.script("localStorage.setItem('cq-theme','dark'); localStorage.removeItem('cq-guide-v2:visitante'); return true;")
    browser.navigate('/')
    browser.script("document.querySelector('.cq-guide-actions button').click(); document.querySelector('.cq-guide-actions button:last-of-type').previousElementSibling.click(); return true;")
    geometry = browser.script("const t=document.querySelector('.hero-actions a[href=\"/modulos\"]'); const r=t.getBoundingClientRect(); const o=document.querySelector('.cq-tour-outline').getBoundingClientRect(); const p=document.querySelector('.cq-guide').getBoundingClientRect(); return {delta:Math.abs(o.top-r.top+8),panelTop:p.top,targetVisible:document.elementFromPoint(r.x+r.width/2,r.y+r.height/2).closest('a')===t};")
    assert geometry['delta'] < 2 and geometry['panelTop'] >= 0 and geometry['targetVisible'], geometry
    # Save a screenshot for visual inspection, without changing application files.
    screenshot = browser.call('WebDriver:TakeScreenshot', {'full': False})['value']
    with open('/tmp/codequest-guide.png', 'wb') as output:
        output.write(base64.b64decode(screenshot))
    element = browser.call('WebDriver:FindElement', {'using':'css selector', 'value':'.hero-actions a[href="/modulos"]'})['value']
    browser.call('WebDriver:ElementClick', {'id': next(iter(element.values()))})
    assert browser.script("return location.pathname") == '/modulos'
    assert browser.script("return document.querySelector('.cq-guide h2').textContent") == 'Seus mundos'
    print('PASS real click advances to worlds tutorial')
    browser.call('WebDriver:SetWindowRect', {'width':390, 'height':844})
    browser.navigate('/')
    browser.script("localStorage.removeItem('cq-guide-v2:visitante'); return true;")
    browser.navigate('/')
    for _ in range(2):
        browser.script("const buttons=Array.from(document.querySelectorAll('.cq-guide-actions button')); buttons.find(b=>b.textContent==='Continuar').click(); return true;")
    assert browser.script("const p=document.querySelector('.cq-guide').getBoundingClientRect(); return p.left>=0 && p.right<=innerWidth && p.top>=0 && p.bottom<=innerHeight;"), 'Mobile panel outside viewport'
    assert browser.script("const t=document.querySelector('.hero-actions a[href=\"/modulos\"]');const r=t.getBoundingClientRect();return document.elementFromPoint(r.x+r.width/2,r.y+r.height/2).closest('a')===t;"), 'Mobile target blocked'
    print('PASS mobile guide stays within viewport and target stays clickable')
    browser.script("localStorage.setItem('cq-guide-v2:visitante', JSON.stringify({skipped:true})); return true;")
    browser.call('WebDriver:SetWindowRect', {'width':1366, 'height':900})
    browser.navigate('/modulos/1/quiz')
    def click(selector):
        element = browser.call('WebDriver:FindElement', {'using':'css selector', 'value':selector})['value']
        browser.call('WebDriver:ElementClick', {'id':next(iter(element.values()))})
    def wait_for(script, timeout=10):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            if browser.script(script): return
            time.sleep(.1)
        raise AssertionError(script)
    wait_for("return document.querySelectorAll('.option-btn').length===4")
    click('.option-btn[data-key="B"]')
    assert browser.script("return document.querySelector('.option-btn.wrong')!==null")
    click('#btnNext')
    click('.option-btn[data-key="A"]')
    assert '5 segundos' in browser.script("return document.getElementById('btnNext').innerText")
    wait_for("return document.getElementById('questionCounter').innerText==='Questão 3/10'")
    for _ in range(8):
        click('.option-btn[data-key="A"]')
        click('#btnNext')
    wait_for("return document.getElementById('resultsContainer').style.display==='block'")
    assert browser.script("return document.getElementById('resultsTitle').innerText") == 'Quiz Concluído!'
    assert '+200 XP' in browser.script("return document.getElementById('totalXpDisplay').innerText")
    print('PASS quiz modules, wrong answer, five-second advance and successful result')
    print('PASS home tour, masking panes and theme surfaces')
finally:
    browser.close()
