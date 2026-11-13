(() => {
    'use strict';
    const read = key => { try { return localStorage.getItem(key); } catch (_) { return null; } };
    const save = (key, value) => { try { localStorage.setItem(key, value); } catch (_) { /* Storage may be unavailable. */ } };
    const storedTheme = read('cq-theme');
    const initialTheme = ['dark', 'light'].includes(storedTheme) ? storedTheme : (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    document.documentElement.dataset.theme = initialTheme;
    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('.cq-theme').forEach(button => {
            const label = () => button.setAttribute('aria-label', document.documentElement.dataset.theme === 'dark' ? 'Ativar modo claro' : 'Ativar modo escuro');
            label();
            button.onclick = () => {
                const theme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
                document.documentElement.dataset.theme = theme;
                save('cq-theme', theme);
                label();
            };
        });
    });
})();
