import Editor from '@monaco-editor/react';

const SqlEditor = ({ code, onCodeChange, theme }) => {

  // CONFIGURACIÓN Y DEFINICIÓN DE TEMAS DE MONACO EDITOR
  function handleEditorWillMount(monaco) {
    monaco.editor.defineTheme('rosa-coquette-theme', {
      base: 'vs', 
      inherit: true,
      rules: [
        { token: 'keyword', foreground: 'ff7096', fontStyle: 'bold' }, 
        { token: 'comment', foreground: 'aa7c88', fontStyle: 'italic' },
        { token: 'number', foreground: '9d4edd' },
        { token: 'string', foreground: '4a5568' },
      ],
      colors: {
        'editor.background': '#ffffff',        
        'editor.foreground': '#5c3d46',        
        'editorLineNumber.foreground': '#f3c6d1', 
        'editorLineNumber.activeForeground': '#ff7096',
        'editor.lineHighlightBackground': '#fff0f3', 
      }
    });
  }

  // CAMBIO DE TEXTO
  function handleEditorChange(value) {
    onCodeChange(value || "");
  }

  // COLORES DE LA INTERFAZ
  const esRosa = theme === 'rosa-coquette-theme';
  const esClaro = theme === 'light';
  const colorBorde = esRosa ? '#f3c6d1' : esClaro ? '#cbd5e1' : '#334155';

  // RENDERIZADO
  return (
    <div style={{ border: `1px solid ${colorBorde}`, borderRadius: '8px', overflow: 'hidden', height: '100%' }}>
      <Editor
        height="45vh"
        defaultLanguage="sql"
        value={code}
        theme={theme} 
        onChange={handleEditorChange}
        beforeMount={handleEditorWillMount}
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