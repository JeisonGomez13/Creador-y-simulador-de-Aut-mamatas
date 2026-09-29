package com.automatas.view.componente;

import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Componente visual que representa una arista de transición dirigida entre dos estados distintos (RF-04).
 * <p>
 * Reglas de diseño y casos límite (RF-04, Casos Límite 5 y 6):
 * <ul>
 *   <li><b>Unificación de símbolos (Caso Límite 5):</b> Múltiples símbolos entre el mismo par ordenado (qA -> qB)
 *       se unifican en una sola flecha rotulada con la lista separada por comas (ej. "a, b").</li>
 *   <li><b>Transiciones bidireccionales opuestas (Caso Límite 6):</b> Cuando existen transiciones simultáneas
 *       en sentidos contrarios (qA -> qB y qB -> qA), se curvan ortogonalmente en direcciones opuestas
 *       mediante {@link QuadCurve} evitando solapamiento de líneas y etiquetas.</li>
 *   <li><b>Punta de flecha poligonal:</b> Cabeza triangular alineada al vector tangente de llegada al perímetro del nodo.</li>
 *   <li><b>Reactivación geométrica viva:</b> Actualiza su trazado ante cualquier movimiento de arrastre de los nodos.</li>
 * </ul>
 */
public class AristaTransicionVista extends Group {

    // =========================================================================
    // 1. CONSTANTES GEOMÉTRICAS Y ESTILOS
    // =========================================================================

    public static final double CURVATURA_DEFECTO = 35.0;
    public static final double LARGO_FLECHA = 11.0;
    public static final double ANCHO_FLECHA = 8.0;

    // =========================================================================
    // 2. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final NodoEstadoVista origen;
    private final NodoEstadoVista destino;
    private final Set<Character> simbolos;
    private boolean curvada;

    private final QuadCurve curva;
    private final Polygon cabezaFlecha;
    private final Label etiqueta;

    // =========================================================================
    // 3. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye una arista visual dirigida entre dos nodos con un conjunto inicial de símbolos.
     *
     * @param origen Nodo estado de origen.
     * @param destino Nodo estado de destino.
     * @param simbolosIniciales Colección de símbolos formalmente asociados a la arista.
     * @throws IllegalArgumentException Si el origen o el destino son nulos, o si ambos son el mismo nodo.
     */
    public AristaTransicionVista(NodoEstadoVista origen, NodoEstadoVista destino, Collection<Character> simbolosIniciales) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("El origen y el destino de la arista no pueden ser nulos.");
        }
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Para transiciones sobre el mismo nodo debe utilizarse AutolazoVista.");
        }

        this.origen = origen;
        this.destino = destino;
        this.simbolos = new TreeSet<>();
        if (simbolosIniciales != null) {
            this.simbolos.addAll(simbolosIniciales);
        }
        this.curvada = false;

        // Paso 1: Crear curva cuadrática paramétrica
        this.curva = new QuadCurve();
        this.curva.setFill(Color.TRANSPARENT);
        this.curva.setStroke(PaletaColor.BORDE_REGULAR);
        this.curva.setStrokeWidth(2.0);

        // Paso 2: Crear cabeza de flecha poligonal
        this.cabezaFlecha = new Polygon();
        this.cabezaFlecha.setFill(PaletaColor.BORDE_REGULAR);
        this.cabezaFlecha.setStroke(Color.TRANSPARENT);

        // Paso 3: Crear etiqueta textual con fondo blanco redondeado
        this.etiqueta = new Label(getTextoSimbolos());
        this.etiqueta.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        this.etiqueta.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        this.etiqueta.setPadding(new Insets(1, 5, 1, 5));
        this.etiqueta.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 4px; -fx-border-color: #E5E7EB; -fx-border-radius: 4px; -fx-border-width: 1px; -fx-text-fill: #000000 !important;");

        // Paso 4: Añadir elementos al grupo
        getChildren().addAll(curva, cabezaFlecha, etiqueta);

        // Paso 5: Escuchadores reactivos para seguir a los nodos durante Drag & Drop
        origen.layoutXProperty().addListener((obs, oldV, newV) -> actualizarGeometria());
        origen.layoutYProperty().addListener((obs, oldV, newV) -> actualizarGeometria());
        destino.layoutXProperty().addListener((obs, oldV, newV) -> actualizarGeometria());
        destino.layoutYProperty().addListener((obs, oldV, newV) -> actualizarGeometria());

        // Asegurar actualización de etiqueta cuando cambie de tamaño
        etiqueta.widthProperty().addListener((obs, oldV, newV) -> actualizarPosicionEtiqueta());
        etiqueta.heightProperty().addListener((obs, oldV, newV) -> actualizarPosicionEtiqueta());

        actualizarGeometria();
    }

    /**
     * Construye una arista visual con un único símbolo inicial.
     *
     * @param origen Nodo estado de origen.
     * @param destino Nodo estado de destino.
     * @param simbolo Símbolo inicial de transición.
     */
    public AristaTransicionVista(NodoEstadoVista origen, NodoEstadoVista destino, char simbolo) {
        this(origen, destino, Set.of(simbolo));
    }

    // =========================================================================
    // 4. GESTIÓN DE SÍMBOLOS Y RÓTULO UNIFICADO
    // =========================================================================

    /**
     * Agrega un símbolo formal a la arista y actualiza su rótulo.
     *
     * @param simbolo Símbolo a incorporar.
     */
    public void agregarSimbolo(char simbolo) {
        if (simbolos.add(simbolo)) {
            actualizarEtiquetaTexto();
        }
    }

    /**
     * Remueve un símbolo formal de la arista.
     *
     * @param simbolo Símbolo a remover.
     */
    public void removerSimbolo(char simbolo) {
        if (simbolos.remove(simbolo)) {
            actualizarEtiquetaTexto();
        }
    }

    /**
     * Retorna la lista ordenada de símbolos separados por comas (ej. "a, b, c").
     *
     * @return Cadena unificada de símbolos.
     */
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
    // 5. CÁLCULO GEOMÉTRICO Y DIBUJO PARAMÉTRICO
    // =========================================================================

    /**
     * Recalcula los puntos de inicio, control y fin en las periferias de los nodos circulares,
     * la orientación de la cabeza de flecha y la posición central de la etiqueta.
     */
    public void actualizarGeometria() {
        double x1 = origen.getCentroX();
        double y1 = origen.getCentroY();
        double x2 = destino.getCentroX();
        double y2 = destino.getCentroY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double distancia = Math.hypot(dx, dy);

        if (distancia < 0.001) {
            return;
        }

        double ux = dx / distancia;
        double uy = dy / distancia;

        // Vector normal perpendicular (rotación de 90° horario)
        double nx = -uy;
        double ny = ux;

        // Punto medio del segmento entre centros
        double mx = (x1 + x2) / 2.0;
        double my = (y1 + y2) / 2.0;

        // Punto de control: recto en el punto medio, o curvado hacia el lado normal
        double cx = mx;
        double cy = my;

        if (curvada) {
            cx += nx * CURVATURA_DEFECTO;
            cy += ny * CURVATURA_DEFECTO;
        }

        // Intersección con el perímetro exterior del nodo origen
        double dirOrigenX = cx - x1;
        double dirOrigenY = cy - y1;
        double distOrigen = Math.hypot(dirOrigenX, dirOrigenY);
        double rOrigen = NodoEstadoVista.RADIO_BASE;

        double startX = distOrigen > 0.001 ? x1 + (dirOrigenX / distOrigen) * rOrigen : x1;
        double startY = distOrigen > 0.001 ? y1 + (dirOrigenY / distOrigen) * rOrigen : y1;

        // Intersección con el perímetro exterior del nodo destino
        double dirDestinoX = x2 - cx;
        double dirDestinoY = y2 - cy;
        double distDestino = Math.hypot(dirDestinoX, dirDestinoY);
        double rDestino = NodoEstadoVista.RADIO_BASE;

        double endX = distDestino > 0.001 ? x2 - (dirDestinoX / distDestino) * rDestino : x2;
        double endY = distDestino > 0.001 ? y2 - (dirDestinoY / distDestino) * rDestino : y2;

        // Asignar parámetros a la curva cuadrática
        curva.setStartX(startX);
        curva.setStartY(startY);
        curva.setControlX(cx);
        curva.setControlY(cy);
        curva.setEndX(endX);
        curva.setEndY(endY);

        // Orientar la cabeza de flecha según el vector tangente de llegada a endX, endY
        double tangX = dirDestinoX / (distDestino > 0.001 ? distDestino : 1.0);
        double tangY = dirDestinoY / (distDestino > 0.001 ? distDestino : 1.0);
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
        // En una QuadCurve B(t) = (1-t)^2 P0 + 2(1-t)t P1 + t^2 P2. Para t = 0.5:
        double sx = curva.getStartX();
        double sy = curva.getStartY();
        double cx = curva.getControlX();
        double cy = curva.getControlY();
        double ex = curva.getEndX();
        double ey = curva.getEndY();

        double lx = 0.25 * sx + 0.5 * cx + 0.25 * ex;
        double ly = 0.25 * sy + 0.5 * cy + 0.25 * ey;

        if (curvada) {
            double dx = ex - sx;
            double dy = ey - sy;
            double dist = Math.hypot(dx, dy);
            if (dist > 0.001) {
                double nx = -dy / dist;
                double ny = dx / dist;
                lx += nx * 10.0;
                ly += ny * 10.0;
            }
        }

        etiqueta.setLayoutX(lx - etiqueta.getWidth() / 2.0);
        etiqueta.setLayoutY(ly - etiqueta.getHeight() / 2.0);
    }

    // =========================================================================
    // 6. GETTERS Y SETTERS
    // =========================================================================

    public NodoEstadoVista getOrigen() {
        return origen;
    }

    public NodoEstadoVista getDestino() {
        return destino;
    }

    public Set<Character> getSimbolos() {
        return simbolos;
    }

    public boolean esCurvada() {
        return curvada;
    }

    public void setCurvada(boolean curvada) {
        this.curvada = curvada;
        actualizarGeometria();
    }

    public QuadCurve getCurva() {
        return curva;
    }

    public Polygon getCabezaFlecha() {
        return cabezaFlecha;
    }

    public Label getEtiqueta() {
        return etiqueta;
    }
}
