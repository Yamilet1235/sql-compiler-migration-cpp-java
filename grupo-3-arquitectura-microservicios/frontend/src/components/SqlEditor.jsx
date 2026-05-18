import React, { useRef, useEffect } from 'react';
import Editor from '@monaco-editor/react';

const SqlEditor = ({ code, dialect, onCodeChange }) => {
  const editorRef = useRef(null);
  const onCodeChangeRef = useRef(onCodeChange);

  // Mantenemos la referencia de la función actualizada sin disparar re-renders
  useEffect(() => {
    onCodeChangeRef.current = onCodeChange;
  }, [onCodeChange]);

  // Escucha cuando el padre (App.jsx) limpia el texto (al cambiar de dialecto)
  useEffect(() => {
    if (editorRef.current && code === "") {
      editorRef.current.setValue("");
    }
  }, [code]);

  // Forzar la re-validación automática en el backend cuando cambias de dialecto
  useEffect(() => {
    if (editorRef.current) {
      const currentText = editorRef.current.getValue();
      // Solo re-valida si hay algo escrito para evitar peticiones vacías
      if (currentText.trim() !== "") {
        onCodeChangeRef.current(currentText);
      }
    }
  }, [dialect]);

  function handleEditorDidMount(editor) {
    editorRef.current = editor;
  }

  function handleEditorChange(value) {
    onCodeChange(value || "");
  }

  return (
    <div style={{ border: '1px solid #1e293b', borderRadius: '8px', overflow: 'hidden' }}>
      <Editor
        height="45vh"
        defaultLanguage="sql"
        value={code} // Enlazado al estado de App.jsx para que se pueda limpiar
        theme="vs-dark"
        onChange={handleEditorChange}
        onMount={handleEditorDidMount}
        options={{
          fontSize: 15,
          minimap: { enabled: false }, 
          automaticLayout: true,
        }}
      />
    </div>
  );
};

export default SqlEditor;