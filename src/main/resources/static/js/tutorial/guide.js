import { pages } from './steps.js';
import { read, save } from '../storage.js';
function startGuide(profile) {
    const key = `cq-guide-v2:${profile}`;
    const path = location.pathname;
    const pageIndex = pages.findIndex(page => page.match.test(path));
    if (pageIndex < 0) return;
    const steps = pages[pageIndex].steps;
    let state;
    try { state = JSON.parse(read(key) || '{}'); } catch (_) { state = {}; }
    if (!state || typeof state !== 'object' || Array.isArray(state)) state = {};
    const persist = () => save(key, JSON.stringify(state));
    let cleanup = () => {};
    function show(index) {
        cleanup();
        const step = steps[index];
        if (!step) { state[pageIndex] = 'done'; persist(); return; }
        const target = step[0].split(',').map(selector => document.querySelector(selector.trim())).find(Boolean);
        if (!target) { show(index + 1); return; }
        state[pageIndex] = index;
        persist();
        document.documentElement.dataset.guideActive = 'true';
        target.scrollIntoView({ block: 'center', behavior: 'instant' });
        const previousFocus = document.activeElement;
        const panes = Array.from({ length: 4 }, () => {
            const pane = document.createElement('div');
            pane.className = 'cq-tour-pane';
            pane.setAttribute('aria-hidden', 'true');
            document.body.append(pane);
            return pane;
        });
        const outline = document.createElement('div');
        outline.className = 'cq-tour-outline';
        outline.setAttribute('aria-hidden', 'true');
        const panel = document.createElement('aside');
        panel.className = 'cq-guide';
        panel.setAttribute('role', 'region');
        panel.setAttribute('aria-label', 'Tutorial CodeQuest');
        panel.setAttribute('aria-live', 'polite');
        const mascot = document.createElement('img');
        mascot.src = '/img/Joinha.png';
        mascot.alt = 'Mascote CodeQuest';
        const heading = document.createElement('h2');
        heading.textContent = step[1];
        const count = document.createElement('small');
        count.textContent = `Orientação ${index + 1} de ${steps.length}`;
        const description = document.createElement('p');
        description.textContent = step[2];
        const actions = document.createElement('div');
        actions.className = 'cq-guide-actions';
        const addButton = (label, action) => {
            const button = document.createElement('button');
            button.type = 'button'; button.className = 'btn btn-secondary';
            button.textContent = label; button.onclick = action;
            actions.append(button);
            return button;
        };
        if (index > 0) addButton('Voltar', () => show(index - 1));
        const next = () => show(index + 1);
        if (!step[3]) addButton(index === steps.length - 1 ? 'Entendi' : 'Continuar', next);
        addButton('Pular tutorial', () => { state = { skipped: true }; persist(); cleanup(); });
        panel.append(mascot, count, heading, description, actions);
        document.body.append(outline, panel);
        function rect(element, x, y, w, h) {
            Object.assign(element.style, { left: x + 'px', top: y + 'px', width: Math.max(0, w) + 'px', height: Math.max(0, h) + 'px' });
        }
        function position() {
            const box = target.getBoundingClientRect();
            const w = innerWidth, h = innerHeight;
            const left = Math.max(0, box.left - 8), top = Math.max(0, box.top - 8);
            const right = Math.min(w, box.right + 8), bottom = Math.min(h, box.bottom + 8);
            rect(panes[0], 0, 0, w, top);
            rect(panes[1], 0, bottom, w, h - bottom);
            rect(panes[2], 0, top, left, bottom - top);
            rect(panes[3], right, top, w - right, bottom - top);
            rect(outline, left, top, right - left, bottom - top);
            const pw = panel.offsetWidth, ph = panel.offsetHeight;
            let x = right + 20, y = top;
            if (x + pw > w - 12) x = left - pw - 20;
            if (x < 12) { x = Math.max(12, Math.min(w - pw - 12, left)); y = bottom + 12; }
            if (y + ph > h - 12) y = Math.max(12, top - ph - 12);
            panel.style.left = x + 'px'; panel.style.top = Math.min(h - ph - 12, Math.max(12, y)) + 'px';
        }
        const clicked = event => {
            if (event.defaultPrevented || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
            state[pageIndex] = 'done'; persist(); cleanup();
        };
        const escape = event => { if (event.key === 'Escape') { state.skipped = true; persist(); cleanup(); } };
        if (step[3]) target.addEventListener('click', clicked);
        window.addEventListener('resize', position);
        window.addEventListener('scroll', position, true);
        document.addEventListener('keydown', escape);
        cleanup = () => {
            panes.forEach(pane => pane.remove()); outline.remove(); panel.remove();
            delete document.documentElement.dataset.guideActive;
            target.removeEventListener('click', clicked);
            window.removeEventListener('resize', position);
            window.removeEventListener('scroll', position, true);
            document.removeEventListener('keydown', escape);
            if (previousFocus && previousFocus.isConnected) previousFocus.focus({ preventScroll: true });
        };
        position();
        if (step[3]) target.focus({ preventScroll: true });
        else actions.querySelector('button').focus({ preventScroll: true });
    }
    document.querySelectorAll('.cq-help').forEach(button => button.onclick = () => { state = {}; persist(); show(0); });
    if (!state.skipped && state[pageIndex] !== 'done') show(Number.isInteger(state[pageIndex]) ? state[pageIndex] : 0);
}

export { startGuide };
