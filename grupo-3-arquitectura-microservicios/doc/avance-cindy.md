
# Avance - Cindy Maytté Ruano Calderón 

Grupo 3 - Arquitectura, Microservicios y Frontend

# Funcionalidades implementadas
    - Estructura Base del Proyecto: Configuración del entorno de desarrollo utilizando React y Vite para una carga ultrarrápida y modular del lado del cliente.

    - Editor SQL Avanzado (Monaco Editor): Integración de @monaco-editor/react para dotar a la plataforma de un editor de código profesional con soporte nativo de resaltado para la sintaxis SQL.

    - Temas Personalizados Dinámicos: Diseño e implementación de múltiples hojas de estilos e interfaces interactivas (Temas: Oscuro Cyberpunk, Claro Minimalista y el tema personalizado Rosa Coquette 🎀 con reglas de coloreado para palabras clave, números y comentarios).

    - Modularización de Componentes de Configuración: Creación de componentes reactivos y modulares como SettingsModal para alternar la estética visual y SchemaModal para la ingesta de metadatos.

    - Gestión Dinámica de Esquemas (Tabla de Símbolos): Implementación de un flujo dual en el cliente para alimentar el compilador: ya sea mediante la subida de un archivo estructurado .sql o a través de la inserción directa de código en texto plano (CREATE TABLE).

    - Conexión Frontend-Backend (Microservicios): Integración de servicios asíncronos (fetch) para comunicar la interfaz con el backend en Spring Boot (puerto 8082), manejando los endpoints de validación de esquemas y procesamiento de consultas por dialectos (MySQL, PostgreSQL, SQL Server y MongoDB).

    - Consola de Respuestas y Árbol AST: Renderizado dinámico de la respuesta del compilador, incluyendo la caja de errores semánticos/léxicos y la integración de estructuras jerárquicas visuales (react-d3-tree) para graficar el Árbol de Sintaxis Abstracta (AST).

# Pruebas e Interfaz de Usuario Realizadas
    1. Inicialización del Entorno de Desarrollo
        - Comando: npm run dev en la ruta correspondiente del frontend.

        - Resultado exitoso: Servidor Vite levantado localmente en http://localhost:5173/ listo para procesar la interfaz interactiva.

    2. Flujo de Configuración y Personalización Visual
        - Acción: Apertura del panel de control general de la aplicación (SettingsModal).

        - Resultado: Cambio dinámico de estados de color en caliente, aplicando exitosamente el esquema Oscuro Cyberpunk y el Modo de Análisis Estricto para la restricción de mayúsculas y puntos y comas.

    3. Validación Visual de Errores Semánticos en Pantalla
        - Consulta ingresada en el Editor: ```sql
            SELECT e.employee_id, e.first_name, e.last_name, d.department_name
            FROM employees e
            INNER JOIN departments d ON e.department_id = d.department_id
            WHERE e.salary > 50000;

        - Error capturado en el panel de advertencias:
            Errores encontrados:
                Tabla 'employees' no existe

        - Resumen del Análisis Léxico generado automáticamente en UI:
            Keywords: SELECT, FROM, INNER, JOIN, ON, WHERE

            Identifiers: e, employee_id, first_name, last_name, d, department_name, employees, departments, department_id, salary

            Symbols: ., ,, ;