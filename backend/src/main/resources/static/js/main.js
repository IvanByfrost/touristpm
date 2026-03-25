import { state } from "./state.js";
window.ANTIGRAVITY_VERSION = 2.2;
console.log("--- ANTIGRAVITY DEPLOYMENT 2.2 ---");
import { Router } from "./router.js";
import { initInicio, template as inicioTemplate } from "./views/inicio.js";
import { initLogin, template as loginTemplate } from "./views/login.js";
import { initRegistro, template as registroTemplate } from "./views/registro.js";
import { initDashboard, template as dashboardTemplate } from "./views/dashboard.js";
import { initCatalogo, template as catalogoTemplate } from "./views/catalogo.js";
import { initAdmin, template as adminTemplate } from "./views/admin.js";
import { initTests, template as testsTemplate } from "./views/tests.js";
import { initCarrito, template as carritoTemplate } from "./views/carrito.js";
import { initConsulta, template as consultaTemplate } from "./views/consulta.js";
import { initOlvide, template as olvideTemplate } from "./views/olvide.js";

// Initialize global state
state.init();

// Initialize router with routes configuration
const router = new Router({
  inicio: { init: initInicio, template: inicioTemplate },
  login: { init: initLogin, template: loginTemplate },
  registro: { init: initRegistro, template: registroTemplate },
  dashboard: { init: initDashboard, template: dashboardTemplate },
  catalogo: { init: initCatalogo, template: catalogoTemplate },
  olvide: { init: initOlvide, template: olvideTemplate },
  admin: { init: initAdmin, template: adminTemplate },
  tests: { init: initTests, template: testsTemplate },
  carrito: { init: initCarrito, template: carritoTemplate },
  consulta: { init: initConsulta, template: consultaTemplate }
});

// Global Navbar Listeners
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('nav-logout-btn')?.addEventListener('click', (e) => {
        e.preventDefault();
        state.logout();
    });
});

// Start routing
router.start();
