import React, { useState } from 'react';
import SqlEditor from './components/SqlEditor';
import './index.css';

function App() {
  const [sqlCode, setSqlCode] = useState('');
  const [dialect, setDialect] = useState('mysql');
  const [resultado, setResultado] = useState(null);

  const compilarSql = async () => {
    try {
      const response = await fetch('http://localhost:8082/api/v1/validate/query', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ query: sqlCode, dialect }),
      });

      const data = await response.json();
      setResultado(data);
    } catch (error) {
      console.error("Error al conectar con el backend:", error);
      setResultado({ error: "El backend no está respondiendo. ¿Está encendido Spring Boot?" });
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h1 style={{ textAlign: 'center', color: '#333' }}>SQL Compiler</h1>

      <div style={{ marginBottom: '10px' }}>
        <label htmlFor="dialect-select" style={{ marginRight: '10px' }}>Dialecto:</label>
        <select id="dialect-select" value={dialect} onChange={(e) => setDialect(e.target.value)}
          style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ccc' }}>
          <option value="mysql">MySQL</option>
          <option value="postgresql">PostgreSQL</option>
          <option value="sqlserver">SQL Server</option>
          <option value="mongodb">MongoDB</option>
        </select>
      </div>

      <SqlEditor onCodeChange={(codigo) => setSqlCode(codigo)} />

      <button onClick={compilarSql}
        style={{ marginTop: '15px', padding: '10px 20px', cursor: 'pointer', backgroundColor: '#007bff', color: '#fff', border: 'none', borderRadius: '4px' }}>
        Compilar / Validar SQL
      </button>

      <div style={{ marginTop: '20px', padding: '15px', background: '#f4f4f4', borderRadius: '5px' }}>
        <h3>Respuesta del Compilador:</h3>
        <pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(resultado, null, 2)}</pre>
      </div>
    </div>
  );
}

export default App;
