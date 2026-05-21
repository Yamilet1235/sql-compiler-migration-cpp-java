import { useState, useEffect } from 'react';
import SqlEditor from './components/SqlEditor';
import SettingsModal from './components/SettingsModal';
import SchemaModal from './components/SchemaModal';
import { TEMAS } from './constants/temas';
import Tree from 'react-d3-tree';

function App() {
  const [sqlCode, setSqlCode] = useState("SELECT e.employee_id, e.first_name, e.last_name, d.department_name\nFROM employees e\nINNER JOIN departments d ON e.department_id = d.department_id\nWHERE e.salary > 50000;");
  const [resultado, setResultado] = useState(null);
  const [dialecto, setDialecto] = useState("MySQL");
  const [mostrarModalSchema, setMostrarModalSchema] = useState(false);
  const [schemaPegado, setSchemaPegado] = useState("")
  const [nombreArchivo, setNombreArchivo] = useState("Sin archivos seleccionados");
  const [nivelIA, setNivelIA] = useState("Principiante");
  const [comentarioIA, setComentarioIA] = useState("");
  const [historialChat, setHistorialChat] = useState([
    { rol: 'ia', texto: '¡Hola! Soy tu asistente de optimización y análisis SQL. ¿En qué puedo ayudarte hoy?' }
  ]);

  const [mostrarSettings, setMostrarSettings] = useState(false);
  const [temaEditor, setTemaEditor] = useState("Oscuro Cyberpunk");
  const [modoEstricto, setModoEstricto] = useState(false);

  const temaActual = TEMAS[temaEditor] || TEMAS["Oscuro Cyberpunk"];
  const colorBotonValidar = temaEditor === "Rosa Coquette" ? "#ff85a2" : "#2563eb";

  useEffect(() => {
    document.body.style.margin = "0";
    document.body.style.padding = "0";
    document.body.style.backgroundColor = temaActual.bgPrincipal; 
    document.body.style.overflow = "hidden"; 
  }, [temaEditor, temaActual]);

  const manejarCambioDialecto = (nuevoDb) => {
    setDialecto(nuevoDb);      
    setSqlCode("");            
    setResultado(null);        
  };

  const enviarEsquemaAlBackend = async (textoSql) => {
    if (!textoSql.trim()) {
      alert("⚠️ El contenido del esquema está vacío.");
      return;
    }
    try {
      const response = await fetch('http://localhost:8082/api/v1/validate/schema/upload', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: textoSql
      });
      
      if (response.ok) {
        alert("✅ Base de datos cargada en memoria exitosamente. ¡Ya puedes validar tu consulta!");
        setMostrarModalSchema(false);
        setSchemaPegado("");
        setNombreArchivo("Sin archivos seleccionados");
      } else {
        alert("Error al cargar el esquema en el backend.");
      }
    } catch (error) {
      console.error("Error de conexión:", error);
      alert("No se pudo conectar con el servidor Spring Boot.");
    }
  };

  const handleSubirBD = (event) => {
    const file = event.target.files[0];
    if (!file) return;
    setNombreArchivo(file.name);

    const reader = new FileReader();
    reader.onload = async (e) => {
      enviarEsquemaAlBackend(e.target.result);
    };
    reader.readAsText(file);
  };

  const llamarIA = async (mensaje, codigo, error, nivel) => {
    const nuevoHistorial = [...historialChat, { rol: 'usuario', texto: mensaje }];
    setHistorialChat([...nuevoHistorial, { rol: 'ia', texto: "🤖 Pensando..." }]);

    try {
      const response = await fetch('http://localhost:8082/api/v1/ai/chat', {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          mensaje,
          nivel,
          codigo,
          contextoError: error
        })
      });

      if (!response.ok) throw new Error(`Error en el servidor: ${response.status}`);
      const data = await response.json();
      const respuestaIA = data.respuesta || JSON.stringify(data);

      setHistorialChat(prev => {
        const clon = [...prev];
        clon[clon.length - 1] = { rol: 'ia', texto: respuestaIA };
        return clon;
      });
    } catch (error) {
      console.error("Error en la petición de IA:", error);
      setHistorialChat(prev => {
        const clon = [...prev];
        clon[clon.length - 1] = { rol: 'ia', texto: "❌ Error de conexión con el Backend de IA." };
        return clon;
      });
    }
  };

  const enviarAIA = async (e) => {
    e.preventDefault();
    if (!comentarioIA.trim()) return;

    const mensaje = comentarioIA;
    setComentarioIA("");

    const errorContexto = resultado?.error || (resultado?.errors?.length > 0 ? resultado.errors.join("\n") : "");
    await llamarIA(mensaje, sqlCode, errorContexto, nivelIA);
  };

  const compilarSql = async () => {
    try {
      setResultado({ mensaje: "Validando..." });
      const response = await fetch('http://localhost:8082/api/v1/validate/query', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: sqlCode, dialect: dialecto, strictMode: modoEstricto }),
      });
      if (!response.ok) throw new Error(`Error en el servidor: ${response.status}`);
      const data = await response.json();
      setResultado(data);

      if (data.valid === false) {
        const errorStr = data.error || (data.errors?.length > 0 ? data.errors.join("\n") : "Error de sintaxis");
        await llamarIA("Acabo de validar mi código y falló. ¿Me explicas qué pasó?", sqlCode, errorStr, nivelIA);
      }
    } catch (err) {
      console.error("Error al compilar:", err);
      setResultado({ error: "El backend no responde. Asegúrate de que Spring Boot esté corriendo." });
    }
  };

  const limpiarEditor = () => {
    setSqlCode("");
    setResultado(null);
    setHistorialChat([
      { rol: 'ia', texto: "¡Hola! Soy tu asistente de optimización y análisis SQL. ¿En qué puedo ayudarte hoy?" }
    ]);
  };

  const obtenerTokensAgrupados = () => {
    if (!resultado?.tokens) return {};
    const keywords = ['SELECT', 'FROM', 'WHERE', 'INNER', 'JOIN', 'ON', 'AND', 'OR', 'ORDER', 'BY', 'GROUP', 'HAVING'];
    const simbolos = ['COMMA', 'SEMICOLON', 'DOT', 'GT', 'LT', 'EQUALS', 'PLUS', 'MINUS', 'ASTERISK', 'SYMBOL'];

    return resultado.tokens.reduce((acum, token) => {
      let cat = token.type;
      if (keywords.includes(token.type.toUpperCase())) cat = 'KEYWORDS';
      else if (simbolos.includes(token.type.toUpperCase())) cat = 'SYMBOLS';

      if (!acum[cat]) acum[cat] = []; 
      if (!acum[cat].includes(token.value)) acum[cat].push(token.value);
      return acum; 
    }, {});
  };

  const tokensAgrupados = obtenerTokensAgrupados();

  return (
    <div style={{ display: 'flex', height: '100vh', width: '100vw', backgroundColor: temaActual.bgPrincipal, color: temaActual.textoPrincipal, fontFamily: 'sans-serif', boxSizing: 'border-box', overflow: 'hidden', transition: '0.3s' }}>
      
      {/* PANEL IZQUIERDO */}
      <div style={{ width: '18%', padding: '20px', borderRight: `1px solid ${temaActual.borde}`, display: 'flex', flexDirection: 'column', justifyContent: 'space-between', boxSizing: 'border-box' }}>
        <div>
          <div style={{ height: '40px', display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: '15px', marginBottom: '25px' }}>
            <h2 style={{ fontSize: '25px', margin: 0, fontWeight: 'bold', textAlign: 'center' }}>─ SQL Compilador ─</h2>
          </div>
          <button 
            onClick={() => setMostrarModalSchema(true)} 
            style={{ width: '100%', padding: '10px', marginBottom: '30px', backgroundColor: temaActual.bgBotonSecundario, color: temaActual.textoPrincipal, border: `2px solid ${temaActual.borde}`, borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }} >
            {temaEditor === "Rosa Coquette" ? "𐙚  Upload DataBase" : "Upload DataBase"}
          </button>
          <p style={{ fontSize: '11px', color: temaActual.textoSecundario, letterSpacing: '1px', marginBottom: '15px', textAlign: 'left', paddingLeft: '5px' }}>SELECT DATABASE</p>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {['MySQL', 'PostgreSQL', 'MongoDB', 'SQLServer', 'MariaDB'].map((db) => (
              <li key={db} onClick={() => manejarCambioDialecto(db)} style={{ padding: '12px 15px', margin: '6px 0', borderRadius: '6px', cursor: 'pointer', fontSize: '14px', backgroundColor: dialecto === db ? colorBotonValidar : 'transparent', color: dialecto === db ? '#ffffff' : temaActual.textoSecundario, fontWeight: dialecto === db ? 'bold' : 'normal', textAlign: 'left' }}>
                {db}
              </li>
            ))}
          </ul>
        </div>
        <button onClick={() => setMostrarSettings(true)} style={{ background: 'none', border: 'none', color: temaActual.textoSecundario, cursor: 'pointer', textAlign: 'left', fontSize: '14px' }}>
          ⚙️ Settings
        </button>
      </div>

      {/* PANEL CENTRAL */}
      <div style={{ width: '52%', padding: '20px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', height: '100%' }}>
        <div style={{ height: '60%', display: 'flex', flexDirection: 'column' }}>
          <div style={{ marginBottom: '12px', fontSize: '13px', color: temaActual.textoSecundario, display: 'flex', justifyContent: 'space-between' }}>
            <span>&lt;&gt; query.sql {modoEstricto && <span style={{ color: '#f59e0b', fontSize: '11px' }}>(Modo Estricto)</span>}</span>
            {resultado?.valid === false && <span style={{ color: '#ef4444', fontWeight: 'bold' }}>⚠️ Error de Sintaxis</span>}
            {resultado?.valid === true && <span style={{ color: '#10b981', fontWeight: 'bold' }}>✅ SQL Válido</span>}
          </div>
          <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
            <SqlEditor code={sqlCode} onCodeChange={(codigo) => setSqlCode(codigo)} theme={temaActual.editorTheme} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '10px', gap: '10px' }}>
            <button onClick={limpiarEditor} style={{ padding: '8px 20px', cursor: 'pointer', backgroundColor: temaActual.bgBotonSecundario, color: temaActual.textoPrincipal, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', fontWeight: 'bold', fontSize: '13px' }}>Limpiar</button>
            <button onClick={compilarSql} style={{ padding: '8px 20px', cursor: 'pointer', backgroundColor: colorBotonValidar, color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', fontSize: '13px' }}>
              {temaEditor === "Rosa Coquette" ? "𐙚  Validar Código" : "Validar Código"}
            </button>
          </div>
        </div>

        <div style={{ height: '37%', marginTop: '3%', padding: '15px', backgroundColor: temaActual.bgBloques, borderRadius: '6px', border: `1px solid ${temaActual.borde}`, overflowY: 'auto', boxSizing: 'border-box' }}>
          {!resultado ? (
            <p style={{ color: temaActual.textoSecundario, margin: 0, fontSize: '13px' }}>{`> Esperando validación para ${dialecto}...`}</p>
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
                  <p style={{ margin: 0, color: '#a7f3d0', fontSize: '13px' }}>La consulta es válida en {dialecto}.</p>
                </div>
              )}
              {resultado.tokens && (
                <div>
                  <h4 style={{ fontSize: '11px', color: temaActual.textoSecundario, textTransform: 'uppercase', marginBottom: '10px' }}> Resumen del Análisis Léxico:</h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {Object.keys(tokensAgrupados).map((tipoToken) => (
                      <div key={tipoToken} style={{ display: 'flex', alignItems: 'center', backgroundColor: temaActual.bgPrincipal, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', padding: '8px 12px', gap: '15px' }}>
                        <div style={{ width: '110px', flexShrink: 0 }}>
                          <span style={{ padding: '2px 6px', borderRadius: '4px', backgroundColor: temaActual.bgBotonSecundario, color: temaActual.textoPrincipal, fontSize: '10px', fontWeight: 'bold', display: 'inline-block', minWidth: '85px', textAlign: 'center' }}>{tipoToken}</span>
                        </div>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                          {tokensAgrupados[tipoToken].map((valor, idx) => (
                            <span key={idx} style={{ fontFamily: 'monospace', fontSize: '11px', color: temaActual.textoPrincipal, backgroundColor: temaActual.bgBloques, border: `1px solid ${temaActual.borde}`, padding: '1px 6px', borderRadius: '4px' }}>{valor}</span>
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
      <div style={{ width: '30%', padding: '20px', borderLeft: `1px solid ${temaActual.borde}`, backgroundColor: temaActual.bgPanelDerecho, display: 'flex', flexDirection: 'column', gap: '15px', boxSizing: 'border-box', height: '100%' }}>
        
        {/* VISUALIZACIÓN AST */}
        <div style={{ height: '60%', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ fontSize: '14px', color: temaActual.textoSecundario, marginTop: 0, marginBottom: '10px' }}>VISUALIZACIÓN AST</h3>
          <div style={{ flexGrow: 1, border: `1px dashed ${temaActual.lineaDashed}`, borderRadius: '8px', display: 'flex', backgroundColor: temaActual.bgBloques, overflow: 'hidden', position: 'relative' }}>
            {!resultado?.astData ? (
              <div style={{ height: '100%', width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: temaActual.textoSecundario, textAlign: 'center', padding: '15px' }}>
                <span style={{ fontSize: '40px', marginBottom: '10px' }}>{temaEditor === "Rosa Coquette" ? "🌸" : "📊"}</span>
                <p style={{ fontSize: '13px', margin: 0 }}>El árbol sintáctico se generará tras la validación.</p>
              </div>
            ) : (
              <div style={{ width: '100%', height: '100%', overflow: 'auto' }}>
                <style>{`
                  .rd3t-label__title { fill: ${temaActual.textoPrincipal} !important; font-size: 13px !important; font-weight: bold !important; font-family: monospace !important; }
                  .rd3t-label__attributes { fill: ${temaActual.textoSecundario} !important; }
                  .rd3t-link { stroke: ${temaActual.astNodeColor} !important; stroke-width: 2px !important; }
                `}</style>
                <Tree 
                  data={resultado.astData} 
                  orientation="horizontal"      
                  pathFunc="diagonal"           
                  translate={{ x: 40, y: 120 }}    
                  collapsible={true}             
                  styles={{
                    links: { stroke: temaActual.astNodeColor, strokeWidth: 2 }, 
                    nodes: {
                      node: {
                        circle: { fill: temaActual.astNodeColor, stroke: temaActual.textoPrincipal, strokeWidth: 2, r: 11 }, 
                        name: { fill: temaActual.textoPrincipal, fontSize: '13px', fontFamily: 'monospace', fontWeight: 'bold' }
                      },
                      leaf: {
                        circle: { fill: temaActual.astLeafColor, stroke: temaActual.textoPrincipal, strokeWidth: 2, r: 9 } 
                      }
                    }
                  }}
                />
              </div>
            )}
          </div>
        </div>

       {/* SECCIÓN ASISTENTE IA */}
        <div style={{ height: '40%', borderTop: `1px solid ${temaActual.borde}`, paddingTop: '15px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <h3 style={{ fontSize: '13px', color: temaActual.textoSecundario, margin: 0, fontWeight: 'bold' }}>Asistente IA</h3>
            
            <select 
              value={nivelIA} 
              onChange={(e) => setNivelIA(e.target.value)} 
              style={{ 
                backgroundColor: temaActual.bgBotonSecundario, 
                color: temaActual.textoPrincipal, 
                border: `1px solid ${temaActual.borde}`, 
                borderRadius: '6px', 
                padding: '5px 12px', 
                fontSize: '12px', 
                fontWeight: 'bold', 
                outline: 'none', 
                cursor: 'pointer',
                boxShadow: 'none'
              }}
            >
              <option value="Principiante" style={{ backgroundColor: temaActual.bgPrincipal, color: temaActual.textoPrincipal }}>Principiante</option>
              <option value="Intermedio" style={{ backgroundColor: temaActual.bgPrincipal, color: temaActual.textoPrincipal }}>Intermedio</option>
              <option value="Avanzado" style={{ backgroundColor: temaActual.bgPrincipal, color: temaActual.textoPrincipal }}>Avanzado</option>
            </select>
          </div>
          
          <div style={{ flexGrow: 1, backgroundColor: temaActual.bgBloques, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', padding: '10px', overflowY: 'auto', marginBottom: '10px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {historialChat.map((msg, index) => (
              <div key={index} style={{ alignSelf: msg.rol === 'usuario' ? 'flex-end' : 'flex-start', backgroundColor: msg.rol === 'usuario' ? colorBotonValidar : temaActual.bgPrincipal, color: msg.rol === 'usuario' ? '#ffffff' : temaActual.textoPrincipal, border: msg.rol !== 'usuario' ? `1px solid ${temaActual.borde}` : 'none', padding: '8px 12px', borderRadius: '8px', maxWidth: '85%', fontSize: '12px', whiteSpace: 'pre-wrap' }}>
                {msg.texto}
              </div>
            ))}
          </div>
          <form onSubmit={enviarAIA} style={{ display: 'flex', gap: '6px' }}>
            <input type="text" value={comentarioIA} onChange={(e) => setComentarioIA(e.target.value)} placeholder={`Pregunta a la IA`} style={{ flexGrow: 1, backgroundColor: temaActual.bgBloques, border: `1px solid ${temaActual.borde}`, borderRadius: '6px', padding: '8px 12px', color: temaActual.textoPrincipal, fontSize: '12px', outline: 'none' }} />
            <button type="submit" style={{ backgroundColor: colorBotonValidar, color: 'white', border: 'none', borderRadius: '6px', padding: '0 15px', cursor: 'pointer', fontWeight: 'bold', fontSize: '12px' }}>Enviar</button>
          </form>
        </div>
      </div>

      {/* MODALES MODULARIZADOS */}
      <SettingsModal 
        mostrar={mostrarSettings} 
        alCerrar={() => setMostrarSettings(false)}
        temaEditor={temaEditor}
        alCambiarTema={setTemaEditor}
        modoEstricto={modoEstricto}
        alCambiarEstricto={setModoEstricto}
        temaActual={temaActual}
        colorBoton={colorBotonValidar}
      />

      <SchemaModal 
        mostrar={mostrarModalSchema}
        alCerrar={() => { setMostrarModalSchema(false); setNombreArchivo("Sin archivos seleccionados"); }}
        temaActual={temaActual}
        colorBoton={colorBotonValidar}
        nombreArchivo={nombreArchivo}
        alSubirArchivo={handleSubirBD}
        schemaPegado={schemaPegado}
        alCambiarSchema={setSchemaPegado}
        alProcesar={() => enviarEsquemaAlBackend(schemaPegado)}
      />

    </div>
  );
}

export default App;