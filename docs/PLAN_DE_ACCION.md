# Plan de Accion — 2 Semanas (11 al 23 de Mayo)

## Resumen para el equipo

El proyecto actual es un compilador SQL de consola. Vamos a convertirlo en una
**API REST con frontend web**, manteniendo la logica central pero haciendola
escalable, multi-dialecto, y con schema dinamico.

---

## SEMANA 1 (11-17 mayo): Base del Backend

### Dia 1-2: nlopezf16 + Cindy — Migrar a Spring Boot

```
[HOY] La estructura ya esta creada. Lo que sigue:
```

**nlopezf16 (Tu):**
1. Copiar TODAS las clases del grupo-1 al proyecto Spring Boot en `grupo-3/backend/src/main/java/com/sqlcompiler/`
2. Verificar que compilan con el nuevo pom.xml (Spring Boot + ANTLR)
3. Crear el package `com.sqlcompiler.lexer` y mover Lexer, Token, TokenType ahi
4. Crear el package `com.sqlcompiler.parser` y mover Parser, ASTNode, etc. ahi

**Cindy:**
1. Abrir el proyecto Spring Boot en `grupo-3/backend/`
2. Implementar `SqlValidationService.validate()` — llamar al Lexer, Parser y SemanticAnalyzer
3. Probar con `curl` que el endpoint `POST /api/v1/validate/query` funcione
4. Configurar CORS para el frontend

### Dia 3-4: Madelin — Schema Dinamico + Strategy

**Madelin:**
1. Modificar `SymbolTable` para que acepte un JSON en lugar de valores hardcodeados
   - Recibir un `Map<String, Table>` en el constructor
   - Crear un metodo `loadFromJson(String json)` que construya el schema
2. Implementar el patron **Strategy** para dialectos:
   - Crear `DialectStrategy` interface (ya esta creada en grupo-2)
   - Crear `MySQLStrategy`, `PostgreSQLStrategy`, `SQLiteStrategy`
   - Cada una valida reglas especificas de ese dialecto
3. Documentar en `grupo-2/docs/` los tipos de datos soportados por dialecto

### Dia 5: nlopezf16 — Detector de Dialecto

1. Integrar `DialectDetector` (ya creado en grupo-2) en el flujo de validacion
2. Si el usuario no especifica dialecto, detectarlo automaticamente
3. Probar con queries de MySQL, PostgreSQL, SQLite

---

## SEMANA 2 (18-23 mayo): Frontend + Integracion

### Dia 1-2: Cindy — Frontend React

**Requisitos minimos:**
```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install @monaco-editor/react axios
```

**Componentes a crear:**
1. `SqlEditor.jsx` — Editor SQL con Monaco Editor (tema oscuro, resaltado)
2. `DialectSelector.jsx` — Dropdown para elegir dialecto (MySQL, PostgreSQL, etc.)
3. `SchemaUploader.jsx` — Input para pegar/cargar schema JSON
4. `ResultPanel.jsx` — Muestra: tokens, AST, errores, resultado valido/invalido
5. `AiChatPanel.jsx` — Chat lateral conectado a Gemini

**Flujo de la app:**
```
[Usuario escribe SQL] → [Selecciona dialecto] → [Carga schema]
       ↓
[POST /api/v1/validate/query]
       ↓
[Muestra: tokens | AST | errores | valido/invalido]
       ↓
[Si hay error → "Ayudame con IA" → Gemini explica]
```

### Dia 3: nlopezf16 — AST Visualizer

Puedes generar el AST como string indentado (ya lo hace `ast.print()`) y mostrarlo
en el frontend dentro de un `<pre>` o con una libreria sencilla como `react-d3-tree`.

**Implementacion rapida:**
```java
// En SemanticAnalyzer o en un nuevo servicio
public String astToJson(SelectNode ast) {
    // Convertir AST a JSON para que el frontend lo renderice como arbol
    // o simplemente devolver el string indentado que ya produce print()
}
```

### Dia 4: Yamilet — QA Final

**Checklist de pruebas:**
1. `mvn test` pasa sin errores (pruebas unitarias existentes)
2. Probar endpoint REST con `curl` o Postman
3. Probar frontend conectado al backend
4. Probar schema JSON cargado vs schema hardcodeado
5. Probar detector de dialecto con cada dialecto

**Reportes en `grupo-4/reports/`:**
- `test-report.md` — Resultados de pruebas
- `bugs-found.md` — Bugs encontrados y soluciones
- `qa-checklist.md` — Checklist de calidad

### Dia 5: Todo el equipo — Integracion + Deploy

1. Subir todo a GitHub
2. Hacer deploy del backend en Railway:
   - `railway login`
   - `railway init`
   - `railway up`
3. Hacer deploy del frontend en Vercel o Netlify (gratis)
4. Probar la app funcionando en produccion
5. Yamilet actualiza README con URLs de deploy

---

## Queda claro lo del AST?

El AST (Abstract Syntax Tree) es la representacion estructurada de la consulta.

Para visualizarlo tienes 2 opciones:

**Opcion 1 — Texto indentado (YA lo tienes funcionando):**
```
SELECT Query:
  Columns: nombre, edad
  FROM: usuarios
  WHERE:
    Condition:
      Left: Identifier: edad
      Operator: >
      Right: Number: 18
```

**Opcion 2 — Arbol visual (para el frontend):**
Con `react-d3-tree` puedes convertir el AST en un arbol expandible:

```json
{
  "name": "SELECT",
  "children": [
    { "name": "Columns: nombre, edad" },
    { "name": "FROM: usuarios" },
    { "name": "WHERE",
      "children": [
        { "name": "edad > 18" }
      ]
    }
  ]
}
```

Sugerencia: en el backend crea un metodo `astToJson()` y en el frontend
usa `<Tree>` de `react-d3-tree` para dibujarlo.

---

## Dependencias entre grupos (orden de implementacion)

```
nlopezf16 (Parser/lexer) ──┐
                           ├──> Cindy (Spring Boot + React)
Madelin (Semantica/Schema) ─┘        │
                                      v
                               Yamilet (QA + Tests)
```

**Orden recomendado:**
1. nlopezf16 verifica que Lexer+Parser compilan en Spring Boot
2. Madelin entrega SymbolTable dinamico + JSON schema
3. Cindy integra todo en Spring Boot y monta el frontend
4. nlopezf16 agrega detector de dialecto + AST visualizer
5. Cindy conecta IA (Gemini)
6. Yamilet prueba todo y documenta bugs
7. Todos deployan

---

## Herramientas gratuitas para deploy

| Servicio | Backend | Frontend | Costo |
|---|---|---|---|
| Railway | Java/Spring | No | 500h/mes gratis |
| Render | Java/Spring | Si | 750h/mes gratis |
| Vercel | No | React/Vite | Gratis |
| Netlify | No | React/Vite | Gratis |
| GitHub Pages | No | React/Vite | Gratis |

---

## Resumen para cada integrante

| Quien | Que hace esta semana | Donde esta su codigo |
|---|---|---|
| **nlopezf16 (Tu)** | Verificar Lexer+Parser en Spring Boot + DialectDetector | `grupo-1/` |
| **Madelin** | Schema dinamico (JSON) + Strategy Pattern | `grupo-2/` |
| **Cindy** | Spring Boot API + Frontend React + IA Gemini | `grupo-3/` |
| **Yamilet** | Pruebas + Reportes + Git | `grupo-4/` |
