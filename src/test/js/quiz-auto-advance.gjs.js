// Run: gjs src/test/js/quiz-auto-advance.gjs.js
const GLib = imports.gi.GLib;
const quizSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/quiz/quiz.js')[1]);
const apiSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/quiz/api.js')[1]).replace(/export /g, '');
const viewSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/quiz/view.js')[1]).replace(/import .*?;\n/g, '').replace(/export /g, '');
const timerSource = imports.byteArray.toString(GLib.file_get_contents('src/main/resources/static/js/quiz/auto-advance.js')[1]).replace(/export /g, '');
const source = timerSource + viewSource + apiSource + "\nlet playSound = () => {}; let showResults = () => {};\n" + quizSource.replace(/import .*?;\n/g, '').split("document.getElementById('btnNext').addEventListener")[0];
function assert(condition, message) { if (!condition) throw Error(message); }
function fixture(count = 3) {
    let clock = 0, serial = 0, submissions = 0;
    const timers = new Map(), nodes = {}, events = {};
    function element() {
        return { style: {}, dataset: {}, children: [], classList: { add() {} },
            appendChild(child) { this.children.push(child); },
            set innerHTML(value) { this.children = []; this.html = value; },
            get innerHTML() { return this.html; }
        };
    }
    const document = {
        querySelector() { return { dataset: { moduloId: '1' } }; },
        getElementById(id) { return nodes[id] || (nodes[id] = element()); },
        createElement: element,
        querySelectorAll() { return nodes.optionsList.children; }
    };
    const window = { AudioContext: function () {}, addEventListener(name, callback) { events[name] = callback; } };
    const schedule = (callback, delay, repeat) => { const id = ++serial; timers.set(id, { callback, at: clock + delay, repeat }); return id; };
    const fetch = async () => { submissions++; return { json: async () => ({ passou: true }) }; };
    const api = new Function('document', 'window', 'fetch', 'setTimeout', 'clearTimeout', 'setInterval', 'clearInterval', source + `
        playSound = () => {};
        showResults = () => {};
        return { init(data) { questions = data; renderQuestion(); },
            answer(key) { const button = document.querySelectorAll('.option-btn').find(b => b.dataset.key === key); selectOption(questions[currentIndex].id, key, button); },
            next: nextQuestion, finish: finishQuiz,
            index: () => currentIndex, lives: () => lives, answers: () => userAnswers };
    `)(document, window, fetch,
        (fn, delay) => schedule(fn, delay, 0), id => timers.delete(id),
        (fn, delay) => schedule(fn, delay, delay), id => timers.delete(id));
    api.init(Array.from({ length: count }, (_, i) => ({ id: i + 1, correta: 'A', alternativas: [{ key: 'A', text: 'Correct' }, { key: 'B', text: 'Wrong' }] })));
    return { api, nodes, events, submissions: () => submissions, timers,
        advance(ms) {
            const end = clock + ms;
            for (;;) {
                const next = [...timers].filter(([, t]) => t.at <= end).sort((a, b) => a[1].at - b[1].at)[0];
                if (!next) break;
                const [id, timer] = next;
                clock = timer.at;
                if (timer.repeat) timer.at += timer.repeat; else timers.delete(id);
                timer.callback();
            }
            clock = end;
        }
    };
}
let total = 0;
function test(name, run) { run(); total++; print('PASS ' + name); }
test('correct answer waits five seconds and updates countdown', () => {
    const e = fixture(); e.api.answer('A');
    assert(e.nodes.btnNext.innerText.includes('5 segundos'), 'initial countdown');
    e.advance(4000); assert(e.api.index() === 0, 'must wait');
    assert(e.nodes.btnNext.innerText.includes('1 segundo'), 'countdown updated');
    e.advance(1000); assert(e.api.index() === 1, 'advances at five seconds');
    assert(e.api.answers()[1] === 'A', 'answer preserved');
    assert(e.timers.size === 0, 'timers cleared');
});
test('wrong answer waits for manual advance and loses one life', () => {
    const e = fixture(); e.api.answer('B'); e.advance(10000);
    assert(e.api.index() === 0 && e.api.lives() === 2, 'wrong answer behavior');
    assert(e.timers.size === 0, 'no automatic timer');
    e.api.next(); assert(e.api.index() === 1, 'manual next');
});
test('manual advance cancels timer and prevents skipping unanswered questions', () => {
    const e = fixture(); e.api.answer('A'); e.advance(2000); e.api.next(); e.api.next(); e.advance(10000);
    assert(e.api.index() === 1, 'no double advance');
});
test('stale timer does not advance a subsequently answered question early', () => {
    const e = fixture(); e.api.answer('A'); e.advance(2000); e.api.next(); e.api.answer('A');
    e.advance(3000); assert(e.api.index() === 1, 'old timer canceled');
    e.advance(2000); assert(e.api.index() === 2, 'new question receives five seconds');
});
test('last correct answer submits exactly once', () => {
    const e = fixture(1); e.api.answer('A');
    assert(e.nodes.btnNext.innerText.includes('Ver resultado'), 'last question label');
    e.advance(5000); e.api.next(); e.api.finish();
    assert(e.submissions() === 1, 'only one submission');
});
test('manual last-question advance cancels delayed submission', () => {
    const e = fixture(1); e.api.answer('A'); e.api.next(); e.advance(10000);
    assert(e.submissions() === 1 && e.timers.size === 0, 'single manual submission');
});
test('leaving page cancels automatic advance', () => {
    const e = fixture(); e.api.answer('A'); e.events.pagehide(); e.advance(10000);
    assert(e.api.index() === 0 && e.timers.size === 0, 'no advance after pagehide');
    assert(e.nodes.btnNext.innerText === 'Continuar →', 'manual fallback on return');
});
test('repeated answer clicks do not create extra timers', () => {
    const e = fixture(); e.api.answer('A'); e.api.answer('B');
    assert(e.timers.size === 2 && e.api.answers()[1] === 'A', 'answer lock');
    e.advance(5000); assert(e.api.index() === 1, 'one advance');
});
print(total + ' tests passed');
