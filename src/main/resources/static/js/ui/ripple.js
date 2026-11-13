export function initializeRipple() {
    // 1. EFEITO DE ONDA TÁTIL (RIPPLE EFFECT)
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.btn, .btn-restart, .btn-next, .option-btn, button:not(.nav-toggle)');
        if (!btn) return;

        if (btn.classList.contains('btn-loading')) return;

        const rect = btn.getBoundingClientRect();
        const ripple = document.createElement('span');
        ripple.className = 'btn-ripple';

        const size = Math.max(rect.width, rect.height) * 2;
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;

        ripple.style.width = `${size}px`;
        ripple.style.height = `${size}px`;
        ripple.style.left = `${x}px`;
        ripple.style.top = `${y}px`;

        btn.appendChild(ripple);

        ripple.addEventListener('animationend', () => {
            ripple.remove();
        });
    });

}
