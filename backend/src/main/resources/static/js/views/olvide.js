// Vista: Olvidé Contraseña
export const template = `
<section id="view-olvide" class="view">
  <div class="container" style="max-width:480px; padding:4rem 1rem 3rem;">
    <div class="glass-card">
      <div class="has-text-centered mb-4">
        <img src="/assets/avion.png" alt="Avión TouristChain" style="height:100px; filter:drop-shadow(0 10px 18px rgba(0,0,0,.25));"/>
      </div>
      <h2 class="title is-4 has-text-centered mb-5">Recuperar contraseña</h2>
      <form id="forgotForm" class="space-y-4">
        <div class="field">
          <label class="label">Correo asociado a tu cuenta</label>
          <div class="control"><input type="email" class="input" placeholder="ejemplo@gmail.com" required /></div>
        </div>
        <div class="field"><button class="button is-cta is-fullwidth">Enviar enlace de recuperación</button></div>
      </form>
      <p class="has-text-centered mt-4"><a href="#/login" class="is-underlined is-size-6">Volver a iniciar sesión</a></p>
    </div>
  </div>
</section>
`;

export function initOlvide() {
    console.log('Inicializando olvide...');
    const form = document.getElementById('forgotForm');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Se ha enviado un enlace de recuperación a tu correo.');
            location.hash = '#/login';
        });
    }
}
