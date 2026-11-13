export function initializeConfirmations() {
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
    });}
