import { adminApi } from '../api.js';
import { toast } from '../ui.js';

export const template = `
<section id="view-tests" class="view">
    <div class="container" style="padding: 2rem 1rem;">
        <h2 class="title is-2 mb-6">Ejecución de <span class="accent-text">Pruebas</span></h2>
        
        <div class="columns">
            <!-- Sidebar: Lista de Tests -->
            <div class="column is-4">
                <div class="glass-card p-5">
                    <h3 class="title is-5 mb-4">Tests Disponibles</h3>
                    <div id="test-list" class="menu">
                        <!-- Cargando dinámicamente -->
                        <p class="has-text-grey-light">Cargando clases de prueba...</p>
                    </div>
                </div>
            </div>

            <!-- Main: Resultados -->
            <div class="column is-8">
                <div id="test-result-panel" class="glass-card p-5" style="display: none;">
                    <div class="is-flex is-justify-content-between is-align-items-center mb-4">
                        <h3 id="current-test-name" class="title is-4 mb-0">Resultado</h3>
                        <span id="test-status-tag" class="tag is-medium">Pendiente</span>
                    </div>
                    <pre id="test-output" style="background: #1e293b; color: #38bdf8; padding: 1rem; border-radius: 8px; font-family: monospace; font-size: 0.85rem; max-height: 500px; overflow: auto;"></pre>
                </div>
                
                <div id="no-test-selected" class="glass-card p-6 has-text-centered">
                    <span style="font-size: 4rem;">🧪</span>
                    <p class="title is-4 mt-4">Selecciona una prueba para ejecutar</p>
                    <p class="subtitle">Se ejecutará 'mvn test' en el servidor backend.</p>
                </div>
            </div>
        </div>
    </div>
</section>
`;

export async function initTests() {
    console.log('Initializing Tests View...');
    await loadTests();
}

async function loadTests() {
    const list = document.getElementById('test-list');
    if (!list) return;

    try {
        const tests = await adminApi.getTests();
        renderTestList(tests);
    } catch (error) {
        list.innerHTML = `<p class="has-text-danger">Error: ${error.message}</p>`;
    }
}

function renderTestList(tests) {
    const list = document.getElementById('test-list');
    list.innerHTML = tests.map(testClass => `
        <p class="menu-label">${testClass.name}</p>
        <ul class="menu-list mb-4">
            ${testClass.methods.map(m => `
                <li>
                    <a onclick="runSingleTest('${testClass.fullClassName}', '${m.methodName}')">
                        ${m.description || m.methodName}
                    </a>
                </li>
            `).join('')}
            <li>
                <a onclick="runSingleTest('${testClass.fullClassName}', '')" style="color: var(--accent-blue); font-weight: bold;">
                    Ejecutar toda la clase
                </a>
            </li>
        </ul>
    `).join('');
}

window.runSingleTest = async (className, methodName) => {
    const resultPanel = document.getElementById('test-result-panel');
    const noSelected = document.getElementById('no-test-selected');
    const output = document.getElementById('test-output');
    const statusTag = document.getElementById('test-status-tag');
    const title = document.getElementById('current-test-name');

    noSelected.style.display = 'none';
    resultPanel.style.display = 'block';
    output.textContent = 'Ejecutando prueba en el servidor...\n(Esto puede tardar unos segundos)';
    statusTag.className = 'tag is-warning is-medium';
    statusTag.textContent = 'En progreso...';
    title.textContent = methodName || className;

    try {
        const result = await adminApi.runTest(className, methodName);
        output.textContent = result.output;
        
        if (result.success) {
            statusTag.className = 'tag is-success is-medium';
            statusTag.textContent = 'ÉXITO';
            toast('¡Prueba completada con éxito!', 'ok');
        } else {
            statusTag.className = 'tag is-danger is-medium';
            statusTag.textContent = 'FALLO';
            toast('La prueba ha fallado', 'err');
        }
    } catch (error) {
        output.textContent = `Error de red o servidor: ${error.message}`;
        statusTag.className = 'tag is-danger is-medium';
        statusTag.textContent = 'ERROR';
    }
};
