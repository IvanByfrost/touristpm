// ============================
// SISTEMA DE NOTIFICACIONES
// ============================
function showNotification(message, type = 'success') {
    const toast = document.getElementById('notification-toast');
    toast.textContent = message;
    toast.className = `notification-toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ============================
// SISTEMA DE AUTENTICACIÓN
// ============================
const authState = {
    adminLoggedIn: localStorage.getItem('touristchain-admin-logged') === 'true',
    userLoggedIn: localStorage.getItem('touristchain-user') !== null,
    
    requireAdminAuth: function() {
        if (!this.adminLoggedIn) {
            showNotification('❌ Acceso denegado. Debes iniciar sesión como administrador.', 'error');
            showView('admin-login');
            return false;
        }
        return true;
    }
};

// ============================
// SISTEMA DE DATOS COMPLETO
// ============================
const database = {
    // Datos de viajes
    viajes: JSON.parse(localStorage.getItem('touristchain-viajes')) || [
        {
            id: 1,
            nombre: "Paraíso en Cancún",
            descripcion: "5 días en resorts todo incluido con playas de arena blanca",
            precio: 1200,
            duracion: "5 días",
            icono: "🏖️",
            destacado: true,
            estado: "activo",
            fecha_inicio: "2024-04-01",
            fecha_fin: "2024-04-05",
            capacidad: 50,
            reservados: 25
        },
        // ... más viajes
    ],

    // Usuarios del sistema
    users: JSON.parse(localStorage.getItem('touristchain-users')) || [
        { id: 1, name: "Carlos Pérez", email: "carlos@email.com", role: "tourist", status: "active", registered: "2024-01-15", phone: "+34 600 111 222" },
        // ... más usuarios
    ],

    // ... resto de los datos

    // Métodos para guardar datos
    saveData: function() {
        localStorage.setItem('touristchain-viajes', JSON.stringify(this.viajes));
        localStorage.setItem('touristchain-users', JSON.stringify(this.users));
        localStorage.setItem('touristchain-companies', JSON.stringify(this.companies));
        localStorage.setItem('touristchain-workers', JSON.stringify(this.workers));
        localStorage.setItem('touristchain-payments', JSON.stringify(this.payments));
    },

    // ... resto de métodos CRUD
};

// ============================
// ESTADO DE LA APLICACIÓN
// ============================
const appState = {
    currentSlide: 0,
    favorites: JSON.parse(localStorage.getItem('touristchain-favorites')) || [],
    reservations: JSON.parse(localStorage.getItem('touristchain-reservations')) || [],
    user: JSON.parse(localStorage.getItem('touristchain-user')) || null,
    theme: localStorage.getItem('touristchain-theme') || 'default',
    adminVisible: true,
    currentEditId: null,
    currentEditType: null
};

// ============================
// FUNCIONES GENERALES
// ============================

// NAVEGACIÓN
function showView(viewId) {
    // Verificar acceso para vistas administrativas
    if (viewId === 'admin-panel' && !authState.adminLoggedIn) {
        showNotification('❌ Acceso denegado. Debes iniciar sesión como administrador.', 'error');
        showView('admin-login');
        return;
    }

    // Verificar si es una sección admin
    if (viewId.startsWith('admin-')) {
        if (!authState.adminLoggedIn) {
            showNotification('❌ Acceso denegado. Debes iniciar sesión como administrador.', 'error');
            showView('admin-login');
            return;
        }
    }

    // Ocultar todas las vistas
    document.querySelectorAll('.view').forEach(view => {
        view.classList.remove('active');
    });
    
    // Mostrar la vista solicitada
    setTimeout(() => {
        const targetView = document.getElementById(viewId);
        if (targetView) {
            targetView.classList.add('active');
            
            // Inicializar componentes específicos de la vista
            if (viewId === 'viajes') {
                initCarousel();
                initViajesGrid();
            } else if (viewId === 'dashboard') {
                updateFavoritesDisplay();
                updateReservationsDisplay();
            } else if (viewId === 'admin-panel') {
                initAdminPanel();
            }
        }
    }, 50);
}

// TEMA
function toggleTheme() {
    document.body.classList.remove('theme-cool', 'theme-dark');
    
    if (appState.theme === 'default') {
        document.body.classList.add('theme-cool');
        appState.theme = 'cool';
        document.getElementById('theme-toggle').textContent = '☀️';
    } else if (appState.theme === 'cool') {
        document.body.classList.add('theme-dark');
        appState.theme = 'dark';
        document.getElementById('theme-toggle').textContent = '🌙';
    } else {
        appState.theme = 'default';
        document.getElementById('theme-toggle').textContent = '❄️';
    }
    
    localStorage.setItem('touristchain-theme', appState.theme);
}

// ============================
// FUNCIONALIDADES DEL SITIO PRINCIPAL
// ============================

// SISTEMA DE RESERVAS
function setupReservationButtons() {
    document.querySelectorAll('.viaje-card .button.is-cta[data-id]').forEach(button => {
        button.addEventListener('click', function() {
            const viajeId = parseInt(this.getAttribute('data-id'));
            const viaje = database.viajes.find(v => v.id === viajeId);
            
            if (viaje) {
                const existingReservation = appState.reservations.find(r => r.id === viajeId);
                
                if (!existingReservation) {
                    appState.reservations.push({
                        id: viaje.id,
                        nombre: viaje.nombre,
                        precio: viaje.precio,
                        fecha: new Date().toLocaleDateString()
                    });
                    
                    localStorage.setItem('touristchain-reservations', JSON.stringify(appState.reservations));
                    showNotification('¡Viaje agregado al carrito! 🛒');
                    updateReservationsDisplay();
                } else {
                    showNotification('Ya tienes este viaje en tus reservas', 'warning');
                }
            }
        });
    });
}

// ... resto de las funciones principales