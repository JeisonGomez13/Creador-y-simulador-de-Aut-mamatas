# Constitución del Proyecto

1. **Stack Tecnológico**: Java 17 y JavaFX nativo. Queda estrictamente prohibido el uso de librerías externas para modelado, renderizado o manipulación de grafos.

2. **Alcance Teórico**: Soporte exclusivo para AFD y AFN sin transiciones vacías (sin ε). Las transiciones solo admiten símbolos pertenecientes al alfabeto formal Σ definido.

3. **Arquitectura y Desacoplamiento**: Patrón MVC estricto. El paquete `model` contiene la lógica matemática formal y no debe importar ningún paquete de JavaFX (`javafx.*`). La UI solo consume y refleja el modelo.

4. **Calidad y Cobertura (Tests)**: Todo algoritmo del modelo (simulación, validación de pertenencia y construcción de subconjuntos) debe contar con pruebas unitarias exhaustivas en JUnit 5.

5. **Idioma y Convenciones**: 
   - Identificadores de dominio (clases, métodos, variables), comentarios y documentación técnica en español con nomenclatura estándar (`camelCase`, `PascalCase`).
   - Todos los textos, etiquetas y mensajes de error de la interfaz gráfica dirigidos al usuario final en español.

6. **Criterio de Entrega (Definition of Done)**: Ninguna tarea se considera terminada si genera advertencias graves de compilación, fallos de tipos o si la suite completa de pruebas (`mvn test`) no finaliza con estado exitoso (en verde).

7. **Interfaz Grafica**: No se puede utilizar el color azul ni el cafe.

8. **Orientado a objetos**: El codigo debe estar enfocado en la programacion orientada a objetos.

9. **Orden de codigo**: El orden de las lineas de codigo es muy importante. Debe tener un orden secuencial facil de entender.