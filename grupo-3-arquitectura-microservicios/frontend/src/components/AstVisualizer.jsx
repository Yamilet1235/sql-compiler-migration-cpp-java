import React, { useState } from 'react';
import SqlEditor from './components/SqlEditor';
import AstViewer from './components/AstViewer';
import './index.css';

function App() {
  const [sqlCode, setSqlCode] = useState('');
  const [dialect, setDialect] = useState('mysql');
  const [resultado, setResultado] = useState(null);
  const [schemaLoaded, setSchemaLoaded] = useState(false);
  const [uploading, setUploading] = useState(false);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const text = await file.text();
    setUploading(true);
    try {
      const resp = await fetch('http://localhost:8082/api/v1/validate/schema', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: text,
      });
      if (!resp.ok) throw new Error('Schema upload failed');
      setSchemaLoaded(true);
    } catch (err) {
      console.error(err);
      setSchemaLoaded(false);
    } finally {
      setUploading(false);
    }
  };

  const compilarSql = async () => {
    if (!schemaLoaded) {
      setResultado({ error: 'Por favor sube primero el archivo de esquema (.sql)' });
      return;
    }
    try {
      const response = await fetch('http://localhost:8082/api/v1/validate/query', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: sqlCode, dialect }),
      });
      const data = await response.json();
      setResultado(data);
    } catch (error) {
      console.error(error);
      setResultado({ error: "El backend no está respondiendo. ¿Está encendido Spring Boot?" });
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h1 style={{ textAlign: 'center', color: '#333' }}>SQL Compiler</h1>

      <div style={{ marginBottom: '10px' }}>
        <label htmlFor="dialect-select" style={{ marginRight: '10px' }}>Dialecto:</label>
        <select
          id="dialect-select"
          value={dialect}
          onChange={(e) => setDialect(e.target.value)}
          style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ccc' }}
        >
          <option value="mysql">MySQL</option>
          <option value="postgresql">PostgreSQL</option>
          <option value="sqlserver">SQL Server</option>
          <option value="mongodb">MongoDB</option>
        </select>
      </div>

      <div style={{ marginBottom: '10px' }}>
        <input
          type="file"
          accept=".sql"
          onChange={handleFileUpload}
          disabled={uploading}
        />
        {uploading ? <span>Subiendo esquema...</span> : schemaLoaded ? <span>Esquema cargado ✅</span> : <span>No hay esquema cargado</span>}
      </div>

      <SqlEditor
        onCodeChange={(codigo) => setSqlCode(codigo)}
        disabled={!schemaLoaded}
      />

      <button
        onClick={compilarSql}
        disabled={!schemaLoaded}
        style={{
          marginTop: '15px',
          padding: '10px 20px',
          cursor: schemaLoaded ? 'pointer' : 'not-allowed',
          backgroundColor: schemaLoaded ? '#007bff' : '#cccccc',
          color: '#fff',
          border: 'none',
          borderRadius: '4px',
        }}
      >
        Compilar / Validar SQL
      </button>

      <div style={{ marginTop: '20px', padding: '15px', background: '#f4f4f4', borderRadius: '5px' }}>
        <h3>Respuesta del Compilador:</h3>
        <pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(resultado, null, 2)}</pre>
      </div>

      {resultado && resultado.ast ? (
        <div style={{ marginTop: '20px' }}>
          <h3>AST Visual:</h3>
          <AstViewer ast={resultado.ast} />
        </div>
      ) : null}
    </div>
  );
}

export default App;