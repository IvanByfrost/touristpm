import { packageApi } from '../api.js';

let allPackages = [];

export async function initCatalogo() {
    console.log('Inicializando catálogo real...');
    
    try {
        allPackages = await packageApi.getAll();
        renderViajes(allPackages);
        setupFiltros();
        setupBusqueda();
    } catch (error) {
        console.error("Error al cargar catálogo:", error);
        toast("No se pudo cargar el catálogo", "error");
    }
}

function getEmojiForDestination(name) {
    if (name.includes('Cancún')) return '🏖️';
    if (name.includes('Andes')) return '⛰️';
    if (name.includes('Safari')) return '🦁';
    if (name.includes('New York')) return '🏙️';
    if (name.includes('París')) return '🗼';
    return '🌎';
}

function renderViajes(viajes) {
    const grid = document.getElementById('viajes-grid');
    if (!grid) return;
    
    grid.innerHTML = viajes.map(viaje => `
        <div class="column is-12-tablet is-6-desktop is-4-widescreen">
            <div class="glass-card tc-viaje-card" data-viaje-id="${viaje.packageId}">
                <div class="tc-viaje-image">
                    ${getEmojiForDestination(viaje.name)}
                </div>
                <h3 class="title is-4">${viaje.name}</h3>
                <p>${viaje.description}</p>
                <div class="mt-3">
                    <strong class="is-size-3">$${viaje.totalPrice}</strong>
                    <span class="is-size-6"> / por persona</span>
                </div>
                <div class="tc-viaje-actions">
                    <button class="button is-cta is-small" onclick="addToCart('${viaje.packageId}')">
                        🛒 Reservar
                    </button>
                    <button class="button is-small ${state.isFavorite(viaje.packageId) ? 'is-danger' : 'is-light'}" 
                            onclick="toggleFavorite('${viaje.packageId}')">
                        ${state.isFavorite(viaje.packageId) ? '❤️' : '🤍'}
                    </button>
                </div>
                ${viaje.availableSlots < 5 ? `<span class="tag is-warning mt-2">🔥 ¡Quedan ${viaje.availableSlots}!</span>` : ''}
            </div>
        </div>
    `).join('');
}

function setupFiltros() {
    const filterDestino = document.getElementById('filter-destino');
    const filterPrecio = document.getElementById('filter-precio');
    
    if (filterDestino) {
        filterDestino.addEventListener('change', aplicarFiltros);
    }
    
    if (filterPrecio) {
        filterPrecio.addEventListener('change', aplicarFiltros);
    }
}

function setupBusqueda() {
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', aplicarFiltros);
    }
}

function aplicarFiltros() {
    const destino = document.getElementById('filter-destino')?.value;
    const precio = document.getElementById('filter-precio')?.value;
    const search = document.getElementById('search-input')?.value.toLowerCase();
    
    let viajesFiltrados = allPackages;
    
    // Filtrar por destino (Mapeando a destination.name o similar)
    if (destino) {
        viajesFiltrados = viajesFiltrados.filter(v => 
            v.destination?.name.toLowerCase().includes(destino.toLowerCase())
        );
    }
    
    // Filtrar por precio
    if (precio) {
        switch (precio) {
            case '0-1000':
                viajesFiltrados = viajesFiltrados.filter(v => v.totalPrice <= 1000);
                break;
            case '1000-2000':
                viajesFiltrados = viajesFiltrados.filter(v => v.totalPrice > 1000 && v.totalPrice <= 2000);
                break;
            case '2000+':
                viajesFiltrados = viajesFiltrados.filter(v => v.totalPrice > 2000);
                break;
        }
    }
    
    // Filtrar por búsqueda
    if (search) {
        viajesFiltrados = viajesFiltrados.filter(v => 
            v.name.toLowerCase().includes(search) || 
            v.description.toLowerCase().includes(search)
        );
    }
    
    renderViajes(viajesFiltrados);
}

// Funciones globales para los botones
window.addToCart = (packageId) => {
    const viaje = allPackages.find(v => v.packageId === packageId);
    if (viaje) {
        state.addToCart(viaje);
        toast(`¡${viaje.name} agregado al carrito!`, 'ok');
    }
};

window.toggleFavorite = (packageId) => {
    const viaje = allPackages.find(v => v.packageId === packageId);
    if (viaje) {
        state.toggleFavorite(viaje);
        const isNowFavorite = state.isFavorite(packageId);
        toast(isNowFavorite ? 'Agregado a favoritos' : 'Removido de favoritos', 'info');
        
        // Re-renderizar para actualizar el corazón
        const card = document.querySelector(`[data-viaje-id="${packageId}"]`);
        if (card) {
            const favBtn = card.querySelector('button[onclick*="toggleFavorite"]');
            if (favBtn) {
                favBtn.className = `button is-small ${isNowFavorite ? 'is-danger' : 'is-light'}`;
                favBtn.innerHTML = isNowFavorite ? '❤️' : '🤍';
            }
        }
    }
};