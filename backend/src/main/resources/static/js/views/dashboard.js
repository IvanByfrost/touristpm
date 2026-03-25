// Vista: Dashboard
import { state } from '../state.js';
import { toast } from '../ui.js';
import { bookingApi } from '../api.js';

export const template = `
<section id="view-dashboard" class="view">
  <div class="container" style="padding: 2rem 1rem;">
    <h2 class="title is-2 mb-6">Mi <span class="accent-text">Panel</span></h2>
    
    <div class="columns is-multiline">
      <!-- User Profile Card -->
      <div class="column is-4">
        <div class="glass-card p-5" style="height: 100%;">
          <div class="has-text-centered mb-4">
            <div style="width: 80px; height: 80px; background: var(--primary-blue); border-radius: 50%; margin: 0 auto; display: flex; align-items: center; justify-content: center; color: white; font-size: 2rem; font-weight: bold; border: 3px solid white; box-shadow: 0 10px 20px rgba(0,0,0,0.1);">
              U
            </div>
          </div>
          <h3 id="user-name" class="title is-4 has-text-centered mb-2">Usuario</h3>
          <p id="user-email" class="has-text-centered subtitle is-6 mb-5">usuario@correo.com</p>
          
          <hr style="background-color: rgba(255,255,255,0.2);">
          
          <div class="is-flex is-justify-content-between mb-2">
            <span>Miembro desde:</span>
            <strong id="user-join">-</strong>
          </div>
          
          <button id="logout-btn" class="button is-danger is-outlined is-fullwidth mt-5">Cerrar Sesión</button>
        </div>
      </div>
      
      <!-- Activity/Bookings -->
      <div class="column is-8">
        <div class="glass-card p-5 mb-5">
          <h3 class="title is-4 mb-4">Mis Reservas</h3>
          <div id="reservas-list" class="space-y-4">
            <!-- Dinámico -->
          </div>
        </div>
        
        <div class="glass-card p-5">
          <h3 class="title is-4 mb-4">Favoritos</h3>
          <div id="favoritos-list" class="space-y-4">
            <!-- Dinámico -->
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`;

export function initDashboard() {
    console.log('Inicializando dashboard...');

    // Actualizar información del usuario
    updateUserInfo();

    // Actualizar reservas y favoritos
    updateReservas();
    updateFavoritos();

    // Setup logout (only once)
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn && !logoutBtn.dataset.initialized) {
        logoutBtn.dataset.initialized = 'true';
        logoutBtn.addEventListener('click', handleLogout);
    }
}

function updateUserInfo() {
    if (!state.user) {
        // Redirigir a login si no hay usuario
        location.hash = '#/login';
        return;
    }

    document.getElementById('user-name').textContent = state.user.fullName || state.user.username || 'Usuario';
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