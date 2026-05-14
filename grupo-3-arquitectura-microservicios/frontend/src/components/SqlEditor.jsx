import React, { useRef } from 'react';
import Editor from '@monaco-editor/react';

const SqlEditor = ({ onCodeChange }) => {
  const editorRef = useRef(null);

  // Se ejecuta cuando el editor carga
  function handleEditorDidMount(editor, monaco) {
    editorRef.current = editor;
  }

  // Captura el texto cada vez que el usuario escribe
  function handleEditorChange(value) {
    onCodeChange(value); 
  }

  return (
    <div style={{ border: '1px solid #333', borderRadius: '8px', overflow: 'hidden' }}>
      <Editor
        height="50vh"
        defaultLanguage="sql"
        defaultValue="/*Escribe tu consulta SQL aquí...*/&#10;SELECT * FROM usuarios;"
        theme="vs-dark" 
        onChange={handleEditorChange}
        onMount={handleEditorDidMount}
        options={{
          fontSize: 16,
          minimap: { enabled: false }, 
          automaticLayout: true,
        }}
      />
    </div>
  );
};

export default SqlEditor;
