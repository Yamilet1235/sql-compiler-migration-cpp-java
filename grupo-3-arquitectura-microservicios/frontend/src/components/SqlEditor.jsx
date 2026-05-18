import { useRef } from 'react';
import Editor from '@monaco-editor/react';

const SqlEditor = ({ code, onCodeChange }) => {
  const editorRef = useRef(null);

  // Se ejecuta cuando el editor se monta en pantalla
  function handleEditorDidMount(editor) {
    editorRef.current = editor;
  }

  // Captura el texto cada vez que el usuario escribe
  function handleEditorChange(value) {
    onCodeChange(value || "");
  }

  return (
    <div style={{ border: '1px solid #1e293b', borderRadius: '8px', overflow: 'hidden' }}>
      <Editor
        height="45vh"
        defaultLanguage="sql"
        value={code} // Se cambió defaultValue por value para permitir la limpieza dinámica
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