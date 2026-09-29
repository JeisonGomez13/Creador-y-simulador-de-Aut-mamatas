# Manual de Usuario — Autómatas (AFD / AFN)

Bienvenido al **Manual de Usuario** de la aplicación de diseño visual, simulación y conversión de **Autómatas Finitos Deterministas (AFD)** y **No Deterministas (AFN)** sin transiciones vacías ($\varepsilon$).

---

## Tabla de Contenido
1. [Introducción y Requisitos del Sistema](#1-introducción-y-requisitos-del-sistema)
2. [Puesta en Marcha y Ejecución](#2-puesta-en-marcha-y-ejecución)
3. [Estructura de la Interfaz Gráfica](#3-estructura-de-la-interfaz-gráfica)
4. [Guía Paso a Paso de Uso](#4-guía-paso-a-paso-de-uso)
   - [Paso 1: Definición del Alfabeto Formal ($\Sigma$)](#paso-1-definición-del-alfabeto-formal-sigma)
   - [Paso 2: Creación y Edición de Estados en el Tablero](#paso-2-creación-y-edición-de-estados-en-el-tablero)
   - [Paso 3: Trazado de Transiciones y Autolazos](#paso-3-trazado-de-transiciones-y-autolazos)
   - [Paso 4: Clasificación Automática (AFD / AFN)](#paso-4-clasificación-automática-afd--afn)
   - [Paso 5: Comprobación de Cadenas y Trazabilidad](#paso-5-comprobación-de-cadenas-y-trazabilidad)
   - [Paso 6: Conversión de AFN a AFD (Construcción de Subconjuntos)](#paso-6-conversión-de-afn-a-afd-construcción-de-subconjuntos)
5. [Gestión Multi-Pestaña y Seguridad](#5-gestión-multi-pestaña-y-seguridad)
6. [Personalización Visual: Modo América](#6-personalización-visual-modo-américa)
7. [Preguntas Frecuentes y Solución de Problemas](#7-preguntas-frecuentes-y-solución-de-problemas)

---

## 1. Introducción y Requisitos del Sistema

Esta herramienta fue desarrollada en **Java 17** y **JavaFX** bajo la metodología **SDD (Specification-Driven Development)** y una arquitectura estricta **MVC**. Proporciona una experiencia fluida e interactiva de arrastrar y soltar (*Drag & Drop*) para el aprendizaje formal y análisis de compiladores y lenguajes formales.

### Requisitos Mínimos:
- **Sistema Operativo:** Windows 10/11, Linux o macOS.
- **Java Runtime:** JDK / JRE 17 o superior.
- **Gestor de Construcción:** Apache Maven 3.8+ (incluido en el repositorio mediante `mvn` o scripts correspondientes).

---

## 2. Puesta en Marcha y Ejecución

Para iniciar la aplicación desde la consola o terminal del proyecto:

```bash
mvn javafx:run
```

Si deseas compilar el archivo empaquetado para distribución:
```bash
mvn clean package
```
El archivo `.jar` resultante se generará en la carpeta `target/automatas-1.0.0-SNAPSHOT.jar`.

---

## 3. Estructura de la Interfaz Gráfica

La ventana principal se divide en 5 áreas funcionales:

```
+-----------------------------------------------------------------------------------+
| [Autómatas (AFD / AFN)]                 [Modo América]  [+ Nuevo Autómata]        | <- Barra Superior Global
+-----------------------------------------------------------------------------------+
| [ Autómata 1 x ] [ Autómata 2 x ]                                                 | <- Pestañas de Trabajo
+-----------------------------------------------------------------------------------+
| Alfabeto (Σ): [ a, b ] [Definir] | [Agregar estado] | Clasificación: [ AFN ] [Convertir a AFD] |
+----------------------------------------------------+------------------------------+
|                                                    | TABLA DE TRANSICIONES        |
|                                                    | Estado |   a    |   b        |
|               TABLERO / LIENZO                     |--------+--------+------------|
|             (Área de Drag & Drop)                  | -> q0  | {q0,q1}|   q0       |
|                                                    |  * q1  |   -    |   -        |
|                                                    |                              |
+----------------------------------------------------+------------------------------+
| Cadena de prueba: [ aab         ] [Comprobar]  «Aceptada»     [Ocultar recorrido] |
| Recorrido de la Cadena: Ruta 1: q0 --(a)--> q1 --(a)--> ...                       |
+-----------------------------------------------------------------------------------+
```

1. **Barra Superior Global:** Contiene el título del software, el conmutador de tema visual **Modo América** y el botón para abrir un **+ Nuevo Autómata**.
2. **Barra de Pestañas (Multi-Lienzo):** Permite trabajar con múltiples autómatas en paralelo de forma aislada e independiente.
3. **Barra de Herramientas de la Pestaña:** Configura el alfabeto, botón rápido de adición de nodos, indicador de tipo y botón de conversión a AFD.
4. **Área Central Dividida:**
   - **Izquierda (Lienzo Virtual Navegable):** Espacio elástico con barras de desplazamiento donde se posicionan y conectan los estados.
   - **Derecha (Tabla de Transiciones Viva):** Matriz matemática que se actualiza automáticamente ante cualquier cambio en el lienzo.
5. **Panel Inferior de Comprobación y Trazabilidad:** Entrada para comprobar cadenas, veredicto inmediato y visualizador detallado paso a paso de cada camino recorrido.

---

## 4. Guía Paso a Paso de Uso

### Paso 1: Definición del Alfabeto Formal ($\Sigma$)
1. Localiza el campo **Alfabeto ($\Sigma$)** en la barra superior del autómata activo.
2. Escribe los símbolos que reconocerá tu autómata separados por comas.  
   *Ejemplo:* `a, b` o `x, y, z`.
3. Haz clic en el botón **Definir** (o presiona `Enter`).
4. **Reglas formales:**
   - Solo se admiten letras del abecedario (`a-z`, `A-Z`).
   - Se distinguen mayúsculas de minúsculas (`a` $\neq$ `A`).
   - Quedan prohibidos números, espacios y caracteres especiales.
   
> [!IMPORTANT]
> Una vez que hayas creado una o más transiciones en el lienzo, el alfabeto quedará bloqueado preventivamente para evitar inconsistencias matemáticas. Si necesitas modificar $\Sigma$, primero elimina las transiciones existentes.

---

### Paso 2: Creación y Edición de Estados en el Tablero

- **Agregar un Estado:**
  - Haz clic en el botón **`Agregar estado`** de la barra de herramientas, o
  - Haz **clic derecho** sobre una zona vacía del lienzo y selecciona **`Agregar Estado`**.
  - Los estados se numeran automáticamente de menor a mayor ($q_0, q_1, q_2, \dots$).
- **Mover Estados (Drag & Drop):**
  - Mantén presionado el botón izquierdo del ratón sobre cualquier estado y arrástralo libremente. Las líneas de transición conectadas se adaptarán dinámicamente en tiempo real.
- **Designar Estado Inicial:**
  - Haz clic derecho sobre el estado deseado y selecciona **`Marcar como Estado Inicial`**. Aparecerá una flecha indicadora a la izquierda del nodo. Solo puede haber un único estado inicial a la vez.
- **Designar Estado de Aceptación (Final):**
  - Haz **doble clic izquierdo** sobre el nodo, o
  - Haz clic derecho y selecciona **`Marcar/Desmarcar Aceptación`**.
  - El estado se dibujará con un doble círculo concéntrico característico de los estados de aceptación.
- **Renombrar Estado:**
  - Haz clic derecho sobre el nodo y elige **`Renombrar`**. Ingresa un nuevo nombre de hasta 8 caracteres sin espacios repetidos.
- **Eliminar Estado:**
  - Haz clic derecho sobre el nodo y selecciona **`Eliminar Estado`**. El nodo y todas sus transiciones incidentes o salientes se borrarán en cascada.

---

### Paso 3: Trazado de Transiciones y Autolazos

1. Haz **clic derecho sobre el nodo origen** y arrastra el ratón. Verás una línea guía discontinua que sigue el puntero.
2. Suelta el clic sobre el **nodo destino** (o sobre el mismo nodo origen si deseas un **autolazo**).
3. Se abrirá automáticamente el **Selector de Símbolos**:
   - Marca las casillas de los símbolos de $\Sigma$ por los cuales transitará.
   - Haz clic en **Confirmar**.
4. **Características inteligentes del trazado:**
   - **Unificación de Símbolos:** Si defines varias transiciones entre el mismo par de nodos, se combinan en una única arista con etiquetas separadas por comas (ej. `"a, b"`).
   - **Curvatura Anticolisión:** Si existen transiciones en ambos sentidos ($q_0 \to q_1$ y $q_1 \to q_0$), ambas aristas se curvan simétricamente en arcos opuestos para evitar solapamiento de flechas.
   - **Autolazos Reflexivos:** Las transiciones sobre el mismo nodo se trazan en el cuadrante superior en forma de lazo cerrado.

---

### Paso 4: Clasificación Automática (AFD / AFN)

El sistema inspecciona en tiempo real la topología del grafo:
- **`«Sin estados»`** (Insignia Gris): Cuando el lienzo no contiene ningún nodo.
- **`«AFD»`** (Insignia Verde): Si cada estado tiene a lo sumo una única transición saliente para cada símbolo de $\Sigma$.
- **`«AFN»`** (Insignia Amarilla): Si se detecta que al menos un estado cuenta con dos o más transiciones con el mismo símbolo (no determinismo).

---

### Paso 5: Comprobación de Cadenas y Trazabilidad

En la barra inferior de la ventana:
1. Escribe la cadena en el campo **Cadena de prueba** (por ejemplo: `abba`, o déjalo vacío para probar la cadena vacía $\lambda$).
2. Presiona el botón **Comprobar** (o la tecla `Enter`).
3. **Veredicto Binario:**
   - **`«Aceptada»`** (Fondo Verde): La cadena pertenece al lenguaje reconocido.
   - **`«Rechazada»`** (Fondo Rojo): La cadena no es aceptada.
4. **Trazabilidad y Recorrido Detallado:**
   - En el panel **Recorrido de la Cadena**, se desglosa la secuencia exacta de estados visitados paso a paso con cada símbolo:
     $$q_0 \xrightarrow{\text{a}} q_1 \xrightarrow{\text{b}} q_2$$
   - **Prioridad de Caminos:** En caso de un AFN con múltiples ramificaciones, **los caminos que conducen a la aceptación se presentan de primero** destacados con borde verde.
   - Puedes ocultar o desplegar este panel con el botón **Ocultar/Ver recorrido**.

---

### Paso 6: Conversión de AFN a AFD (Construcción de Subconjuntos)

Cuando tu autómata esté clasificado como **AFN**:
1. Asegúrate de tener definido el **estado inicial** y al menos un **estado de aceptación**.
2. Haz clic en el botón **`Convertir a AFD`** ubicado en la barra de herramientas superior.
3. El sistema ejecutará el algoritmo formal de **Construcción de Subconjuntos**:
   - Se creará automáticamente una **nueva pestaña** nombrada `"AFD (de Autómata X)"`.
   - Los nuevos estados se ordenarán de forma armónica utilizando una **disposición circular equidistante**.
   - En el panel lateral derecho se desplegará la **Tabla de Subconjuntos**, donde cada fila muestra el nuevo estado determinista ($k_0, k_1, \dots$), el subconjunto de estados del AFN original que agrupa (ej. $\{q_0, q_1\}$) y sus destinos formales ante cada símbolo.
   - Si se genera un estado trampa vacío, se representa de manera formal con el símbolo $\emptyset$.
4. **Modo Protegido:** La pestaña generada es una vista matemática formal inmutable (no permite agregar nodos espurios ni alterar el alfabeto), pero te permite mover sus estados libremente y probar cualquier cadena de entrada.

---

## 5. Gestión Multi-Pestaña y Seguridad

- **Crear Pestaña:** Haz clic en **`+ Nuevo Autómata`** en la esquina superior derecha. Se abrirá una pestaña en blanco rotulada correlativamente (`"Autómata 2"`, `"Autómata 3"`, etc.).
- **Cierre Seguro:** Al presionar la **`X`** de cualquier pestaña que contenga estados o transiciones, el sistema solicitará confirmación previa mediante un diálogo modal para evitar pérdidas accidentales.
- **Regeneración Automática:** Si cierras todas las pestañas abiertas, el sistema regenerará automáticamente una pestaña `"Autómata 1"` limpia en blanco.

---

## 6. Personalización Visual: Modo América

Para una experiencia visual temática inspirada en el **América de Cali**:
1. Presiona el botón **`Modo América`** en la barra superior.
2. **Transformación instantánea:**
   - La interfaz adoptará los colores rojo escarlata (`#C8102E`) y blanco puro (`#FFFFFF`).
   - El tablero desplegará como marca de agua en el fondo el **escudo oficial del América de Cali** con una transparencia calibrada al 22% (`mouseTransparent = true`), permitiendo mover estados y trazar flechas sobre él con total legibilidad.
3. Al volver a pulsar el botón, la aplicación regresará de inmediato a su modo neutro estándar.

---

## 7. Preguntas Frecuentes y Solución de Problemas

### ¿Por qué el botón "Comprobar" no evalúa mi cadena?
Verifica que:
1. Hayas definido el estado inicial (marcado con la flecha de entrada).
2. Todos los caracteres de la cadena ingresada pertenezcan al alfabeto $\Sigma$ activo.

### ¿Por qué no puedo editar el alfabeto?
Si ya creaste transiciones, el sistema bloquea el campo de alfabeto para garantizar la consistencia de los datos. Elimina las transiciones existentes si deseas definir un nuevo conjunto de símbolos.

### ¿Cómo selecciono varios símbolos para una sola transición?
Al soltar la arista sobre el nodo destino, mantén seleccionadas las casillas de verificación de los símbolos deseados en el diálogo emergente antes de presionar **Confirmar**.

### ¿Admite transiciones vacías ($\varepsilon$ o $\lambda$)?
Siguiendo la constitución y especificación formal de la cátedra de compiladores, el sistema opera exclusivamente con **autómatas finitos sin transiciones vacías**.

---

*Manual elaborado bajo la metodología SDD — Autómatas AFD / AFN (2026).*
