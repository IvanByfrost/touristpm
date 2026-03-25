import { state } from '../state.js';
import { toast } from '../ui.js';
import { packageApi, partnerApi, bookingApi, userApi, auditApi, itineraryApi, destinationApi } from '../api.js';

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
          <li class="mb-2"><a class="admin-link" data-section="partners">🤝 Socios</a></li>
          <li class="mb-2"><a class="admin-link" data-section="destinations">💸 Tarifas</a></li>
          <li class="mb-2"><a class="admin-link" data-section="audit">📜 Auditoría</a></li>
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

  <!-- Modal de Edición de Paquete -->
  <div id="modal-edit-package" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Editar Paquete</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <form id="form-edit-package">
          <input type="hidden" id="edit-package-id">
          <div class="field">
            <label class="label">Nombre del Paquete</label>
            <div class="control">
              <input id="edit-package-name" class="input" type="text" required>
            </div>
          </div>
          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label">Precio Total ($)</label>
                <div class="control">
                  <input id="edit-package-price" class="input" type="number" step="0.01" required>
                </div>
              </div>
            </div>
            <div class="column">
              <div class="field">
                <label class="label">Cupos Disponibles</label>
                <div class="control">
                  <input id="edit-package-slots" class="input" type="number" required>
                </div>
              </div>
            </div>
          </div>
          <div class="field">
            <label class="label">Descripción</label>
            <div class="control">
              <textarea id="edit-package-desc" class="textarea" rows="3"></textarea>
            </div>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-save-package" class="button is-link">Guardar Cambios</button>
        <button class="button modal-close-btn">Cancelar</button>
      </footer>
    </div>
  </div>

  <!-- Modal de Registro de Socio -->
  <div id="modal-new-partner" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Registrar Nuevo Socio</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <form id="form-new-partner">
          <div class="field">
            <label class="label">ID de Socio (NIT / RUT)</label>
            <div class="control">
              <input id="partner-id" class="input" type="text" placeholder="SOC-001" required>
            </div>
          </div>
          <div class="field">
            <label class="label">Razon Social / Empresa</label>
            <div class="control">
              <input id="partner-name" class="input" type="text" placeholder="Ej: Aerolíneas X" required>
            </div>
          </div>
          <div class="field">
            <label class="label">Dirección Corporativa</label>
            <div class="control">
              <input id="partner-address" class="input" type="text" placeholder="Calle 123 #45-67" required>
            </div>
          </div>
          <div class="field">
            <label class="label">Teléfono de Contacto</label>
            <div class="control">
              <input id="partner-phone" class="input" type="text" placeholder="+57 300..." required>
            </div>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-save-partner" class="button is-success">Guardar Registro</button>
        <button class="button modal-close-btn">Cancelar</button>
      </footer>
    </div>
  </div>

  <!-- Modal de Edición de Socio -->
  <div id="modal-edit-partner" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Editar Socio</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <form id="form-edit-partner">
          <input type="hidden" id="edit-partner-id">
          <div class="field">
            <label class="label">Razon Social / Empresa</label>
            <div class="control">
              <input id="edit-partner-name" class="input" type="text" required>
            </div>
          </div>
          <div class="field">
            <label class="label">Dirección Corporativa</label>
            <div class="control">
              <input id="edit-partner-address" class="input" type="text" required>
            </div>
          </div>
          <div class="field">
            <label class="label">Teléfono de Contacto</label>
            <div class="control">
              <input id="edit-partner-phone" class="input" type="text" required>
            </div>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-update-partner" class="button is-link">Guardar Cambios</button>
        <button class="button modal-close-btn">Cancelar</button>
      </footer>
    </div>
  </div>

  <!-- Modal Reserva Administrativa -->
  <div id="modal-admin-booking" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Generar Reserva Administrativa</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <form id="form-admin-booking">
          <div class="field">
            <label class="label">Destinatario (Usuario)</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select id="admin-booking-user" required>
                  <option value="">Seleccione un usuario...</option>
                </select>
              </div>
            </div>
          </div>
          <div class="field">
            <label class="label">Paquete Turístico</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select id="admin-booking-package" required>
                  <option value="">Seleccione un paquete...</option>
                </select>
              </div>
            </div>
          </div>
          <div class="field">
            <label class="label">Total a Cobrar ($)</label>
            <div class="control">
              <input id="admin-booking-amount" class="input" type="number" step="0.01" required>
            </div>
          </div>
          <div class="field">
            <label class="label">Detalles de Trazabilidad</label>
            <div class="control">
              <textarea id="admin-booking-details" class="textarea" placeholder="Ej: Asignación por compensación..."></textarea>
            </div>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-save-admin-booking" class="button is-link">Generar Reserva</button>
        <button class="button modal-close-btn">Cancelar</button>
      </footer>
    </div>
  </div>

  <!-- Modal Edición de Itinerario -->
  <div id="modal-itinerary" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Gestionar Itinerario</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <form id="form-itinerary">
          <input type="hidden" id="itinerary-id">
          <div class="field">
            <label class="label">Descripción Detallada del Plan</label>
            <div class="control">
              <textarea id="itinerary-desc" class="textarea" rows="8" placeholder="Día 1: Vuelo y Hotel..."></textarea>
            </div>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-save-itinerary" class="button is-success">Guardar Cambios</button>
        <button class="button modal-close-btn">Cancelar</button>
      </footer>
    </div>
  </div>

  <!-- Modal Anulación de Reserva -->
  <div id="modal-cancel-booking" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Anular Reserva</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <div class="notification is-warning is-light">
          <strong>Atención:</strong> Esta acción anulará la reserva y liberará los cupos para otros usuarios.
        </div>
        <form id="form-cancel-booking">
          <input type="hidden" id="cancel-booking-id">
          <div class="field">
            <label class="label">Motivo de la Anulación</label>
            <div class="control">
              <textarea id="cancel-booking-reason" class="textarea" placeholder="Ej: Solicitud del cliente, error en datos..." required></textarea>
            </div>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-confirm-cancel" class="button is-danger">Confirmar Anulación</button>
        <button class="button modal-close-btn">Cerrar</button>
      </footer>
    </div>
  </div>

  <!-- Modal Edición de Tarifas de Destino -->
  <div id="modal-edit-destination" class="modal">
    <div class="modal-background"></div>
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Ajustar Tarifas de Destino</p>
        <button class="delete modal-close-btn" aria-label="close"></button>
      </header>
      <section class="modal-card-body">
        <form id="form-edit-destination">
          <input type="hidden" id="edit-dest-id">
          <div class="field">
            <label class="label">Destino / Servicio</label>
            <div class="control">
              <input id="edit-dest-name" class="input" type="text" readonly>
            </div>
          </div>
          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label">Precio Base ($)</label>
                <div class="control">
                  <input id="edit-dest-price" class="input" type="number" step="0.01" required>
                </div>
              </div>
            </div>
            <div class="column">
              <div class="field">
                <label class="label">Impuestos (%)</label>
                <div class="control">
                  <input id="edit-dest-tax" class="input" type="number" step="0.1" required>
                </div>
              </div>
            </div>
          </div>
          <div class="notification is-info is-light">
            <p id="dest-total-preview">Total Calculado: $0.00</p>
          </div>
        </form>
      </section>
      <footer class="modal-card-foot">
        <button id="btn-save-destination" class="button is-link">Actualizar Tarifa</button>
        <button class="button modal-close-btn">Cancelar</button>
      </footer>
    </div>
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
    setupModalHandlers();
    setupPartnerModalHandlers();
    setupEditPartnerModalHandlers();
    setupAdminBookingHandlers();
    setupItineraryHandlers();
    setupCancellationHandlers();
    setupDestinationHandlers();
    loadSection('stats');
}

function setupCancellationHandlers() {
    const modal = document.getElementById('modal-cancel-booking');
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const confirmBtn = document.getElementById('btn-confirm-cancel');

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    confirmBtn.onclick = async () => {
        const id = document.getElementById('cancel-booking-id').value;
        const reason = document.getElementById('cancel-booking-reason').value;

        if (!reason.trim()) {
            toast("Debe ingresar un motivo", "info");
            return;
        }

        confirmBtn.classList.add('is-loading');
        try {
            await bookingApi.cancel(id, reason);
            toast("Reserva anulada y cupos liberados ✅", "ok");
            modal.classList.remove('is-active');
            loadSection('bookings');
        } catch (error) {
            toast("Error al anular reserva", "err");
        } finally {
            confirmBtn.classList.remove('is-loading');
        }
    };
}

window.openCancelModal = (bookingId) => {
    document.getElementById('cancel-booking-id').value = bookingId;
    document.getElementById('cancel-booking-reason').value = "";
    document.getElementById('modal-cancel-booking').classList.add('is-active');
};

function setupDestinationHandlers() {
    const modal = document.getElementById('modal-edit-destination');
    if (!modal) return;
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const saveBtn = document.getElementById('btn-save-destination');
    const priceInput = document.getElementById('edit-dest-price');
    const taxInput = document.getElementById('edit-dest-tax');
    const preview = document.getElementById('dest-total-preview');

    const updatePreview = () => {
        const base = parseFloat(priceInput.value) || 0;
        const tax = parseFloat(taxInput.value) || 0;
        const total = base * (1 + tax / 100);
        preview.innerText = `Total Calculado: $${total.toLocaleString(undefined, {minimumFractionDigits: 2})}`;
    };

    if(priceInput) priceInput.oninput = updatePreview;
    if(taxInput) taxInput.oninput = updatePreview;

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    if(saveBtn) saveBtn.onclick = async () => {
        const id = document.getElementById('edit-dest-id').value;
        const data = {
            name: document.getElementById('edit-dest-name').value,
            basePrice: parseFloat(priceInput.value),
            taxPercentage: parseFloat(taxInput.value)
        };

        saveBtn.classList.add('is-loading');
        try {
            await destinationApi.update(id, data);
            toast("Tarifa actualizada correctamente ✅", "ok");
            modal.classList.remove('is-active');
            loadSection('destinations');
        } catch (error) {
            toast("Error al actualizar tarifas", "err");
        } finally {
            saveBtn.classList.remove('is-loading');
        }
    };
}

window.openEditDestinationModal = async (id) => {
    const modal = document.getElementById('modal-edit-destination');
    try {
        const dest = await destinationApi.getById(id);
        document.getElementById('edit-dest-id').value = dest.destinationId;
        document.getElementById('edit-dest-name').value = dest.name;
        document.getElementById('edit-dest-price').value = dest.basePrice || 0;
        document.getElementById('edit-dest-tax').value = dest.taxPercentage || 0;
        
        const base = dest.basePrice || 0;
        const tax = dest.taxPercentage || 0;
        document.getElementById('dest-total-preview').innerText = `Total Calculado: $${(base * (1 + tax / 100)).toLocaleString()}`;
        
        modal.classList.add('is-active');
    } catch (error) {
        toast("Error al cargar destino", "err");
    }
};

async function renderDestinations(container) {
    const destinations = await destinationApi.getAll();
    container.innerHTML = `
        <h2 class="title is-3 mb-6">💸 Gestión de Tarifas por Destino</h2>
        <div class="notification is-info is-light mb-6">
            Ajuste los precios base e impuestos. Estos valores se aplicarán automáticamente a las nuevas cotizaciones.
        </div>
        <table class="table is-fullwidth is-hoverable" style="background: transparent;">
            <thead>
                <tr>
                    <th>Destino</th>
                    <th>Continente/País</th>
                    <th>Precio Base</th>
                    <th>IVA/Impuestos</th>
                    <th>Total Público</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                ${destinations.map(d => {
                    const base = d.basePrice || 0;
                    const tax = d.taxPercentage || 0;
                    const total = base * (1 + tax / 100);
                    return `
                        <tr>
                            <td><strong>${d.name}</strong></td>
                            <td>${d.country}</td>
                            <td>$${base.toLocaleString()}</td>
                            <td><span class="tag is-warning is-light">${tax}%</span></td>
                            <td><strong class="has-text-link">$${total.toLocaleString()}</strong></td>
                            <td>
                                <button class="button is-small is-link is-light" onclick="openEditDestinationModal('${d.destinationId}')">Ajustar Tarifa</button>
                            </td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table>
    `;
}

function setupItineraryHandlers() {
    const modal = document.getElementById('modal-itinerary');
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const saveBtn = document.getElementById('btn-save-itinerary');

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    saveBtn.onclick = async () => {
        const id = document.getElementById('itinerary-id').value;
        const data = { description: document.getElementById('itinerary-desc').value };

        saveBtn.classList.add('is-loading');
        try {
            await itineraryApi.update(id, data);
            toast("Itinerario actualizado y auditado 📜", "ok");
            modal.classList.remove('is-active');
        } catch (error) {
            toast("Error al actualizar itinerario", "err");
        } finally {
            saveBtn.classList.remove('is-loading');
        }
    };
}

window.openItineraryModal = async (bookingId) => {
    const modal = document.getElementById('modal-itinerary');
    const descField = document.getElementById('itinerary-desc');
    const idField = document.getElementById('itinerary-id');

    descField.value = "Cargando...";
    modal.classList.add('is-active');

    try {
        const items = await itineraryApi.getByBooking(bookingId);
        if (items.length > 0) {
            const it = items[0];
            idField.value = it.itineraryId;
            descField.value = it.description;
        } else {
            // Si no existe, podríamos crearlo, pero para el CP asumimos existencia
            descField.value = "No hay itinerario registrado para esta reserva.";
            toast("No existe registro de itinerario", "info");
        }
    } catch (error) {
        toast("Error al cargar itinerario", "err");
    }
};

async function renderAuditLogs(container) {
    const logs = await auditApi.getAll();
    
    container.innerHTML = `
        <h2 class="title is-3 mb-6">📜 Bitácora de Auditoría</h2>
        <div class="glass-card p-0" style="overflow: hidden;">
            <table class="table is-fullwidth is-hoverable is-striped" style="background: transparent;">
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Usuario</th>
                        <th>Acción</th>
                        <th>Entidad</th>
                        <th>Detalles</th>
                    </tr>
                </thead>
                <tbody>
                    ${logs.map(l => `
                        <tr>
                            <td><small>${new Date(l.timestamp).toLocaleString()}</small></td>
                            <td><span class="tag is-dark">${l.performedBy}</span></td>
                            <td><span class="tag is-info is-light">${l.action}</span></td>
                            <td><small>${l.entityType}: ${l.entityId.substring(0,8)}</small></td>
                            <td><small>${l.details}</small></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
}

function setupAdminBookingHandlers() {
    const modal = document.getElementById('modal-admin-booking');
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const saveBtn = document.getElementById('btn-save-admin-booking');

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    saveBtn.onclick = async () => {
        const userId = document.getElementById('admin-booking-user').value;
        const packageId = document.getElementById('admin-booking-package').value;
        const amount = document.getElementById('admin-booking-amount').value;
        
        const data = {
            user: { userId: userId },
            travelPackage: packageId ? { packageId: packageId } : null,
            totalAmount: parseFloat(amount),
            bookingType: 'Reserva Administrativa',
            details: document.getElementById('admin-booking-details').value,
            status: 'Confirmed' // Se crea confirmada por defecto
        };

        saveBtn.classList.add('is-loading');
        try {
            await bookingApi.create(data);
            toast("Reserva administrativa creada con éxito ✅", "ok");
            modal.classList.remove('is-active');
            loadSection('bookings');
        } catch (error) {
            toast("Error al crear reserva: " + error.message, "err");
        } finally {
            saveBtn.classList.remove('is-loading');
        }
    };
}

window.openAdminBookingModal = async () => {
    const modal = document.getElementById('modal-admin-booking');
    const userSelect = document.getElementById('admin-booking-user');
    const packageSelect = document.getElementById('admin-booking-package');
    
    // Reset and Load
    userSelect.innerHTML = '<option value="">Cargando usuarios...</option>';
    packageSelect.innerHTML = '<option value="">Cargando paquetes...</option>';
    modal.classList.add('is-active');

    try {
        const [users, packages] = await Promise.all([
            userApi.getAll(),
            packageApi.getAll()
        ]);

        userSelect.innerHTML = '<option value="">Seleccione un usuario...</option>' + 
            users.map(u => `<option value="${u.userId}">${u.fullName} (${u.email})</option>`).join('');

        packageSelect.innerHTML = '<option value="">Seleccione un paquete...</option>' + 
            packages.map(p => `<option value="${p.packageId}" 
                data-price="${p.totalPrice}" 
                data-dest-price="${p.destination?.basePrice || ''}"
                data-dest-tax="${p.destination?.taxPercentage || ''}">${p.name}</option>`).join('');

        packageSelect.onchange = () => {
            const opt = packageSelect.selectedOptions[0];
            if (opt) {
                const amountField = document.getElementById('admin-booking-amount');
                if (opt.dataset.destPrice) {
                    // CP-ADM-058: Cálculo automático basado en destino
                    const base = parseFloat(opt.dataset.destPrice);
                    const tax = parseFloat(opt.dataset.destTax) || 0;
                    const total = base * (1 + tax / 100);
                    amountField.value = total.toFixed(2);
                    toast("💰 Precio calculado automáticamente según tarifa de destino", "info");
                } else if (opt.dataset.price) {
                    amountField.value = opt.dataset.price;
                }
            }
        };

    } catch (error) {
        toast("Error al cargar datos", "err");
    }
};

function setupEditPartnerModalHandlers() {
    const modal = document.getElementById('modal-edit-partner');
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const updateBtn = document.getElementById('btn-update-partner');

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    updateBtn.onclick = async () => {
        const id = document.getElementById('edit-partner-id').value;
        const data = {
            companyName: document.getElementById('edit-partner-name').value,
            address: document.getElementById('edit-partner-address').value,
            phone: document.getElementById('edit-partner-phone').value
        };

        updateBtn.classList.add('is-loading');
        try {
            await partnerApi.update(id, data);
            toast("Información del socio actualizada", "ok");
            modal.classList.remove('is-active');
            loadSection('partners');
        } catch (error) {
            toast("Error al actualizar: " + error.message, "err");
        } finally {
            updateBtn.classList.remove('is-loading');
        }
    };
}

window.editPartner = async (id) => {
    const modal = document.getElementById('modal-edit-partner');
    try {
        // Asumo que existe un endpoint partnerApi.getById o uso la lista actual
        const partners = await partnerApi.getAll();
        const partner = partners.find(p => p.partnerId === id);
        
        if (partner) {
            document.getElementById('edit-partner-id').value = partner.partnerId;
            document.getElementById('edit-partner-name').value = partner.companyName;
            document.getElementById('edit-partner-address').value = partner.address;
            document.getElementById('edit-partner-phone').value = partner.phone;
            modal.classList.add('is-active');
        }
    } catch (error) {
        toast("Error al cargar socio", "err");
    }
};

function setupPartnerModalHandlers() {
    const modal = document.getElementById('modal-new-partner');
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const saveBtn = document.getElementById('btn-save-partner');

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    saveBtn.onclick = async () => {
        const data = {
            partnerId: document.getElementById('partner-id').value,
            companyName: document.getElementById('partner-name').value,
            address: document.getElementById('partner-address').value,
            phone: document.getElementById('partner-phone').value,
            status: 'Activo'
        };

        if (!data.partnerId || !data.companyName) {
            toast("ID y Nombre son obligatorios", "info");
            return;
        }

        saveBtn.classList.add('is-loading');
        try {
            await partnerApi.create(data);
            toast("Socio registrado con éxito ✅", "ok");
            modal.classList.remove('is-active');
            loadSection('partners');
        } catch (error) {
            toast("Error al registrar: " + error.message, "err");
        } finally {
            saveBtn.classList.remove('is-loading');
        }
    };
}

window.openNewPartnerModal = () => {
    document.getElementById('form-new-partner').reset();
    document.getElementById('modal-new-partner').classList.add('is-active');
};

async function renderPartners(container, query = '') {
    let partners;
    if (query.trim()) {
        partners = await partnerApi.search(query);
    } else {
        partners = await partnerApi.getAll();
    }
    
    container.innerHTML = `
        <div class="is-flex is-justify-content-between is-align-items-center mb-6">
            <h2 class="title is-3 mb-0">Gestión de Socios</h2>
            <button class="button is-success is-rounded" onclick="openNewPartnerModal()">
                <strong>+ Registrar Nuevo Socio</strong>
            </button>
        </div>

        <!-- Barra de Búsqueda -->
        <div class="field has-addons mb-6" style="max-width: 500px;">
            <div class="control is-expanded">
                <input id="partner-search-input" class="input is-rounded" type="text" placeholder="Buscar por ID o Nombre..." value="${query}">
            </div>
            <div class="control">
                <button id="btn-partner-search" class="button is-link is-rounded">
                    🔍 Buscar
                </button>
            </div>
        </div>
        
        <table class="table is-fullwidth is-hoverable" style="background: transparent;">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Empresa</th>
                    <th>Teléfono</th>
                    <th>Dirección</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                ${partners.length === 0 ? `
                    <tr><td colspan="6" class="has-text-centered py-6 has-text-grey">No se encontraron socios.</td></tr>
                ` : partners.map(s => `
                    <tr>
                        <td><code>${s.partnerId}</code></td>
                        <td><strong>${s.companyName}</strong></td>
                        <td>${s.phone}</td>
                        <td>${s.address}</td>
                        <td><span class="tag is-success is-light">${s.status || 'Activo'}</span></td>
                        <td>
                            <button class="button is-small is-link is-light" onclick="editPartner('${s.partnerId}')">✏️</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    // Listener para búsqueda
    const searchInput = document.getElementById('partner-search-input');
    const searchBtn = document.getElementById('btn-partner-search');

    searchBtn.onclick = () => {
        renderPartners(container, searchInput.value);
    };

    searchInput.onkeypress = (e) => {
        if (e.key === 'Enter') {
            renderPartners(container, searchInput.value);
        }
    };
}

function setupModalHandlers() {
    const modal = document.getElementById('modal-edit-package');
    const closeBtns = modal.querySelectorAll('.modal-close-btn');
    const saveBtn = document.getElementById('btn-save-package');

    closeBtns.forEach(btn => {
        btn.onclick = () => modal.classList.remove('is-active');
    });

    saveBtn.onclick = async () => {
        const id = document.getElementById('edit-package-id').value;
        const data = {
            name: document.getElementById('edit-package-name').value,
            totalPrice: parseFloat(document.getElementById('edit-package-price').value),
            availableSlots: parseInt(document.getElementById('edit-package-slots').value),
            description: document.getElementById('edit-package-desc').value,
        };

        saveBtn.classList.add('is-loading');
        try {
            await packageApi.update(id, data);
            toast("Paquete actualizado con éxito", "ok");
            modal.classList.remove('is-active');
            loadSection('packages'); // Recargar tabla
        } catch (error) {
            toast("Error al actualizar: " + error.message, "err");
        } finally {
            saveBtn.classList.remove('is-loading');
        }
    };
}

window.editPackage = async (id) => {
    const modal = document.getElementById('modal-edit-package');
    try {
        const pkg = await packageApi.getById(id);
        
        document.getElementById('edit-package-id').value = pkg.packageId;
        document.getElementById('edit-package-name').value = pkg.name;
        document.getElementById('edit-package-price').value = pkg.totalPrice;
        document.getElementById('edit-package-slots').value = pkg.availableSlots;
        document.getElementById('edit-package-desc').value = pkg.description || '';
        
        modal.classList.add('is-active');
    } catch (error) {
        toast("Error al cargar datos del paquete", "err");
    }
};

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
            case 'destinations':
                await renderDestinations(content);
                break;
            case 'partners':
                await renderPartners(content);
                break;
            case 'audit':
                await renderAuditLogs(content);
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
                            <button class="button is-small is-link is-light" onclick="editPackage('${p.packageId}')">✏️</button>
                            <button class="button is-small is-danger is-light" onclick="alert('Baja de paquete próximamente')">🗑️</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function renderBookings(container) {
    const bookings = await bookingApi.getAll();
    
    container.innerHTML = `
        <div class="is-flex is-justify-content-between is-align-items-center mb-6">
            <h2 class="title is-3 mb-0">Gestión de Reservas</h2>
            <div class="buttons">
                <button class="button is-link is-rounded" onclick="openAdminBookingModal()">
                    <strong>+ Generar Reserva Administrativa</strong>
                </button>
                <div class="tags has-addons mb-0">
                    <span class="tag is-dark">Total</span>
                    <span class="tag is-link">${bookings.length}</span>
                </div>
            </div>
        </div>
        
        <table class="table is-fullwidth is-hoverable" style="background: transparent;">
            <thead>
                <tr>
                    <th class="has-text-grey-light">Ref</th>
                    <th class="has-text-grey-light">Cliente</th>
                    <th class="has-text-grey-light">Paquete / Destino</th>
                    <th class="has-text-grey-light">Fecha</th>
                    <th class="has-text-grey-light">Total</th>
                    <th class="has-text-grey-light">Estado / Tipo</th>
                    <th class="has-text-grey-light">Acciones</th>
                </tr>
            </thead>
            <tbody id="admin-bookings-table">
                ${bookings.map(bh => {
                    const statusClass = bh.status === 'Confirmed' ? 'is-success' : (bh.status === 'Cancelled' ? 'is-danger' : 'is-warning');
                    const cliente = bh.user?.fullName || 'Desconocido';
                    const paquete = bh.travelPackage?.name || bh.details || 'Paquete';
                    const fecha = new Date(bh.bookingDate).toLocaleDateString();
                    const isAdmin = bh.bookingType === 'Reserva Administrativa';
                    
                    return `
                        <tr>
                            <td><small class="has-text-grey">${bh.bookingCode}</small></td>
                            <td><strong>${cliente}</strong></td>
                            <td>
                                ${paquete}
                                ${isAdmin ? '<br><small class="tag is-info is-light is-small">ADM</small>' : ''}
                            </td>
                            <td>${fecha}</td>
                            <td><strong>$${(parseFloat(bh.totalAmount) || 0).toLocaleString()}</strong></td>
                            <td>
                                <span class="tag ${statusClass} is-light">${bh.status}</span>
                            </td>
                            <td>
                                <div class="buttons are-small">
                                    <button class="button is-info is-light" onclick="openItineraryModal('${bh.bookingId}')">📋 Plan</button>
                                    ${bh.status !== 'Cancelled' ? `
                                        ${bh.status !== 'Confirmed' ? `<button class="button is-success is-light btn-confirm" data-id="${bh.bookingId}">Confirmar</button>` : ''}
                                        <button class="button is-danger is-light" onclick="openCancelModal('${bh.bookingId}')">Anular</button>
                                    ` : ''}
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table>
    `;

    // Listeners for buttons
    container.querySelectorAll('.btn-confirm').forEach(btn => {
        btn.onclick = async () => {
            if (confirm('¿Confirmar esta reserva?')) {
                btn.classList.add('is-loading');
                try {
                    await bookingApi.confirm(btn.dataset.id);
                    toast('Reserva confirmada ✅', 'ok');
                    renderBookings(container);
                } catch (e) {
                    toast('Error al confirmar', 'err');
                    btn.classList.remove('is-loading');
                }
            }
        };
    });

    // Eliminamos el listener de .btn-cancel anterior ya que ahora usamos onclick="openCancelModal"
}

async function renderUsers(container) {
    container.innerHTML = `
        <h2 class="title is-3 mb-6">Control de Usuarios</h2>
        <p class="has-text-centered py-6">Módulo de gestión de usuarios en desarrollo.</p>
    `;
}
