# Plan de Implementación Arquitectónico: Autómatas (AFD / AFN)

## 1. Introducción y Principios de Diseño

Este plan técnico establece la arquitectura, diseño modular, decisiones de ingeniería y estrategia de pruebas para la aplicación de diseño visual, simulación y conversión de Autómatas Finitos Deterministas (AFD) y No Deterministas (AFN) sin transiciones vacías ($\varepsilon$).

El diseño respeta de manera intransigente los 7 principios de la **Constitución del Proyecto** ([docs/constitution.md](file:///c:/Users/JeisonPc/Documents/Univerisdad/Compiladores/Automatas/docs/constitution.md)) y satisface la totalidad de los requisitos especificados en [specs/001-Automatas/spec.md](file:///c:/Users/JeisonPc/Documents/Univerisdad/Compiladores/Automatas/specs/001-Automatas/spec.md).

---

## 2. Estructura de Módulos y Paquetes

La solución se estructura bajo el patrón **MVC Estricto** en un único proyecto modular Maven/Gradle, garantizando que el paquete `model` sea 100% independiente de cualquier API gráfica (prohibición estricta de imports `javafx.*` según el Principio 3 de la Constitución).

```
src/
├── main/
│   ├── java/
│   │   └── com/automatas/
│   │       ├── Main.java                          # Lanzador de la aplicación JavaFX
│   │       │
│   │       ├── model/                             # [LÓGICA FORMAL PURA - CERO JAVAFX]
│   │       │   ├── Alfabeto.java                  # Representación y validación de Σ (RF-01)
│   │       │   ├── Estado.java                    # Entidad de estado formal (RF-02, RF-03)
│   │       │   ├── Transicion.java                # Transición dirigida formal (RF-04)
│   │       │   ├── TipoAutomata.java              # Enum: SIN_ESTADOS, AFD, AFN (RF-05)
│   │       │   ├── Automata.java                  # Estructura del autómata 5-tupla (RF-01 a RF-05)
│   │       │   ├── SimuladorAutomata.java         # Motor de simulación inmediata (RF-06)
│   │       │   ├── ConvertidorSubconjuntos.java   # Algoritmo de determinización y poda (RF-07)
│   │       │   └── ResultadoConversion.java       # DTO con AFD generado y TablaSubconjuntos (RF-07)
│   │       │
│   │       ├── controller/                        # [CONTROLADORES Y ORQUESTACIÓN MVC]
│   │       │   ├── ControladorPrincipal.java      # Coordinador de ventanas y menú global (RF-08)
│   │       │   ├── ControladorPestana.java        # Controlador de una pestaña de autómata (RF-01 a RF-08)
│   │       │   ├── ControladorLienzo.java         # Lógica de interacciones Drag & Drop del grafo (RF-02, RF-04)
│   │       │   └── ControladorSimulacion.java     # Manejador del panel de comprobación (RF-06)
│   │       │
│   │       └── view/                              # [INTERFAZ GRÁFICA JAVAFX NATIVA]
│   │           ├── VistaPrincipal.java            # Contenedor raíz (BorderPane con TabPane) (RF-08)
│   │           ├── VistaPestanaAutomata.java      # SplitPane: Lienzo navegable + Tabla lateral (RF-08)
│   │           ├── LienzoGrafo.java               # Pane sobre ScrollPane con expansión dinámica (RF-02)
│   │           ├── componente/                    # Nodos gráficos nativos reutilizables
│   │           │   ├── NodoEstadoVista.java       # Circle + Label + Doble círculo + Flecha inicial (RF-02, RF-03)
│   │           │   ├── AristaTransicionVista.java # Path/QuadCurve con flecha y rótulo unificado (RF-04)
│   │           │   ├── AutolazoVista.java         # Arco reflexivo circular nativo (RF-04)
│   │           │   ├── SelectorSimbolosDialog.java# Diálogo asistido con CheckBoxes de Σ (RF-04)
│   │           │   └── TablaTransicionesVista.java# TableView dinámica para AFN/AFD y subconjuntos (RF-08)
│   │           │
│   │           └── util/
│   │               ├── DisposicionCircular.java   # Algoritmo geométrico de layout nativo (RF-07)
│   │               └── PaletaColor.java           # Tokens de color (sin azul ni café) (RNF-06, Const. 7)
│   │
│   └── resources/
│       ├── css/
│       │   └── estilos.css                        # Estilos Vanilla JavaFX (grises, esmeralda, ámbar, violeta)
│       └── imagenes/                              # Recursos gráficos neutros (iconos sin azul/café)
│
└── test/
    └── java/
        └── com/automatas/
            ├── model/                             # Pruebas unitarias exhaustivas JUnit 5
            │   ├── AlfabetoTest.java              # Validación sintáctica, espacios, mayúsculas (RF-01)
            │   ├── EstadoTest.java                # Unicidad, nombres q_i, límites y renombrado (RF-02)
            │   ├── AutomataTest.java              # Topología, inicial único, clasificación AFD/AFN (RF-03, RF-05)
            │   ├── SimuladorAutomataTest.java     # Cadenas AFD/AFN, λ, caminos muertos, trim (RF-06)
            │   └── ConvertidorSubconjuntosTest.java# Subconjuntos, nombres k, poda, aceptación (RF-07)
            │
            └── arquitectura/
                └── DesacoplamientoArquitecturaTest.java # Test que garantiza que model no importa javafx.*
```

---

## 3. Matriz de Trazabilidad: Requisitos Funcionales vs. Componentes

| Requisito Funcional | Componentes del Modelo (`model`) | Componentes de la Vista (`view`) | Controlador Responsable |
| :--- | :--- | :--- | :--- |
| **RF-01: Configuración de $\Sigma$** | `Alfabeto` | `DialogoAlfabeto`, `VistaPestanaAutomata` | `ControladorPestana` |
| **RF-02: Lienzo y Drag & Drop** | `Automata`, `Estado` | `LienzoGrafo`, `NodoEstadoVista` | `ControladorLienzo` |
| **RF-03: Estado Inicial y Final** | `Automata`, `Estado` | `NodoEstadoVista` (doble anillo, flecha $q_0$) | `ControladorLienzo` |
| **RF-04: Transiciones Asistidas** | `Transicion`, `Automata` | `AristaTransicionVista`, `SelectorSimbolosDialog` | `ControladorLienzo` |
| **RF-05: Detección AFD vs. AFN** | `Automata`, `TipoAutomata` | `VistaPestanaAutomata` (badge indicador) | `ControladorPestana` |
| **RF-06: Comprobación Inmediata** | `SimuladorAutomata` | `PanelComprobacionVista` | `ControladorSimulacion` |
| **RF-07: Conversión y Subconjuntos**| `ConvertidorSubconjuntos`, `ResultadoConversion` | `TablaTransicionesVista`, `DisposicionCircular` | `ControladorPestana` |
| **RF-08: Gestión Multi-Lienzo** | Instancias aisladas de `Automata` | `VistaPrincipal` (`TabPane`), `SplitPane` | `ControladorPrincipal` |

---

## 4. Decisiones Técnicas Justificadas y Alternativas Descartadas

### Decisión 1: Modelado Formal Unificado con Clasificación Dinámica (Cubre RF-01 a RF-05)
- **Solución elegida**: Una única clase matemática `Automata` que gestiona $Q$, $\Sigma$, $q_0$, $F$ y $\delta$. El método `obtenerTipo()` calcula dinámicamente si el autómata es `SIN_ESTADOS`, `AFD` o `AFN` analizando si existe un par $(q, a)$ con $|\delta(q, a)| \ge 2$.
- **Justificación**: En el lienzo, un autómata muta continuamente mientras el usuario agrega aristas; modelar una jerarquía rígida obligaría a recrear objetos o mutar la clase del autómata en cada clic.
- **Alternativa descartada**: *Herencia con clases separadas `AutomataFinitoDeterminista extends Automata` y `AutomataFinitoNoDeterminista extends Automata`*. Se descartó porque el usuario no sabe a priori si su dibujo terminará siendo determinista y la conversión de tipos en tiempo de edición generaría código acoplado y frágil.

### Decisión 2: Clave Inmutable de Subconjuntos mediante `Set<Estado>` en Determinización (Cubre RF-07)
- **Solución elegida**: Durante la construcción de subconjuntos, cada macro-estado compuesto se indexa formalmente mediante un `Set<Estado>` inmutable (`Collections.unmodifiableSet(new TreeSet<>(...))`), mapeado a una entidad `Estado` con nombre $k_i$.
- **Justificación**: Garantiza unicidad matemática estricta basada en igualdad de conjuntos (independiente del orden de inserción de los estados) y calcula la aceptación $k_i \in F_{AFD} \iff k_i \cap F_{AFN} \neq \emptyset$ mediante una operación directa de intersección `!Collections.disjoint(subconjunto, F_afn)`.
- **Alternativa descartada**: *Representación de estados compuestos mediante concatenación de nombres en Strings (ej. `"q0_q1"`)*. Se descartó por ser propensa a fallos si el orden varía (`"q1_q0"` vs `"q0_q1"`) y por carecer de rigor matemático.

### Decisión 3: Renderizado de Grafos con Nodos Nativos de JavaFX en Scene Graph (Cubre RF-02, RF-04, Constitución 1)
- **Solución elegida**: Renderizado basado en nodos del árbol de escena nativo de JavaFX: `Group`, `Circle`, `Path` y `QuadCurve` contenidos en un `Pane` dentro de un `ScrollPane`.
- **Justificación**: Proporciona soporte inherente para eventos del ratón (Drag & Drop, hover, clic) en cada elemento individual, redibujo automático acelerado por hardware y enlaces de propiedades (`DoubleProperty.bind()`) para que las aristas sigan a los nodos al ser arrastrados.
- **Alternativa descartada**: *Dibujo manual en un `Canvas` mediante `GraphicsContext2D`*. Se descartó porque requeriría programar manualmente un motor de detección de colisiones, cálculo de hit-boxes y repintado completo del lienzo en cada micro-movimiento del cursor.

### Decisión 4: Aristas Curvadas Paramétricas para Transiciones Opuestas y Autolazos (Cubre RF-04, RNF-04)
- **Solución elegida**:
  - Para transiciones unidireccionales $q_A \to q_B$: línea directa con cabeza de flecha poligonal.
  - Para transiciones bidireccionales ($q_A \to q_B$ y $q_B \to q_A$ simultáneas): `QuadCurve` con punto de control desviado ortogonalmente mediante cálculo vectorial respecto al vector director de la arista.
  - Para autolazos ($q_A \to q_A$): `CubicCurve` nativa trazada en el cuadrante superior del nodo.
  - Transiciones múltiples con mismo origen y destino unificadas en un único texto separado por comas (ej. `"a, b"`).
- **Justificación**: Resuelve de manera limpia el problema de superposición de flechas sin recurrir a ninguna biblioteca externa, cumpliendo el Principio 1 de la Constitución.
- **Alternativa descartada**: *Líneas rectas rígidas con desplazamiento estático de coordenadas*. Se descartó porque las etiquetas numéricas y las cabezas de flecha colisionan visualmente al cambiar el ángulo de los estados.

### Decisión 5: Selector Asistido Modal para Asociación de Símbolos (Cubre RF-04)
- **Solución elegida**: Componente `SelectorSimbolosDialog` que extiende `Dialog<List<Character>>`, mostrando una lista de casillas de verificación (`CheckBox`) generadas dinámicamente a partir de los símbolos de $\Sigma$. Si se presiona cancelar o escape, retorna lista vacía y la arista se descarta.
- **Justificación**: Elimina al 100% la posibilidad de que el usuario introduzca símbolos ajenos a $\Sigma$, caracteres vacíos ($\varepsilon$) o errores tipográficos.
- **Alternativa descartada**: *Cuadro de texto libre con `TextField`*. Se descartó porque obligaría a parsear y validar en línea, mostrando mensajes de error constantes y degradando la fluidez del diseño.

### Decisión 6: Algoritmo de Disposición Circular Uniforme Nativo con Centro para N=1 (Cubre RF-07, Constitución 1)
- **Solución elegida**: Clase `DisposicionCircular` que calcula las posiciones espaciales $(x_i, y_i)$ de los $N$ estados compuestos del AFD:
  - Si $N = 1$: nodo ubicado en $(X_{centro}, Y_{centro})$.
  - Si $N > 1$: $x_i = X_{centro} + R \cdot \cos\left(\frac{2\pi i}{N} - \frac{\pi}{2}\right)$, $y_i = Y_{centro} + R \cdot \sin\left(\frac{2\pi i}{N} - \frac{\pi}{2}\right)$, con radio $R$ escalable según $N$.
- **Justificación**: Algoritmo determinista, O(N), extremadamente liviano y sin ninguna dependencia externa.
- **Alternativa descartada**: *Disposición aleatoria (Random Layout) o en Rejilla fija*. Se descartó porque produce superposición de aristas y un grafo ilegible.

### Decisión 7: Paleta Neutra con Acentos Permitidos (Cubre RNF-06, Constitución 7)
- **Solución elegida**: Definición centralizada en `PaletaColor.java` y `estilos.css`:
  - Fondos y paneles: Tonos grafito oscuro y gris perla (`#1E1E24`, `#2B2D42`, `#F4F4F6`, `#FFFFFF`).
  - Nodos regulares: Fondo blanco/neutro con borde grafito `#4A4E69`.
  - Nodos de aceptación: Doble anillo en color **verde esmeralda** (`#10B981`).
  - Estado activo / selección / hover: **Violeta neutro** (`#8B5CF6`).
  - Advertencias / Clasificación AFN: **Ámbar / Naranja** (`#F59E0B`).
  - Texto y etiquetas: Negro carbón (`#111827`) y blanco puro.
  - **RESTRICCIÓN ABSOLUTA**: 0% de componentes cromáticos en rango azul (HSL 190°–250°) y café/marrón (HSL 25°–45° oscuros).
- **Justificación**: Cumplimiento riguroso del Principio 7 de la Constitución, garantizando estética moderna y contraste profesional.
- **Alternativa descartada**: *Usar el tema predeterminado Modena de JavaFX*. Se descartó porque utiliza el color azul cielo nativo en botones, barras de scroll y focus rings.

### Decisión 8: Tabla de Transiciones Reactiva Mediante `TableView` Nativa (Cubre RF-08)
- **Solución elegida**: Componente `TablaTransicionesVista` basado en `TableView<FilaTransicion>`, con columnas generadas dinámicamente a partir de $\Sigma$ y poblada en cada ciclo de actualización mediante eventos disparados por el controlador.
- **Justificación**: Desacopla la vista de la matriz matemática interna y permite mostrar de forma nativa los conjuntos `{q1, q2}`, el símbolo $\emptyset$ y la columna de subconjuntos en pestañas de conversión.
- **Alternativa descartada**: *Dibujar la tabla en un `GridPane` con `Label` manuales*. Se descartó por falta de soporte para ordenamiento, selección de filas y scroll virtualizado integrado.

---

## 5. Estrategia de Pruebas Automatizadas (JUnit 5)

En cumplimiento de los Principios 4 y 6 de la Constitución, todo el código del modelo y los algoritmos matemáticos contarán con cobertura unitaria exhaustiva sin dependencias de entorno gráfico (pruebas 100% headless que ejecutan en milisegundos con `mvn test`).

### 5.1. Matriz de Pruebas Unitarias del Modelo

#### A. Alfabeto (`AlfabetoTest`) — Cubre RF-01
- `testCreacionValidaConLetrasUnicas()`: Alfabeto con `'a'`, `'b'`, `'C'`.
- `testDistingueMayusculasDeMinusculas()`: `'a'` y `'A'` coexisten como símbolos distintos.
- `testRechazaNumerosYOperadores()`: Falla si se intenta ingresar `'0'`, `'1'`, `'+'`.
- `testRechazaEspacioYComa()`: Falla con espacios o comas como símbolos.
- `testRecortaEspaciosAlrededorDeComas()`: `"a ,  b , c"` se interpreta como `{'a', 'b', 'c'}`.
- `testBloqueoConTransicionesActivas()`: Falla al intentar modificar si el autómata tiene transiciones.

#### B. Estados y Manipulación (`EstadoTest`, `AutomataTest`) — Cubre RF-02, RF-03, RF-05
- `testAsignacionSecuencialMenorIndiceLibre()`: Creación de $q_0, q_1, q_2$; borrado de $q_1$; el siguiente es $q_1$.
- `testRenombradoValidoYUnicidad()`: Renombrar $q_0$ a `inicio` es válido; colisión con nombre existente lanza excepción.
- `testRechazoNombreVacioOBlancos()`: Renombrar con `""` o `"   "` lanza excepción.
- `testLimiteLongitudNombre()`: Nombres con más de 8 caracteres son rechazados.
- `testUnicoEstadoInicial()`: Marcar $q_1$ como inicial desmarca automáticamente $q_0$.
- `testEliminacionEstadoInicialDejaAutomataSinInicial()`: Al borrar el nodo inicial, `getInicial()` retorna `Optional.empty()`.
- `testBorradoEnCascadaDeTransiciones()`: Borrar $q_1$ elimina todas las transiciones incidentes y salientes.
- `testClasificacionSinEstados()`: Autómata con 0 estados reporta `TipoAutomata.SIN_ESTADOS`.
- `testClasificacionAFD()`: Grafo sin salidas múltiples para el mismo símbolo reporta `TipoAutomata.AFD`.
- `testClasificacionAFN()`: Estado con 2 salidas para `'a'` reporta `TipoAutomata.AFN`.

#### C. Simulación de Cadenas (`SimuladorAutomataTest`) — Cubre RF-06
- `testCadenaVaciaAceptadaSiInicialEsFinal()`: $\lambda$ sobre $q_0 \in F \implies$ Aceptada.
- `testCadenaVaciaRechazadaSiInicialNoEsFinal()`: $\lambda$ sobre $q_0 \notin F \implies$ Rechazada.
- `testSimulacionAFDExito()`: Cadena que finaliza en estado de aceptación $\implies$ Aceptada.
- `testSimulacionAFDRechazoCaminoMuerto()`: Transición inexistente $\implies$ Rechazo inmediato.
- `testSimulacionAFNMultiplesCaminos()`: Aceptada si al menos una rama llega a aceptación tras consumir la cadena.
- `testSimulacionAFNTodasLasRamasMueren()`: Todas las ramas bloqueadas $\implies$ Rechazada.
- `testRechazoCaracteresAjenosAlAlfabeto()`: Cadena con símbolo $\notin \Sigma$ aborta con excepción.
- `testAbortoSinEstadoInicial()`: Simular sin inicial lanza `IllegalStateException`.
- `testRecorteAutomaticoTrim()`: `"  ab  "` se evalúa idéntico a `"ab"`.

#### D. Conversión por Subconjuntos (`ConvertidorSubconjuntosTest`) — Cubre RF-07
- `testPrecedenciaValidaEstadoInicial()`: Conversión sin inicial lanza excepción instructiva.
- `testPrecedenciaRechazaSiYaEsAFD()`: Conversión sobre AFD lanza excepción informativa.
- `testNomenclaturaK()`: Los estados del AFD generado se nombran estrictamente $k_0, k_1, \dots$.
- `testK0EsEstadoInicialAutomatico()`: $k_0$ tiene siempre condición de inicial en el AFD resultante.
- `testAceptacionSubconjuntoInterseccion()`: $k_i$ es final $\iff k_i \cap F_{AFN} \neq \emptyset$.
- `testPodaEstadosInalcanzables()`: Estados del AFN inalcanzables desde $q_0$ no generan estados en el AFD.
- `testK0PreservadoSinAristasEntrantes()`: $k_0$ se conserva siempre aunque carezca de transiciones de entrada.
- `testAFDGeneradoEsDeterministaPuro()`: Todo estado del AFD tiene a lo sumo 1 transición por símbolo de $\Sigma$.
- `testTablaSubconjuntosRegistraVacio()`: Transiciones no definidas se registran con $\emptyset$.
- `testCasoLimiteN1()`: AFN que genera un único estado $k_0$ sin transiciones se determiniza limpiamente.

#### E. Test de Arquitectura y Pureza del Modelo (`DesacoplamientoArquitecturaTest`) — Cubre Constitución 3
- `testPaqueteModelNoImportaJavaFX()`: Test automatizado mediante reflexión y escaneo de bytecode que verifica que ninguna clase bajo `com.automatas.model` importe clases pertenecientes a `javafx.*`.

---

## 6. Fases de Implementación y Criterios de Validación

1. **Fase 1: Modelo Matemático Puro y Suite de Tests Unitarios**
   - Implementar `Alfabeto`, `Estado`, `Transicion`, `Automata`, `SimuladorAutomata` y `ConvertidorSubconjuntos`.
   - Ejecutar y validar que el 100% de los tests en JUnit 5 pasen en verde con `mvn test`.
2. **Fase 2: Infraestructura Gráfica Nativa (JavaFX)**
   - Construir `NodoEstadoVista`, `AristaTransicionVista`, `AutolazoVista` y `SelectorSimbolosDialog`.
   - Implementar `LienzoGrafo` con soporte de arrastre, anclaje reactivo de aristas y scroll dinámico.
   - Aplicar la paleta de colores (`PaletaColor` / `estilos.css`) verificando ausencia total de azul y café.
3. **Fase 3: Paneles de Control y Tabla de Transiciones**
   - Construir `TablaTransicionesVista` con SplitPane colapsable.
   - Integrar el panel de comprobación inmediata de cadenas con veredicto binario.
4. **Fase 4: Orquestación Multi-Pestaña y Conversión AFN $\to$ AFD**
   - Conectar el algoritmo de conversión con la creación de pestañas rotuladas (`AFD (de Autómata X)`).
   - Implementar el algoritmo de layout circular y el modo de inspección con topología protegida.
   - Implementar la prevención de pérdida de datos al cerrar pestañas y la auto-creación de `Autómata 1`.
5. **Fase 5: Verificación Final y Criterio de Entrega (DoD)**
   - Ejecución de `mvn clean test` sin advertencias.
   - Verificación de cumplimiento de los 11 criterios del Definition of Done de [specs/001-Automatas/spec.md](file:///c:/Users/JeisonPc/Documents/Univerisdad/Compiladores/Automatas/specs/001-Automatas/spec.md).
