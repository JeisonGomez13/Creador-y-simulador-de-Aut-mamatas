# Autómatas (AFD / AFN) — Entorno de Diseño Visual, Simulación y Conversión

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21-007396?logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/JUnit%205-185%20Passing-brightgreen?logo=junit5&logoColor=white)](https://junit.org/)
[![Manual](https://img.shields.io/badge/Manual-Usuario-blue)](docs/manual_de_usuario.md)
[![Methodology](https://img.shields.io/badge/Methodology-SDD%20(Spec--Driven)-8B5CF6)](specs/001-Automatas/)

Aplicación de escritorio moderna construida en **Java 17** y **JavaFX** para el diseño interactivo mediante *Drag and Drop*, simulación con trazabilidad paso a paso y conversión formal de **Autómatas Finitos Deterministas (AFD)** y **No Deterministas (AFN)** sin transiciones vacías ($\varepsilon$).

---

## 📖 Documentación Principal

- 📘 **[Manual de Usuario Completo](docs/manual_de_usuario.md)**: Guía paso a paso ilustrada para el diseño, simulación, trazabilidad de cadenas y conversión a AFD.
- 📜 **[Constitución del Proyecto](docs/constitution.md)**: Principios de gobernanza técnica, desacoplamiento MVC y restricciones cromáticas.
- 📐 **[Especificación Funcional (SDD)](specs/001-Automatas/spec.md)**: Requisitos formales EARS y catálogo de 24 casos límite.

---

## 1. Metodología de Desarrollo: SDD (Specification-Driven Development)

El proyecto fue concebido, diseñado y construido siguiendo rigurosamente la metodología **SDD (Desarrollo Guiado por Especificaciones)**. A diferencia de los enfoques ad-hoc, SDD establece una cadena formal e inmutable donde **el código nunca se escribe sin una especificación contractual previa aprobada**.

```mermaid
graph TD
    A["1. Constitución (docs/constitution.md)<br>• Reglas y Restricciones Inmutables"] --> B["2. Especificación Formal (specs/001-Automatas/spec.md)<br>• Historias de Usuario & Criterios EARS<br>• 24 Casos Límite Formales"]
    B --> C["3. Plan Arquitectónico (specs/001-Automatas/plan.md)<br>• Arquitectura MVC Estricta<br>• Contratos de Datos & Interfaces"]
    C --> D["4. Desglose Atómico de Tareas (specs/001-Automatas/tasks.md)<br>• TDD: Red -> Green -> Refactor<br>• Verificación por Fases"]
    D --> E["5. Verificación & DoD<br>• 185 Pruebas Unitarias Exitosas<br>• 0% Regresiones"]
```

---

## 2. Características del Sistema

- **Definición Estricta del Alfabeto ($\Sigma$):** Admite exclusivamente letras individuales (`a-z`, `A-Z`) separadas por comas con bloqueo preventivo ante transiciones activas.
- **Lienzo Visual Interactivo (Drag & Drop):** Creación dinámica de nodos con numeración correlativa automática ($q_0, q_1, q_2, \dots$), renombrado interactivo y eliminación en cascada.
- **Transiciones y Autolazos Inteligentes:** Diálogo asistido para selección de símbolos, unificación de flechas en el mismo sentido, arcos curvados anticolisión en transiciones bidireccionales y lazos reflexivos superiores.
- **Clasificación Reactiva en Tiempo Real:** Detección matemática continua del estado formal del autómata (`«Sin estados»`, `«AFD»` o `«AFN»`).
- **Simulación y Trazabilidad Paso a Paso:** Evaluación binaria inmediata (`«Aceptada»` / `«Rechazada»`) con desglose gráfico del recorrido de estados y prioridad visual para los caminos aceptados.
- **Conversión AFN a AFD (Construcción de Subconjuntos):** Generación automática de una nueva pestaña protegida con disposición circular equidistante y tabla viva de subconjuntos.
- **Gestión Multi-Pestaña Segura:** Confirmación preventiva ante cierre involuntario y regeneración automática si se cierran todas las pestañas.
- **Personalización Temática (Modo América):** Botón de alternancia a paleta escarlata y blanca con marca de agua oficial en el fondo del tablero.

---

## 3. Instrucciones de Ejecución

### Ejecución Directa (Desarrollo)
```bash
mvn javafx:run
```

### Ejecutar Pruebas Automatizadas
```bash
mvn test
```

### Empaquetar para Producción
```bash
mvn clean package
```
El archivo JAR ejecutable se generará en `target/automatas-1.0.0-SNAPSHOT.jar`.
