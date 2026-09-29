package com.automatas.view.componente;

import com.automatas.model.Estado;
import com.automatas.view.util.PaletaColor;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Componente visual que renderiza un estado formal de un autómata finito en el árbol de escena (RF-02, RF-03, RNF-04).
 * <p>
 * Estructura gráfica nativa basada en {@link StackPane}:
 * <ul>
 *   <li><b>Círculo base:</b> Representa el nodo del estado con fondo blanco y borde grafito pizarra.</li>
 *   <li><b>Etiqueta de texto:</b> Muestra el identificador textual del estado (q0, q1, k0, ...).</li>
 *   <li><b>Círculo concéntrico:</b> Doble anillo interior en verde esmeralda para estados de aceptación (F).</li>
 *   <li><b>Flecha inicial:</b> Polígono triangular orientado hacia el nodo para el estado inicial (q0).</li>
 *   <li><b>Resaltado de selección:</b> Borde violeta neutro (#8B5CF6) cuando el nodo está activo o seleccionado.</li>
 * </ul>
 */
public class NodoEstadoVista extends StackPane {

    // =========================================================================
    // 1. CONSTANTES GEOMÉTRICAS
    // =========================================================================

    public static final double RADIO_BASE = 25.0;
    public static final double RADIO_ACEPTACION = 20.0;
    public static final double GROSOR_BORDE_REGULAR = 2.0;
    public static final double GROSOR_BORDE_SELECCION = 2.8;

    // =========================================================================
    // 2. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Estado estado;
    private boolean inicial;
    private boolean seleccionado;

    private final Circle circuloBase;
    private final Circle circuloAceptacion;
    private final Label etiquetaNombre;
    private final Polygon flechaInicial;

    private final DoubleBinding centroXBinding;
    private final DoubleBinding centroYBinding;

    // =========================================================================
    // 3. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye la vista del estado especificando su entidad de dominio y si es inicial.
     *
     * @param estado Entidad formal de estado {@link Estado}.
     * @param inicial {@code true} si el estado es el inicial del autómata; {@code false} en caso contrario.
     * @throws IllegalArgumentException Si el estado es nulo.
     */
    public NodoEstadoVista(Estado estado, boolean inicial) {
        if (estado == null) {
            throw new IllegalArgumentException("La entidad formal Estado no puede ser nula.");
        }

        this.estado = estado;
        this.inicial = inicial;
        this.seleccionado = false;

        // Paso 1: Configurar dimensiones del contenedor StackPane
        double diametro = RADIO_BASE * 2.0;
        setMinSize(diametro, diametro);
        setPrefSize(diametro, diametro);
        setMaxSize(diametro, diametro);

        // Paso 2: Crear el círculo base principal
        this.circuloBase = new Circle(RADIO_BASE);
        this.circuloBase.setFill(PaletaColor.FONDO_BLANCO);
        this.circuloBase.setStroke(PaletaColor.BORDE_REGULAR);
        this.circuloBase.setStrokeWidth(GROSOR_BORDE_REGULAR);

        // Paso 3: Crear el círculo concéntrico para aceptación (F)
        this.circuloAceptacion = new Circle(RADIO_ACEPTACION);
        this.circuloAceptacion.setFill(Color.TRANSPARENT);
        this.circuloAceptacion.setStroke(PaletaColor.ACEPTACION);
        this.circuloAceptacion.setStrokeWidth(1.8);

        // Paso 4: Crear la etiqueta con el nombre del estado
        this.etiquetaNombre = new Label(estado.getNombre());
        this.etiquetaNombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        this.etiquetaNombre.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        this.etiquetaNombre.setStyle("-fx-text-fill: #000000 !important;");

        // Paso 5: Crear la flecha triangular indicadora de estado inicial
        // Triángulo apuntando hacia la izquierda del círculo base
        this.flechaInicial = new Polygon(
                -16.0, -7.0,
                -16.0, 7.0,
                -2.0, 0.0
        );
        this.flechaInicial.setFill(PaletaColor.BORDE_REGULAR);
        this.flechaInicial.setStroke(Color.TRANSPARENT);
        this.flechaInicial.setManaged(false);

        // Vincular posición de la flecha al borde izquierdo del nodo
        this.flechaInicial.layoutXProperty().bind(widthProperty().divide(2.0).subtract(RADIO_BASE));
        this.flechaInicial.layoutYProperty().bind(heightProperty().divide(2.0));

        // Paso 6: Ensamblar los nodos en el árbol de escena del StackPane
        getChildren().addAll(flechaInicial, circuloBase, circuloAceptacion, etiquetaNombre);

        // Paso 7: Configurar bindings de centro geométrico para conexión de aristas
        this.centroXBinding = Bindings.createDoubleBinding(
                () -> getLayoutX() + RADIO_BASE,
                layoutXProperty()
        );
        this.centroYBinding = Bindings.createDoubleBinding(
                () -> getLayoutY() + RADIO_BASE,
                layoutYProperty()
        );

        // Paso 8: Sincronizar visibilidad y estilos iniciales
        actualizarVista();
    }

    /**
     * Construye la vista del estado asumiendo que no es el estado inicial.
     *
     * @param estado Entidad formal de estado {@link Estado}.
     */
    public NodoEstadoVista(Estado estado) {
        this(estado, false);
    }

    // =========================================================================
    // 4. MÉTODOS DE ACTUALIZACIÓN Y SINCRONIZACIÓN VISUAL
    // =========================================================================

    /**
     * Sincroniza la representación visual con el estado interno del modelo formal.
     */
    public void actualizarVista() {
        // 1. Sincronizar texto del nombre
        etiquetaNombre.setText(estado.getNombre());

        // 2. Sincronizar visibilidad del anillo concéntrico de aceptación
        circuloAceptacion.setVisible(esAceptacion());

        // 3. Sincronizar visibilidad de la flecha inicial
        flechaInicial.setVisible(inicial);

        // 4. Sincronizar borde según estado de selección
        if (seleccionado) {
            circuloBase.setStroke(PaletaColor.SELECCION);
            circuloBase.setStrokeWidth(GROSOR_BORDE_SELECCION);
        } else {
            circuloBase.setStroke(PaletaColor.BORDE_REGULAR);
            circuloBase.setStrokeWidth(GROSOR_BORDE_REGULAR);
        }
    }

    // =========================================================================
    // 5. GETTERS Y SETTERS DE ESTADO VISUAL Y DOMINIO
    // =========================================================================

    /**
     * Obtiene la entidad formal del modelo asociada a este nodo.
     *
     * @return Instancia de {@link Estado}.
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * Indica si este nodo está marcado como estado inicial en la vista.
     *
     * @return {@code true} si es estado inicial; {@code false} en caso contrario.
     */
    public boolean esInicial() {
        return inicial;
    }

    /**
     * Establece si el nodo es el estado inicial y actualiza la vista.
     *
     * @param inicial {@code true} para mostrar flecha inicial; {@code false} para ocultarla.
     */
    public void setInicial(boolean inicial) {
        this.inicial = inicial;
        actualizarVista();
    }

    /**
     * Indica si el estado subyacente es de aceptación.
     *
     * @return {@code true} si pertenece a F; {@code false} en caso contrario.
     */
    public boolean esAceptacion() {
        return estado.esAceptacion();
    }

    /**
     * Establece la condición de aceptación tanto en el modelo formal como en la vista.
     *
     * @param aceptacion {@code true} para marcar como final; {@code false} para desmarcar.
     */
    public void setAceptacion(boolean aceptacion) {
        estado.setAceptacion(aceptacion);
        actualizarVista();
    }

    /**
     * Indica si el nodo se encuentra actualmente seleccionado por el usuario.
     *
     * @return {@code true} si está seleccionado; {@code false} en caso contrario.
     */
    public boolean esSeleccionado() {
        return seleccionado;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    /**
     * Modifica el estado de selección del nodo y actualiza su borde visual.
     *
     * @param seleccionado {@code true} para resaltar en violeta; {@code false} para borde regular.
     */
    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        actualizarVista();
    }

    // =========================================================================
    // 6. ACCESORES A NODOS GRÁFICOS INTERNOS
    // =========================================================================

    public Circle getCirculoBase() {
        return circuloBase;
    }

    public Circle getCirculoAceptacion() {
        return circuloAceptacion;
    }

    public Label getEtiquetaNombre() {
        return etiquetaNombre;
    }

    public Polygon getFlechaInicial() {
        return flechaInicial;
    }

    // =========================================================================
    // 7. PROPIEDADES GEOMÉTRICAS DE ENLACE (BINDINGS PARA ARISTAS)
    // =========================================================================

    /**
     * Propiedad observable que expone la coordenada X del centro geométrico del nodo.
     *
     * @return {@link DoubleBinding} con layoutX + RADIO_BASE.
     */
    public DoubleBinding centroXProperty() {
        return centroXBinding;
    }

    /**
     * Propiedad observable que expone la coordenada Y del centro geométrico del nodo.
     *
     * @return {@link DoubleBinding} con layoutY + RADIO_BASE.
     */
    public DoubleBinding centroYProperty() {
        return centroYBinding;
    }

    /**
     * Obtiene la coordenada X del centro del nodo en su contenedor padre.
     *
     * @return layoutX + RADIO_BASE.
     */
    public double getCentroX() {
        return getLayoutX() + RADIO_BASE;
    }

    /**
     * Obtiene la coordenada Y del centro del nodo en su contenedor padre.
     *
     * @return layoutY + RADIO_BASE.
     */
    public double getCentroY() {
        return getLayoutY() + RADIO_BASE;
    }

    /**
     * Posiciona el nodo centrando su círculo en las coordenadas especificadas.
     *
     * @param x Coordenada X del centro deseado.
     * @param y Coordenada Y del centro deseado.
     */
    public void setCentro(double x, double y) {
        setLayoutX(x - RADIO_BASE);
        setLayoutY(y - RADIO_BASE);
    }
}
