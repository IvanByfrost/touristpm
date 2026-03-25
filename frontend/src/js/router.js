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
        console.log(`Router: Showing route [${routeName}] (Hash: ${location.hash})`);
        showLoading();
        
        // Pequeño delay para mejor UX
        await new Promise(resolve => setTimeout(resolve, 300));
        
        document.querySelectorAll(".view").forEach(v => v.classList.remove("active"));
        const target = document.getElementById(`view-${routeName}`) || document.getElementById("view-inicio");
        
        if (target) {
            target.classList.add("active");
            const controller = this.routes[routeName];
            if (controller) {
                console.log(`Router: Calling controller for [${routeName}]`);
                await controller();
            }
        } else {
            console.warn(`Router: Target view [view-${routeName}] not found!`);
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