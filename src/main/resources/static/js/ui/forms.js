export function initializeForms() {
    // 2. INDICADOR DE CARREGAMENTO NOS FORMULÁRIOS
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => {
            if (e.defaultPrevented || !form.checkValidity()) return;

            const submitBtn = form.querySelector('button[type="submit"], input[type="submit"], .btn-primary');
            if (submitBtn && !submitBtn.classList.contains('btn-loading')) {
                submitBtn.dataset.originalContent = submitBtn.innerHTML;
                submitBtn.classList.add('btn-loading');
                submitBtn.setAttribute('disabled', 'true');

                submitBtn.innerHTML = `
                    <span class="btn-spinner"></span>
                    <span class="btn-loading-text">Processando...</span>
                `;
            }
        });
    });

}
