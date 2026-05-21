# Grupo 2 - Análisis de Lógica y Gestión Documental

**Responsable:** Madelin Jazmin Ceron Molina 

## Contenido

### `semantic-analyzer/`
Validador semántico: verifica tablas, columnas y tipos de datos.
- `SemanticAnalyzer.java` — Valida el AST contra el schema
- `SymbolTable.java` — Tabla de símbolos con schema hardcodeado (migrar a dinámico)
- `Table.java` — Representación de una tabla
- `Column.java` — Representación de una columna
- `DataType.java` — Enum con tipos de datos soportados

### `schema/`
Definiciones de esquemas de base de datos en JSON.
- `schema-template.json` — Template de schema intercambiable

### `docs/`
Documentación técnica del proyecto.

## Responsabilidades
- Migrar SymbolTable de hardcodeado a schema dinámico (JSON)
- Implementar patrón Strategy para múltiples dialectos SQL
- Validación de tipos y reglas semánticas
- Documentación técnica
