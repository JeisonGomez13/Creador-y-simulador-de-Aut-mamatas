# Especificación Funcional: Diseño, Simulación y Conversión de Autómatas (AFD / AFN)

## 1. Contexto y Objetivo
El aprendizaje y aplicación práctica de la teoría de autómatas formales en ciencias de la computación requiere herramientas interactivas que permitan materializar conceptos abstractos. El objetivo de este sistema es proporcionar un entorno visual e intuitivo para el diseño interactivo (arrastrar y soltar), la comprobación determinista/no determinista de cadenas y la transformación formal de Autómatas Finitos No Deterministas (AFN) a Autómatas Finitos Deterministas (AFD) mediante el algoritmo de construcción de subconjuntos, garantizando rigor formal sin admitir transiciones vacías.

---

## 2. Usuarios
- **Estudiantes de Computación / Compiladores**: Diseñan autómatas, validan cadenas de prueba para verificar lenguajes formales y contrastan el proceso de determinización con sus ejercicios teóricos.
- **Docentes y Evaluadores**: Crean ejemplos visuales para demostraciones conceptuales y validan rápidamente equivalencias formales y tablas de transición de subconjuntos.

---

## 3. Historias de Usuario

- **HU-01**: *Como usuario, quiero definir el alfabeto formal exclusivamente con letras del abecedario antes de dibujar mi autómata para evitar el ingreso de caracteres inválidos.*
- **HU-02**: *Como usuario, quiero diseñar el grafo de estados y transiciones en un lienzo interactivo con soporte de navegación, barras de desplazamiento y expansión dinámica mediante arrastrar y soltar para crear estructuras visuales de cualquier tamaño sin restricciones de espacio.*
- **HU-03**: *Como usuario, quiero renombrar estados (hasta 8 caracteres válidos sin espacios en blanco) y contar con identificadores automáticos sin colisiones para mantener un diagrama ordenado.*
- **HU-04**: *Como usuario, quiero designar explícitamente el estado inicial y los estados de aceptación en lienzos de diseño manual para completar la definición formal de mi autómata.*
- **HU-05**: *Como usuario, quiero seleccionar símbolos de transición a partir de una lista asistida de mi alfabeto activo, con la posibilidad de cancelar la acción limpiamente sin dejar aristas huérfanas.*
- **HU-06**: *Como usuario, quiero trazar autolazos fácilmente, unificar múltiples símbolos en una sola flecha dirigida y visualizar arcos curvados en transiciones bidireccionales para evitar que las líneas y etiquetas se superpongan.*
- **HU-07**: *Como usuario, quiero ver cómo la tabla de transiciones se actualiza en tiempo real en un panel dividido conforme dibujo en el lienzo, visualizando conjuntos de destinos entre llaves en caso de no determinismo.*
- **HU-08**: *Como usuario, quiero que el sistema me informe automáticamente si el autómata dibujado es un AFD o un AFN (o «Sin estados» si el lienzo está vacío) para saber si requiere conversión previa a su análisis determinista.*
- **HU-09**: *Como usuario, quiero ingresar cadenas de texto y recibir un veredicto binario inmediato (Aceptada / Rechazada) con recorte de espacios para verificar la validez del lenguaje reconocido.*
- **HU-10**: *Como usuario, quiero convertir un AFN a su AFD equivalente para inspeccionarlo, reacomodar sus nodos visualmente y comprobar cadenas sobre él junto a su tabla de subconjuntos fidedigna sin alterar el diseño original.*
- **HU-11**: *Como usuario, quiero gestionar múltiples pestañas con nombres correlativos claros y prevención de cierre accidental para trabajar con varios autómatas simultáneamente.*

---

## 4. Requisitos Funcionales (RF)

### RF-01: Configuración del Alfabeto Formal ($\Sigma$)
El sistema debe permitir al usuario especificar el conjunto finito de símbolos válidos que conforman el alfabeto antes de iniciar el trazado de transiciones en el lienzo activo.

- **Criterios de Aceptación (EARS)**:
  - **Ubiquitous**: El sistema debe exigir una lista de símbolos alfabéticos únicos separados por comas (ej. `a, b, C`), admitiendo estrictamente letras del abecedario en minúsculas y mayúsculas (`a-z`, `A-Z`).
  - **Ubiquitous**: El sistema debe distinguir estrictamente entre mayúsculas y minúsculas (`a` $\neq$ `A`).
  - **Ubiquitous**: El sistema debe rechazar números (`0-9`), operadores aritméticos, caracteres de control y caracteres especiales como símbolos del alfabeto formal.
  - **Ubiquitous**: El sistema debe utilizar el carácter coma (`,`) exclusivamente como separador sintáctico, ignorar los espacios en blanco alrededor de las comas y prohibir el espacio en blanco como símbolo del alfabeto formal.
  - **Event-driven**: Cuando el usuario confirme la definición del alfabeto, el sistema debe habilitar las herramientas de dibujo de transiciones en el lienzo activo.
  - **State-driven**: Mientras existan transiciones creadas en el lienzo activo, el sistema debe bloquear la edición (adición o eliminación) de símbolos del alfabeto $\Sigma$.
  - **Unwanted behavior**: Si el usuario intenta modificar el alfabeto mientras existan transiciones en el lienzo, entonces el sistema debe mostrar un mensaje advirtiendo que debe eliminar las transiciones existentes antes de reconfigurar el alfabeto.
  - **Unwanted behavior**: Si el usuario ingresa caracteres no alfabéticos, símbolos duplicados o cadenas multicarácter como símbolo, entonces el sistema debe rechazar la entrada e informar el formato correcto (letras individuales de la `a` a la `z` o `A` a la `Z`).

### RF-02: Edición Visual y Manipulación del Grafo (Lienzo / Drag and Drop)
El sistema debe proveer un lienzo interactivo para la creación, reposicionamiento, renombrado y eliminación de estados y transiciones, soportando navegación y desplazamientos con límites dinámicos.

- **Criterios de Aceptación (EARS)**:
  - **Ubiquitous**: El sistema debe contener el lienzo de dibujo dentro de un panel con barras de desplazamiento (scroll) horizontal y vertical.
  - **Event-driven**: Cuando el usuario arrastre un estado o elemento hacia los bordes del área visible, el sistema debe expandir dinámicamente las dimensiones del lienzo virtual para adaptar el rango de desplazamiento del scroll sin topar con límites artificiales.
  - **Event-driven**: Cuando el usuario seleccione la acción de agregar estado y haga clic o arrastre al lienzo, el sistema debe crear un nuevo nodo visual con un identificador automático asignado al menor índice entero libre disponible ($q_0, q_1, q_2, \dots$).
  - **Event-driven**: Cuando el usuario solicite renombrar un estado existente, el sistema debe permitir modificar su identificador textual.
  - **Event-driven**: Cuando el usuario arrastre un estado por el lienzo, el sistema debe actualizar su posición en tiempo real manteniendo ancladas sus transiciones incidentes y salientes.
  - **Event-driven**: Cuando el usuario seleccione un estado o transición y ejecute la acción de eliminación, el sistema debe remover el elemento del modelo y del lienzo visual.
  - **State-driven**: Mientras un estado sea eliminado, el sistema debe eliminar en cascada todas las transiciones que tengan a dicho estado como origen o destino.
  - **State-driven**: Mientras el usuario requiera modificar los símbolos de una transición ya trazada, el sistema debe exigir la eliminación de la transición existente y la creación de una nueva con los símbolos deseados (sin edición en línea de aristas).
  - **Event-driven**: Cuando el usuario elimine el nodo marcado como estado inicial, el sistema debe dejar el autómata temporalmente sin estado inicial definido hasta que el usuario marque uno nuevo.
  - **Unwanted behavior**: Si el usuario intenta asignar un nombre vacío, compuesto únicamente por espacios en blanco, con longitud mayor a 8 caracteres tras aplicar recorte (`trim()`), con saltos de línea o que ya pertenezca a otro estado del mismo autómata, entonces el sistema debe rechazar la modificación, notificar la razón y preservar el nombre anterior.

### RF-03: Definición de Estados Inicial y de Aceptación
El sistema debe permitir designar la condición inicial y final de cada estado en el autómata de forma explícita en lienzos manuales.

- **Criterios de Aceptación (EARS)**:
  - **Ubiquitous**: El sistema debe permitir a lo sumo un único estado inicial en el autómata durante su edición ($|Q_{inicial}| \le 1$).
  - **Ubiquitous**: En los lienzos creados desde cero, el sistema no debe designar automáticamente el primer estado creado como estado inicial, requiriendo que el usuario lo establezca de manera explícita.
  - **Ubiquitous**: El sistema debe requerir estrictamente la existencia de exactamente un estado inicial para poder ejecutar la simulación de cadenas o la conversión a AFD.
  - **Event-driven**: Cuando el usuario marque un estado como inicial, el sistema debe desmarcar automáticamente cualquier estado inicial previo y agregar el indicador visual de entrada (flecha inicial).
  - **Event-driven**: Cuando el usuario marque o desmarque un estado como de aceptación (final), el sistema debe actualizar su representación visual (doble círculo concéntrico).
  - **Optional**: Donde existan múltiples estados de aceptación, el sistema debe mantenerlos simultáneamente en el conjunto $F \subseteq Q$.

### RF-04: Creación y Validación de Transiciones Formales
El sistema debe permitir conectar pares de estados (o un estado consigo mismo en lazo) asignándoles uno o más símbolos de transición mediante selección asistida y unificación visual.

- **Criterios de Aceptación (EARS)**:
  - **Event-driven**: Cuando el usuario inicie una conexión arrastrando desde un nodo origen hacia un nodo destino (o sobre el mismo nodo origen para crear un autolazo), el sistema debe presentar un selector interactivo con los símbolos pertenecientes al alfabeto $\Sigma$ activo.
  - **Event-driven**: Cuando el usuario confirme la selección de uno o varios símbolos en dicho selector, el sistema debe crear la transición y rotularla en el lienzo.
  - **Event-driven**: Cuando el usuario cancele el selector, cierre el diálogo o no seleccione ningún símbolo, el sistema debe abortar el trazado de inmediato sin generar ninguna arista visual ni alterar el modelo formal.
  - **Ubiquitous**: Cuando existan múltiples transiciones dirigidas entre el mismo par ordenado de estados ($q_A \to q_B$), el sistema debe unificarlas visualmente en una **única flecha dirigida** rotulada con la lista de símbolos separados por comas (ej. `"a, b"`), evitando la duplicidad de líneas en el mismo sentido.
  - **Ubiquitous**: Cuando existan transiciones dirigidas en ambos sentidos entre dos estados ($q_A \to q_B$ y $q_B \to q_A$), el sistema debe trazarlas mediante arcos curvados opuestos para garantizar la legibilidad y evitar la superposición visual de flechas y rótulos.
  - **Unwanted behavior**: Si el usuario intenta crear una transición con un símbolo que ya existe entre el mismo par ordenado de estados (mismo origen y mismo destino), entonces el sistema debe ignorar la acción redundante para evitar transiciones duplicadas.

### RF-05: Detección Automática de Tipo de Autómata (AFD vs. AFN)
El sistema debe analizar de forma continua las transiciones y clasificar el autómata según las reglas formales de determinismo.

- **Criterios de Aceptación (EARS)**:
  - **State-driven**: Mientras el lienzo activo contenga 0 estados, el sistema debe exhibir el indicador de clasificación en un estado neutro (**«Sin estados»**).
  - **State-driven**: Mientras el lienzo contenga al menos 1 estado, el sistema debe clasificar el autómata como **AFN** si y solo si existe al menos un estado que posea dos o más transiciones salientes asociadas al mismo símbolo del alfabeto.
  - **State-driven**: Mientras el lienzo contenga al menos 1 estado, el sistema debe clasificar el autómata como **AFD** si ningún estado posee salidas múltiples para el mismo símbolo (los autómatas con transiciones parciales o incompletas sin salidas repetidas se consideran deterministas).
  - **Event-driven**: Cuando ocurra cualquier adición, modificación o eliminación de estados o transiciones, el sistema debe reevaluar y actualizar visiblemente en la interfaz la etiqueta de estado del autómata (`«Sin estados»`, `«AFD»` o `«AFN»`).

### RF-06: Comprobación Inmediata de Cadenas (Simulación)
El sistema debe evaluar cadenas de entrada y determinar de forma inmediata si son aceptadas o rechazadas por el autómata.

- **Criterios de Aceptación (EARS)**:
  - **Event-driven**: Cuando el usuario ingrese una cadena y active la acción de comprobación, el sistema debe aplicar automáticamente recorte de espacios en blanco en los extremos (`trim()`) y emitir un veredicto binario estricto de **«Aceptada»** o **«Rechazada»**.
  - **Event-driven**: Cuando la cadena resultante sea la cadena vacía ($\lambda$ o campo en blanco tras recortar), el sistema debe aceptarla si y solo si el estado inicial está marcado como estado de aceptación.
  - **Unwanted behavior**: Si la cadena contiene caracteres que no forman parte del alfabeto $\Sigma$, entonces el sistema debe rechazar la comprobación antes de evaluar, señalando los caracteres inválidos.
  - **Unwanted behavior**: Si el autómata carece de un estado inicial definido al solicitar la comprobación, entonces el sistema debe abortar la evaluación e indicar al usuario que debe definir un estado inicial.
  - **State-driven**: Mientras se evalúe una cadena sobre un autómata incompleto (ausencia de transición para el símbolo actual), el sistema debe considerar ese camino de ejecución como muerto y determinar rechazo inmediato para dicha rama.
  - **State-driven**: Mientras se evalúe sobre un AFN, el sistema debe considerar la cadena aceptada si al menos una de las ramas de ejecución culmina en un estado de aceptación tras consumir la totalidad de la cadena.

### RF-07: Conversión de AFN a AFD, Precedencia, Tabla de Subconjuntos y Layout
El sistema debe implementar el algoritmo formal de construcción de subconjuntos para transformar cualquier AFN en un AFD equivalente, organizando su presentación visual en modo de inspección interactiva.

- **Criterios de Aceptación (EARS)**:
  - **Event-driven**: Cuando el usuario solicite la conversión de un autómata, el sistema debe ejecutar obligatoriamente el siguiente orden de validaciones previas:
    1. *Validación de estado inicial*: Si el autómata carece de estado inicial definido, el sistema debe abortar la operación notificando: *«Debe designar un estado inicial antes de convertir»*.
    2. *Validación de clasificación*: Si el autómata clasifica como AFD, el sistema debe abortar la operación notificando: *«El autómata ya es determinista y no requiere conversión»*.
  - **Event-driven**: Cuando las validaciones previas sean superadas, el sistema debe ejecutar el algoritmo formal de construcción de subconjuntos.
  - **Ubiquitous**: El sistema debe asignar nombres secuenciales a los estados del AFD generado utilizando la letra `k` ($k_0, k_1, k_2, \dots$).
  - **Ubiquitous**: El estado compuesto $k_0$ debe nacer automáticamente designado como el estado inicial del AFD generado (con su flecha de ingreso visible).
  - **Ubiquitous**: Un estado compuesto $k_i$ debe ser marcado como estado de aceptación en el AFD si y solo si contiene al menos un estado de aceptación del AFN original ($k_i \cap F_{AFN} \neq \emptyset$).
  - **Ubiquitous**: El sistema debe incluir en el AFD generado únicamente los estados alcanzables a través de caminos dirigidos a partir del estado inicial compuesto $k_0$, preservando siempre a $k_0$ con independencia de si tiene o no transiciones entrantes.
  - **Event-driven**: Cuando la conversión concluya con éxito, el sistema debe generar una nueva pestaña conteniendo el AFD resultante y su tabla de subconjuntos asociada, dejando intacto el AFN original.
  - **Ubiquitous**: En el lienzo del nuevo AFD generado, el sistema no debe dibujar ningún nodo ni arista para el estado vacío $\emptyset$, manteniendo un grafo limpio con transiciones parciales.
  - **Ubiquitous**: El sistema debe posicionar inicialmente los nodos del nuevo AFD generado mediante una distribución circular uniforme centrada en el lienzo; si el autómata consta de un único estado ($N = 1$), dicho nodo debe situarse directamente en el centro geométrico del lienzo.
  - **State-driven**: Mientras el usuario interactúe con la pestaña del AFD generado, el sistema debe proteger la integridad de la topología resultante configurándola en modo de inspección (permitiendo reacomodar libremente los nodos en el lienzo mediante arrastrar y soltar y comprobar cadenas de prueba, pero impidiendo añadir, eliminar o renombrar estados y aristas).

### RF-08: Gestión Multi-Lienzo y Tablas de Transiciones Vivas (Vistas / Pestañas)
El sistema debe permitir mantener múltiples áreas de trabajo independientes durante la sesión en memoria, manteniendo sincronizada su respectiva tabla de transiciones y resolviendo correlativos de nombres.

- **Criterios de Aceptación (EARS)**:
  - **Ubiquitous**: Todas las pestañas (tanto lienzos creados desde cero como pestañas generadas por conversión) deben incorporar un panel dividido colapsable (SplitPane lateral o inferior) con la **Tabla de Transiciones**, la cual debe recalcularse y reflejar en tiempo real cualquier cambio realizado sobre el grafo del lienzo de diseño.
  - **Ubiquitous**: En la tabla de transiciones de cualquier pestaña, cuando existan múltiples estados destino para un mismo símbolo (comportamiento AFN), el sistema debe exhibirlos formalmente como un conjunto entre llaves (ej. `{q1, q2}`). Si no hay transición definida para un símbolo, debe mostrar el símbolo de vacío ($\emptyset$).
  - **Ubiquitous**: En las pestañas nacidas de conversión, la tabla de transiciones debe exhibir adicionalmente la columna de mapeo inmutable correspondiente a los subconjuntos del AFN original ($k_i \to \{q_x, q_y\}$).
  - **Ubiquitous**: El sistema debe rotular las pestañas de diseño manual como `Autómata 1`, `Autómata 2`, etc.
  - **Event-driven**: Cuando se genere una primera conversión a partir de un autómata fuente (`Autómata X`), el sistema debe rotular la pestaña resultante como `AFD (de Autómata X)`.
  - **Event-driven**: Cuando el usuario solicite conversiones sucesivas a partir de la misma pestaña fuente (`Autómata X`), el sistema debe generar pestañas con correlativo incremental diferenciado (ej. `AFD 2 (de Autómata X)`, `AFD 3 (de Autómata X)`).
  - **Event-driven**: Cuando se genere un AFD a partir de una conversión, la nueva pestaña debe heredar idénticamente el alfabeto $\Sigma$ del AFN original y nacer con dicho alfabeto bloqueado para edición.
  - **Event-driven**: Cuando el usuario solicite crear un nuevo autómata, el sistema debe abrir una pestaña en blanco permitiendo configurar un nuevo alfabeto y diseñar desde cero.
  - **Event-driven**: Cuando el usuario intente cerrar una pestaña que contenga al menos un estado o transición, el sistema debe desplegar un cuadro de confirmación preventiva (*«¿Desea cerrar esta pestaña? Se perderán los datos del autómata en memoria»*).
  - **Event-driven**: Cuando el usuario cierre la última pestaña abierta del editor, el sistema debe generar automáticamente una nueva pestaña en blanco (`Autómata 1`), asegurando que siempre exista un área de trabajo activa.
  - **Event-driven**: Cuando el usuario alterne entre pestañas o vistas, el sistema debe preservar el estado, grafo, alfabeto y configuraciones de cada autómata sin pérdidas de datos en memoria.

---

## 5. Requisitos No Funcionales (RNF)

- **RNF-01 (Idioma de Interfaz)**: Toda la interfaz gráfica, menús, botones, cuadros de diálogo, etiquetas y mensajes de error o confirmación deben presentarse exclusivamente en idioma español.
- **RNF-02 (Tiempo de Respuesta en Comprobación)**: La comprobación inmediata de cadenas de hasta 100 caracteres debe ofrecer resultado al usuario en un tiempo inferior a 200 milisegundos.
- **RNF-03 (Tiempo de Respuesta en Conversión)**: La conversión de subconjuntos para autómatas de hasta 15 estados y alfabetos de hasta 5 símbolos debe completarse y renderizarse en menos de 1 segundo.
- **RNF-04 (Usabilidad, Navegación y Claridad Visual)**: El lienzo debe contar con barras de desplazamiento (scroll) con expansión dinámica para grafos extensos, diferenciar claramente estados iniciales (flecha de ingreso), estados de aceptación (doble círculo concéntrico), autolazos y flechas dirigidas con unificación de etiquetas y arcos curvados en transiciones opuestas con tipografía legible.
- **RNF-05 (Integridad en Memoria)**: Ninguna acción sobre una pestaña secundaria (como el AFD generado) debe alterar o corromper el modelo del autómata original en su propia vista.
- **RNF-06 (Restricción de Paleta de Colores - Constitución Regla 7)**: Queda estrictamente prohibido el uso de tonos de color **azul** o **café/marrón** en cualquier componente de la interfaz gráfica (nodos, aristas, paneles, fondos, bordes o botones). El diseño debe emplear una paleta contrastada basada en tonos neutros (grises, grafito, negro, blanco) y acentos visuales permitidos (verde esmeralda, violeta neutro, ámbar, etc.).

---

## 6. Casos Límite y Manejo de Errores

1. **Cadena Vacía ($\lambda$)**: Evaluada formalmente tras recorte de espacios (`trim()`). Se acepta si y solo si el estado inicial está marcado como de aceptación ($q_{inicial} \in F$).
2. **Autómatas Incompletos (Ausencia de Transiciones)**: Si en cualquier estado no existe una transición para un símbolo de la cadena, la rama termina de inmediato (camino muerto) y conduce al veredicto de «Rechazada».
3. **Intentos de Transición $\varepsilon$**: Imposibilidad absoluta de asignar transiciones vacías; el selector asistido solo ofrece símbolos válidos del alfabeto formal activo.
4. **Estados Aislados / Inalcanzables en Diseño Manual**: Son formalmente válidos en el lienzo original; durante la simulación nunca formarán parte de ningún camino desde el estado inicial.
5. **Transiciones Múltiples en la Misma Dirección**: Transiciones entre el mismo par de nodos en el mismo sentido se unifican en una sola flecha con rótulo separado por comas (ej. `"a, b"`).
6. **Transiciones Bidireccionales Opuestas**: Se trazan visualmente con arcos curvados opuestos para que no se encimen flechas ni etiquetas en sentidos contrarios.
7. **Autolazos Directos**: Se crean arrastrando desde un nodo y soltando sobre sí mismo.
8. **Cancelación en Creación de Transiciones**: Si el usuario cierra el selector asistido o no selecciona ningún símbolo, la acción se cancela limpiamente sin dejar residuos gráficos ni aristas huérfanas.
9. **Inmutabilidad de Aristas Creadas**: Para modificar símbolos de una transición ya trazada, se debe eliminar la arista y crearla nuevamente.
10. **Bloqueo del Alfabeto**: El alfabeto $\Sigma$ queda estrictamente bloqueado para edición tan pronto exista al menos una transición en el lienzo activo. Para redefinirlo, el usuario debe limpiar las transiciones existentes.
11. **Restricción Estricta de $\Sigma$**: Solo se admiten letras del abecedario (`a-z`, `A-Z`). Se rechazan números, operadores y caracteres especiales. La coma es exclusivamente separador sintáctico.
12. **Precedencia en Conversión**:
    1. Si falta estado inicial $\to$ aborta con mensaje instructivo.
    2. Si no es AFN $\to$ aborta con mensaje informativo de que ya es determinista.
13. **Poda de Estados Inaccesibles en Conversión**: Durante la transformación de AFN a AFD, todo estado compuesto que no sea alcanzable a través de caminos dirigidos a partir del estado inicial $k_0$ se descarta. El estado inicial $k_0$ se preserva siempre, incluso si carece de transiciones incidentes.
14. **Regla de Aceptación para $k_i$**: Un estado $k_i$ es final en el AFD si y solo si $k_i \cap F_{AFN} \neq \emptyset$.
15. **Estado Trampa $\emptyset$**: Se refleja en la tabla de transiciones con el símbolo $\emptyset$. No se materializa ningún círculo ni arista en el lienzo visual.
16. **Layout con $N = 1$**: Si el AFD generado tiene un único nodo, se posiciona directamente en el centro geométrico del lienzo.
17. **Asignación y Reutilización de Identificadores**: La generación automática asigna siempre el menor índice entero libre disponible ($q_0, q_1, \dots$). El renombrado aplica `trim()` y limita los nombres a 8 caracteres alfanuméricos sin saltos de línea ni cadenas en blanco.
18. **Autómata sin Estado Inicial**: Si el usuario elimina el nodo inicial, el autómata queda sin inicial. Cualquier intento de comprobación o conversión en este estado es abortado informando al usuario que debe marcar un estado inicial.
19. **Lienzo Vacío (0 estados)**: El indicador de clasificación se muestra en estado neutro (`«Sin estados»`) hasta que se agregue el primer nodo al lienzo.
20. **Topología Protegida del AFD Generado**: La pestaña del AFD generado no admite añadir, eliminar ni renombrar estados/aristas (evitando desincronizaciones con la tabla de subconjuntos), pero permite reacomodar libremente los nodos y comprobar cadenas.
21. **Expansión Dinámica de Scroll**: El lienzo virtual expande sus dimensiones al arrastrar nodos hacia los bordes, adaptando el scroll sin truncar el movimiento.
22. **Conversiones Sucesivas de la Misma Fuente**: Si se convierte repetidamente el mismo autómata, se crean pestañas diferenciadas correlativas (`AFD (de Autómata 1)`, `AFD 2 (de Autómata 1)`, etc.).
23. **Confirmación de Cierre de Pestaña**: Si el lienzo tiene contenido, el sistema solicita confirmación antes de cerrar la pestaña para evitar pérdidas de datos en memoria.
24. **Cierre de Última Pestaña**: Si el usuario cierra la última pestaña, se auto-crea de inmediato una nueva pestaña en blanco (`Autómata 1`).
25. **Transiciones Redundantes Idénticas**: Intentos de agregar una transición con un símbolo que ya une ese mismo origen y destino son ignorados para evitar duplicidades.

---

## 7. Fuera de Alcance

- **Persistencia en Disco**: Guardar y cargar autómatas en archivos (JSON, XML u otros formatos) queda excluido del MVP.
- **Exportación de Gráficos**: Exportar imágenes (PNG, SVG, PDF) queda excluido del MVP.
- **Transiciones Vacías ($\varepsilon$-transiciones)**: Excluidas formalmente por constitución del proyecto.
- **Simulación Paso a Paso Animada**: Resaltado visual interactivo o animado paso a paso en el lienzo (solo se ofrece comprobación inmediata).
- **Minimización de AFD**: Algoritmos de minimización de estados (Hopcroft / Moore) fuera del alcance inicial.
- **Conversión Inversa o Equivalencias**: Conversión de AFD a expresiones regulares o pruebas de equivalencia entre autómatas.

---

## 8. Criterios de Finalización (Definition of Done)

1. El usuario puede definir el alfabeto formal $\Sigma$ (separado por comas, exclusivamente letras `a-z` y `A-Z`, sensible a mayúsculas/minúsculas, sin espacios) y verificar su bloqueo tras crear transiciones.
2. El usuario puede crear estados con nombre por defecto según el menor índice libre ($q_i$), renombrarlos con validación de longitud (máx. 8 caracteres), `trim()` y rechazo de blancos/unicidad, moverlos, desplazarse por el lienzo con barras de scroll con expansión dinámica y eliminarlos interactivamente.
3. El usuario puede designar explícitamente el estado inicial en lienzos manuales (máximo uno) y marcar múltiples estados de aceptación.
4. El sistema presenta un selector asistido con los símbolos de $\Sigma$ para crear transiciones y autolazos, cancelando limpiamente si no se eligen símbolos, unificando múltiples símbolos en una sola flecha dirigida (`"a, b"`) y trazando arcos curvados en transiciones opuestas bidireccionales.
5. El sistema muestra y actualiza en tiempo real la tabla de transiciones en un panel dividido (SplitPane) en todas las pestañas de diseño, mostrando conjuntos entre llaves (ej. `{q1, q2}`) para no determinismo y $\emptyset$ para ausencia de transiciones.
6. El sistema clasifica en tiempo real el diseño visual como `AFD` o `AFN`, mostrando `«Sin estados»` mientras el lienzo permanezca vacío.
7. El usuario puede ingresar cadenas (incluida la cadena vacía), recibiendo tras el recorte automático (`trim()`) el veredicto binario estricto de **«Aceptada»** o **«Rechazada»**.
8. Al solicitar la conversión de un AFN, el sistema valida primero la existencia de estado inicial y luego la clasificación no determinista; genera el AFD en una nueva pestaña rotulada con correlativo (`AFD (de Autómata X)`, `AFD 2 (de Autómata X)`) con su estado inicial $k_0$ autoasignado, distribución circular uniforme (o central para $N=1$), poda de estados inalcanzables, criterio formal $k_i \cap F_{AFN} \neq \emptyset$, topología protegida con reacomodación visual permitida, comprobación de cadenas habilitada y tabla de subconjuntos inmutable y fidedigna.
9. El usuario puede gestionar pestañas rotuladas (`Autómata N`), abrir pestañas en blanco, recibir diálogo de confirmación preventiva antes de cerrar una pestaña con contenido y contar con la auto-creación de `Autómata 1` si se cierra la última pestaña.
10. Toda la interfaz gráfica cumple estrictamente con el Principio 7 de la Constitución, sin utilizar tonos de color azul ni café/marrón.
11. Toda la interacción, etiquetas y mensajes del sistema se encuentran exclusivamente en español.

---

## 9. Dudas Abiertas

- No existen dudas abiertas en este momento. Todos los requisitos funcionales, criterios teóricos formales, mecanismos de interacción asistida, convenciones gráficas estándar, restricciones constitucionales de paleta, manejo de estados de la UI y casos límite fueron acordados y formalizados con el usuario.
