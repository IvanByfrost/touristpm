import { authApi } from './api.js';

const LS_THEME = "tc_theme";
const LS_USER = "tc_user";
const LS_CART = "tc_cart";
const LS_FAVORITES = "tc_favorites";

const listeners = new Set();
const notify = () => listeners.forEach(fn => fn(state));

function applyThemeToDOM(theme) {
    document.body.classList.toggle("theme-cool", theme === "cool");
    const toggle = document.getElementById('theme-toggle');
    if (toggle) {
        toggle.textContent = theme === 'cool' ? '☀️' : '🌙';
    }
}

function initialState() {
    const theme = localStorage.getItem(LS_THEME) || "warm";
    let user = null;
    let cart = [];
    let favorites = [];

    try {
        user = JSON.parse(localStorage.getItem(LS_USER) || "null");
        cart = JSON.parse(localStorage.getItem(LS_CART) || "[]");
        favorites = JSON.parse(localStorage.getItem(LS_FAVORITES) || "[]");
    } catch (e) {
        console.error("Error loading state:", e);
    }

    return { theme, user, cart, favorites };
}

export const state = {
    theme: "warm",
    user: null,
    cart: [],
    favorites: [],

    init() {
        const s = initialState();
        this.theme = s.theme;
        this.user = s.user;
        this.cart = s.cart;
        this.favorites = s.favorites;
        applyThemeToDOM(this.theme);
        this.updateUI();
        notify();
    },

    // --- USER METHODS (Auth) ---
    setUser(userObj) {
        if (userObj && userObj.roles && !userObj.role) {
            userObj.role = userObj.roles[0];
        }
        this.user = userObj;
        localStorage.setItem(LS_USER, JSON.stringify(userObj));
        this.updateUI();
        notify();
    },

    clearUser() {
        this.user = null;
        localStorage.removeItem(LS_USER);
        this.updateUI();
        notify();
    },

    // REINTEGRADA: La función de login que se había perdido
    async login(email, password) {
        try {
            const userData = await authApi.login(email, password);
            this.setUser(userData);
            return userData;
        } catch (error) {
            console.error("Login Error:", error);
            throw error;
        }
    },

    // REINTEGRADA: La función de registro
    async signup(fullName, document, email, password, role = ["ROLE_USER"]) {
        try {
            const message = await authApi.signup({
                fullName,
                document,
                email,
                password,
                role
            });
            return message;
        } catch (error) {
            console.error("Signup Error:", error);
            throw error;
        }
    },

    logout() {
        this.clearUser();
        this.clearCart();
        location.hash = '#/inicio';
    },

    // --- CART METHODS (Sincronizados con MySQL) ---
    addToCart(viaje) {
        // Buscamos por packageId (llave primaria de tu tabla packages)
        const existing = this.cart.find(item => String(item.packageId) === String(viaje.packageId));

        if (existing) {
            existing.quantity = (existing.quantity || 1) + 1;
        } else {
            this.cart.push({
                ...viaje,
                quantity: 1,
                cartId: Date.now()
            });
        }
        this.saveCart();
        this.updateUI();
        notify();
    },

    removeFromCart(packageId) {
        this.cart = this.cart.filter(item => String(item.packageId) !== String(packageId));
        this.saveCart();
        this.updateUI();
        notify();
    },

    clearCart() {
        this.cart = [];
        this.saveCart();
        this.updateUI();
        notify();
    },

    saveCart() {
        localStorage.setItem(LS_CART, JSON.stringify(this.cart));
    },

    getCartTotal() {
        return this.cart.reduce((total, item) => {
            const precio = parseFloat(item.totalPrice) || 0;
            return total + (precio * (item.quantity || 1));
        }, 0);
    },

    // --- FAVORITES (Sincronizados con packageId) ---
    toggleFavorite(viaje) {
        const index = this.favorites.findIndex(fav => String(fav.packageId) === String(viaje.packageId));
        if (index > -1) {
            this.favorites.splice(index, 1);
        } else {
            this.favorites.push(viaje);
        }
        localStorage.setItem(LS_FAVORITES, JSON.stringify(this.favorites));
        this.updateUI();
        notify();
    },

    isFavorite(packageId) {
        return this.favorites.some(fav => String(fav.packageId) === String(packageId));
    },

    // --- UI & PUB/SUB ---
    updateUI() {
        const loginBtn = document.getElementById('nav-login');
        const dashboardBtn = document.getElementById('nav-dashboard');

        if (this.user) {
            if (loginBtn) loginBtn.classList.add('is-hidden');
            if (dashboardBtn) dashboardBtn.classList.remove('is-hidden');
            
            // Mostrar links de admin solo si tiene el rol
            const roles = this.user.roles || this.user.role || [];
            const roleList = Array.isArray(roles) ? roles : [roles];
            const normalizedRoles = roleList.map(r => r.startsWith('ROLE_') ? r.substring(5) : r);
            const isAdmin = normalizedRoles.includes('ADMIN');
            
            const adminLink = document.getElementById('nav-admin-link');
            const testsLink = document.getElementById('nav-tests-link');
            
            if (adminLink) {
                isAdmin ? adminLink.classList.remove('is-hidden') : adminLink.classList.add('is-hidden');
            }
            if (testsLink) {
                isAdmin ? testsLink.classList.remove('is-hidden') : testsLink.classList.add('is-hidden');
            }
        } else {
            if (loginBtn) loginBtn.classList.remove('is-hidden');
            if (dashboardBtn) dashboardBtn.classList.add('is-hidden');
        }

        const cartCount = document.querySelector('.tc-cart-count');
        if (cartCount) {
            cartCount.textContent = this.cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
        }
    },

    subscribe(fn) { listeners.add(fn); return () => listeners.delete(fn); }
};

window.tcState = state;