// ============================
// SISTEMA DE MODALES
// ============================

let currentModal = null;

function openModal(modalId) {
    currentModal = modalId;
    const modal = document.getElementById(`${modalId}-modal`);
    if (modal) {
        modal.classList.add('active');
        
        // Inicializar formularios específicos
        if (modalId === 'add-worker') {
            loadCompaniesForSelect();
        }
    }
}

function closeModal() {
    if (currentModal) {
        const modal = document.getElementById(`${currentModal}-modal`);
        if (modal) {
            modal.classList.remove('active');
        }
        currentModal = null;
    }
    
    const editModal = document.getElementById('edit-modal');
    if (editModal) {
        editModal.classList.remove('active');
    }
}

// ... resto de funciones de modales