
# Avance - Madelin Ceron Molina 

Grupo 2 - Lógica Semántica y Datos

Funcionalidades implementadas: 

Implementación de SemanticAnalyzer
Migración de SymbolTable de hardcodeado a schema dinámico
Implementación de JsonSchemaRepository
Validación de existencia de tablas
Validación de existencia de columnas
Validación de aliases SQL
Validación de JOINs
Compatibilidad con schema JSON dinámico
Ampliación de DataType para múltiples dialectos SQL
Pruebas realizadas

Consulta válida:

SELECT nombre, precio FROM productos;

Error semántico detectado:

SELECT stock FROM productos;

Resultado:

Columna 'stock' no existe en tabla 'productos'

Error de tabla inexistente:

SELECT * FROM tabla_fake;

Resultado:

Tabla 'tabla_fake' no existe


