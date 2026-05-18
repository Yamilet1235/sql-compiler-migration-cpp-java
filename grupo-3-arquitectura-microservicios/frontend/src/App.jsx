import { useState, useEffect } from 'react';
import SqlEditor from './components/SqlEditor';

function App() {
  const [sqlCode, setSqlCode] = useState("SELECT e.employee_id, e.first_name, e.last_name, d.department_name\nFROM employees e\nINNER JOIN departments d ON e.department_id = d.department_id\nWHERE e.salary > 50000;");
  const [resultado, setResultado] = useState(null);
  const [dialecto, setDialecto] = useState("MySQL");

  // Estados para la IA
  const [nivelIA, setNivelIA] = useState("Principiante");
  const [comentarioIA, setComentarioIA] = useState("");
  const [historialChat, setHistorialChat] = useState([
    { rol: 'ia', texto: '¡Hola! Soy tu asistente de optimización y análisis SQL. ¿En qué puedo ayudarte hoy?' }
  ]);

  // Estados para Settings
  const [mostrarSettings, setMostrarSettings] = useState(false);
  const [temaEditor, setTemaEditor] = useState("Oscuro Cyberpunk");
  const [modoEstricto, setModoEstricto] = useState(false);

  useEffect(() => {
    document.body.style.margin = "0";
    document.body.style.padding = "0";
    document.body.style.backgroundColor = temaEditor === "Oscuro Cyberpunk" ? "#0b0f19" : "#f1f5f9"; 
    document.body.style.overflow = "hidden"; 
  }, [temaEditor]);

  const manejarCambioDialecto = (nuevoDb) => {
    setDialecto(nuevoDb);      
    setSqlCode("");            
    setResultado(null);        
  };

  const handleSubirBD = () => {
    alert("Función para subir esquema o archivo de Base de Datos (.sql) próximamente disponible.");
  };

  // 🌐 FUNCIÓN CORREGIDA: Ahora consulta a tu propio Backend proxy en Spring Boot
  const enviarAIA = async (e) => {
    e.preventDefault();
    if (!comentarioIA.trim()) return;

    const mensajeUsuarioOriginal = comentarioIA;
    const nuevoHistorial = [...historialChat, { rol: 'usuario', texto: mensajeUsuarioOriginal }];
    setHistorialChat(nuevoHistorial);
    setComentarioIA("");

    setHistorialChat(prev => [...prev, { rol: 'ia', texto: "🤖 Pensando..." }]);

    try {
      const contextoSistema = `Eres un asistente experto en Bases de Datos y Teoría de Compiladores.
Tu objetivo es ayudar al usuario a entender, corregir u optimizar su código.

CONTEXTO DE LA APLICACIÓN ACTUAL:
- Dialecto seleccionado por el usuario: ${dialecto}
- Nivel de explicación solicitado: ${nivelIA}.
- Código SQL actualmente escrito en el editor:
"""
${sqlCode}
"""
- Estado del Analizador del Backend: ${resultado ? JSON.stringify(resultado) : "El usuario aún no ha ejecutado la validación."}`;

      const response = await fetch('http://localhost:8082/api/v1/ai/chat', {
        method: "POST",
        headers: { 
          "Content-Type": "application/json" 
        },
        body: JSON.stringify({
          prompt: `${contextoSistema}\n\nPregunta del usuario: ${mensajeUsuarioOriginal}`
        })
      });

      if (!response.ok) {
        throw new Error(`Error en el servidor backend: ${response.status}`);
      }

      const data = await response.json();
      
      // ✨ Validación robusta: Si 'data' es un string directo, lo usa. Si es objeto, busca '.respuesta' o '.output'
      const respuestaIA = typeof data === 'string' 
        ? data 
        : (data.respuesta || data.output || JSON.stringify(data));

      setHistorialChat(prev => {
        const clon = [...prev];
        clon[clon.length - 1] = { rol: 'ia', texto: respuestaIA };
        return clon;
      });

    } catch (err) {
      console.error("Error al consultar el backend de IA:", err);
      setHistorialChat(prev => {
        const clon = [...prev];
        clon[clon.length - 1] = {
          rol: 'ia',
          texto: "❌ Error de conexión con el Backend de IA. Asegúrate de compilar los cambios en Spring Boot y revisar la consola de Java."
        };
        return clon;
      });
    }
  };

  const compilarSql = async () => {
    try {
      setResultado({ mensaje: "Validando..." });
      const response = await fetch('http://localhost:8082/api/v1/validate/query', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          query: sqlCode, 
          dialect: dialecto,
          strictMode: modoEstricto 
        }),
      });

      if (!response.ok) throw new Error(`Error en el servidor: ${response.status}`);
      const data = await response.json();
      setResultado(data); 
    } catch (err) {
      console.error(err); 
      setResultado({ error: "El backend no responde. Asegúrate de que Spring Boot esté corriendo." });
    }
  };

  const obtenerTokensAgrupados = () => {
    if (!resultado?.tokens) return {};
    const listaKeywords = ['SELECT', 'FROM', 'WHERE', 'INNER', 'JOIN', 'ON', 'AND', 'OR', 'ORDER', 'BY', 'GROUP', 'HAVING'];
    const listaSimbolos = ['COMMA', 'SEMICOLON', 'DOT', 'GT', 'LT', 'EQUALS', 'PLUS', 'MINUS', 'ASTERISK', 'SYMBOL'];

    return resultado.tokens.reduce((acumulador, token) => {
      let categoriaDestino = token.type;
      if (listaKeywords.includes(token.type.toUpperCase())) categoriaDestino = 'KEYWORDS';
      else if (listaSimbolos.includes(token.type.toUpperCase())) categoriaDestino = 'SYMBOLS';

      if (!acumulador[categoriaDestino]) acumulador[categoriaDestino] = [];
      if (!acumulador[categoriaDestino].includes(token.value)) acumulador[categoriaDestino].push(token.value);
      return acumulador;
    }, {});
  };

  const tokensAgrupados = obtenerTokensAgrupados();
  const esOscuro = temaEditor === "Oscuro Cyberpunk";

  return (
    <div style={{ display: 'flex', height: '100vh', width: '100vw', backgroundColor: esOscuro ? '#0b0f19' : '#f1f5f9', color: esOscuro ? '#f8fafc' : '#0f172a', fontFamily: 'sans-serif', boxSizing: 'border-box', overflow: 'hidden', transition: '0.3s' }}>
      
      {/* PANEL IZQUIERDO */}
      <div style={{ width: '18%', padding: '20px', borderRight: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, display: 'flex', flexDirection: 'column', justifyContent: 'space-between', boxSizing: 'border-box' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
            <span style={{ fontSize: '20px' }}>⚙️</span>
            <h2 style={{ fontSize: '18px', margin: 0, fontWeight: 'bold' }}>SQL Syntax Master</h2>
          </div>

          <button onClick={handleSubirBD} style={{ width: '100%', padding: '10px', marginBottom: '25px', backgroundColor: esOscuro ? '#1e293b' : '#e2e8f0', color: esOscuro ? '#f8fafc' : '#0f172a', border: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
            📁 Subir Base de Datos
          </button>
          
          <p style={{ fontSize: '11px', color: '#64748b', letterSpacing: '1px', marginBottom: '15px' }}>DIALECT SELECTOR</p>
          
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {['MySQL', 'PostgreSQL', 'MongoDB', 'SQLServer', 'MariaDB'].map((db) => (
              <li key={db} onClick={() => manejarCambioDialecto(db)} style={{ padding: '12px 15px', margin: '6px 0', borderRadius: '6px', cursor: 'pointer', fontSize: '14px', backgroundColor: dialecto === db ? '#2563eb' : 'transparent', color: dialecto === db ? '#ffffff' : '#94a3b8', transition: '0.2s' }}>
                {db}
              </li>
            ))}
          </ul>
        </div>
        
        <button onClick={() => setMostrarSettings(true)} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', textAlign: 'left', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          ⚙️ Settings
        </button>
      </div>

      {/* PANEL CENTRAL */}
      <div style={{ width: '52%', padding: '20px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', height: '100%' }}>
        <div style={{ height: '60%', display: 'flex', flexDirection: 'column' }}>
          <div style={{ marginBottom: '12px', fontSize: '13px', color: '#94a3b8', display: 'flex', justifyContent: 'space-between' }}>
            <span>&lt;&gt; query.sql {modoEstricto && <span style={{ color: '#f59e0b', fontSize: '11px', marginLeft: '6px' }}>(Modo Estricto)</span>}</span>
            {resultado?.valid === false && <span style={{ color: '#ef4444', fontWeight: 'bold' }}>⚠️ Error de Sintaxis</span>}
            {resultado?.valid === true && <span style={{ color: '#10b981', fontWeight: 'bold' }}>✅ SQL Válido</span>}
          </div>
          
          <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
            <SqlEditor code={sqlCode} onCodeChange={(codigo) => setSqlCode(codigo)} theme={temaEditor} />
          </div>
          
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '10px' }}>
            <button onClick={compilarSql} style={{ padding: '8px 20px', cursor: 'pointer', backgroundColor: '#4f46e5', color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', fontSize: '13px' }}>
              ▶ Validar Código
            </button>
          </div>
        </div>

        <div style={{ height: '37%', marginTop: '3%', padding: '15px', backgroundColor: esOscuro ? '#05070f' : '#ffffff', borderRadius: '6px', border: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, overflowY: 'auto', boxSizing: 'border-box' }}>
          {!resultado ? (
            <p style={{ color: '#64748b', margin: 0, fontSize: '13px' }}>{`> Esperando validación para ${dialecto}...`}</p>
          ) : (
            <div>
              {resultado.error && <p style={{ color: '#ef4444', margin: 0, fontSize: '13px' }}>{resultado.error}</p>}
              {resultado.errors?.length > 0 && (
                <div style={{ marginBottom: '12px', padding: '10px', backgroundColor: '#450a0a', borderLeft: '4px solid #ef4444', borderRadius: '4px' }}>
                  <h4 style={{ margin: '0 0 5px 0', color: '#fca5a5', fontSize: '13px' }}>Errores encontrados:</h4>
                  {resultado.errors.map((err, i) => <p key={i} style={{ margin: 0, fontSize: '12px', color: '#fecaca' }}>• {err}</p>)}
                </div>
              )}
              {resultado.valid && (
                <div style={{ marginBottom: '12px', padding: '10px', backgroundColor: '#064e3b', borderLeft: '4px solid #10b981', borderRadius: '4px' }}>
                  <p style={{ margin: 0, color: '#a7f3d0', fontSize: '13px' }}>🎉 ¡Éxito! La consulta es válida en {dialecto}.</p>
                </div>
              )}

              {resultado.tokens && (
                <div>
                  <h4 style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '10px' }}>🔍 Resumen del Análisis Léxico:</h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {Object.keys(tokensAgrupados).map((tipoToken) => (
                      <div key={tipoToken} style={{ display: 'flex', alignItems: 'center', backgroundColor: esOscuro ? '#090d16' : '#f8fafc', border: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, borderRadius: '6px', padding: '8px 12px', gap: '15px' }}>
                        <div style={{ width: '110px', flexShrink: 0 }}>
                          <span style={{ padding: '2px 6px', borderRadius: '4px', backgroundColor: tipoToken === 'KEYWORDS' ? '#1e1b4b' : tipoToken === 'SYMBOLS' ? '#1c1917' : '#1e293b', color: tipoToken === 'KEYWORDS' ? '#a5b4fc' : tipoToken === 'SYMBOLS' ? '#d6d3d1' : '#94a3b8', fontSize: '10px', fontWeight: 'bold', display: 'inline-block', textAlign: 'center', minWidth: '85px' }}>{tipoToken}</span>
                        </div>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                          {tokensAgrupados[tipoToken].map((valor, idx) => (
                            <span key={idx} style={{ fontFamily: 'monospace', fontSize: '11px', color: tipoToken === 'UNKNOWN' ? '#ef4444' : tipoToken === 'KEYWORDS' ? '#818cf8' : '#60a5fa', backgroundColor: esOscuro ? '#05070f' : '#ffffff', border: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, padding: '1px 6px', borderRadius: '4px' }}>{valor}</span>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* PANEL DERECHO */}
      <div style={{ width: '30%', padding: '20px', borderLeft: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, backgroundColor: esOscuro ? '#090d16' : '#ffffff', display: 'flex', flexDirection: 'column', gap: '15px', boxSizing: 'border-box', height: '100%' }}>
        <div style={{ height: '35%', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ fontSize: '14px', color: '#94a3b8', marginTop: 0, marginBottom: '10px' }}>VISUALIZACIÓN AST</h3>
          <div style={{ flexGrow: 1, border: `1px dashed ${esOscuro ? '#334155' : '#cbd5e1'}`, borderRadius: '8px', display: 'flex', padding: '15px', overflowY: 'auto' }}>
            {!resultado?.ast ? (
              <div style={{ height: '100%', width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#475569', textAlign: 'center' }}>
                <span style={{ fontSize: '40px', marginBottom: '10px' }}>📊</span>
                <p style={{ fontSize: '13px' }}>El árbol sintáctico se generará tras la validación.</p>
              </div>
            ) : (
              <pre style={{ fontSize: '11px', color: '#94a3b8', margin: 0, whiteSpace: 'pre-wrap' }}>{resultado.ast}</pre>
            )}
          </div>
        </div>

        <div style={{ height: '65%', borderTop: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, paddingTop: '15px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <h3 style={{ fontSize: '13px', color: '#94a3b8', margin: 0 }}>💬 ASISTENTE DE IA REAL</h3>
            <select value={nivelIA} onChange={(e) => setNivelIA(e.target.value)} style={{ backgroundColor: '#1e293b', color: '#ffffff', border: '1px solid #334155', borderRadius: '4px', padding: '4px 8px', fontSize: '12px' }}>
              <option value="Principiante">Principiante</option>
              <option value="Intermedio">Intermedio</option>
              <option value="Avanzado">Avanzado</option>
              <option value="Pro">Pro</option>
            </select>
          </div>

          <div style={{ flexGrow: 1, backgroundColor: esOscuro ? '#05070f' : '#f8fafc', border: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, borderRadius: '6px', padding: '10px', overflowY: 'auto', marginBottom: '10px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {historialChat.map((msg, index) => (
              <div key={index} style={{ alignSelf: msg.rol === 'usuario' ? 'flex-end' : 'flex-start', backgroundColor: msg.rol === 'usuario' ? '#2563eb' : esOscuro ? '#1e293b' : '#e2e8f0', color: msg.rol === 'usuario' || esOscuro ? '#ffffff' : '#0f172a', padding: '8px 12px', borderRadius: '8px', maxWidth: '85%', fontSize: '12px', whiteSpace: 'pre-wrap' }}>
                {msg.texto}
              </div>
            ))}
          </div>

          <form onSubmit={enviarAIA} style={{ display: 'flex', gap: '6px' }}>
            <input type="text" value={comentarioIA} onChange={(e) => setComentarioIA(e.target.value)} placeholder={`Pregunta a Gemini en modo ${nivelIA}...`} style={{ flexGrow: 1, backgroundColor: esOscuro ? '#05070f' : '#ffffff', border: `1px solid ${esOscuro ? '#1e293b' : '#cbd5e1'}`, borderRadius: '6px', padding: '8px 12px', color: esOscuro ? 'white' : 'black', fontSize: '12px', outline: 'none' }} />
            <button type="submit" style={{ backgroundColor: '#4f46e5', color: 'white', border: 'none', borderRadius: '6px', padding: '0 15px', cursor: 'pointer', fontWeight: 'bold', fontSize: '12px' }}>Enviar</button>
          </form>
        </div>
      </div>

      {/* MODAL DE SETTINGS */}
      {mostrarSettings && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(5, 7, 15, 0.85)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999 }}>
          <div style={{ backgroundColor: '#090d16', border: '1px solid #1e293b', borderRadius: '12px', padding: '25px', width: '380px', color: '#f8fafc' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
              <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>⚙️ CONFIGURACIÓN</h3>
              <button onClick={() => setMostrarSettings(false)} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '16px' }}>✕</button>
            </div>
            <hr style={{ border: '0', borderTop: '1px solid #1e293b', marginBottom: '20px' }} />
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', fontSize: '13px', color: '#94a3b8', marginBottom: '8px' }}>Tema de Color del Editor</label>
              <select value={temaEditor} onChange={(e) => setTemaEditor(e.target.value)} style={{ width: '100%', backgroundColor: '#05070f', color: '#ffffff', border: '1px solid #1e293b', borderRadius: '6px', padding: '8px 10px', fontSize: '13px' }}>
                <option value="Oscuro Cyberpunk">Oscuro Cyberpunk</option>
                <option value="Claro Minimalista">Claro Minimalista</option>
              </select>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '25px', backgroundColor: '#05070f', padding: '12px', borderRadius: '6px', border: '1px solid #1e293b' }}>
              <div>
                <p style={{ margin: '0 0 3px 0', fontSize: '13px', fontWeight: 'bold' }}>Modo de Análisis Estricto</p>
                <p style={{ margin: 0, fontSize: '11px', color: '#64748b' }}>Fuerza punto y coma (;) y valida mayúsculas.</p>
              </div>
              <input type="checkbox" checked={modoEstricto} onChange={(e) => setModoEstricto(e.target.checked)} style={{ width: '18px', height: '18px', cursor: 'pointer' }} />
            </div>
            <button onClick={() => setMostrarSettings(false)} style={{ width: '100%', padding: '10px', backgroundColor: '#4f46e5', color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>Aplicar Ajustes</button>
          </div>
        </div>
      )}

    </div>
  );
}

export default App;