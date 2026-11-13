// Run: gjs src/test/js/experience.gjs.js
const GLib = imports.gi.GLib;
const themeSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/experience.js')[1]);
const guideSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/tutorial/guide.js')[1]).replace(/import .*?;\n/g, '').replace(/export \{.*?\};/g, '');
const storageSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/storage.js')[1]).replace(/export /g, '');
const stepSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/tutorial/steps.js')[1]).replace(/export /g, '');
const source = stepSource + themeSource + storageSource + guideSource + "\ndocument.addEventListener('DOMContentLoaded', () => startGuide('visitante'));";
let total = 0;
function assert(value, message) { if (!value) throw Error(message); }
class Element {
    constructor(tag = 'div') { this.tag = tag; this.children = []; this.style = {}; this.listeners = {}; this.attributes = {}; this.dataset = {}; this.isConnected = true; this.offsetWidth = 340; this.offsetHeight = 190; }
    append(...elements) { elements.forEach(e => { e.parent = this; this.children.push(e); }); }
    prepend(e) { this.children.unshift(e); }
    remove() { this.isConnected = false; if (this.parent) this.parent.children = this.parent.children.filter(c => c !== this); }
    setAttribute(k, v) { this.attributes[k] = v; }
    addEventListener(k, fn) { this.listeners[k] = fn; }
    removeEventListener(k) { delete this.listeners[k]; }
    focus() {}
    scrollIntoView() {}
    getBoundingClientRect() { return { left: 40, top: 100, right: 340, bottom: 160 }; }
    querySelector(selector) { return this.children.find(c => c.tag === selector) || this.children.map(c => c.querySelector(selector)).find(Boolean) || null; }
    contains(e) { return e === this || this.children.some(c => c.contains(e)); }
}
function boot(path = '/', storage = {}, options = {}) {
    const body = new Element('body');
    const theme = new Element('button'), help = new Element('button');
    const targets = {};
    const document = { body, documentElement: new Element('html'), activeElement: new Element(),
        createElement: tag => new Element(tag),
        addEventListener(k, fn) { const previous = this[k]; this[k] = previous ? () => { previous(); fn(); } : fn; }, removeEventListener(k) { delete this[k]; },
        querySelector(selector) {
            if (selector === '[data-world]') return null;
            return targets[selector] || (targets[selector] = new Element());
        },
        querySelectorAll(selector) { return selector === '.cq-theme' ? [theme] : selector === '.cq-help' ? [help] : []; }
    };
    const localStorage = {
        getItem(k) { if (options.blocked) throw Error('denied'); return storage[k] || null; },
        setItem(k, v) { if (options.blocked) throw Error('denied'); storage[k] = v; }
    };
    const window = { addEventListener() {}, removeEventListener() {} };
    const Params = class { get() { return null; } };
    new Function('document', 'localStorage', 'matchMedia', 'location', 'window', 'innerWidth', 'innerHeight', 'URLSearchParams', source)(document, localStorage, () => ({ matches: !!options.dark }), { pathname: path, search: '' }, window, 1000, 800, Params);
    document.DOMContentLoaded();
    return { body, document, targets, theme, help, storage, panel: () => body.children.find(e => e.className === 'cq-guide') };
}
function clickLabel(env, label) {
    const actions = env.panel().children.find(e => e.className === 'cq-guide-actions');
    const button = actions.children.find(e => e.textContent === label);
    assert(button, 'Missing button: ' + label); button.onclick();
}
function test(name, fn) { fn(); total++; print('PASS ' + name); }
test('saved theme overrides system and toggle persists', () => {
    const e = boot('/admin/modulos', { 'cq-theme': 'light' }, { dark: true });
    assert(e.document.documentElement.dataset.theme === 'light', 'saved theme');
    e.theme.onclick(); assert(e.storage['cq-theme'] === 'dark', 'toggle');
});
test('blocked storage still initializes dark theme and guide', () => {
    const e = boot('/', {}, { blocked: true, dark: true });
    assert(e.document.documentElement.dataset.theme === 'dark', 'system preference'); assert(e.panel(), 'guide');
});
test('home guide opens with mascot and four masking panes', () => {
    const e = boot(); assert(e.panel().querySelector('img').src === '/img/Joinha.png', 'mascot');
    assert(e.body.children.filter(c => c.className === 'cq-tour-pane').length === 4, 'panes');
    assert(e.panel().querySelector('h2').textContent === 'Bem-vindo ao CodeQuest', 'home start');
});
test('navigation waits for click and resumes on worlds', () => {
    const e = boot(); clickLabel(e, 'Continuar'); clickLabel(e, 'Continuar');
    const key = 'cq-guide-v2:visitante';
    assert(JSON.parse(e.storage[key])[0] === 2, 'pending navigation');
    e.targets['.hero-actions a[href="/modulos"]'].listeners.click({ button: 0 });
    assert(!e.panel(), 'cleanup'); assert(JSON.parse(e.storage[key])[0] === 'done', 'saved navigation');
    const worlds = boot('/modulos', e.storage); assert(worlds.panel().querySelector('h2').textContent === 'Seus mundos', 'next page');
});
test('reload resumes current explanation and back updates it', () => {
    const e = boot(); clickLabel(e, 'Continuar');
    const resumed = boot('/', e.storage); assert(resumed.panel().querySelector('h2').textContent === 'Escolha seu tema', 'resume');
    clickLabel(resumed, 'Voltar'); assert(resumed.panel().querySelector('h2').textContent === 'Bem-vindo ao CodeQuest', 'back');
});
test('skip persists across pages and replay clears it', () => {
    const e = boot(); clickLabel(e, 'Pular tutorial');
    const worlds = boot('/modulos', e.storage); assert(!worlds.panel(), 'skip persisted');
    worlds.help.onclick(); assert(worlds.panel(), 'replay');
});
test('modified clicks preserve pending navigation', () => {
    const e = boot(); clickLabel(e, 'Continuar'); clickLabel(e, 'Continuar');
    e.targets['.hero-actions a[href="/modulos"]'].listeners.click({ button: 0, ctrlKey: true });
    assert(e.panel(), 'guide stays for modified click');
});
test('quiz tutorial completes and stays closed on return', () => {
    const e = boot('/modulos/1/quiz'); clickLabel(e, 'Continuar'); clickLabel(e, 'Entendi');
    assert(!boot('/modulos/1/quiz', e.storage).panel(), 'completion persisted');
});
test('corrupted persisted state recovers', () => { assert(boot('/', { 'cq-guide-v2:visitante': '{broken' }).panel(), 'recovery'); });
test('new visitor follows light system preference', () => {
    const e = boot('/admin/modulos');
    assert(e.document.documentElement.dataset.theme === 'light', 'light preference');
});
test('new visitor follows dark system preference', () => {
    const e = boot('/admin/modulos', {}, { dark: true });
    assert(e.document.documentElement.dataset.theme === 'dark', 'dark preference');
});
test('invalid saved theme falls back to system preference', () => {
    const e = boot('/admin/modulos', { 'cq-theme': 'invalid' }, { dark: true });
    assert(e.document.documentElement.dataset.theme === 'dark', 'invalid preference fallback');
});
print(total + ' tests passed');
