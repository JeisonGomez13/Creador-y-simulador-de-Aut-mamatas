package com.automatas.view;

import com.automatas.model.Alfabeto;
import com.automatas.model.CaminoSimulacion;
import com.automatas.model.Estado;
import com.automatas.view.componente.AristaTransicionVista;
import com.automatas.view.componente.AutolazoVista;
import com.automatas.view.componente.NodoEstadoVista;
import com.automatas.view.componente.SelectorSimbolosDialog;
import com.automatas.view.util.PaletaColor;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contenedor visual navegable para el modelado interactivo y renderizado de grafos de autómatas finitos (RF-02, RF-04).
 * <p>
 * Reglas de diseño y casos límite (RF-02, RF-04, Casos Límite 6, 7 y 21):
 * <ul>
 *   <li><b>Árbol de escena nativo:</b> Extiende {@link ScrollPane} y hospeda un {@link Pane} virtual estructurado en capas
 *       estratificadas (aristas en capa inferior, nodos en capa superior) garantizando legibilidad.</li>
 *   <li><b>Drag & Drop en tiempo real:</b> Permite desplazar estados reactivamente con el ratón manteniendo sus aristas
 *       vinculadas y actualizadas instantáneamente mediante properties.</li>
 *   <li><b>Expansión dinámica (Caso Límite 21):</b> Al arrastrar o posicionar un nodo próximo a los bordes derecho o inferior,
 *       las dimensiones virtuales del lienzo crecen automáticamente extendiendo las barras de desplazamiento.</li>
 *   <li><b>Curvado automático bidireccional (Caso Límite 6):</b> Al conectar estados con transiciones en sentidos opuestos,
 *       ambas aristas se curvan simétricamente sin solapamiento de etiquetas.</li>
 *   <li><b>Autolazos superiores (Caso Límite 7):</b> Transiciones sobre el mismo nodo se modelan mediante arcos reflexivos superiores.</li>
 *   <li><b>Restricción cromática estricta (Constitución Principio 7):</b> 0% de componentes cromáticos en azul o café.</li>
 * </ul>
 */
public class LienzoGrafo extends ScrollPane {

    // =========================================================================
    // 1. CONSTANTES GEOMÉTRICAS Y DE EXPANSIÓN
    // =========================================================================

    public static final double ANCHO_INICIAL = 1200.0;
    public static final double ALTO_INICIAL = 800.0;
    public static final double MARGEN_EXPANSION = 80.0;
    public static final double PASO_EXPANSION = 400.0;

    // =========================================================================
    // 2. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Pane lienzoVirtual;
    private final Group capaFondo;
    private final Group capaAristas;
    private final Group capaNodos;
    private final Group capaInteraccion;
    private final Line lineaGuia;
    private ImageView vistaFondoAmerica;

    private final Map<Estado, NodoEstadoVista> nodosPorEstado;
    private final Map<String, AristaTransicionVista> aristasPorClave;
    private final Map<Estado, AutolazoVista> autolazosPorEstado;

    private double anchoVirtual;
    private double altoVirtual;
    private Alfabeto alfabeto;
    private boolean modoProtegido;

    private SolicitudTransicionListener onSolicitudTransicion;
    private Consumer<Estado> onEstadoSeleccionado;
    private BiConsumer<Double, Double> onCrearEstadoEnPosicion;
    private Consumer<Estado> onSolicitudEliminarEstado;
    private Consumer<Estado> onSolicitudEstablecerInicial;
    private Runnable onSolicitudDesmarcarInicial;
    private Consumer<Estado> onSolicitudAlternarAceptacion;
    private BiConsumer<Estado, String> onSolicitudRenombrarEstado;

    // Variables de seguimiento para interacción de arrastre
    private Estado estadoArrastreOrigen;
    private double posicionInicialRatonX;
    private double posicionInicialRatonY;
    private double posicionInicialNodoX;
    private double posicionInicialNodoY;
    private boolean haArrastradoTransicion;

    // =========================================================================
    // 3. INTERFAZ FUNCIONAL DE SOLICITUD DE TRANSICIÓN
    // =========================================================================

    /**
     * Escuchador de eventos cuando el usuario solicita interactiva o programáticamente conectar dos estados.
     */
    @FunctionalInterface
    public interface SolicitudTransicionListener {
        void onSolicitarTransicion(Estado origen, Estado destino);
    }

    // =========================================================================
    // 4. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye un nuevo lienzo de grafo con dimensiones virtuales iniciales y políticas de desplazamiento.
     */
    public LienzoGrafo() {
        this.anchoVirtual = ANCHO_INICIAL;
        this.altoVirtual = ALTO_INICIAL;
        this.modoProtegido = false;

        this.nodosPorEstado = new LinkedHashMap<>();
        this.aristasPorClave = new LinkedHashMap<>();
        this.autolazosPorEstado = new LinkedHashMap<>();

        // Paso 1: Configurar contenedor virtual interno
        this.lienzoVirtual = new Pane();
        this.lienzoVirtual.setPrefWidth(anchoVirtual);
        this.lienzoVirtual.setPrefHeight(altoVirtual);
        this.lienzoVirtual.setMinWidth(anchoVirtual);
        this.lienzoVirtual.setMinHeight(altoVirtual);
        this.lienzoVirtual.setStyle("-fx-background-color: " + PaletaColor.HEX_FONDO_PRINCIPAL + ";");

        // Paso 2: Configurar capas del árbol de escena
        this.capaFondo = new Group();
        this.capaAristas = new Group();
        this.capaNodos = new Group();
        this.capaInteraccion = new Group();

        inicializarFondoAmerica();

        // Línea guía visual para el trazado de transiciones
        this.lineaGuia = new Line();
        this.lineaGuia.setStroke(PaletaColor.SELECCION);
        this.lineaGuia.setStrokeWidth(2.0);
        this.lineaGuia.getStrokeDashArray().addAll(6.0, 4.0);
        this.lineaGuia.setVisible(false);
        this.capaInteraccion.getChildren().add(lineaGuia);

        // Ensamblar capas en orden visual: fondo abajo, aristas encima, nodos en medio, interacción arriba
        this.lienzoVirtual.getChildren().addAll(capaFondo, capaAristas, capaNodos, capaInteraccion);

        // Paso 3: Configurar ScrollPane contenedor
        setContent(lienzoVirtual);
        setPannable(false);
        setFitToWidth(false);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: " + PaletaColor.HEX_FONDO_PRINCIPAL + "; -fx-border-color: transparent;");

        // Paso 4: Interacción directa sobre el lienzo vacío (RF-02)
        this.lienzoVirtual.setOnMouseClicked(event -> {
            if (modoProtegido) {
                return;
            }
            if (encontrarEstadoEnPunto(event.getX(), event.getY()) == null) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    if (onCrearEstadoEnPosicion != null) {
                        onCrearEstadoEnPosicion.accept(event.getX(), event.getY());
                    }
                    event.consume();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    mostrarMenuContextualLienzo(event.getX(), event.getY(), event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            }
        });
    }

    // =========================================================================
    // 5. GESTIÓN DE ESTADOS (RF-02, CASO LÍMITE 21)
    // =========================================================================

    /**
     * Agrega un estado formal al lienzo posicionándolo en las coordenadas dadas.
     *
     * @param estado Entidad formal {@link Estado}.
     * @param x Coordenada X inicial del borde superior izquierdo.
     * @param y Coordenada Y inicial del borde superior izquierdo.
     * @param inicial {@code true} si es el estado inicial del autómata; {@code false} en caso contrario.
     * @return Vista creada {@link NodoEstadoVista}.
     */
    public NodoEstadoVista agregarEstado(Estado estado, double x, double y, boolean inicial) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }

        NodoEstadoVista nodoVista = new NodoEstadoVista(estado, inicial);
        nodoVista.setLayoutX(x);
        nodoVista.setLayoutY(y);

        configurarInteraccionNodo(nodoVista);

        nodosPorEstado.put(estado, nodoVista);
        capaNodos.getChildren().add(nodoVista);

        verificarYExpandirLienzo(x, y);

        return nodoVista;
    }

    /**
     * Agrega un estado no inicial en las coordenadas dadas.
     *
     * @param estado Entidad formal {@link Estado}.
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @return Vista creada {@link NodoEstadoVista}.
     */
    public NodoEstadoVista agregarEstado(Estado estado, double x, double y) {
        return agregarEstado(estado, x, y, false);
    }

    /**
     * Elimina un estado del lienzo junto con todas sus aristas incidentes y autolazos en cascada.
     *
     * @param estado Estado formal a remover.
     */
    public void eliminarEstado(Estado estado) {
        if (estado == null) {
            return;
        }

        NodoEstadoVista nodoVista = nodosPorEstado.remove(estado);
        if (nodoVista != null) {
            capaNodos.getChildren().remove(nodoVista);
        }

        // Eliminar autolazo si existía
        AutolazoVista autolazo = autolazosPorEstado.remove(estado);
        if (autolazo != null) {
            capaAristas.getChildren().remove(autolazo);
        }

        // Eliminar en cascada todas las aristas conectadas
        List<String> clavesAEliminar = new ArrayList<>();
        for (Map.Entry<String, AristaTransicionVista> entry : aristasPorClave.entrySet()) {
            AristaTransicionVista arista = entry.getValue();
            if (arista.getOrigen().getEstado().equals(estado) || arista.getDestino().getEstado().equals(estado)) {
                clavesAEliminar.add(entry.getKey());
                capaAristas.getChildren().remove(arista);
            }
        }

        for (String clave : clavesAEliminar) {
            aristasPorClave.remove(clave);
        }

        // Si una arista opuesta remanente quedó sin su par, descurvarla
        for (AristaTransicionVista aristaRestante : aristasPorClave.values()) {
            Estado o = aristaRestante.getOrigen().getEstado();
            Estado d = aristaRestante.getDestino().getEstado();
            String claveOpuesta = generarClaveArista(d, o);
            if (!aristasPorClave.containsKey(claveOpuesta) && aristaRestante.esCurvada()) {
                aristaRestante.setCurvada(false);
            }
        }
    }

    /**
     * Actualiza el mapeo interno del lienzo y sus componentes cuando un estado es renombrado (RF-02).
     *
     * @param estado Estado formal renombrado.
     * @param nombreAnterior Identificador previo.
     * @param nuevoNombre Nuevo identificador.
     */
    public void renombrarEstado(Estado estado, String nombreAnterior, String nuevoNombre) {
        if (estado == null || nuevoNombre == null) {
            return;
        }
        String normalizado = nuevoNombre.trim();

        // 1. Reindexar en nodosPorEstado
        NodoEstadoVista nodoEncontrado = null;
        for (Iterator<Map.Entry<Estado, NodoEstadoVista>> it = nodosPorEstado.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Estado, NodoEstadoVista> entry = it.next();
            if (entry.getValue().getEstado() == estado || entry.getKey() == estado) {
                nodoEncontrado = entry.getValue();
                it.remove();
                break;
            }
        }
        if (nodoEncontrado != null) {
            nodosPorEstado.put(estado, nodoEncontrado);
            nodoEncontrado.actualizarVista();
        }

        // 2. Reindexar en autolazosPorEstado
        AutolazoVista lazoEncontrado = null;
        for (Iterator<Map.Entry<Estado, AutolazoVista>> it = autolazosPorEstado.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Estado, AutolazoVista> entry = it.next();
            if (entry.getKey() == estado) {
                lazoEncontrado = entry.getValue();
                it.remove();
                break;
            }
        }
        if (lazoEncontrado != null) {
            autolazosPorEstado.put(estado, lazoEncontrado);
        }

        // 3. Reindexar aristas conectadas
        if (nombreAnterior != null && !nombreAnterior.equals(normalizado)) {
            Map<String, AristaTransicionVista> nuevasAristas = new LinkedHashMap<>();
            for (Map.Entry<String, AristaTransicionVista> entry : aristasPorClave.entrySet()) {
                String clave = entry.getKey();
                String nuevaClave = clave;
                if (clave.startsWith(nombreAnterior + "->")) {
                    nuevaClave = normalizado + "->" + clave.substring((nombreAnterior + "->").length());
                }
                if (nuevaClave.endsWith("->" + nombreAnterior)) {
                    nuevaClave = nuevaClave.substring(0, nuevaClave.length() - nombreAnterior.length()) + normalizado;
                }
                nuevasAristas.put(nuevaClave, entry.getValue());
            }
            aristasPorClave.clear();
            aristasPorClave.putAll(nuevasAristas);
        }
    }

    /**
     * Obtiene la vista gráfica correspondiente a un estado formal si existe en el lienzo.
     *
     * @param estado Estado formal a consultar.
     * @return {@link Optional} con la vista del nodo.
     */
    public Optional<NodoEstadoVista> obtenerNodoVista(Estado estado) {
        if (estado == null) {
            return Optional.empty();
        }
        NodoEstadoVista nodo = nodosPorEstado.get(estado);
        if (nodo != null) {
            return Optional.of(nodo);
        }
        for (NodoEstadoVista n : nodosPorEstado.values()) {
            if (n.getEstado() == estado || n.getEstado().equals(estado)) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    /**
     * Retorna la colección inmutable de vistas de nodos presentes en el lienzo.
     *
     * @return Colección de {@link NodoEstadoVista}.
     */
    public Collection<NodoEstadoVista> getNodosVista() {
        return Collections.unmodifiableCollection(nodosPorEstado.values());
    }

    /**
     * Resalta en el lienzo la secuencia de estados visitados durante la simulación de un camino (RF-06).
     *
     * @param camino Camino formal explorado.
     */
    public void resaltarCamino(CaminoSimulacion camino) {
        limpiarResaltado();
        if (camino == null) {
            return;
        }
        for (Estado estado : camino.getSecuenciaEstados()) {
            obtenerNodoVista(estado).ifPresent(nodo -> nodo.setSeleccionado(true));
        }
    }

    /**
     * Limpia el resaltado de selección de todos los nodos del lienzo.
     */
    public void limpiarResaltado() {
        for (NodoEstadoVista nodo : nodosPorEstado.values()) {
            nodo.setSeleccionado(false);
        }
    }

    // =========================================================================
    // 6. GESTIÓN DE ARISTAS Y AUTOLAZOS (RF-04, CASOS LÍMITE 5, 6 Y 7)
    // =========================================================================

    /**
     * Conecta dos estados distintos mediante una arista dirigida. Si ya existía, unifica los símbolos;
     * si existe una arista en dirección opuesta, ambas se curvan automáticamente.
     *
     * @param origen Estado formal de origen.
     * @param destino Estado formal de destino.
     * @param simbolos Conjunto de símbolos formalmente habilitados.
     * @return Vista de la arista {@link AristaTransicionVista}.
     */
    public AristaTransicionVista conectarEstados(Estado origen, Estado destino, Set<Character> simbolos) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Los estados origen y destino no pueden ser nulos.");
        }
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Para transiciones reflexivas sobre el mismo nodo debe emplearse crearAutolazo().");
        }

        NodoEstadoVista nodoOrigen = nodosPorEstado.get(origen);
        NodoEstadoVista nodoDestino = nodosPorEstado.get(destino);
        if (nodoOrigen == null || nodoDestino == null) {
            throw new IllegalArgumentException("Los estados a conectar deben haber sido agregados previamente al lienzo.");
        }

        String claveDirecta = generarClaveArista(origen, destino);
        AristaTransicionVista aristaExistente = aristasPorClave.get(claveDirecta);

        if (aristaExistente != null) {
            if (simbolos != null) {
                simbolos.forEach(aristaExistente::agregarSimbolo);
            }
            return aristaExistente;
        }

        AristaTransicionVista nuevaArista = new AristaTransicionVista(nodoOrigen, nodoDestino, simbolos);

        // Curvado automático si existe arista opuesta
        String claveOpuesta = generarClaveArista(destino, origen);
        AristaTransicionVista aristaOpuesta = aristasPorClave.get(claveOpuesta);
        if (aristaOpuesta != null) {
            nuevaArista.setCurvada(true);
            aristaOpuesta.setCurvada(true);
        }

        aristasPorClave.put(claveDirecta, nuevaArista);
        capaAristas.getChildren().add(nuevaArista);

        return nuevaArista;
    }

    /**
     * Crea o amplía un autolazo reflexivo en el cuadrante superior de un estado.
     *
     * @param estado Estado formal base.
     * @param simbolos Conjunto de símbolos formalmente habilitados.
     * @return Vista del autolazo {@link AutolazoVista}.
     */
    public AutolazoVista crearAutolazo(Estado estado, Set<Character> simbolos) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del autolazo no puede ser nulo.");
        }

        NodoEstadoVista nodoVista = nodosPorEstado.get(estado);
        if (nodoVista == null) {
            throw new IllegalArgumentException("El estado debe haber sido agregado al lienzo antes de crear un autolazo.");
        }

        AutolazoVista lazoExistente = autolazosPorEstado.get(estado);
        if (lazoExistente != null) {
            if (simbolos != null) {
                simbolos.forEach(lazoExistente::agregarSimbolo);
            }
            return lazoExistente;
        }

        AutolazoVista nuevoLazo = new AutolazoVista(nodoVista, simbolos);
        autolazosPorEstado.put(estado, nuevoLazo);
        capaAristas.getChildren().add(nuevoLazo);

        return nuevoLazo;
    }

    /**
     * Obtiene la vista de la arista dirigida entre dos estados si existe.
     *
     * @param origen Estado formal origen.
     * @param destino Estado formal destino.
     * @return {@link Optional} con la arista encontrada.
     */
    public Optional<AristaTransicionVista> obtenerAristaVista(Estado origen, Estado destino) {
        if (origen == null || destino == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(aristasPorClave.get(generarClaveArista(origen, destino)));
    }

    /**
     * Obtiene la vista del autolazo de un estado si existe.
     *
     * @param estado Estado formal consultado.
     * @return {@link Optional} con el autolazo encontrado.
     */
    public Optional<AutolazoVista> obtenerAutolazoVista(Estado estado) {
        if (estado == null) {
            return Optional.empty();
        }
        AutolazoVista lazo = autolazosPorEstado.get(estado);
        if (lazo != null) {
            return Optional.of(lazo);
        }
        for (Map.Entry<Estado, AutolazoVista> entry : autolazosPorEstado.entrySet()) {
            if (entry.getKey() == estado || entry.getKey().equals(estado)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Elimina la transición dirigida entre dos estados, descurvando la opuesta si correspondiera.
     *
     * @param origen Estado formal origen.
     * @param destino Estado formal destino.
     */
    public void eliminarArista(Estado origen, Estado destino) {
        if (origen == null || destino == null) {
            return;
        }

        if (origen.equals(destino)) {
            AutolazoVista autolazo = autolazosPorEstado.remove(origen);
            if (autolazo != null) {
                capaAristas.getChildren().remove(autolazo);
            }
            return;
        }

        String clave = generarClaveArista(origen, destino);
        AristaTransicionVista arista = aristasPorClave.remove(clave);
        if (arista != null) {
            capaAristas.getChildren().remove(arista);

            String claveOpuesta = generarClaveArista(destino, origen);
            AristaTransicionVista aristaOpuesta = aristasPorClave.get(claveOpuesta);
            if (aristaOpuesta != null) {
                aristaOpuesta.setCurvada(false);
            }
        }
    }

    // =========================================================================
    // 7. EXPANSIÓN DINÁMICA DEL LIENZO (CASO LÍMITE 21)
    // =========================================================================

    /**
     * Evalúa si una posición dada se aproxima a los márgenes derecho o inferior del lienzo virtual
     * y expande sus dimensiones si es necesario para permitir desplazamiento continuo en el ScrollPane.
     *
     * @param x Coordenada X del elemento evaluado.
     * @param y Coordenada Y del elemento evaluado.
     */
    public void verificarYExpandirLienzo(double x, double y) {
        boolean expandido = false;

        if (x + MARGEN_EXPANSION >= anchoVirtual) {
            anchoVirtual = Math.max(anchoVirtual + PASO_EXPANSION, x + MARGEN_EXPANSION * 2.5);
            lienzoVirtual.setPrefWidth(anchoVirtual);
            lienzoVirtual.setMinWidth(anchoVirtual);
            expandido = true;
        }

        if (y + MARGEN_EXPANSION >= altoVirtual) {
            altoVirtual = Math.max(altoVirtual + PASO_EXPANSION, y + MARGEN_EXPANSION * 2.5);
            lienzoVirtual.setPrefHeight(altoVirtual);
            lienzoVirtual.setMinHeight(altoVirtual);
            expandido = true;
        }

        if (expandido && vistaFondoAmerica != null) {
            vistaFondoAmerica.setLayoutX(Math.max(0, (anchoVirtual - 420) / 2.0));
            vistaFondoAmerica.setLayoutY(Math.max(0, (altoVirtual - 420) / 2.0));
        }
    }

    // =========================================================================
    // 8. INTERACCIÓN REACTIVA DE RATÓN (DRAG & DROP Y CONEXIÓN VISUAL)
    // =========================================================================

    private void configurarInteraccionNodo(NodoEstadoVista nodoVista) {
        // Presionar ratón sobre el nodo
        nodoVista.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Doble clic primario: alternar condición de aceptación (final) rápidamente
                if (event.getClickCount() == 2 && !modoProtegido) {
                    if (onSolicitudAlternarAceptacion != null) {
                        onSolicitudAlternarAceptacion.accept(nodoVista.getEstado());
                    } else {
                        alternarAceptacionEstado(nodoVista.getEstado());
                    }
                    event.consume();
                    return;
                }

                // Iniciar arrastre del nodo
                posicionInicialRatonX = event.getSceneX();
                posicionInicialRatonY = event.getSceneY();
                posicionInicialNodoX = nodoVista.getLayoutX();
                posicionInicialNodoY = nodoVista.getLayoutY();

                if (onEstadoSeleccionado != null) {
                    onEstadoSeleccionado.accept(nodoVista.getEstado());
                }
                nodoVista.toFront();
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY && !modoProtegido) {
                // Preparar posible trazado interactivo de transición o menú contextual
                estadoArrastreOrigen = nodoVista.getEstado();
                posicionInicialRatonX = event.getSceneX();
                posicionInicialRatonY = event.getSceneY();
                haArrastradoTransicion = false;

                lineaGuia.setStartX(nodoVista.getCentroX());
                lineaGuia.setStartY(nodoVista.getCentroY());
                lineaGuia.setEndX(nodoVista.getCentroX());
                lineaGuia.setEndY(nodoVista.getCentroY());
                lineaGuia.setVisible(false);
                event.consume();
            }
        });

        // Arrastrar ratón desde el nodo
        nodoVista.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double deltaX = event.getSceneX() - posicionInicialRatonX;
                double deltaY = event.getSceneY() - posicionInicialRatonY;

                double nuevoX = Math.max(0.0, posicionInicialNodoX + deltaX);
                double nuevoY = Math.max(0.0, posicionInicialNodoY + deltaY);

                nodoVista.setLayoutX(nuevoX);
                nodoVista.setLayoutY(nuevoY);

                verificarYExpandirLienzo(nuevoX, nuevoY);
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY && !modoProtegido && estadoArrastreOrigen != null) {
                double distanciaArrastre = Math.hypot(event.getSceneX() - posicionInicialRatonX, event.getSceneY() - posicionInicialRatonY);

                // Solo activar la línea guía y marcar arrastre si se desplazó más de 6 píxeles
                if (distanciaArrastre > 6.0) {
                    haArrastradoTransicion = true;
                    lineaGuia.setVisible(true);

                    Point2D puntoLocal = lienzoVirtual.sceneToLocal(event.getSceneX(), event.getSceneY());
                    if (puntoLocal != null) {
                        lineaGuia.setEndX(puntoLocal.getX());
                        lineaGuia.setEndY(puntoLocal.getY());
                    }
                }
                event.consume();
            }
        });

        // Soltar ratón tras arrastre o clic
        nodoVista.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.SECONDARY && !modoProtegido && estadoArrastreOrigen != null) {
                lineaGuia.setVisible(false);

                if (haArrastradoTransicion) {
                    // Se realizó un arrastre efectivo: buscar nodo destino para crear transición o autolazo
                    Point2D puntoLocal = lienzoVirtual.sceneToLocal(event.getSceneX(), event.getSceneY());
                    if (puntoLocal != null) {
                        Estado estadoDestino = encontrarEstadoEnPunto(puntoLocal.getX(), puntoLocal.getY());
                        if (estadoDestino != null) {
                            solicitarTransicion(estadoArrastreOrigen, estadoDestino);
                        }
                    }
                } else {
                    // Fue un clic secundario simple sin arrastre: desplegar menú contextual del nodo
                    mostrarMenuContextual(nodoVista, event.getScreenX(), event.getScreenY());
                }

                estadoArrastreOrigen = null;
                haArrastradoTransicion = false;
                event.consume();
            }
        });
    }

    private void mostrarMenuContextual(NodoEstadoVista nodoVista, double screenX, double screenY) {
        ContextMenu menu = new ContextMenu();

        // 1. Alternar Estado Inicial (RF-03)
        MenuItem itemInicial = new MenuItem(nodoVista.esInicial() ? "Quitar Estado Inicial" : "Marcar como Estado Inicial");
        itemInicial.setOnAction(e -> {
            if (nodoVista.esInicial()) {
                if (onSolicitudDesmarcarInicial != null) {
                    onSolicitudDesmarcarInicial.run();
                } else {
                    desmarcarEstadoInicial();
                }
            } else {
                if (onSolicitudEstablecerInicial != null) {
                    onSolicitudEstablecerInicial.accept(nodoVista.getEstado());
                } else {
                    establecerEstadoInicial(nodoVista.getEstado());
                }
            }
        });

        // 2. Alternar Estado de Aceptación (Final) (RF-03)
        MenuItem itemAceptacion = new MenuItem(nodoVista.esAceptacion() ? "Quitar Aceptación (Final)" : "Marcar como Aceptación (Final)");
        itemAceptacion.setOnAction(e -> {
            if (onSolicitudAlternarAceptacion != null) {
                onSolicitudAlternarAceptacion.accept(nodoVista.getEstado());
            } else {
                alternarAceptacionEstado(nodoVista.getEstado());
            }
        });

        // 3. Renombrar Estado (RF-02)
        MenuItem itemRenombrar = new MenuItem("Renombrar Estado...");
        itemRenombrar.setOnAction(e -> solicitarRenombrarEstado(nodoVista.getEstado()));

        SeparatorMenuItem separador = new SeparatorMenuItem();

        // 4. Eliminar Estado (RF-02)
        MenuItem itemEliminar = new MenuItem("Eliminar Estado");
        itemEliminar.setOnAction(e -> {
            if (onSolicitudEliminarEstado != null) {
                onSolicitudEliminarEstado.accept(nodoVista.getEstado());
            } else {
                eliminarEstado(nodoVista.getEstado());
            }
        });

        menu.getItems().addAll(itemInicial, itemAceptacion, itemRenombrar, separador, itemEliminar);
        menu.show(nodoVista, screenX, screenY);
    }

    private void mostrarMenuContextualLienzo(double canvasX, double canvasY, double screenX, double screenY) {
        if (modoProtegido) {
            return;
        }
        ContextMenu menu = new ContextMenu();
        MenuItem itemNuevo = new MenuItem("+ Agregar Estado aquí");
        itemNuevo.setOnAction(e -> {
            if (onCrearEstadoEnPosicion != null) {
                onCrearEstadoEnPosicion.accept(canvasX, canvasY);
            }
        });
        menu.getItems().add(itemNuevo);
        menu.show(lienzoVirtual, screenX, screenY);
    }

    /**
     * Despliega un diálogo emergente para renombrar interactivamente el estado (RF-02).
     *
     * @param estado Estado a renombrar.
     */
    public void solicitarRenombrarEstado(Estado estado) {
        if (estado == null || modoProtegido) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog(estado.getNombre());
        dialog.setTitle("Renombrar Estado");
        dialog.setHeaderText("Modificar identificador del estado");
        dialog.setContentText("Nuevo identificador (máx 8 caracteres):");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String nuevo = resultado.get().trim();
            if (!nuevo.isEmpty()) {
                if (onSolicitudRenombrarEstado != null) {
                    onSolicitudRenombrarEstado.accept(estado, nuevo);
                } else {
                    renombrarEstado(estado, estado.getNombre(), nuevo);
                }
            }
        }
    }

    /**
     * Establece el estado especificado como inicial único en el lienzo, desmarcando
     * automáticamente cualquier otro nodo previamente inicial (RF-03).
     *
     * @param estado Estado formal a designar como inicial.
     */
    public void establecerEstadoInicial(Estado estado) {
        if (estado == null) {
            return;
        }
        for (NodoEstadoVista nodo : nodosPorEstado.values()) {
            nodo.setInicial(nodo.getEstado().equals(estado));
        }
    }

    /**
     * Desmarca el estado inicial en todo el lienzo (dejando el autómata temporalmente sin inicial) (RF-03).
     */
    public void desmarcarEstadoInicial() {
        for (NodoEstadoVista nodo : nodosPorEstado.values()) {
            nodo.setInicial(false);
        }
    }

    /**
     * Alterna la condición de aceptación (final) de un estado en el lienzo (RF-03).
     *
     * @param estado Estado formal a modificar.
     */
    public void alternarAceptacionEstado(Estado estado) {
        if (estado == null) {
            return;
        }
        NodoEstadoVista nodo = nodosPorEstado.get(estado);
        if (nodo != null) {
            nodo.setAceptacion(!nodo.esAceptacion());
        }
    }

    private Estado encontrarEstadoEnPunto(double x, double y) {
        for (NodoEstadoVista nodo : nodosPorEstado.values()) {
            double cx = nodo.getCentroX();
            double cy = nodo.getCentroY();
            double distancia = Math.hypot(x - cx, y - cy);
            if (distancia <= NodoEstadoVista.RADIO_BASE + 8.0) {
                return nodo.getEstado();
            }
        }
        return null;
    }

    /**
     * Solicita la creación de una transición entre dos estados invocando el callback registrado
     * o presentando el diálogo de selección de símbolos si no hay controlador conectado.
     *
     * @param origen Estado origen.
     * @param destino Estado destino.
     */
    public void solicitarTransicion(Estado origen, Estado destino) {
        if (origen == null || destino == null) {
            return;
        }

        if (onSolicitudTransicion != null) {
            onSolicitudTransicion.onSolicitarTransicion(origen, destino);
            return;
        }

        // Si hay alfabeto formal y no hay listener externo, abrir diálogo asistido
        if (alfabeto != null && !alfabeto.getSimbolos().isEmpty()) {
            SelectorSimbolosDialog dialog = new SelectorSimbolosDialog(alfabeto);
            Optional<List<Character>> resultado = dialog.showAndWait();
            if (resultado.isPresent() && !resultado.get().isEmpty()) {
                Set<Character> simbolos = new HashSet<>(resultado.get());
                if (origen.equals(destino)) {
                    crearAutolazo(origen, simbolos);
                } else {
                    conectarEstados(origen, destino, simbolos);
                }
            }
        }
    }

    // =========================================================================
    // 9. LIMPIEZA TOTAL Y CONFIGURACIÓN
    // =========================================================================

    /**
     * Limpia completamente todos los estados, aristas y autolazos presentes en el lienzo,
     * restableciendo sus dimensiones virtuales iniciales.
     */
    public void limpiar() {
        nodosPorEstado.clear();
        aristasPorClave.clear();
        autolazosPorEstado.clear();

        capaNodos.getChildren().clear();
        capaAristas.getChildren().clear();
        lineaGuia.setVisible(false);

        anchoVirtual = ANCHO_INICIAL;
        altoVirtual = ALTO_INICIAL;
        lienzoVirtual.setPrefWidth(anchoVirtual);
        lienzoVirtual.setPrefHeight(altoVirtual);
        lienzoVirtual.setMinWidth(anchoVirtual);
        lienzoVirtual.setMinHeight(altoVirtual);
    }

    private String generarClaveArista(Estado origen, Estado destino) {
        return origen.getNombre() + "->" + destino.getNombre();
    }

    // =========================================================================
    // 10. GETTERS Y SETTERS
    // =========================================================================

    public Pane getLienzoVirtual() {
        return lienzoVirtual;
    }

    public double getAnchoVirtual() {
        return anchoVirtual;
    }

    public double getAltoVirtual() {
        return altoVirtual;
    }

    public Alfabeto getAlfabeto() {
        return alfabeto;
    }

    public void setAlfabeto(Alfabeto alfabeto) {
        this.alfabeto = alfabeto;
    }

    public boolean esModoProtegido() {
        return modoProtegido;
    }

    public void setModoProtegido(boolean modoProtegido) {
        this.modoProtegido = modoProtegido;
    }

    public void setOnSolicitudTransicion(SolicitudTransicionListener onSolicitudTransicion) {
        this.onSolicitudTransicion = onSolicitudTransicion;
    }

    public void setOnEstadoSeleccionado(Consumer<Estado> onEstadoSeleccionado) {
        this.onEstadoSeleccionado = onEstadoSeleccionado;
    }

    public void setOnCrearEstadoEnPosicion(BiConsumer<Double, Double> onCrearEstadoEnPosicion) {
        this.onCrearEstadoEnPosicion = onCrearEstadoEnPosicion;
    }

    public void setOnSolicitudEliminarEstado(Consumer<Estado> onSolicitudEliminarEstado) {
        this.onSolicitudEliminarEstado = onSolicitudEliminarEstado;
    }

    public void setOnSolicitudEstablecerInicial(Consumer<Estado> onSolicitudEstablecerInicial) {
        this.onSolicitudEstablecerInicial = onSolicitudEstablecerInicial;
    }

    public void setOnSolicitudDesmarcarInicial(Runnable onSolicitudDesmarcarInicial) {
        this.onSolicitudDesmarcarInicial = onSolicitudDesmarcarInicial;
    }

    public void setOnSolicitudAlternarAceptacion(Consumer<Estado> onSolicitudAlternarAceptacion) {
        this.onSolicitudAlternarAceptacion = onSolicitudAlternarAceptacion;
    }

    public void setOnSolicitudRenombrarEstado(BiConsumer<Estado, String> onSolicitudRenombrarEstado) {
        this.onSolicitudRenombrarEstado = onSolicitudRenombrarEstado;
    }

    // =========================================================================
    // 12. FONDO Y MARCA DE AGUA (MODO AMÉRICA)
    // =========================================================================

    private void inicializarFondoAmerica() {
        try {
            var resource = getClass().getResource("/img/america.png");
            if (resource == null) {
                resource = getClass().getResource("/img/escudo_america.jpg");
            }
            if (resource != null) {
                Image img = new Image(resource.toExternalForm());
                vistaFondoAmerica = new ImageView(img);
                vistaFondoAmerica.setPreserveRatio(true);
                vistaFondoAmerica.setFitWidth(420);
                vistaFondoAmerica.setOpacity(0.22);
                vistaFondoAmerica.setMouseTransparent(true);
                vistaFondoAmerica.setVisible(false);

                double posX = Math.max(0, (anchoVirtual - 420) / 2.0);
                double posY = Math.max(0, (altoVirtual - 420) / 2.0);
                vistaFondoAmerica.setLayoutX(posX);
                vistaFondoAmerica.setLayoutY(posY);

                capaFondo.getChildren().add(vistaFondoAmerica);
            }
        } catch (Exception ignored) {
            // Entorno de prueba o sin subsistema gráfico completo
        }
    }

    /**
     * Activa o desactiva la marca de agua del escudo del América de Cali en el fondo del tablero.
     *
     * @param activo {@code true} para mostrar la imagen de fondo, {@code false} para ocultarla.
     */
    public void activarMarcaDeAguaAmerica(boolean activo) {
        if (vistaFondoAmerica != null) {
            vistaFondoAmerica.setVisible(activo);
        }
    }

    public boolean isMarcaDeAguaAmericaActiva() {
        return vistaFondoAmerica != null && vistaFondoAmerica.isVisible();
    }
}
