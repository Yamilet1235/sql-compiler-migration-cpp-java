# SQL Compiler Migration C++ → Java

## Proyecto Académico
**Tema:** Justificación de Refactorización: C++ → Java  
**Empresa:** DataQuery Solutions S.A.  
**Proyecto:** Migración del Compilador SQL de C++ a Java  

---

## Descripción del Proyecto

Front-end de compilador para consultas `SELECT` que ejecuta 3 fases:
1. **Analisis lexico** (Lexer) — tokeniza la consulta
2. **Analisis sintactico** (Parser) — construye un AST y valida la gramatica
3. **Analisis semantico** (SemanticAnalyzer) — verifica tablas, columnas y tipos

Actualmente migrado a Java. En evolucion a API REST con Spring Boot + React.

---

## Distribucion de Integrantes y Responsabilidades

| Integrante | Rol | Carpeta |
|---|---|---|
| nlopezf16 (Tu) | Direccion de Desarrollo / Parser | `/grupo-1-direccion-desarrollo/` |
| Madelin Ceron | Logica y Documentacion | `/grupo-2-logica-documentacion/` |
| Cindy | Arquitectura / Microservicios | `/grupo-3-arquitectura-microservicios/` |
| Yamilet Lindo | QA y Gestion de Versiones | `/grupo-4-qa-versiones/` |

---

## Estructura del Repositorio

```
sql-compiler-migration-cpp-java/
|-- README.md
|-- docs/
|-- src/                         (codigo fuente original - Maven)
|-- grupo-1-direccion-desarrollo/
|   |-- lexer-parser/            (Lexer, Parser, AST nodes)
|   |-- cpp-reference/           (codigo C++ original de referencia)
|   |-- README.md
|-- grupo-2-logica-documentacion/
|   |-- semantic-analyzer/       (SemanticAnalyzer, SymbolTable, etc.)
|   |-- schema/                  (schema-template.json)
|   |-- docs/                    (documentacion tecnica)
|   |-- README.md
|-- grupo-3-arquitectura-microservicios/
|   |-- backend/                 (Spring Boot API)
|   |-- frontend/                (React + Monaco Editor)
|   |-- ai-integration/          (Gemini API chat)
|   |-- README.md
|-- grupo-4-qa-versiones/
|   |-- tests/                   (pruebas unitarias + ejemplos)
|   |-- reports/                 (reportes de calidad)
|   |-- README.md
|-- entregables/
```

---

## Stack Tecnologico

| Componente | Tecnologia |
|---|---|
| Backend | Java 17 + Spring Boot 3.2 |
| Frontend | React + Vite + Monaco Editor |
| Parser | ANTLR 4 (multi-dialecto) |
| AI | Gemini API (REST) |
| Build | Maven |
| Deploy | Railway / Render (gratuito) |
| Testing | JUnit 4 |

---

## Fechas Clave

- **23 de mayo** — Fecha de entrega
- Semana 1 (11-17 mayo): Migracion a Spring Boot + ANTLR + API REST basica
- Semana 2 (18-23 mayo): Frontend + detector de dialecto + schema dinamico
