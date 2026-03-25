import { authApi } from './api.js';

// TouristChain — State (tema + usuario + carrito) con persistencia
const LS_THEME = "tc_theme";
const LS_USER = "tc_user";
const LS_CART = "tc_cart";
const LS_FAVORITES = "tc_favorites";

const listeners = new Set();
const notify = () => listeners.forEach(fn => fn(state));

function applyThemeToDOM(theme) {
    document.body.classList.toggle("theme-cool", theme === "cool");
    // Actualizar ícono del toggle
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

    // Theme
    setTheme(theme) {
        if (theme !== "warm" && theme !== "cool") return;
        this.theme = theme;
        localStorage.setItem(LS_THEME, theme);
        applyThemeToDOM(theme);
        notify();
    },

    toggleTheme() { 
        this.setTheme(this.theme === "cool" ? "warm" : "cool"); 
    },

    // User
    setUser(userObj) { 
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
        location.hash = '#/inicio';
    },

    // Cart
    addToCart(viaje) {
        const existing = this.cart.find(item => item.id === viaje.id);
        if (existing) {
            existing.quantity = (existing.quantity || 1) + 1;
        } else {
            this.cart.push({
                ...viaje,
                quantity: 1,
                cartId: Date.now() // ID único para el carrito
            });
        }
        this.saveCart();
        this.updateUI();
        notify();
    },

    removeFromCart(cartId) {
        this.cart = this.cart.filter(item => item.cartId !== cartId);
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
        return this.cart.reduce((total, item) => total + (item.precio * item.quantity), 0);
    },

    // Favorites
    toggleFavorite(viaje) {
        const index = this.favorites.findIndex(fav => fav.id === viaje.id);
        if (index > -1) {
            this.favorites.splice(index, 1);
        } else {
            this.favorites.push(viaje);
        }
        localStorage.setItem(LS_FAVORITES, JSON.stringify(this.favorites));
        this.updateUI();
        notify();
    },

    isFavorite(viajeId) {
        return this.favorites.some(fav => fav.id === viajeId);
    },

    // UI Updates
    updateUI() {
        // Actualizar navegación
        const loginBtn = document.getElementById('nav-login');
        const dashboardBtn = document.getElementById('nav-dashboard');
        
        if (this.user) {
            if (loginBtn) loginBtn.classList.add('is-hidden');
            if (dashboardBtn) dashboardBtn.classList.remove('is-hidden');
        } else {
            if (loginBtn) loginBtn.classList.remove('is-hidden');
            if (dashboardBtn) dashboardBtn.classList.add('is-hidden');
        }

        // Actualizar contador del carrito
        const cartCount = document.querySelector('.tc-cart-count');
        if (cartCount) {
            cartCount.textContent = this.cart.reduce((sum, item) => sum + item.quantity, 0);
        }
    },

    // Pub/Sub
    subscribe(fn) { listeners.add(fn); return () => listeners.delete(fn); }
};

// Global reference for debugging and consistency
window.tcState = state;