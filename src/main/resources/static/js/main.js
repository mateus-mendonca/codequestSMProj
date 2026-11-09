/**
 * CodeQuest - Efeitos Visuais, Interações, Toasts, Confirmações Modais e Animações
 */
document.addEventListener('DOMContentLoaded', () => {

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

    // 2. INDICADOR DE CARREGAMENTO NOS FORMULÁRIOS
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => {
            if (!form.checkValidity()) return;

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

    // 3. SISTEMA DE TOAST NOTIFICATIONS GAMIFICADAS
    window.showToast = function(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = '/img/sprites/icon-bulb.png';
        if (type === 'success') icon = '/img/sprites/icon-party.png';
        if (type === 'warning') icon = '/img/sprites/icon-lock.png';
        if (type === 'danger') icon = '/img/sprites/icon-target.png';

        toast.innerHTML = `
            <span class="toast-icon"><img src="${icon}" alt="" class="sprite-icon-md"></span>
            <span class="toast-msg">${message}</span>
            <span class="toast-close">&times;</span>
        `;

        toast.querySelector('.toast-close').onclick = () => {
            toast.classList.add('toast-hiding');
            setTimeout(() => toast.remove(), 300);
        };

        container.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.classList.add('toast-hiding');
                setTimeout(() => toast.remove(), 300);
            }
        }, 4000);
    };

    // Auto-detectar parâmetros de URL para exibir toasts
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('bloqueado') === 'true') {
        showToast('Esta etapa ainda está bloqueada. Siga a trilha em ordem!', 'warning');
    }
    if (urlParams.get('quizBloqueado') === 'true') {
        showToast('Complete todas as etapas do módulo para desbloquear o Quiz Final!', 'warning');
    }
    if (urlParams.get('moduloBloqueado') === 'true') {
        showToast('Conclua o módulo anterior para desbloquear este mundo!', 'warning');
    }

    // 4. MODAL ELEGANTE DE CONFIRMAÇÃO PARA AÇÕES DELETIVAS
    document.querySelectorAll('form[action*="excluir"], form.confirm-delete').forEach(form => {
        form.addEventListener('submit', (e) => {
            if (form.dataset.confirmed === 'true') {
                return; // Já foi confirmado pelo modal
            }

            e.preventDefault();

            let modal = document.getElementById('cq-confirm-modal');
            if (!modal) {
                modal = document.createElement('div');
                modal.id = 'cq-confirm-modal';
                modal.className = 'cq-modal-backdrop';
                modal.innerHTML = `
                    <div class="cq-modal-card">
                        <div class="cq-modal-icon"><img src="/img/sprites/icon-target.png" alt="Atenção" class="sprite-icon-lg"></div>
                        <h3 class="cq-modal-title">Confirmar Exclusão</h3>
                        <p class="cq-modal-desc">Você tem certeza que deseja excluir este registro? Essa ação não pode ser desfeita.</p>
                        <div class="cq-modal-actions">
                            <button type="button" class="btn btn-secondary" id="modalBtnCancel">Cancelar</button>
                            <button type="button" class="btn btn-danger" id="modalBtnConfirm">Sim, Excluir</button>
                        </div>
                    </div>
                `;
                document.body.appendChild(modal);
            }

            modal.style.display = 'flex';

            const cancelBtn = modal.querySelector('#modalBtnCancel');
            const confirmBtn = modal.querySelector('#modalBtnConfirm');

            const closeModal = () => {
                modal.style.display = 'none';
            };

            cancelBtn.onclick = closeModal;
            modal.onclick = (event) => {
                if (event.target === modal) closeModal();
            };

            confirmBtn.onclick = () => {
                closeModal();
                form.dataset.confirmed = 'true';
                form.submit();
            };
        });
    });
});
