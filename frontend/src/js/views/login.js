// Vista: Login
import { state } from '../state.js';
import { toast } from '../ui.js';

export function initLogin() {
    console.log('Inicializando login...');
    
    const form = document.getElementById('loginForm');
    if (!form) return;
    
    const btn = form.querySelector('button');
    const email = form.querySelector('input[type="email"]');
    const password = form.querySelector('input[type="password"]');
    
    // Auto-focus en el email
    if (email) {
        setTimeout(() => email.focus(), 300);
    }
    
    form.addEventListener('submit', (ev) => {
        ev.preventDefault();
        
        if (!email.value || !password.value) {
            toast('Por favor, completa ambos campos', 'warn');
            return;
        }
        
        // Validación básica de email
        if (!isValidEmail(email.value)) {
            toast('Por favor ingresa un email válido', 'warn');
            return;
        }
        
        // Llamada a API real
        realApiLogin(btn, email.value, password.value);
    });
    
    // Demo: llenar campos de demo automáticamente
    setTimeout(() => {
        if (email && !email.value) {
            email.value = 'demo@touristchain.com';
            password.value = 'demo123';
        }
    }, 500);
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

async function realApiLogin(btn, email, password) {
    const originalText = btn.textContent;
    
    // Mostrar loading
    btn.disabled = true;
    btn.textContent = '🛫 Ingresando...';
    btn.classList.add('is-loading');
    
    try {
        await state.login(email, password);
        toast(`¡Bienvenido de nuevo!`, 'ok');
        location.hash = '#/dashboard';
    } catch (error) {
        toast(error.message || 'Error al iniciar sesión', 'err');
    } finally {
        // Restaurar botón
        btn.disabled = false;
        btn.textContent = originalText;
        btn.classList.remove('is-loading');
    }
}