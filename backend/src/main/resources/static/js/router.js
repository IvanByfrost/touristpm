// Router por hash muy simple
import { showLoading, hideLoading } from './ui.js';

export class Router {
    constructor(routes) { 
        this.routes = routes; 
        window.addEventListener("hashchange", () => this.sync()); 
    }
    
    getCurrentRoute(){ 
        return (location.hash || "#/inicio").replace("#/",""); 
    }
    async show(routeName){
        showLoading();
        
        const config = this.routes[routeName] || this.routes['inicio'];
        const appContainer = document.getElementById('app');
        
        if (appContainer && config) {
            console.log(`Router: Rendering [${routeName}]`);
            
            // Inyectar template si existe
            if (config.template) {
                appContainer.innerHTML = config.template;
            }
            
            // Activar la vista (clase active para animaciones CSS)
            const target = appContainer.querySelector('.view');
            if (target) {
                target.classList.add('active');
            }

            // Ejecutar controlador/init
            if (config.init) {
                console.log(`Router: Initializing [${routeName}]`);
                await config.init();
            }
        } else {
            console.warn(`Router: Route [${routeName}] or #app container not found!`);
        }
        
        hideLoading();
    }
    
    sync(){ 
        const route = this.getCurrentRoute();
        console.log(`Router: Syncing to [${route}]`);
        this.show(route); 
    }
    
    start(){ 
        this.sync(); 
        // Esconder loading después de cargar todo
        setTimeout(hideLoading, 1000);
    }
}