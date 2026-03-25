// Vista: Dashboard
import { state } from '../state.js';
import { toast } from '../ui.js';
import { bookingApi } from '../api.js';

export const template = `
<section id="view-dashboard" class="view">
  <div class="container" style="padding: 2rem 1rem;">
    <h2 class="title is-2 mb-6">Mi <span class="accent-text">Panel</span></h2>
    
    <div class="columns is-multiline">
      <div class="column is-4">
        <div class="glass-card p-6 has-text-centered" style="height: auto; min-height: 400px; border-radius: 40px; border: 2px solid rgba(255,255,255,0.4);">
          <!-- Avatar Container -->
          <div class="is-flex is-justify-content-center mb-5">
            <div id="user-avatar" style="width: 100px; height: 100px; background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 3rem; font-weight: 900; border: 4px solid white; box-shadow: 0 20px 40px rgba(59, 130, 246, 0.3);">
              U
            </div>
          </div>
          
          <!-- Info Container -->
          <div class="user-info-text mb-6">
            <h3 id="user-name" class="title is-3 mb-5" style="color: var(--primary-blue) !important; font-weight: 800; letter-spacing: -1px;">Usuario</h3>
            <p id="user-email" class="subtitle is-6 has-text-grey" style="opacity: 0.8;">usuario@correo.com</p>
          </div>
          
          <div class="stats-mini is-flex is-justify-content-around p-4 mb-6" style="background: rgba(0,0,0,0.03); border-radius: 20px;">
             <div class="has-text-centered">
                <span class="is-block is-size-7 has-text-grey uppercase">Miembro</span>
                <strong id="user-join" class="is-block is-size-6">-</strong>
             </div>
          </div>
          
          <div class="buttons">
            <button id="edit-profile-btn" class="button is-link is-light is-fullwidth is-rounded py-5" style="height: auto; font-weight: 700;">
              ✏️ Editar Perfil
            </button>
            <button id="consulta-btn" class="button is-info is-light is-fullwidth is-rounded mt-3">
              🔍 Consultar Reserva (Email)
            </button>
            <button id="logout-btn" class="button is-danger is-outlined is-fullwidth is-rounded mt-3">
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>

      <!-- Modal Editar Perfil -->
      <div id="edit-profile-modal" class="modal">
        <div class="modal-background"></div>
        <div class="modal-card">
          <header class="modal-card-head" style="background: var(--glass-bg); backdrop-filter: blur(10px);">
            <p class="modal-card-title has-text-white">Editar Perfil</p>
            <button class="delete modal-close-btn" aria-label="close"></button>
          </header>
          <section class="modal-card-body" style="background: #1a202c; color: white;">
            <form id="edit-profile-form">
              <div class="field">
                <label class="label has-text-grey-light">Nombre Completo</label>
                <div class="control">
                  <input id="edit-name" class="input" type="text" placeholder="Tu nombre" required>
                </div>
              </div>
              <div class="field">
                <label class="label has-text-grey-light">Correo Electrónico</label>
                <div class="control">
                  <input id="edit-email" class="input" type="email" placeholder="tu@email.com" required>
                </div>
              </div>
              <div class="field">
                <label class="label has-text-grey-light">Documento / Pasaporte</label>
                <div class="control">
                  <input id="edit-doc" class="input" type="text" placeholder="Nº Documento">
                </div>
              </div>
            </form>
          </section>
          <footer class="modal-card-foot" style="background: var(--glass-bg); border-top: none;">
            <button id="save-profile-btn" class="button is-link is-fullwidth">Guardar Cambios</button>
          </footer>
        </div>
      </div>
      
      <div class="column is-8">
        <div class="is-flex is-justify-content-between is-align-items-center mb-4">
            <h3 class="title is-4 mb-0">Mis Reservas</h3>
            <a href="#/catalogo" class="button is-small is-link is-light is-rounded">
                + Nueva Reserva
            </a>
        </div>
        
        <div class="glass-card p-5 mb-5">
            <div id="reservas-list" class="space-y-4">
                <p class="has-text-grey-light is-italic">Cargando reservas...</p>
            </div>
        </div>
        
        <h3 class="title is-4 mb-4">Favoritos</h3>
        <div class="glass-card p-5">
          <div id="favoritos-list" class="space-y-4">
            </div>
        </div>
      </div>
    </div>
  </div>
</section>
`;

export function initDashboard() {
  console.log('Inicializando dashboard...');
  updateUserInfo();
  updateReservas();
  updateFavoritos();

  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn && !logoutBtn.dataset.initialized) {
    logoutBtn.dataset.initialized = 'true';
    logoutBtn.addEventListener('click', handleLogout);
  }

  const editBtn = document.getElementById('edit-profile-btn');
  if (editBtn && !editBtn.dataset.initialized) {
    editBtn.dataset.initialized = 'true';
    editBtn.addEventListener('click', openEditModal);
  }

  const closeBtns = document.querySelectorAll('.modal-close-btn, .modal-background');
  closeBtns.forEach(btn => btn.addEventListener('click', closeEditModal));

  const saveBtn = document.getElementById('save-profile-btn');
  if (saveBtn && !saveBtn.dataset.initialized) {
    saveBtn.dataset.initialized = 'true';
    saveBtn.addEventListener('click', handleSaveProfile);
  }

  const consBtn = document.getElementById('consulta-btn');
  if (consBtn) {
    consBtn.addEventListener('click', () => window.location.hash = '#/consulta');
  }
}

function openEditModal() {
  document.getElementById('edit-name').value = state.user.fullName || '';
  document.getElementById('edit-email').value = state.user.email || '';
  document.getElementById('edit-doc').value = state.user.document || '';
  document.getElementById('edit-profile-modal').classList.add('is-active');
}

function closeEditModal() {
  document.getElementById('edit-profile-modal').classList.remove('is-active');
}

import { userApi } from '../api.js';

async function handleSaveProfile() {
  const btn = document.getElementById('save-profile-btn');
  const newData = {
    fullName: document.getElementById('edit-name').value,
    email: document.getElementById('edit-email').value,
    document: document.getElementById('edit-doc').value
  };

  btn.classList.add('is-loading');

  try {
    // El backend tiene validación de correo único
    const updatedUser = await userApi.update(state.user.id || state.user.userId, newData);

    // Si llegamos aquí, fue exitoso
    state.setUser({ ...state.user, ...newData });

    toast('¡Perfil actualizado! ✨', 'ok');
    closeEditModal();
    updateUserInfo();
  } catch (error) {
    console.error('Update profile error:', error);
    // Mostrar el error real del backend (ej: "El correo ya está en uso")
    toast(error.message || 'Error al actualizar perfil', 'err');
  } finally {
    btn.classList.remove('is-loading');
  }
}

function updateUserInfo() {
  if (!state.user) {
    location.hash = '#/login';
    return;
  }

  const name = state.user.fullName || 'Usuario';
  document.getElementById('user-name').textContent = name;
  document.getElementById('user-email').textContent = state.user.email || '-';

  // Usar la inicial real para el avatar
  document.getElementById('user-avatar').textContent = name.charAt(0).toUpperCase();

  // Fecha de creación (Si viene del backend)
  const fecha = state.user.createdAt ? new Date(state.user.createdAt).toLocaleDateString() : '25/03/2026';
  document.getElementById('user-join').textContent = fecha;
}

async function updateReservas() {
  const reservasList = document.getElementById('reservas-list');
  if (!reservasList) return;

  try {
    const reservas = await bookingApi.getAll(); // Debes asegurar que este endpoint devuelva solo las del usuario
    
    if (reservas.length === 0) {
      reservasList.innerHTML = '<p class="has-text-centered has-text-grey-light py-4">No tienes viajes programados.</p>';
      return;
    }

    reservasList.innerHTML = reservas.map(res => {
      const nombre = res.travelPackage?.name || res.details || 'Paquete Turístico';
      const fecha = new Date(res.bookingDate).toLocaleDateString();
      const code = res.bookingCode || 'Pendiente';

      return `
          <div class="is-flex is-justify-content-between is-align-items-center p-4 mb-3" style="background: rgba(255,255,255,0.05); border-radius: 12px; border-left: 4px solid var(--accent-blue);">
              <div>
                  <div class="is-flex is-align-items-center mb-1">
                      <span class="tag is-info is-light mr-2">${code}</span>
                      <strong class="is-size-6">${nombre}</strong>
                  </div>
                  <small class="has-text-grey">Fecha: ${fecha} • Estado: <span class="has-text-info">${res.status}</span></small>
              </div>
              <div class="has-text-right">
                  <strong class="has-text-link is-size-5">$${(parseFloat(res.totalAmount) || 0).toLocaleString()}</strong>
              </div>
          </div>
      `;
    }).join('');
  } catch (error) {
    console.error("Error al cargar reservas:", error);
    reservasList.innerHTML = '<p class="has-text-danger py-4">Error al cargar historial de reservas.</p>';
  }
}

function updateFavoritos() {
  const favoritosList = document.getElementById('favoritos-list');
  if (!favoritosList) return;

  if (state.favorites.length === 0) {
    favoritosList.innerHTML = '<p class="has-text-centered has-text-grey-light py-4">Aún no has guardado destinos.</p>';
    return;
  }

  favoritosList.innerHTML = state.favorites.map(fav => `
        <div class="is-flex is-justify-content-between is-align-items-center p-3 mb-2" style="background: rgba(255,255,255,0.05); border-radius: 8px;">
            <div>
                <strong class="is-size-6">${fav.name}</strong>
                <br>
                <small class="has-text-grey">${fav.description ? fav.description.substring(0, 40) + '...' : 'Destino exclusivo'}</small>
            </div>
            <div class="is-flex is-align-items-center">
                <strong class="mr-4">$${(parseFloat(fav.totalPrice) || 0).toLocaleString()}</strong>
                <button class="button is-small is-danger is-light is-rounded" onclick="removeFavorite('${fav.packageId}')">
                    🗑️
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

// Función global corregida para packageId
window.removeFavorite = (packageId) => {
  const viaje = state.favorites.find(fav => String(fav.packageId) === String(packageId));
  if (viaje) {
    state.toggleFavorite(viaje);
    updateFavoritos();
    toast('Eliminado de favoritos', 'info');
  }
};