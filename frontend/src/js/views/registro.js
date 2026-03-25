// Vista: Registro
import { state } from '../state.js';
import { toast } from '../ui.js';

export function initRegistro() {
    console.log('Inicializando registro...');
    
    const form = document.getElementById('registerForm');
    if (!form) return;
    
    const btn = form.querySelector('button');
    const inputs = form.querySelectorAll('input[required]');
    
    // Auto-focus en el primer input
    if (inputs[0]) {
        setTimeout(() => inputs[0].focus(), 300);
    }
    
    form.addEventListener('submit', (ev) => {
        ev.preventDefault();
        
        const formData = getFormData(form);
        
        // Validaciones
        if (!validateForm(formData)) {
            return;
        }
        
        // Llamada a API real
        realApiRegistration(btn, formData);
    });
    
    // Validación en tiempo real para confirmar contraseña
    const passwordInputs = form.querySelectorAll('input[type="password"]');
    if (passwordInputs.length >= 2) {
        passwordInputs[1].addEventListener('input', () => {
            validatePasswords(passwordInputs[0], passwordInputs[1]);
        });
    }
}

function getFormData(form) {
    return {
        fullName: document.getElementById('reg-fullname').value.trim(),
        document: document.getElementById('reg-document').value.trim(),
        email: document.getElementById('reg-email').value.trim(),
        password: form.querySelectorAll('input[type="password"]')[0].value,
        password2: form.querySelectorAll('input[type="password"]')[1].value,
        role: translateRole(form.querySelector('select').value),
        terminos: form.querySelector('input[type="checkbox"]').checked
    };
}

function translateRole(label) {
    switch (label) {
        case 'Turista': return ['ROLE_USER'];
        case 'Socio': return ['ROLE_PARTNER'];
        case 'Administrador': return ['ROLE_ADMIN'];
        default: return ['ROLE_USER'];
    }
}

function validateForm(data) {
    if (!data.fullName || !data.document || !data.email || !data.password || !data.password2) {
        toast('Por favor, completa todos los campos', 'warn');
        return false;
    }
    
    if (data.fullName.length < 3) {
        toast('El nombre debe tener al menos 3 caracteres', 'warn');
        return false;
    }

    if (!/^\d+$/.test(data.document)) {
        toast('El documento debe contener solo números', 'warn');
        return false;
    }
    
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
        toast('Por favor ingresa un email válido', 'warn');
        return false;
    }
    
    if (data.password.length < 8) {
        toast('La contraseña debe tener al menos 8 caracteres', 'warn');
        return false;
    }
    
    if (data.password !== data.password2) {
        toast('Las contraseñas no coinciden', 'err');
        return false;
    }
    
    if (!data.terminos) {
        toast('Debes aceptar los términos y condiciones', 'warn');
        return false;
    }
    
    return true;
}

function validatePasswords(pass1, pass2) {
    if (pass2.value && pass1.value !== pass2.value) {
        pass2.style.borderColor = '#ef4444';
    } else {
        pass2.style.borderColor = '';
    }
}

async function realApiRegistration(btn, data) {
    const originalText = btn.textContent;
    
    // Mostrar loading
    btn.disabled = true;
    btn.textContent = '✨ Creando cuenta...';
    btn.classList.add('is-loading');
    
    try {
        await state.signup(data.fullName, data.document, data.email, data.password, data.role);
        toast(`¡Cuenta creada exitosamente! Por favor inicia sesión.`, 'ok');
        
        // Restaurar botón brevemente antes de redirigir
        btn.disabled = false;
        btn.textContent = '✅ ¡Listo!';
        
        setTimeout(() => {
            location.hash = '#/login';
        }, 1500);
    } catch (error) {
        toast(error.message || 'Error al crear la cuenta', 'err');
        btn.disabled = false;
        btn.textContent = originalText;
    } finally {
        btn.classList.remove('is-loading');
    }
}