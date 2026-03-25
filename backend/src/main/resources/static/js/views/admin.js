import { state } from '../state.js';
import { toast } from '../ui.js';
import { packageApi, partnerApi, bookingApi } from '../api.js';

export const template = `
<section id="view-admin" class="view">
  <div class="admin-layout" style="display: flex; min-height: calc(100vh - 80px);">
    <!-- Sidebar -->
    <aside class="admin-sidebar glass-card" style="width: 260px; margin: 1rem; border-radius: 20px;">
      <div class="p-5">
        <h2 class="title is-5 mb-5">Admin Panel</h2>
        <ul class="admin-menu">
          <li class="mb-2"><a class="admin-link active" data-section="stats">📊 Estadísticas</a></li>
          <li class="mb-2"><a class="admin-link" data-section="packages">✈️ Paquetes</a></li>
          <li class="mb-2"><a class="admin-link" data-section="bookings">📅 Reservas</a></li>
          <li class="mb-2"><a class="admin-link" data-section="users">👥 Usuarios</a></li>
        </ul>
        <hr>
        <button id="admin-logout-btn" class="button is-danger is-light is-fullwidth">Salir</button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="admin-main glass-card" style="flex: 1; margin: 1rem 1rem 1rem 0; border-radius: 20px; overflow-y: auto;">
      <div id="admin-section-content" class="p-6">
        <!-- Contenido dinámico aquí -->
      </div>
    </main>
  </div>
</section>
`;

export async function initAdmin() {
    console.log('Initializing Admin View...');
    
    // Check if user is admin
    const roles = state.user?.roles || state.user?.role || [];
    const isAdmin = Array.isArray(roles) ? roles.includes('ROLE_ADMIN') : roles === 'ROLE_ADMIN';

    if (!isAdmin) {
        toast("Acceso denegado: Se requiere rol de administrador", "err");
        window.location.hash = "#/dashboard";
        return;
    }

    setupAdminNavigation();
    loadSection('stats');
}

function setupAdminNavigation() {
    const links = document.querySelectorAll('.admin-link');
    links.forEach(link => {
        link.addEventListener('click', (e) => {
            links.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
            loadSection(link.dataset.section);
        });
    });

    document.getElementById('admin-logout-btn')?.addEventListener('click', () => {
        state.logout();
    });
}

async function loadSection(sectionId) {
    const content = document.getElementById('admin-section-content');
    if (!content) return;

    content.innerHTML = '<p class="has-text-centered py-6">Cargando...</p>';

    try {
        switch (sectionId) {
            case 'stats':
                renderStats(content);
                break;
            case 'packages':
                await renderPackages(content);
                break;
            case 'bookings':
                await renderBookings(content);
                break;
            case 'users':
                await renderUsers(content);
                break;
        }
    } catch (error) {
        content.innerHTML = `<div class="notification is-danger">Error: ${error.message}</div>`;
    }
}

function renderStats(container) {
    container.innerHTML = `
        <h2 class="title is-3 mb-6">Panel General</h2>
        <div class="columns is-multiline">
            <div class="column is-4">
                <div class="glass-card p-5 has-text-centered" style="background: rgba(59, 130, 246, 0.1);">
                    <h3 class="heading">Ventas Totales</h3>
                    <p class="title is-2">$45,280</p>
                    <span class="has-text-success">↑ 12% este mes</span>
                </div>
            </div>
            <div class="column is-4">
                <div class="glass-card p-5 has-text-centered" style="background: rgba(16, 185, 129, 0.1);">
                    <h3 class="heading">Nuevos Usuarios</h3>
                    <p class="title is-2">128</p>
                    <span class="has-text-success">↑ 8% este mes</span>
                </div>
            </div>
            <div class="column is-4">
                <div class="glass-card p-5 has-text-centered" style="background: rgba(245, 158, 11, 0.1);">
                    <h3 class="heading">Paquetes Activos</h3>
                    <p class="title is-2">42</p>
                </div>
            </div>
        </div>
        <div class="glass-card mt-6 p-6" style="height: 300px; display: flex; align-items: center; justify-content: center;">
            <p class="has-text-grey">Gráfico de Actividad (Próximamente)</p>
        </div>
    `;
}

async function renderPackages(container) {
    const packages = await packageApi.getAll();
    container.innerHTML = `
        <div class="is-flex is-justify-content-between is-align-items-center mb-6">
            <h2 class="title is-3 mb-0">Gestión de Paquetes</h2>
            <button class="button is-link is-rounded" onclick="alert('Funcionalidad de creación próximamente')">+ Nuevo Paquete</button>
        </div>
        <table class="table is-fullwidth is-hoverable is-vcentered" style="background: transparent;">
            <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Destino</th>
                    <th>Cupos</th>
                    <th>Precio</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                ${packages.map(p => `
                    <tr>
                        <td><strong>${p.name}</strong></td>
                        <td>${p.destination?.name || 'Varios'}</td>
                        <td>
                            <span class="tag ${p.availableSlots < 5 ? 'is-danger' : 'is-info'} is-light">
                                ${p.availableSlots || 0}
                            </span>
                        </td>
                        <td>$${p.totalPrice}</td>
                        <td><span class="tag is-success is-light">Activo</span></td>
                        <td>
                            <button class="button is-small is-light">✏️</button>
                            <button class="button is-small is-danger is-light">🗑️</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function renderBookings(container) {
    // Note: You'll need to update api.js to have getAllBookings for Admin
    container.innerHTML = `
        <h2 class="title is-3 mb-6">Reservas Recientes</h2>
        <div class="has-text-centered py-6">
            <p class="has-text-grey">Cargando datos desde el servidor...</p>
        </div>
    `;
}

async function renderUsers(container) {
    container.innerHTML = `
        <h2 class="title is-3 mb-6">Control de Usuarios</h2>
        <p class="has-text-centered py-6">Módulo de gestión de usuarios en desarrollo.</p>
    `;
}
