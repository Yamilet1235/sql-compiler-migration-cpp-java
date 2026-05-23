export default function SchemaModal({ mostrar, alCerrar, temaActual, colorBoton, nombreArchivo, alSubirArchivo, schemaPegado, alCambiarSchema, alProcesar }) {
  if (!mostrar) return null;

  // CONTENEDOR GENERAL
  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(5, 7, 15, 0.65)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999 }}>
      <div style={{ backgroundColor: temaActual.bgPanelDerecho, border: `1px solid ${temaActual.borde}`, borderRadius: '12px', padding: '25px', width: '500px', color: temaActual.textoPrincipal, boxSizing: 'border-box', boxShadow: '0 10px 25px rgba(0,0,0,0.3)' }}>
        
        {/* ENCABEZADO Y DESCRIPCIÓN */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
          <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '8px' }}>
            📁 CONFIGURAR ESQUEMA
          </h3>
          <button onClick={alCerrar} style={{ background: 'none', border: 'none', color: temaActual.textoSecundario, cursor: 'pointer', fontSize: '16px' }}>✕</button>
        </div>
        
        <p style={{ margin: '0 0 15px 0', fontSize: '12px', color: temaActual.textoSecundario }}>Elige el método para alimentar la tabla de símbolos.</p>
        
        {/* CARGA POR ARCHIVO ESTRUCTURADO (.SQL) */}
        <div style={{ backgroundColor: temaActual.bgCardModal, padding: '15px', borderRadius: '8px', border: `1px solid ${temaActual.borde}`, marginBottom: '15px' }}>
          <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '10px' }}>Opción 1: Archivo estructurado</label>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <label style={{ padding: '8px 14px', backgroundColor: colorBoton, color: '#ffffff', borderRadius: '6px', fontSize: '12px', fontWeight: 'bold', cursor: 'pointer', display: 'inline-block', border: 'none', boxShadow: '0 2px 4px rgba(0,0,0,0.15)' }}>
              Seleccionar archivo
              <input type="file" accept=".sql" onChange={alSubirArchivo} style={{ display: 'none' }} />
            </label>
            <span style={{ fontSize: '12px', color: temaActual.textoSecundario, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '240px' }}>
              {nombreArchivo}
            </span>
          </div>
        </div>

        {/* EDITOR INTEGRADO (COPIAR Y PEGAR CÓDIGO) */}
        <div style={{ backgroundColor: temaActual.bgCardModal, padding: '15px', borderRadius: '8px', border: `1px solid ${temaActual.borde}`, marginBottom: '20px' }}>
          <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '8px' }}>Opción 2: Copiar y pegar código SQL</label>
          <textarea rows="5" value={schemaPegado} onChange={(e) => alCambiarSchema(e.target.value)} placeholder="CREATE TABLE Clientes..." style={{ width: '100%', backgroundColor: temaActual.bgBloques, color: temaActual.textoPrincipal, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', padding: '10px', fontSize: '12px', fontFamily: 'monospace', outline: 'none', resize: 'vertical', boxSizing: 'border-box' }} />
          
          <button 
            onClick={alProcesar} 
            style={{ 
              width: '100%', 
              marginTop: '12px', 
              padding: '10px', 
              backgroundColor: colorBoton, 
              color: 'white', 
              border: 'none', 
              borderRadius: '6px', 
              fontWeight: 'bold', 
              cursor: 'pointer', 
              fontSize: '13px', 
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)' 
            }}
          >
            💾 Procesar Esquema
          </button>
        </div>

        {/* BOTÓN CANCELAR/CERRAR */}
        <button 
          onClick={alCerrar} 
          style={{ width: '100%', padding: '10px', backgroundColor: temaActual.bgBotonSecundario, color: temaActual.textoPrincipal, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer', fontSize: '13px', boxSizing: 'border-box' }}
        >
          Cancelar
        </button>
      </div>
    </div>
  );
}