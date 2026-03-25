// Vista: Inicio
import { state } from '../state.js';
import { toast } from '../ui.js';
import { packageApi } from '../api.js';

export async function initInicio() {
    console.log('Inicializando vista inicio...');
    
    const plane = document.getElementById('plane-main');
    const btn = document.getElementById('cta-reservar');
    const canvas = document.getElementById('bgCanvas');
    
    // Setup animación del avión (Inmediato)
    if (btn && plane) {
        setupPlaneAnimation(btn, plane);
    }

    // Inicializar olas animadas
    if (canvas) {
        initWaves(canvas);
    }
    
    // Cargar destinos destacados (de catálogo Real - Asíncrono)
    try {
        const packages = await packageApi.getAll();
        renderDestacados(packages.slice(0, 3));
    } catch (error) {
        console.error("Error al cargar paquetes:", error);
    }
}

function getEmojiForDestination(name) {
    if (name.includes('Cancún')) return '🏖️';
    if (name.includes('Andes')) return '⛰️';
    if (name.includes('Safari')) return '🦁';
    if (name.includes('New York')) return '🏙️';
    if (name.includes('París')) return '🗼';
    return '🌎';
}

function renderDestacados(destacados) {
    const grid = document.getElementById('home-destinos-grid');
    if (!grid) return;
    
    grid.innerHTML = destacados.map(d => `
        <div class="column is-4">
            <div class="glass-card">
                <div class="tc-package-header">
                    <span class="tc-viaje-emoji">${getEmojiForDestination(d.name)}</span>
                </div>
                <div class="tc-package-content has-text-centered">
                    <div>
                        <h3 class="title is-4 mb-4" style="font-weight:700;">${d.name}</h3>
                        <p class="subtitle is-6 mb-0" style="color:#64748b;">${d.description.substring(0, 65)}...</p>
                    </div>
                    
                    <div class="tc-price-tag">
                        <span class="is-size-7 has-text-grey-light is-uppercase" style="letter-spacing:1px; font-weight:600;">Desde</span>
                        <div class="price-value is-size-2 mt-1">$${d.totalPrice}</div>
                    </div>
                    
                    <a href="#/catalogo" class="button is-link is-light is-fullwidth is-rounded has-text-weight-bold">Explorar Destino</a>
                </div>
            </div>
        </div>
    `).join('');
}

function initWaves(canvas) {
    const ctx = canvas.getContext('2d');
    let W = 0, H = 0, T = 0;
    
    // Colores de olas según el tema
    const waveColors = state.theme === 'cool' 
        ? ["rgba(180,215,247,.55)", "rgba(127,176,255,.45)", "rgba(18,71,243,.35)"]
        : ["rgba(255,255,255,.55)", "rgba(255,210,160,.45)", "rgba(255,180,150,.35)"];
    
    function resize() { 
        W = canvas.width = innerWidth; 
        H = canvas.height = innerHeight * 0.38; 
    }
    
    addEventListener("resize", resize); 
    resize();
    
    (function draw() {
        ctx.clearRect(0, 0, W, H);
        
        for (let i = 0; i < 3; i++) {
            ctx.beginPath();
            for (let x = 0; x < W; x++) {
                const y = Math.sin((x + T * 2) * 0.02 + i) * 15 + H / 2 + i * 10;
                ctx.lineTo(x, y);
            }
            ctx.strokeStyle = waveColors[i];
            ctx.lineWidth = 2 + i;
            ctx.stroke();
        }
        
        T += 0.5; 
        requestAnimationFrame(draw);
    })();
}

function setupPlaneAnimation(btn, plane) {
    btn.addEventListener('click', (e) => {
        e.preventDefault();
        
        // Si el usuario no está logueado, ir a login
        if (!state.user) {
            animateToLogin(plane);
        } else {
            // Si está logueado, ir al catálogo
            animateToCatalogo(plane);
        }
    });
}

function animateToLogin(plane) {
    plane.classList.remove("tc-float", "takeoff-step1", "takeoff-step2");
    void plane.offsetWidth; // reset animation
    
    plane.classList.add("takeoff-step1");
    plane.addEventListener("animationend", function step1() {
        plane.removeEventListener("animationend", step1);
        plane.classList.remove("takeoff-step1");
        plane.classList.add("takeoff-step2");
        plane.addEventListener("animationend", function step2() {
            plane.removeEventListener("animationend", step2);
            plane.classList.remove("takeoff-step2");
            location.hash = "#/login";
        }, { once: true });
    }, { once: true });
}

function animateToCatalogo(plane) {
    plane.classList.remove("tc-float", "takeoff-step1", "takeoff-step2");
    void plane.offsetWidth; // reset animation
    
    toast('¡Explora nuestros destinos!', 'info');
    plane.classList.add("takeoff-step1");
    plane.addEventListener("animationend", function step1() {
        plane.removeEventListener("animationend", step1);
        plane.classList.remove("takeoff-step1");
        plane.classList.add("takeoff-step2");
        plane.addEventListener("animationend", function step2() {
            plane.removeEventListener("animationend", step2);
            plane.classList.remove("takeoff-step2");
            location.hash = "#/catalogo";
        }, { once: true });
    }, { once: true });
}