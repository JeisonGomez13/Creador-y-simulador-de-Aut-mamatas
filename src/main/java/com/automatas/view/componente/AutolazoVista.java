package com.automatas.view.componente;

import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Componente visual que representa un autolazo reflexivo (qA -> qA) trazado en el cuadrante superior (RF-04).
 * <p>
 * Reglas de diseño y casos límite (RF-04, Casos Límite 5 y 7):
 * <ul>
 *   <li><b>Geometría en cuadrante superior:</b> Implementado mediante {@link CubicCurve} que se eleva sobre
 *       el nodo, manteniendo visibilidad y evitando interferir con aristas laterales.</li>
 *   <li><b>Cabeza de flecha orientada:</b> Punta triangular orientada al vector tangente de llegada al perímetro del nodo.</li>
 *   <li><b>Unificación de símbolos (Caso Límite 5):</b> Múltiples símbolos sobre el mismo autolazo se presentan
 *       en una sola etiqueta unificada con comas (ej. "a, b").</li>
 *   <li><b>Reactivación geométrica viva:</b> Actualiza automáticamente su trazado al mover el nodo en el lienzo.</li>
 * </ul>
 */
public class AutolazoVista extends Group {

    // =========================================================================
    // 1. CONSTANTES GEOMÉTRICAS Y ESTILOS
    // =========================================================================

    public static final double LARGO_FLECHA = 10.0;
    public static final double ANCHO_FLECHA = 8.0;

    // =========================================================================
    // 2. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final NodoEstadoVista nodo;
    private final Set<Character> simbolos;

    private final CubicCurve curva;
    private final Polygon cabezaFlecha;
    private final Label etiqueta;

    // =========================================================================
    // 3. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye un autolazo sobre el nodo especificado con una colección de símbolos iniciales.
     *
     * @param nodo Nodo de estado sobre el cual se traza el autolazo.
     * @param simbolosIniciales Colección inicial de símbolos.
     * @throws IllegalArgumentException Si el nodo proporcionado es nulo.
     */
    public AutolazoVista(NodoEstadoVista nodo, Collection<Character> simbolosIniciales) {
        if (nodo == null) {
            throw new IllegalArgumentException("El nodo asociado al autolazo no puede ser nulo.");
        }

        this.nodo = nodo;
        this.simbolos = new TreeSet<>();
        if (simbolosIniciales != null) {
            this.simbolos.addAll(simbolosIniciales);
        }

        // Paso 1: Crear curva cúbica para el arco superior
        this.curva = new CubicCurve();
        this.curva.setFill(Color.TRANSPARENT);
        this.curva.setStroke(PaletaColor.BORDE_REGULAR);
        this.curva.setStrokeWidth(2.0);

        // Paso 2: Crear cabeza de flecha
        this.cabezaFlecha = new Polygon();
        this.cabezaFlecha.setFill(PaletaColor.BORDE_REGULAR);
        this.cabezaFlecha.setStroke(Color.TRANSPARENT);

        // Paso 3: Crear etiqueta textual centrada arriba
        this.etiqueta = new Label(getTextoSimbolos());
        this.etiqueta.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        this.etiqueta.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        this.etiqueta.setPadding(new Insets(1, 5, 1, 5));
        this.etiqueta.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 4px; -fx-border-color: #E5E7EB; -fx-border-radius: 4px; -fx-border-width: 1px; -fx-text-fill: #000000 !important;");

        // Paso 4: Añadir al grupo
        getChildren().addAll(curva, cabezaFlecha, etiqueta);

        // Paso 5: Escuchadores reactivos
        nodo.layoutXProperty().addListener((obs, oldV, newV) -> actualizarGeometria());
        nodo.layoutYProperty().addListener((obs, oldV, newV) -> actualizarGeometria());
        etiqueta.widthProperty().addListener((obs, oldV, newV) -> actualizarPosicionEtiqueta());
        etiqueta.heightProperty().addListener((obs, oldV, newV) -> actualizarPosicionEtiqueta());

        actualizarGeometria();
    }

    /**
     * Construye un autolazo con un único símbolo inicial.
     *
     * @param nodo Nodo de estado sobre el cual se traza el autolazo.
     * @param simbolo Símbolo de transición.
     */
    public AutolazoVista(NodoEstadoVista nodo, char simbolo) {
        this(nodo, Set.of(simbolo));
    }

    // =========================================================================
    // 4. GESTIÓN DE SÍMBOLOS
    // =========================================================================

    public void agregarSimbolo(char simbolo) {
        if (simbolos.add(simbolo)) {
            actualizarEtiquetaTexto();
        }
    }

    public void removerSimbolo(char simbolo) {
        if (simbolos.remove(simbolo)) {
            actualizarEtiquetaTexto();
        }
    }

    public String getTextoSimbolos() {
        return simbolos.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private void actualizarEtiquetaTexto() {
        etiqueta.setText(getTextoSimbolos());
        actualizarGeometria();
    }

    // =========================================================================
    // 5. CÁLCULO GEOMÉTRICO (CUADRANTE SUPERIOR)
    // =========================================================================

    public void actualizarGeometria() {
        double x0 = nodo.getCentroX();
        double y0 = nodo.getCentroY();
        double r = NodoEstadoVista.RADIO_BASE;

        // Puntos de inicio y fin en el perímetro superior del nodo circular (r = 25)
        // 15^2 + 20^2 = 225 + 400 = 625 = 25^2
        double startX = x0 - 15.0;
        double startY = y0 - 20.0;
        double endX = x0 + 15.0;
        double endY = y0 - 20.0;

        // Puntos de control que elevan el lazo en el cuadrante superior
        double c1x = x0 - 35.0;
        double c1y = y0 - 65.0;
        double c2x = x0 + 35.0;
        double c2y = y0 - 65.0;

        curva.setStartX(startX);
        curva.setStartY(startY);
        curva.setControlX1(c1x);
        curva.setControlY1(c1y);
        curva.setControlX2(c2x);
        curva.setControlY2(c2y);
        curva.setEndX(endX);
        curva.setEndY(endY);

        // Vector tangente de llegada en endX, endY (procedente de C2)
        double tangX = endX - c2x; // -20.0
        double tangY = endY - c2y; // +45.0
        double angulo = Math.atan2(tangY, tangX);

        double cos = Math.cos(angulo);
        double sin = Math.sin(angulo);
        double anchoMedio = ANCHO_FLECHA / 2.0;

        double xBase1 = endX - LARGO_FLECHA * cos + anchoMedio * sin;
        double yBase1 = endY - LARGO_FLECHA * sin - anchoMedio * cos;
        double xBase2 = endX - LARGO_FLECHA * cos - anchoMedio * sin;
        double yBase2 = endY - LARGO_FLECHA * sin + anchoMedio * cos;

        cabezaFlecha.getPoints().setAll(
                endX, endY,
                xBase1, yBase1,
                xBase2, yBase2
        );

        actualizarPosicionEtiqueta();
    }

    private void actualizarPosicionEtiqueta() {
        double x0 = nodo.getCentroX();
        double y0 = nodo.getCentroY();

        // En t=0.5, la altura de la curva cúbica es aproximadamente y0 - 53.75
        double ly = y0 - 54.0;

        etiqueta.setLayoutX(x0 - etiqueta.getWidth() / 2.0);
        etiqueta.setLayoutY(ly - 14.0 - etiqueta.getHeight() / 2.0);
    }

    // =========================================================================
    // 6. GETTERS
    // =========================================================================

    public NodoEstadoVista getNodo() {
        return nodo;
    }

    public Set<Character> getSimbolos() {
        return simbolos;
    }

    public CubicCurve getCurva() {
        return curva;
    }

    public Polygon getCabezaFlecha() {
        return cabezaFlecha;
    }

    public Label getEtiqueta() {
        return etiqueta;
    }
}
