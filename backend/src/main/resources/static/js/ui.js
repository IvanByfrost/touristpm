// Utilitarios UI
export const qs = (s, r = document) => r.querySelector(s);
export const qsa = (s, r = document) => [...r.querySelectorAll(s)];
export const on = (el, ev, fn, opts) => el && el.addEventListener(ev, fn, opts);

export const cx = {
    add: (el, ...c) => el && el.classList.add(...c),
    rm: (el, ...c) => el && el.classList.remove(...c),
    tog: (el, c, f) => el && el.classList.toggle(c, f),
};

let toastTimer = null;
export function toast(msg, type = "info", ms = 1800) {
    let el = qs("#tc-toast");
    if (!el) {
        el = document.createElement("div");
        el.id = "tc-toast";
        el.style.cssText = "position:fixed;left:50%;bottom:28px;transform:translateX(-50%);background:rgba(0,0,0,.75);color:#fff;padding:12px 18px;border-radius:12px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.25);z-index:9999;opacity:0;transition:opacity .3s,transform .3s;backdrop-filter:blur(10px)";
        document.body.appendChild(el);
    }
    
    const colors = { info: "#0ea5e9", ok: "#16a34a", warn: "#f59e0b", err: "#ef4444" };
    el.className = "tc-toast-" + type;
    el.textContent = msg;
    el.style.borderLeft = `4px solid ${colors[type] || colors.info}`;
    el.style.opacity = "1";
    el.style.transform = "translateX(-50%) translateY(0)";
    
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        el.style.opacity = "0";
        el.style.transform = "translateX(-50%) translateY(6px)";
    }, ms);
}

// Loading global
export function showLoading() {
    const loading = qs('#global-loading');
    if (loading) loading.classList.remove('is-hidden');
}

export function hideLoading() {
    const loading = qs('#global-loading');
    if (loading) loading.classList.add('is-hidden');
}

// Toggle carrito
export function setupCartToggle() {
    const cartBtn = qs('#cart-btn');
    const cartDropdown = qs('#cart-dropdown');
    
    if (cartBtn && cartDropdown) {
        on(cartBtn, 'click', (e) => {
            e.stopPropagation();
            cartDropdown.classList.toggle('is-hidden');
        });
        
        // Cerrar al hacer click fuera
        on(document, 'click', () => {
            cartDropdown.classList.add('is-hidden');
        });
        
        // Prevenir que se cierre al hacer click dentro
        on(cartDropdown, 'click', (e) => {
            e.stopPropagation();
        });
    }
}

// Setup tema toggle
export function setupThemeToggle() {
    const toggle = qs('#theme-toggle');
    if (toggle) {
        on(toggle, 'click', () => {
            state.toggleTheme();
        });
    }
}