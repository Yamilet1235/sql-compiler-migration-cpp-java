import React, { useState } from 'react';
import SqlEditor from './components/SqlEditor';
import './index.css';

function App() {
  const [sqlCode, setSqlCode] = useState('');
  const [resultado, setResultado] = useState(null);

  // Esta función se llama cuando el usuario hace clic en "Ejecutar"
  const compilarSql = async () => {
    try {
      // Aquí nos conectamos al puerto 8080 donde corre Spring Boot (Backend)
      const response = await fetch('http://localhost:8080/api/sql/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ query: sqlCode }), // Enviamos el código escrito
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
      <h1 className="text-tertiary text-4xl font-bold p-10">
  
</h1>
      
      {/* Nuestro editor de código */}
      <SqlEditor onCodeChange={(codigo) => setSqlCode(codigo)} />
      
      <button 
        onClick={compilarSql} 
        style={{ marginTop: '15px', padding: '10px 20px', cursor: 'pointer' }}>
        Compilar / Validar SQL
      </button>

      {/* Caja para mostrar la respuesta del Backend o de Gemini */}
      <div style={{ marginTop: '20px', padding: '15px', background: '#f4f4f4', borderRadius: '5px' }}>
        <h3>Respuesta del Compilador:</h3>
        <pre>{JSON.stringify(resultado, null, 2)}</pre>
      </div>
    </div>
  );
}

export default App;
