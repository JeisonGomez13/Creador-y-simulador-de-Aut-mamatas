# Desglose de Tareas de Implementación: Autómatas (AFD / AFN)

Este documento desglosa el plan arquitectónico en unidades de trabajo atómicas (20 a 30 minutos por tarea), secuenciadas estrictamente por orden de dependencias. Cada tarea especifica los requisitos funcionales que cubre y su criterio observable de finalización.

---

## Fase 0: Configuración del Proyecto y Blindaje Arquitectónico

- [x] **Tarea 0.1: Inicialización del Proyecto Maven con Java 17 y JavaFX 17+**
  - **Estimación**: 20 min
  - **Cubre**: [Constitución 1, Constitución 6]
  - **Dependencias**: Ninguna
  - **Descripción**: Configurar `pom.xml` con Java 17 (`maven-compiler-plugin`), dependencias de `javafx-controls`, `javafx-fxml`, JUnit 5 (`junit-jupiter-engine`, `junit-jupiter-params`), y `maven-surefire-plugin`. Prohibir expresamente cualquier dependencia externa de grafos.
  - **Hecho cuando**: El comando `mvn clean compile` compila exitosamente un proyecto esqueleto sin advertencias ni dependencias externas de diagramación.

- [x] **Tarea 0.2: Test de Arquitectura para el Desacoplamiento de `model`**
  - **Estimación**: 20 min
  - **Cubre**: [Constitución 3]
  - **Dependencias**: Tarea 0.1
  - **Descripción**: Crear `DesacoplamientoArquitecturaTest` bajo `src/test/java/com/automatas/arquitectura/` que escanee recursivamente las clases del paquete `com.automatas.model` y valide que ninguna importe `javafx.*`.
  - **Hecho cuando**: `mvn test -Dtest=DesacoplamientoArquitecturaTest` se ejecuta en verde y falla si se inyecta artificialmente un import `javafx.*` en `model`.

---

## Fase 1: Modelo Matemático Puro y Suite de Pruebas Unitarias (JUnit 5)

- [x] **Tarea 1.1: Entidad `Alfabeto` y Pruebas Unitarias**
  - **Estimación**: 25 min
  - **Cubre**: [RF-01, Caso Límite 10]
  - **Dependencias**: Tarea 0.2
  - **Descripción**: Implementar `Alfabeto.java` (inmutable tras confirmar, validación de letras `a-z` y `A-Z`, distinción de mayúsculas/minúsculas, rechazo de números/operadores/espacios, coma como delimitador sintáctico y bloqueo ante transiciones activas). Implementar `AlfabetoTest.java`.
  - **Hecho cuando**: `mvn test -Dtest=AlfabetoTest` pasa al 100% cubriendo validación de letras, mayúsculas/minúsculas, descarte de espacios y excepciones por caracteres inválidos.

- [x] **Tarea 1.2: Entidades `Estado`, `Transicion` y Pruebas Básicas**
  - **Estimación**: 25 min
  - **Cubre**: [RF-02, RF-03, RF-04, Casos Límite 16, 25]
  - **Dependencias**: Tarea 1.1
  - **Descripción**: Implementar `Estado.java` (identificador textual, flag de aceptación, validación con `trim()`, longitud máx. 8 caracteres, no blanco) y `Transicion.java` (origen, destino, símbolo de $\Sigma$, igualdad y hashCode formal). Implementar `EstadoTest.java`.
  - **Hecho cuando**: `mvn test -Dtest=EstadoTest` verifica la inmutabilidad de identificadores válidos, rechazo de nombres $>8$ caracteres o en blanco, y comparación de transiciones idénticas.

- [x] **Tarea 1.3: Entidad `Automata` y Generación Secuencial de Identificadores**
  - **Estimación**: 30 min
  - **Cubre**: [RF-02, RF-03, Casos Límite 16, 17]
  - **Dependencias**: Tarea 1.2
  - **Descripción**: Implementar en `Automata.java` la gestión de $Q$, designación manual de estado inicial único $q_0 \in Q$ (con reemplazo automático), método para calcular el menor índice entero libre disponible ($q_0, q_1, \dots$), y eliminación de estados con cascada de transiciones.
  - **Hecho cuando**: `mvn test -Dtest=AutomataTest#testAsignacionSecuencialMenorIndiceLibre` y `#testBorradoEnCascada` pasen exitosamente.

- [x] **Tarea 1.4: Detección Automática de Tipo de Autómata (`TipoAutomata`)**
  - **Estimación**: 20 min
  - **Cubre**: [RF-05, Casos Límite 18]
  - **Dependencias**: Tarea 1.3
  - **Descripción**: Implementar `TipoAutomata.java` (`SIN_ESTADOS`, `AFD`, `AFN`) y el método `obtenerTipo()` en `Automata.java`. Evalúa `SIN_ESTADOS` si $|Q| = 0$; `AFN` si existe al menos un par $(q, a)$ con múltiples salidas; y `AFD` si no existen bifurcaciones duplicadas para el mismo símbolo.
  - **Hecho cuando**: `mvn test -Dtest=AutomataTest#testClasificacion*` comprueba que un lienzo vacío retorne `SIN_ESTADOS`, un grafo sin bifurcaciones retorne `AFD` y uno con salidas repetidas retorne `AFN`.

- [x] **Tarea 1.5: Motor de Simulación Inmediata (`SimuladorAutomata`)**
  - **Estimación**: 30 min
  - **Cubre**: [RF-06, Casos Límite 1, 2, 9]
  - **Dependencias**: Tarea 1.4
  - **Descripción**: Implementar `SimuladorAutomata.java` con el método `evaluarCadena(Automata, String)`. Realiza `trim()`, valida pertenencia a $\Sigma$, evalúa $\lambda$ según estado inicial $\in F$, aborta si no hay inicial, y ejecuta la simulación determinista o no determinista (exploración de caminos sin $\varepsilon$). Retorna veredicto binario estricto.
  - **Hecho cuando**: `mvn test -Dtest=SimuladorAutomataTest` valide los 9 casos de prueba previstos (cadenas válidas, caminos muertos, evaluación de $\lambda$ y rechazo por símbolos ajenos a $\Sigma$).

- [x] **Tarea 1.6: Algoritmo de Determinización por Subconjuntos (`ConvertidorSubconjuntos`)**
  - **Estimación**: 30 min
  - **Cubre**: [RF-07, Casos Límite 11, 12, 13, 14, 15]
  - **Dependencias**: Tarea 1.5
  - **Descripción**: Implementar `ResultadoConversion.java` y `ConvertidorSubconjuntos.java`. Valida precedencia (1º inicial, 2º que sea AFN), genera estados $k_0, k_1, \dots$ usando `Set<Estado>` inmutable, marca $k_0$ como inicial, define aceptación mediante $k_i \cap F_{AFN} \neq \emptyset$, poda inalcanzables preservando $k_0$, y registra celdas $\emptyset$ en la tabla de transiciones.
  - **Hecho cuando**: `mvn test -Dtest=ConvertidorSubconjuntosTest` pase al 100%, comprobando precedencia de errores, nomenclatura `k`, poda estricta y caso límite $N=1$.

---

## Fase 2: Infraestructura Gráfica Nativa JavaFX y Tokens de Color

- [x] **Tarea 2.1: Sistema de Estilos y Paleta de Color sin Azul ni Café**
  - **Estimación**: 20 min
  - **Cubre**: [RNF-06, Constitución 7]
  - **Dependencias**: Tarea 0.1
  - **Descripción**: Crear `PaletaColor.java` y `src/main/resources/css/estilos.css`. Configurar tokens neutros (grafito `#1E1E24`, `#2B2D42`, fondo `#F4F4F6`), verde esmeralda (`#10B981`) para aceptación, violeta neutro (`#8B5CF6`) para selección y ámbar (`#F59E0B`) para advertencias/AFN. Prohibir totalmente tonos azul y café.
  - **Hecho cuando**: Una prueba unitaria en `PaletaColorTest` inspeccione los códigos hexadecimales/RGB de la paleta y certifique que ningún color cae en el espectro HSL de azul (190°–250°) ni café (25°–45° oscuro).

- [x] **Tarea 2.2: Componente Visual `NodoEstadoVista`**
  - **Estimación**: 25 min
  - **Cubre**: [RF-02, RF-03, RNF-04]
  - **Dependencias**: Tarea 2.1, Tarea 1.2
  - **Descripción**: Crear `NodoEstadoVista.java` extendiendo `StackPane`. Incluir `Circle` principal, `Label` con el identificador del estado, `Circle` concéntrico para estados de aceptación ($F$) y `Polygon` (flecha triangular orientada hacia el nodo) para estado inicial.
  - **Hecho cuando**: El componente renderice visualmente un nodo en un `Pane` de prueba reflejando correctamente las condiciones regular, inicial, aceptación y simultáneamente inicial+aceptación.

- [x] **Tarea 2.3: Componentes de Aristas `AristaTransicionVista` y `AutolazoVista`**
  - **Estimación**: 30 min
  - **Cubre**: [RF-04, Casos Límite 5, 6]
  - **Dependencias**: Tarea 2.2
  - **Descripción**: Implementar `AristaTransicionVista.java` (línea directa o `QuadCurve` curvada para conexiones bidireccionales opuestas con cálculo ortogonal nativo y unificación de rótulos `"a, b"`) y `AutolazoVista.java` (`CubicCurve` en cuadrante superior). Añadir cabeza de flecha poligonal orientada al vector tangente de llegada.
  - **Hecho cuando**: Dos estados conectados en ambas direcciones muestren dos arcos curvados separados con sus respectivas etiquetas legibles sin solaparse.

- [x] **Tarea 2.4: Diálogo Asistido `SelectorSimbolosDialog`**
  - **Estimación**: 20 min
  - **Cubre**: [RF-04, Casos Límite 7, 8]
  - **Dependencias**: Tarea 2.1, Tarea 1.1
  - **Descripción**: Implementar `SelectorSimbolosDialog.java` extendiendo `Dialog<List<Character>>`. Presentar dinámicamente casillas de verificación (`CheckBox`) correspondientes a los símbolos del alfabeto formal activo. Al cancelar o cerrar sin selección, retornar lista vacía.
  - **Hecho cuando**: Al invocar el diálogo sobre un alfabeto `{'a', 'b', 'c'}`, se muestren los 3 checkboxes y al pulsar cancelar se retorne una lista vacía que impida crear la arista.

- [x] **Tarea 2.5: Contenedor `LienzoGrafo` con Scroll Dinámico y Drag & Drop**
  - **Estimación**: 30 min
  - **Cubre**: [RF-02, RF-04, Casos Límite 7, 21]
  - **Dependencias**: Tarea 2.2, Tarea 2.3, Tarea 2.4
  - **Descripción**: Implementar `LienzoGrafo.java` contenido en un `ScrollPane`. Soportar arrastrar nodos en tiempo real (manteniendo las aristas vinculadas mediante properties), creación de transiciones arrastrando de un nodo a otro, creación de autolazos al soltar sobre sí mismo, y expansión dinámica de las dimensiones del lienzo virtual al acercar un nodo a los bordes.
  - **Hecho cuando**: Al arrastrar un estado hacia el extremo derecho, las barras de desplazamiento del `ScrollPane` aumenten su rango permitiendo continuar la navegación.

---

## Fase 3: Paneles de Control, Simulación y Tabla de Transiciones Reactiva

- [x] **Tarea 3.1: Componente `TablaTransicionesVista` en SplitPane**
  - **Estimación**: 25 min
  - **Cubre**: [RF-08, Casos Límite 14]
  - **Dependencias**: Tarea 1.4, Tarea 2.1
  - **Descripción**: Crear `TablaTransicionesVista.java` basado en `TableView`. Generar columnas dinámicas por cada símbolo de $\Sigma$. En AFN mostrar conjuntos `{q1, q2}` y el símbolo $\emptyset$ para vacíos. En pestañas de conversión incluir la columna inmutable de subconjuntos del AFN original.
  - **Hecho cuando**: La tabla refleje instantáneamente en sus celdas las transiciones añadidas o eliminadas en el autómata de prueba.

- [x] **Tarea 3.2: Panel Superior de Alfabeto y Estado del Autómata**
  - **Estimación**: 25 min
  - **Cubre**: [RF-01, RF-05, Casos Límite 18]
  - **Dependencias**: Tarea 1.1, Tarea 1.4, Tarea 2.1
  - **Descripción**: Diseñar la barra superior de la pestaña: campo para configurar $\Sigma$ con botón de confirmación, bloqueo automático si existen transiciones en el lienzo, y etiqueta destacada indicando el tipo de autómata actual (`«Sin estados»`, `«AFD»` o `«AFN»`).
  - **Hecho cuando**: Al definir $\Sigma$ y agregar la primera transición, el campo de alfabeto quede deshabilitado y el indicador cambie de `«Sin estados»` a `«AFD»` o `«AFN»`.

- [x] **Tarea 3.3: Panel Inferior de Comprobación Inmediata de Cadenas**
  - **Estimación**: 20 min
  - **Cubre**: [RF-06]
  - **Dependencias**: Tarea 1.5, Tarea 2.1
  - **Descripción**: Implementar `PanelComprobacionVista.java`: campo de texto para la cadena de prueba, botón «Comprobar» y etiqueta de veredicto binario con estilos contrastados (`«Aceptada»` en verde esmeralda y `«Rechazada»` en ámbar/rojo neutro).
  - **Hecho cuando**: Ingresar una cadena válida y presionar «Comprobar» actualice la etiqueta inmediatamente con el veredicto sin parpadeos.

- [x] **Tarea 3.4: Integración del Controlador de Pestaña `ControladorPestana`**
  - **Estimación**: 30 min
  - **Cubre**: [RF-01 a RF-06, RF-08]
  - **Dependencias**: Tarea 2.5, Tarea 3.1, Tarea 3.2, Tarea 3.3
  - **Descripción**: Crear `ControladorPestana.java` y `VistaPestanaAutomata.java`. Ensamblar el `SplitPane` (Lienzo interactivo a la izquierda/centro y `TablaTransicionesVista` a la derecha colapsable) sincronizando el modelo `Automata` con la vista ante cualquier evento de diseño.
  - **Hecho cuando**: Al agregar o renombrar un estado en el lienzo, la tabla de transiciones lateral y la etiqueta de clasificación se actualicen al unísono.

---

## Fase 4: Multi-Pestañas, Layout Circular y Flujo de Conversión AFN $\to$ AFD

- [x] **Tarea 4.1: Algoritmo de Disposición Geométrica `DisposicionCircular`**
  - **Estimación**: 20 min
  - **Cubre**: [RF-07, Caso Límite 15]
  - **Dependencias**: Tarea 1.6
  - **Descripción**: Implementar `DisposicionCircular.java`. Para $N = 1$ asignar coordenadas exactamente al centro geométrico del lienzo. Para $N > 1$ distribuir los $N$ nodos equidistantes en una circunferencia de radio proporcional a $N$.
  - **Hecho cuando**: Una prueba unitaria verifique que para $N=1$ las coordenadas coincidan con el centro $(X_c, Y_c)$ y para $N=4$ se ubiquen en los 4 cuadrantes trigonométricos simétricos.

- [x] **Tarea 4.2: Flujo Completo de Conversión y Pestaña de Topología Protegida**
  - **Estimación**: 30 min
  - **Cubre**: [RF-07, Casos Límite 11, 12, 19, 22]
  - **Dependencias**: Tarea 1.6, Tarea 4.1, Tarea 3.4
  - **Descripción**: Conectar la acción «Convertir a AFD»: validar precedencia (falta inicial $\to$ alerta instructiva; es AFD $\to$ alerta informativa), ejecutar `ConvertidorSubconjuntos`, instanciar nueva pestaña `VistaPestanaAutomata` con título correlativo (`AFD (de Autómata X)` o `AFD 2...`), bloquear mutaciones de aristas/estados y aplicar `DisposicionCircular`.
  - **Hecho cuando**: Convertir un AFN válido cree una nueva pestaña con el AFD en disposición circular limpia, tabla con columnas de subconjuntos, y los estados solo permitan reposicionamiento mediante arrastre.

- [x] **Tarea 4.3: Controlador Global `ControladorPrincipal` y Ventana Raíz**
  - **Estimación**: 25 min
  - **Cubre**: [RF-08, Casos Límite 23, 24]
  - **Dependencias**: Tarea 4.2
  - **Descripción**: Implementar `VistaPrincipal.java` (`BorderPane` con menú y `TabPane`) y `ControladorPrincipal.java`. Gestionar pestañas rotuladas (`Autómata 1`, `Autómata 2`), botón «Nuevo Autómata», confirmación preventiva antes de cerrar pestañas con contenido y regeneración automática de `Autómata 1` si se cierra la última.
  - **Hecho cuando**: Cerrar la última pestaña abierta del editor abra automáticamente una nueva pestaña en blanco `Autómata 1` sin dejar la aplicación vacía.

---

## Fase 5: Verificación Integral del Definition of Done y Pruebas Finales

- [ ] **Tarea 5.1: Ejecución y Certificación de la Suite Completa de Tests**
  - **Estimación**: 20 min
  - **Cubre**: [Constitución 4, Constitución 6]
  - **Dependencias**: Tarea 4.3
  - **Descripción**: Ejecutar `mvn clean test` asegurando que todos los tests unitarios y de arquitectura pasen con cero fallos y cero advertencias de compilación.
  - **Hecho cuando**: La salida de `mvn test` muestre `BUILD SUCCESS` con 100% de tests unitarios y de arquitectura aprobados.

- [ ] **Tarea 5.2: Checklist de Validación Manual de los 11 Criterios de Aceptación (DoD)**
  - **Estimación**: 25 min
  - **Cubre**: [DoD 1 a DoD 11 de spec.md]
  - **Dependencias**: Tarea 5.1
  - **Descripción**: Ejecutar la aplicación (`mvn javafx:run`) y validar los 11 criterios del Definition of Done: bloqueo de $\Sigma$, nombres $q_i$ y renombres $\le 8$ caracteres, único inicial, selector asistido y arcos curvados, tabla en SplitPane, etiqueta `«Sin estados»`/`AFD`/`AFN`, veredicto binario con `trim()`, conversión circular con $k_0$ y modo protegido, gestión de pestañas correlativas y ausencia total de colores azul y café.
  - **Hecho cuando**: Los 11 criterios de DoD de [specs/001-Automatas/spec.md](file:///c:/Users/JeisonPc/Documents/Univerisdad/Compiladores/Automatas/specs/001-Automatas/spec.md) se verifiquen satisfactoriamente en la interfaz de usuario.
