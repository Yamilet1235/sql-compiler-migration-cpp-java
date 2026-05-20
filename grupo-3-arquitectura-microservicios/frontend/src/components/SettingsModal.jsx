export default function SettingsModal({ mostrar, alCerrar, temaEditor, alCambiarTema, modoEstricto, alCambiarEstricto, temaActual, colorBoton }) {
  if (!mostrar) return null;

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(5, 7, 15, 0.55)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999 }}>
      <div style={{ backgroundColor: temaActual.bgBloques, border: `1px solid ${temaActual.borde}`, borderRadius: '12px', padding: '25px', width: '380px', color: temaActual.textoPrincipal }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
          <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>⚙️ CONFIGURACIÓN</h3>
          <button onClick={alCerrar} style={{ background: 'none', border: 'none', color: temaActual.textoSecundario, cursor: 'pointer', fontSize: '16px' }}>✕</button>
        </div>
        <hr style={{ border: '0', borderTop: `1px solid ${temaActual.borde}`, marginBottom: '20px' }} />
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', fontSize: '13px', color: temaActual.textoSecundario, marginBottom: '8px' }}>Tema de Color del Editor</label>
          <select value={temaEditor} onChange={(e) => alCambiarTema(e.target.value)} style={{ width: '100%', backgroundColor: temaActual.bgPrincipal, color: temaActual.textoPrincipal, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', padding: '8px 10px', fontSize: '13px' }}>
            <option value="Oscuro Cyberpunk">Oscuro Cyberpunk</option>
            <option value="Claro Minimalista">Claro Minimalista</option>
            <option value="Rosa Coquette">Rosa Coquette 🎀</option>
          </select>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '25px', backgroundColor: temaActual.bgPrincipal, padding: '12px', borderRadius: '6px', border: `1px solid ${temaActual.borde}` }}>
          <div>
            <p style={{ margin: '0 0 3px 0', fontSize: '13px', fontWeight: 'bold' }}>Modo de Análisis Estricto</p>
            <p style={{ margin: 0, fontSize: '11px', color: temaActual.textoSecundario }}>Fuerza punto y coma (;) y valida mayúsculas.</p>
          </div>
          <input type="checkbox" checked={modoEstricto} onChange={(e) => alCambiarEstricto(e.target.checked)} style={{ width: '18px', height: '18px', cursor: 'pointer' }} />
        </div>
        <button onClick={alCerrar} style={{ width: '100%', padding: '10px', backgroundColor: colorBoton, color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>Aplicar Ajustes</button>
      </div>
    </div>
  );
}