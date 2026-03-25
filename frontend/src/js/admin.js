// ============================
// PANEL ADMINISTRATIVO
// ============================

// LOGIN ADMIN
document.getElementById('admin-login-form').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const username = document.getElementById('admin-username').value;
    const password = document.getElementById('admin-password').value;
    const code = document.getElementById('admin-code').value;
    
    if (username === 'admin' && password === 'Admin123!' && code === '4321') {
        authState.adminLoggedIn = true;
        localStorage.setItem('touristchain-admin-logged', 'true');
        showNotification('✅ Acceso concedido al panel administrativo');
        showView('admin-panel');
        initAdminPanel();
    } else {
        showNotification('❌ Credenciales incorrectas. Usa: admin / Admin123! / 4321', 'error');
    }
});

// LOGOUT ADMIN
document.getElementById('admin-logout').addEventListener('click', function() {
    authState.adminLoggedIn = false;
    localStorage.removeItem('touristchain-admin-logged');
    showNotification('👋 Sesión administrativa cerrada');
    showView('inicio');
});

// NAVEGACIÓN ADMIN
function showAdminSection(sectionId) {
    if (!authState.adminLoggedIn) {
        showNotification('❌ Acceso denegado', 'error');
        showView('admin-login');
        return;
    }
    
    document.querySelectorAll('.admin-section').forEach(section => {
        section.classList.remove('active');
    });
    
    document.querySelectorAll('.admin-menu-item').forEach(item => {
        item.classList.remove('active');
    });
    
    const section = document.getElementById(`admin-${sectionId}-section`);
    if (section) {
        section.classList.add('active');
    }
    
    const menuItem = document.querySelector(`[onclick="showAdminSection('${sectionId}')"]`);
    if (menuItem) {
        menuItem.classList.add('active');
    }
    
    // Cargar datos específicos
    switch(sectionId) {
        case 'dashboard':
            updateAdminStats();
            initCharts();
            loadActivity();
            break;
        case 'users':
            loadUsers();
            break;
        case 'workers':
            loadWorkers();
            break;
        case 'companies':
            loadCompanies();
            break;
        case 'payments':
            loadPayments();
            break;
        case 'trips':
            loadAdminTrips();
            break;
    }
}

// INICIALIZAR PANEL ADMIN
function initAdminPanel() {
    updateAdminStats();
    initCharts();
    loadUsers();
    loadWorkers();
    loadCompanies();
    loadPayments();
    loadActivity();
    loadAdminTrips();
}

// ... resto de funciones administrativas