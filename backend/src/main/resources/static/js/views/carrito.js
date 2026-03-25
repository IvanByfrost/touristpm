import { state } from '../state.js';
import { toast } from '../ui.js';
import { bookingApi } from '../api.js';

export const template = `
<section id="view-carrito" class="view">
    <div class="container" style="padding: 2rem 1rem;">
        <h2 class="title is-2 mb-6">Mi <span class="accent-text">Carrito</span></h2>
        
        <div class="columns">
            <div class="column is-8">
                <div class="glass-card p-5">
                    <div id="cart-items-list">
                        <!-- Se llena dinámicamente -->
                    </div>
                </div>
            </div>
            
            <div class="column is-4">
                <div class="glass-card p-5">
                    <h3 class="title is-4 mb-5">Resumen</h3>
                    <div class="is-flex is-justify-content-between mb-2">
                        <span>Subtotal</span>
                        <strong id="cart-subtotal">$0</strong>
                    </div>
                    <div class="is-flex is-justify-content-between mb-4">
                        <span>Impuestos (Incl.)</span>
                        <strong>$0</strong>
                    </div>
                    <hr>
                    <div class="is-flex is-justify-content-between is-size-4 mb-6">
                        <strong>Total</strong>
                        <strong id="cart-total" class="has-text-link">$0</strong>
                    </div>
                    
                    <button id="checkout-btn" class="button is-cta-premium is-fullwidth py-4" style="height: auto;">
                        CONFIRMAR RESERVA
                    </button>
                    <p class="has-text-centered has-text-grey mt-3 is-size-7">Acepto los términos y condiciones</p>
                </div>
            </div>
        </div>
    </div>
</section>
`;

export function initCarrito() {
    console.log('Initializing Cart View...');
    renderCart();

    document.getElementById('checkout-btn')?.addEventListener('click', handleCheckout);
}

function renderCart() {
    const list = document.getElementById('cart-items-list');
    const subtotalEl = document.getElementById('cart-subtotal');
    const totalEl = document.getElementById('cart-total');

    if (!list) return;

    if (state.cart.length === 0) {
        list.innerHTML = `
            <div class="has-text-centered py-6">
                <span style="font-size: 3rem;">🛒</span>
                <p class="title is-4 mt-4">Tu carrito está vacío</p>
                <a href="#/catalogo" class="button is-link is-light is-rounded mt-4">Ver Catálogo</a>
            </div>
        `;
        subtotalEl.textContent = '$0';
        totalEl.textContent = '$0';
        return;
    }

    list.innerHTML = state.cart.map(item => `
        <div class="is-flex is-justify-content-between is-align-items-center mb-4 p-4" style="background: rgba(255,255,255,0.05); border-radius: 12px;">
            <div class="is-flex is-align-items-center">
                <span class="mr-4" style="font-size: 2rem;">🌎</span>
                <div>
                    <strong class="is-size-5">${item.name}</strong>
                    <br>
                    <small class="has-text-grey">
                        📅 ${item.departureDate || 'Sin fecha'} al ${item.returnDate || 'Sin fecha'}
                    </small>
                </div>
            </div>
            <div class="has-text-right">
                <div class="is-flex is-align-items-center mb-1">
                    <button class="button is-small is-light" onclick="updateQty('${item.packageId}', -1)">-</button>
                    <span class="mx-3"><strong>${item.quantity}</strong></span>
                    <button class="button is-small is-light" onclick="updateQty('${item.packageId}', 1)">+</button>
                </div>
                <strong>$${(item.totalPrice * item.quantity).toLocaleString()}</strong>
                <br>
                <a class="is-size-7 has-text-danger" onclick="removeFromCart('${item.packageId}')">Eliminar</a>
            </div>
        </div>
    `).join('');

    const total = state.getCartTotal();
    subtotalEl.textContent = `$${total.toLocaleString()}`;
    totalEl.textContent = `$${total.toLocaleString()}`;
}

async function handleCheckout() {
    if (!state.user) {
        toast('Inicia sesión para reservar', 'info');
        window.location.hash = '#/login';
        return;
    }

    if (state.cart.length === 0) {
        toast('El carrito está vacío', 'err');
        return;
    }

    const btn = document.getElementById('checkout-btn');
    btn.classList.add('is-loading');

    try {
        const codes = [];
        for (const item of state.cart) {
            const resp = await bookingApi.create({
                packageId: item.packageId,
                bookingType: "Package",
                totalAmount: item.totalPrice * item.quantity,
                quantity: item.quantity,
                departureDate: item.departureDate,
                returnDate: item.returnDate,
                details: `Reserva de ${item.name} x${item.quantity}`
            });
            if (resp && resp.bookingCode) codes.push(resp.bookingCode);
        }
        
        const msg = codes.length > 0 
            ? `¡Reserva confirmada! Código: ${codes.join(', ')} ✈️`
            : '¡Reserva confirmada con éxito! ✈️';
            
        toast(msg, 'ok');
        state.clearCart();
        
        // Efecto visual
        document.body.classList.add('takeoff-step1');
        setTimeout(() => {
            window.location.hash = '#/dashboard';
            document.body.classList.remove('takeoff-step1');
        }, 1200);

    } catch (error) {
        toast(`Error al reservar: ${error.message}`, 'err');
    } finally {
        btn.classList.remove('is-loading');
    }
}

// Global functions for cart
window.updateQty = (id, delta) => {
    const item = state.cart.find(item => String(item.packageId) === String(id));
    if (item) {
        item.quantity = Math.max(1, item.quantity + delta);
        state.saveCart();
        renderCart();
        state.updateUI();
    }
};

window.removeFromCart = (id) => {
    state.removeFromCart(id);
    renderCart();
};
