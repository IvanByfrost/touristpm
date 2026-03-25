import { state } from '../state.js';
import { toast } from '../ui.js';

export const template = `
<section id="view-login" class="view">
  <div class="container">
    <div class="glass-card tc-auth-card tc-login-card">
      <div class="has-text-centered mb-5">
        <img src="/assets/avion.png" alt="Logo" style="height:120px; filter:drop-shadow(0 15px 30px rgba(0,0,0,0.15));"/>
      </div>
      <h2 class="title is-2 has-text-centered mb-5">Iniciar sesión</h2>
      <form id="loginForm">
        <div class="field">
          <label class="label">Correo electrónico</label>
          <input type="email" class="input" placeholder="ejemplo@gmail.com" required />
        </div>
        <div class="field">
          <label class="label">Contraseña</label>
          <input type="password" class="input" placeholder="********" required />
          <p class="has-text-right mt-2"><a href="#/olvide" class="is-underlined is-size-7">¿Olvidaste tu contraseña?</a></p>
        </div>
        <button class="button is-login-btn is-fullwidth mt-5">INGRESAR</button>
      </form>
      <p class="has-text-centered mt-5">¿No tienes cuenta?
        <a href="#/registro" class="has-text-weight-bold is-underlined">Regístrate</a>
      </p>
      <p class="has-text-centered mt-4"><a href="#/inicio" class="is-size-7 opacity-7">Volver al inicio</a></p>
    </div>
  </div>
</section>
`;

export function initLogin() {
    console.log('Inicializando login...');
    
    const form = document.getElementById('loginForm');
    if (!form || form.dataset.initialized) return;
    form.dataset.initialized = 'true';
    
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