# Grupo 1 - Dirección de Desarrollo

**Responsable:** Marìa de los Angeles Lopez

## Contenido

### `lexer-parser/`
Núcleo del compilador: análisis léxico y sintáctico para consultas SELECT.
- `Lexer.java` — Tokenizador: convierte SQL crudo en tokens
- `Parser.java` — Parser descendente recursivo: construye el AST
- `ASTNode.java` — Clase base abstracta para nodos del AST
- `SelectNode.java` — Nodo raíz del AST para SELECT
- `ConditionNode.java` — Nodo para condiciones WHERE
- `ExpressionNode.java` — Nodo para expresiones (identificadores, números, strings)
- `Token.java` — Representación de un token con tipo, valor y ubicación
- `TokenType.java` — Enum con todos los tipos de token soportados
- `CompOperator.java` — Enum con operadores de comparación



## Responsabilidades
- Mantener y evolucionar el Lexer + Parser
- Migrar lógica de C++ a Java
- Soportar nuevos dialectos SQL via ANTLR (futuro)
