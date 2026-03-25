import { bookingApi } from '../api.js';
import { toast } from '../ui.js';
import { state } from '../state.js';

export const template = `
<section id="view-consulta" class="view">
    <div class="container" style="max-width: 800px; padding: 4rem 1rem;">
        <div class="has-text-centered mb-6">
            <h2 class="title is-2">Consultar <span class="accent-text">Reserva</span></h2>
            <p class="subtitle is-5">Ingresa tu correo para ver los detalles de tu viaje</p>
        </div>

        <div class="glass-card p-6">
            <div class="field">
                <label class="label">Correo Electrónico</label>
                <div class="control has-icons-left">
                    <input id="consulta-email" class="input is-medium is-rounded" type="email" placeholder="test.viajero@mail.com">
                    <span class="icon is-left">📧</span>
                </div>
            </div>

            <div class="field">
                <label class="label">O Número de Documento</label>
                <div class="control has-icons-left">
                    <input id="consulta-doc" class="input is-medium is-rounded" type="text" placeholder="Ej: 12345678">
                    <span class="icon is-left">🪪</span>
                </div>
            </div>

            <hr>

            <button id="btn-buscar-reserva" class="button is-link is-fullwidth is-medium is-rounded" style="height: auto; padding: 1rem;">
                <strong>🔍 BUSCAR RESERVAS</strong>
            </button>

            <div id="consulta-results" class="mt-6">
                <!-- Resultados aquí -->
            </div>
        </div>
    </div>
</section>
`;

export function initConsulta() {
    if (!state.user) {
        toast("Debes iniciar sesión para consultar reservas", "info");
        window.location.hash = "#/login";
        return;
    }
    document.getElementById('btn-buscar-reserva')?.addEventListener('click', handleSearch);
}

async function handleSearch() {
    console.log("--- INICIANDO BÚSQUEDA ---");
    const email = document.getElementById('consulta-email').value.trim();
    const docNumber = document.getElementById('consulta-doc').value.trim();
    const resultsContainer = document.getElementById('consulta-results');
    const btn = document.getElementById('btn-buscar-reserva');
    
    if (!email && !docNumber) {
        toast("Por favor ingresa un correo electrónico o un número de documento", "info");
        return;
    }

    console.log("Consultando con email:", email, "o documento:", docNumber);
    resultsContainer.innerHTML = '<div class="has-text-centered py-6"><div class="button is-loading is-ghost">Cargando</div><p>Buscando reservas...</p></div>';
    btn.classList.add('is-loading');

    try {
        const searchParams = {};
        if (email) searchParams.email = email;
        if (docNumber) searchParams.document = docNumber;

        const bookings = await bookingApi.search(searchParams);
        console.log("Reservas recibidas:", bookings);
        
        btn.classList.remove('is-loading');

        if (!bookings || bookings.length === 0) {
            resultsContainer.innerHTML = `
                <div class="notification is-warning is-light has-text-centered">
                    No se encontraron reservas vinculadas a <strong>${email}</strong>.
                </div>`;
            return;
        }

        resultsContainer.innerHTML = bookings.map(res => {
            const package_name = res.travelPackage?.name || res.details || 'Paquete Turístico';
            const date = new Date(res.bookingDate).toLocaleDateString();
            const amount = (parseFloat(res.totalAmount) || 0).toLocaleString();
            
            return `
                <div class="glass-card p-5 mb-4 booking-box" style="border-left: 5px solid var(--accent-blue);">
                    <div class="columns is-mobile is-vcentered">
                        <div class="column">
                            <span class="tag is-link is-light mb-2">${res.bookingCode}</span>
                            <h4 class="title is-5 mb-1">${package_name}</h4>
                            <p class="is-size-7 has-text-grey">
                                📅 Fecha: ${date} | 🏷️ Estado: <span class="has-text-info">${res.status}</span>
                            </p>
                        </div>
                        <div class="column is-narrow has-text-right">
                            <p class="title is-4 has-text-link mb-2">$${amount}</p>
                            <button class="button is-small is-light is-rounded" onclick="window.print()">
                                🖨️ Imprimir
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

    } catch (error) {
        console.error("DEBUG - Error en consulta:", error);
        toast("Error al consultar reserva", "err");
        resultsContainer.innerHTML = `<div class="notification is-danger is-light">
            Hubo un problema al conectar con el servidor: <br>
            <strong>${error.message}</strong>
        </div>`;
    }
}
