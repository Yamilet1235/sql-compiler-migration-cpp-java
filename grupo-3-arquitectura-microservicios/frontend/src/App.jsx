import { useState } from 'react';
import SqlEditor from './components/SqlEditor';

function App() {
  // Estado inicial con un query de ejemplo
  const [sqlCode, setSqlCode] = useState("SELECT e.employee_id, e.first_name, e.last_name, d.department_name\nFROM employees e\nINNER JOIN departments d ON e.department_id = d.department_id\nWHERE e.salary > 50000;");
  const [resultado, setResultado] = useState(null);
  const [dialecto, setDialecto] = useState("MySQL");

  // Función para limpiar todo al cambiar de Base de Datos
  const manejarCambioDialecto = (nuevoDb) => {
    setDialecto(nuevoDb);      // Cambia el dialecto seleccionado
    setSqlCode("");            // Limpia el editor de texto
    setResultado(null);        // Borra los mensajes de error/éxito previos
  };

  const compilarSql = async () => {
    try {
      setResultado({ mensaje: "Validando..." });
      
      // La ruta correcta según el controlador del backend
      const response = await fetch('http://localhost:8082/api/v1/validate/query', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          query: sqlCode, 
          dialect: dialecto 
        }),
      });

      if (!response.ok) {
        throw new Error(`Error en el servidor: ${response.status}`);
      }

      const data = await response.json();
      setResultado(data); // Aquí recibimos el valid: true/false, errores y tokens
    } catch (err) {
      console.error("Detalle del error:", err); // Corrección para que VS Code no marque error
      setResultado({ 
        error: "El backend no responde. Asegúrate de que Spring Boot esté corriendo en el puerto 8082." 
      });
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', backgroundColor: '#0b0f19', color: '#f8fafc', fontFamily: 'sans-serif' }}>
      
      {/* PANEL IZQUIERDO: Selector de Dialectos */}
      <div style={{ width: '18%', padding: '20px', borderRight: '1px solid #1e293b', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '25px' }}>
            <span style={{ fontSize: '20px' }}>⚙️</span>
            <h2 style={{ fontSize: '18px', margin: 0, fontWeight: 'bold' }}>SQL Syntax Master</h2>
          </div>
          
          <p style={{ fontSize: '11px', color: '#64748b', letterSpacing: '1px', marginBottom: '15px' }}>DIALECT SELECTOR</p>
          
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {['MySQL', 'PostgreSQL', 'MongoDB', 'PL/SQL', 'MariaDB'].map((db) => (
              <li 
                key={db} 
                onClick={() => manejarCambioDialecto(db)} // Llama a la función de limpieza
                style={{
                  padding: '12px 15px', margin: '6px 0', borderRadius: '6px', cursor: 'pointer', fontSize: '14px',
                  backgroundColor: dialecto === db ? '#2563eb' : 'transparent',
                  color: dialecto === db ? '#ffffff' : '#94a3b8',
                  transition: '0.2s'
                }}
              >
                {db}
              </li>
            ))}
          </ul>
        </div>
        
        {/* BOTÓN SETTINGS (Ahora funcional) */}
        <button 
          onClick={() => alert("Configuración: Aquí puedes ajustar el tema y las preferencias de la API.")}
          style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', textAlign: 'left', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          ⚙️ Settings
        </button>
      </div>

      {/* PANEL CENTRAL: Editor y Consola */}
      <div style={{ width: '52%', padding: '20px', display: 'flex', flexDirection: 'column' }}>
        <div style={{ marginBottom: '12px', fontSize: '13px', color: '#94a3b8', display: 'flex', justifyContent: 'space-between' }}>
          <span>&lt;&gt; query.sql</span>
          {resultado?.valid === false && <span style={{ color: '#ef4444', fontWeight: 'bold' }}>⚠️ Error de Sintaxis</span>}
          {resultado?.valid === true && <span style={{ color: '#10b981', fontWeight: 'bold' }}>✅ SQL Válido</span>}
        </div>
        
        {/* Editor de Código (Recibe el estado sqlCode para poder limpiarse) */}
        <SqlEditor code={sqlCode} onCodeChange={(codigo) => setSqlCode(codigo)} />
        
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '15px', gap: '10px' }}>
          {/* BOTÓN CHAT IA */}
          <button
            onClick={() => alert("Asistente IA: 'Analizando tu consulta para sugerir optimizaciones...'")}
            style={{ padding: '10px 20px', cursor: 'pointer', backgroundColor: '#1e293b', color: 'white', border: '1px solid #334155', borderRadius: '6px', fontWeight: 'bold' }}
          >
            💬 Consultar IA
          </button>
          
          <button
            onClick={compilarSql}
            style={{ padding: '10px 25px', cursor: 'pointer', backgroundColor: '#4f46e5', color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold' }}
          >
            ▶ Validar Código
          </button>
        </div>

        {/* CONSOLA DE RESULTADOS MEJORADA */}
        <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#05070f', borderRadius: '6px', flexGrow: 1, border: '1px solid #1e293b', overflowY: 'auto' }}>
          {!resultado ? (
            <p style={{ color: '#64748b' }}>{`> Esperando validación para ${dialecto}...`}</p>
          ) : (
            <div>
              {/* SECCIÓN DE ERRORES (Si existen) */}
              {resultado.errors?.length > 0 && (
                <div style={{ marginBottom: '15px', padding: '10px', backgroundColor: '#450a0a', borderLeft: '4px solid #ef4444', borderRadius: '4px' }}>
                  <h4 style={{ margin: '0 0 5px 0', color: '#fca5a5', fontSize: '14px' }}>Errores encontrados:</h4>
                  {resultado.errors.map((err, i) => <p key={i} style={{ margin: 0, fontSize: '13px', color: '#fecaca' }}>• {err}</p>)}
                </div>
              )}

              {/* SECCIÓN DE ÉXITO */}
              {resultado.valid && (
                <div style={{ marginBottom: '15px', padding: '10px', backgroundColor: '#064e3b', borderLeft: '4px solid #10b981', borderRadius: '4px' }}>
                  <p style={{ margin: 0, color: '#a7f3d0' }}>🎉 ¡Éxito! La consulta es válida en {dialecto}.</p>
                </div>
              )}

              {/* VISUALIZADOR DE TOKENS LÉXICOS (Del Backend) */}
              {resultado.tokens && (
                <div style={{ marginTop: '10px' }}>
                  <h4 style={{ fontSize: '12px', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '10px' }}>Análisis Léxico (Tokens):</h4>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '5px' }}>
                    {resultado.tokens.map((t, i) => (
                      <span key={i} style={{ fontSize: '11px', padding: '2px 8px', backgroundColor: '#1e293b', borderRadius: '4px', color: '#cbd5e1', border: '1px solid #334155' }}>
                        {t.type}: <b style={{ color: '#60a5fa' }}>{t.value}</b>
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* PANEL DERECHO: Visualización AST */}
      <div style={{ width: '30%', padding: '20px', borderLeft: '1px solid #1e293b', backgroundColor: '#090d16', display: 'flex', flexDirection: 'column' }}>
        <h3 style={{ fontSize: '14px', color: '#94a3b8', marginTop: 0, marginBottom: '20px', letterSpacing: '0.5px' }}>VISUALIZACIÓN AST</h3>
        <div style={{ flexGrow: 1, border: '1px dashed #334155', borderRadius: '8px', display: 'flex', flexDirection: 'column', padding: '15px', overflowY: 'auto' }}>
          {!resultado?.ast ? (
            <div style={{ height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#475569', textAlign: 'center' }}>
              <span style={{ fontSize: '40px', marginBottom: '10px' }}>📊</span>
              <p style={{ fontSize: '13px' }}>El árbol sintáctico se generará tras la validación.</p>
            </div>
          ) : (
            <pre style={{ fontSize: '11px', color: '#94a3b8', margin: 0, whiteSpace: 'pre-wrap' }}>
              {resultado.ast}
            </pre>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;