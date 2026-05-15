import { useRef } from 'react';
import Editor from '@monaco-editor/react';

const SqlEditor = ({ onCodeChange }) => {
  const editorRef = useRef(null);

  // Se ejecuta cuando el editor se monta en pantalla
  function handleEditorDidMount(editor) {
    editorRef.current = editor;
  }

  // Captura el texto cada vez que el usuario escribe
  function handleEditorChange(value) {
    onCodeChange(value);
  }

  return (
    <div style={{ border: '1px solid #1e293b', borderRadius: '8px', overflow: 'hidden' }}>
      <Editor
        height="45vh"
        defaultLanguage="sql"
        defaultValue="SELECT e.employee_id, e.first_name, e.last_name, d.department_name&#10;FROM employees e&#10;INNER JOIN departments d ON e.department_id = d.department_id&#10;WHERE e.salary > 50000;"
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