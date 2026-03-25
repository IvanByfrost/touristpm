import { packageApi } from '../api.js';
import { state } from '../state.js';
import { toast } from '../ui.js';

export const template = `
<section id="view-catalogo" class="view">
    <section class="tc-catalogo-hero">
        <div class="container has-text-centered">
            <h1 class="title is-1 hero-main-title">Explora <span class="accent-text">el Mundo</span></h1>
            <p class="subtitle is-4 hero-sub-title">Destinos exclusivos y experiencias inolvidables.</p>
        </div>
    </section>

    <div class="container" style="padding: 0 1rem 4rem;">
        <div class="glass-card mb-6 p-5">
            <div class="columns is-vcentered">
                <div class="column is-4">
                    <div class="control has-icons-left">
                        <input id="search-input" class="input" type="text" placeholder="Buscar destino...">
                        <span class="icon is-left">🔍</span>
                    </div>
                </div>
                <div class="column is-3">
                    <div class="select is-fullwidth">
                        <select id="filter-destino">
                            <option value="">Todos los destinos</option>
                            <option value="Cancún">Cancún</option>
                            <option value="París">París</option>
                            <option value="Tokio">Tokio</option>
                        </select>
                    </div>
                </div>
                <div class="column is-3">
                    <div class="select is-fullwidth">
                        <select id="filter-precio">
                            <option value="">Cualquier precio</option>
                            <option value="0-1000">Hasta $1,000</option>
                            <option value="1000-2000">$1,000 - $2,000</option>
                        </select>
                    </div>
                </div>
                <div class="column is-2">
                    <div class="combined-tabs" style="margin: 0; padding: 0.5rem; border-radius: 12px;">
                        <button class="combined-tab active" data-target="viajes-content">✈️</button>
                        <button class="combined-tab" data-target="renta-content">🚗</button>
                    </div>
                </div>
            </div>
        </div>

        <div id="viajes-content" class="combined-content active">
            <div id="viajes-grid" class="columns is-multiline"></div>
        </div>

        <div id="renta-content" class="combined-content">
            <div class="has-text-centered py-6">
                <span style="font-size: 4rem;">🚗</span>
                <h3 class="title is-3 mt-4">Próximamente...</h3>
            </div>
        </div>
    </div>
</section>
`;

let allPackages = [];

export async function initCatalogo() {
    setupTabs();

    const grid = document.getElementById('viajes-grid');
    if (grid) {
        // Clonar el nodo para asegurar que no haya listeners duplicados al navegar
        const newGrid = grid.cloneNode(true);
        grid.parentNode.replaceChild(newGrid, grid);

        newGrid.addEventListener('click', (e) => {
            const card = e.target.closest('.tc-package-card');
            if (!card) return;
            const id = card.dataset.viajeId;

            // 1. Lógica de Reservar (Añadir al carrito local)
            if (e.target.closest('.btn-reservar')) {
                const viaje = allPackages.find(v => String(v.packageId) === String(id));
                const depInput = card.querySelector(`.input-dep-${id}`);
                const retInput = card.querySelector(`.input-ret-${id}`);
                
                const depDate = depInput?.value;
                const retDate = retInput?.value;

                // CP-TUR-018: Validación de coherencia en fechas
                if (depDate && retDate && new Date(retDate) < new Date(depDate)) {
                    toast("La fecha de regreso no puede ser anterior a la de salida", "err");
                    retInput.classList.add('is-danger');
                    return;
                }
                retInput?.classList.remove('is-danger');

                if (viaje) {
                    state.addToCart({
                        ...viaje,
                        departureDate: depDate,
                        returnDate: retDate
                    });
                    toast(`${viaje.name} agregado al carrito. ¡Revisa tu reserva! 🛒`, 'ok');
                    setTimeout(() => window.location.hash = '#/carrito', 800);
                }
            }

            // 2. Lógica de Favoritos (Persistencia local en state)
            if (e.target.closest('.btn-toggle-fav')) {
                const viaje = allPackages.find(v => String(v.packageId) === String(id));
                if (viaje) {
                    state.toggleFavorite(viaje);
                    renderViajes(aplicarFiltrosDirect()); // Re-render para actualizar el emoji del corazón
                }
            }
        });
    }

    try {
        allPackages = await packageApi.getAll();
        renderViajes(allPackages);
        setupFiltros();
        setupBusqueda();
    } catch (error) {
        console.error("Error en catálogo:", error);
        toast("Error al conectar con MySQL", "err");
    }
}

function renderViajes(viajes) {
    const grid = document.getElementById('viajes-grid');
    if (!grid) return;

    if (viajes.length === 0) {
        grid.innerHTML = '<div class="column is-12 has-text-centered"><p>No hay paquetes disponibles.</p></div>';
        return;
    }

    grid.innerHTML = viajes.map(viaje => {
        const isFav = state.isFavorite(viaje.packageId);
        const emoji = getEmojiForDestination(viaje.name);

        return `
            <div class="column is-12-tablet is-6-desktop is-4-widescreen">
                <div class="glass-card tc-package-card" data-viaje-id="${viaje.packageId}">
                    <div class="tc-package-header" style="height: 180px; display: flex; align-items: center; justify-content: center; background: #f1f5f9;">
                        <span class="tc-viaje-emoji" style="font-size: 4rem;">${emoji}</span>
                        <div class="tc-package-overlay" style="position: absolute; top: 1rem; right: 1rem;">
                            <button class="button is-rounded is-white btn-toggle-fav ${isFav ? 'is-fav' : ''}">
                                ${isFav ? '❤️' : '🤍'}
                            </button>
                        </div>
                    </div>
                    <div class="tc-package-content p-5">
                        <h3 class="title is-5 mb-2">${viaje.name}</h3>
                        <p class="subtitle is-7 has-text-grey mb-3" style="height: 3em; overflow: hidden;">${viaje.description || ''}</p>
                        
                        <div class="columns is-mobile mb-3">
                            <div class="column">
                                <label class="label is-size-7 mb-1">Salida</label>
                                <input type="date" class="input is-small input-dep-${viaje.packageId}" value="${new Date().toISOString().split('T')[0]}">
                            </div>
                            <div class="column">
                                <label class="label is-size-7 mb-1">Regreso</label>
                                <input type="date" class="input is-small input-ret-${viaje.packageId}" value="${new Date(Date.now() + 86400000 * 7).toISOString().split('T')[0]}">
                            </div>
                        </div>
                        <div class="is-flex is-justify-content-between is-align-items-center">
                            <div>
                                <div class="is-flex is-align-items-center mb-1">
                                    <span class="tag is-light is-info mr-2">${viaje.availableSlots || 0} cupos</span>
                                    <span class="is-size-7 has-text-grey-light">Precio total</span>
                                </div>
                                <div class="price-value is-size-4 has-text-weight-bold">$${viaje.totalPrice}</div>
                            </div>
                            <button class="button is-cta-premium btn-reservar">RESERVAR</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// --- Soporte de UI ---

function setupTabs() {
    const tabs = document.querySelectorAll('.combined-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const targetId = tab.dataset.target;
            document.querySelectorAll('.combined-content').forEach(c => c.classList.remove('active'));
            document.getElementById(targetId).classList.add('active');
        });
    });
}

function getEmojiForDestination(name) {
    const n = name.toLowerCase();
    if (n.includes('cancún')) return '🏖️';
    if (n.includes('parís')) return '🗼';
    if (n.includes('tokio')) return '⛩️';
    return '🌎';
}

function aplicarFiltrosDirect() {
    const search = document.getElementById('search-input')?.value.toLowerCase();
    return search ? allPackages.filter(v => v.name.toLowerCase().includes(search)) : allPackages;
}

function setupFiltros() {
    document.getElementById('filter-destino')?.addEventListener('change', () => renderViajes(aplicarFiltrosDirect()));
}

function setupBusqueda() {
    document.getElementById('search-input')?.addEventListener('input', () => renderViajes(aplicarFiltrosDirect()));
}

// --- LÓGICA DE PERSISTENCIA EN MYSQL ---

export function procederAlPago() {
    window.location.hash = "#/carrito";
}

window.procederAlPago = procederAlPago;