// Vista: Dashboard
import { state } from '../state.js';
import { toast } from '../ui.js';

export function initDashboard() {
    console.log('Inicializando dashboard...');
    
    // Actualizar información del usuario
    updateUserInfo();
    
    // Actualizar reservas y favoritos
    updateReservas();
    updateFavoritos();
    
    // Setup logout
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
}

function updateUserInfo() {
    if (!state.user) {
        // Redirigir a login si no hay usuario
        location.hash = '#/login';
        return;
    }
    
    document.getElementById('user-name').textContent = state.user.nombre || 'Usuario';
    document.getElementById('user-email').textContent = state.user.email || '-';
    document.getElementById('user-join').textContent = new Date().toLocaleDateString('es-ES');
}

function updateReservas() {
    const reservasList = document.getElementById('reservas-list');
    if (!reservasList) return;
    
    // Usar el carrito como "reservas" por ahora
    if (state.cart.length === 0) {
        reservasList.innerHTML = '<p class="has-text-centered">No tienes reservas aún</p>';
        return;
    }
    
    reservasList.innerHTML = state.cart.map(item => `
        <div class="tc-cart-item">
            <div>
                <strong>${item.nombre}</strong>
                <br>
                <small>Cantidad: ${item.quantity} • $${item.precio} c/u</small>
            </div>
            <div>
                <strong>$${item.precio * item.quantity}</strong>
            </div>
        </div>
    `).join('');
}

function updateFavoritos() {
    const favoritosList = document.getElementById('favoritos-list');
    if (!favoritosList) return;
    
    if (state.favorites.length === 0) {
        favoritosList.innerHTML = '<p class="has-text-centered">No tienes favoritos</p>';
        return;
    }
    
    favoritosList.innerHTML = state.favorites.map(fav => `
        <div class="tc-cart-item">
            <div>
                <strong>${fav.nombre}</strong>
                <br>
                <small>${fav.descripcion}</small>
            </div>
            <div>
                <strong>$${fav.precio}</strong>
                <br>
                <button class="button is-small is-danger" onclick="removeFavorite(${fav.id})">
                    ❌
                </button>
            </div>
        </div>
    `).join('');
}

function handleLogout() {
    state.clearUser();
    state.clearCart();
    toast('Sesión cerrada correctamente', 'info');
    location.hash = '#/inicio';
}

// Función global para remover favoritos
window.removeFavorite = (viajeId) => {
    const viaje = state.favorites.find(fav => fav.id === viajeId);
    if (viaje) {
        state.toggleFavorite(viaje);
        updateFavoritos();
        toast('Eliminado de favoritos', 'info');
    }
};