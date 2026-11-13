export function initializeNotifications() {
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

}
