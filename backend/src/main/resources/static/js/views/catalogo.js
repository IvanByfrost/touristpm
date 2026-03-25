import { packageApi } from '../api.js';
import { state } from '../state.js';
import { toast } from '../ui.js';

export const template = `
<section id="view-catalogo" class="view">
    <!-- Hero Catálogo -->
    <section class="tc-catalogo-hero">
        <div class="container has-text-centered">
            <h1 class="title is-1 hero-main-title">Explora <span class="accent-text">el Mundo</span></h1>
            <p class="subtitle is-4 hero-sub-title">Encuentra los destinos más exclusivos y vive experiencias inolvidables.</p>
        </div>
    </section>

    <div class="container" style="padding: 0 1rem 4rem;">
        <!-- Filtros y Búsqueda -->
        <div class="glass-card mb-6 p-5">
            <div class="columns is-vcentered">
                <div class="column is-4">
                    <div class="field">
                        <div class="control has-icons-left">
                            <input id="search-input" class="input" type="text" placeholder="Buscar destino...">
                            <span class="icon is-left">🔍</span>
                        </div>
                    </div>
                </div>
                <div class="column is-3">
                    <div class="field">
                        <div class="control is-expanded">
                            <div class="select is-fullwidth">
                                <select id="filter-destino">
                                    <option value="">Todos los destinos</option>
                                    <option value="Cancún">Cancún</option>
                                    <option value="París">París</option>
                                    <option value="Tokio">Tokio</option>
                                    <option value="Dubai">Dubai</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="column is-3">
                    <div class="field">
                        <div class="control is-expanded">
                            <div class="select is-fullwidth">
                                <select id="filter-precio">
                                    <option value="">Cualquier precio</option>
                                    <option value="0-1000">Hasta $1,000</option>
                                    <option value="1000-2000">$1,000 - $2,000</option>
                                    <option value="2000+">Más de $2,000</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="column is-2">
                    <div class="combined-tabs" style="margin: 0; padding: 0.5rem; border-radius: 12px;">
                        <button class="combined-tab active" data-target="viajes-content" style="padding: 0.5rem 1rem;">✈️</button>
                        <button class="combined-tab" data-target="renta-content" style="padding: 0.5rem 1rem;">🚗</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Contenido Dinámico -->
        <div id="viajes-content" class="combined-content active">
            <div id="viajes-grid" class="columns is-multiline">
                <!-- Se llena dinámicamente -->
            </div>
        </div>

        <div id="renta-content" class="combined-content">
            <div class="has-text-centered py-6">
                <span style="font-size: 4rem;">🚗</span>
                <h3 class="title is-3 mt-4">Próximamente...</h3>
                <p class="subtitle">Estamos preparando la mejor flota para tu viaje.</p>
            </div>
        </div>
    </div>
</section>
`;

let allPackages = [];

export async function initCatalogo() {
    console.log('Inicializando catálogo premium...');
    
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

    try {
        allPackages = await packageApi.getAll();
        renderViajes(allPackages);
        setupFiltros();
        setupBusqueda();
    } catch (error) {
        console.error("Error al cargar catálogo:", error);
        toast("No se pudo cargar el catálogo", "err");
    }
}

function getEmojiForDestination(name) {
    const n = name.toLowerCase();
    if (n.includes('playa') || n.includes('cancún')) return '🏖️';
    if (n.includes('montaña') || n.includes('andes')) return '⛰️';
    if (n.includes('safari') || n.includes('kenia')) return '🦁';
    if (n.includes('ciudad') || n.includes('york')) return '🏙️';
    if (n.includes('parís') || n.includes('europa')) return '🗼';
    if (n.includes('dubai') || n.includes('desierto')) return '🐫';
    return '🌎';
}

function renderViajes(viajes) {
    const grid = document.getElementById('viajes-grid');
    if (!grid) return;
    
    if (viajes.length === 0) {
        grid.innerHTML = `
            <div class="column is-12 has-text-centered py-6">
                <p class="title is-4 has-text-grey-light">No encontramos paquetes con esos filtros.</p>
            </div>
        `;
        return;
    }
    
    grid.innerHTML = viajes.map(viaje => {
        const isFav = state.isFavorite(viaje.id);
        const emoji = getEmojiForDestination(viaje.name);
        
        return `
            <div class="column is-12-tablet is-6-desktop is-4-widescreen">
                <div class="glass-card tc-package-card" data-viaje-id="${viaje.id}">
                    <div class="tc-package-header" style="height: 240px; background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%); overflow: hidden;">
                        <span class="tc-viaje-emoji">${emoji}</span>
                        <div class="tc-package-overlay">
                            <button class="button is-rounded is-white tc-btn-fav ${isFav ? 'is-fav' : ''}" 
                                    onclick="toggleFavorite('${viaje.id}')">
                                ${isFav ? '❤️' : '🤍'}
                            </button>
                        </div>
                    </div>
                    <div class="tc-package-content p-5">
                        <div class="is-flex is-justify-content-between is-align-items-start mb-3">
                            <h3 class="title is-4 mb-0">${viaje.name}</h3>
                            <span class="tag is-info is-light is-rounded">${viaje.category || 'Premium'}</span>
                        </div>
                        <p class="subtitle is-6 mb-5" style="color: #64748b; height: 3em; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">
                            ${viaje.description}
                        </p>
                        
                        <div class="is-flex is-justify-content-between is-align-items-center mt-auto">
                            <div class="tc-price-display">
                                <span class="is-size-7 has-text-grey-light is-uppercase">Desde</span>
                                <div class="price-value is-size-3">$${viaje.totalPrice}</div>
                            </div>
                            <button class="button is-cta-premium p-4" onclick="addToCart('${viaje.id}')">
                                RESERVAR
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function setupFiltros() {
    const filterDestino = document.getElementById('filter-destino');
    const filterPrecio = document.getElementById('filter-precio');
    if (filterDestino) filterDestino.addEventListener('change', aplicarFiltros);
    if (filterPrecio) filterPrecio.addEventListener('change', aplicarFiltros);
}

function setupBusqueda() {
    const searchInput = document.getElementById('search-input');
    if (searchInput) searchInput.addEventListener('input', aplicarFiltros);
}

function aplicarFiltros() {
    const destino = document.getElementById('filter-destino')?.value;
    const precio = document.getElementById('filter-precio')?.value;
    const search = document.getElementById('search-input')?.value.toLowerCase();
    
    let filtered = allPackages;
    if (destino) {
        filtered = filtered.filter(v => 
            v.name.toLowerCase().includes(destino.toLowerCase()) || 
            v.description.toLowerCase().includes(destino.toLowerCase())
        );
    }
    if (precio) {
        if (precio === '0-1000') filtered = filtered.filter(v => v.totalPrice <= 1000);
        else if (precio === '1000-2000') filtered = filtered.filter(v => v.totalPrice > 1000 && v.totalPrice <= 2000);
        else if (precio === '2000+') filtered = filtered.filter(v => v.totalPrice > 2000);
    }
    if (search) {
        filtered = filtered.filter(v => 
            v.name.toLowerCase().includes(search) || 
            v.description.toLowerCase().includes(search)
        );
    }
    renderViajes(filtered);
}

window.addToCart = (id) => {
    const viaje = allPackages.find(v => v.id === id);
    if (viaje) {
        state.addToCart(viaje);
        toast(`${viaje.name} agregado al carrito`, 'ok');
    }
};

window.toggleFavorite = (id) => {
    const viaje = allPackages.find(v => v.id === id);
    if (viaje) {
        state.toggleFavorite(viaje);
        renderViajes(aplicarFiltrosDirect());
    }
};

function aplicarFiltrosDirect() {
    const destino = document.getElementById('filter-destino')?.value;
    const precio = document.getElementById('filter-precio')?.value;
    const search = document.getElementById('search-input')?.value.toLowerCase();
    let filtered = allPackages;
    if (destino) filtered = filtered.filter(v => v.name.toLowerCase().includes(destino.toLowerCase()));
    if (precio) {
        if (precio === '0-1000') filtered = filtered.filter(v => v.totalPrice <= 1000);
        else if (precio === '1000-2000') filtered = filtered.filter(v => v.totalPrice > 1000 && v.totalPrice <= 2000);
        else filtered = filtered.filter(v => v.totalPrice > 2000);
    }
    if (search) filtered = filtered.filter(v => v.name.toLowerCase().includes(search) || v.description.toLowerCase().includes(search));
    return filtered;
}