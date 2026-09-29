# AGENTS.md — Autómatas (AFN / AFD)

## Proyecto
Aplicación de escritorio para el diseño visual (Drag and Drop), simulación y conversión de Autómatas Finitos Deterministas (AFD) y No Deterministas (AFN) sin transiciones vacías.

## Estilo
- Lenguaje: Java 17.
- Interfaz Gráfica: JavaFX (renderizado con nodos nativos, sin librerías externas de grafos).
- Arquitectura: MVC.
- Idioma: Código, comentarios y mensajes de usuario en Español.

## Reglas
- Lee `docs/constitution.md` y la especificación activa en `specs/` antes de escribir o modificar código.
- No modifiques archivos dentro de `specs/` salvo que el usuario lo solicite explícitamente.

## Verificación Obligatoria
Al terminar cualquier tarea o cambio:
- Ejecutar los tests: `mvn test` (o `./gradlew test`).
- Asegurar que el proyecto compile sin errores ni regresiones.