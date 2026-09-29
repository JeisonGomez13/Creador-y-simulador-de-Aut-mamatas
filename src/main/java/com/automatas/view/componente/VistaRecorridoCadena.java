package com.automatas.view.componente;

import com.automatas.model.CaminoSimulacion;
import com.automatas.model.Estado;
import com.automatas.model.PasoTransicion;
import com.automatas.model.ResultadoSimulacion;
import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente visual para mostrar de forma interactiva y detallada el recorrido completo
 * de una cadena al ser procesada por el autómata (RF-06).
 * <p>
 * Reglas de diseño y casos límite:
 * <ul>
 *   <li><b>Trazabilidad completa:</b> Despliega visualmente la secuencia de estados visitados,
 *       los símbolos consumidos y el estado de aceptación o bloqueo (camino muerto) por cada rama.</li>
 *   <li><b>Soporte AFN:</b> Muestra todas las bifurcaciones y rutas exploradas, permitiendo
 *       inspeccionar qué rama condujo a la aceptación o dónde se produjo el rechazo.</li>
 *   <li><b>Tipografía en negro absoluto:</b> Todos los textos, etiquetas, flechas y descripciones
 *       se presentan con {@code -fx-text-fill: #000000;} inline para garantizar máxima legibilidad y contraste.</li>
 *   <li><b>Restricción cromática estricta (Constitución Principio 7):</b> Prohibición de tonos azul y café.</li>
 * </ul>
 */
public class VistaRecorridoCadena extends VBox {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Label etiquetaTitulo;
    private final Label etiquetaResumenRutas;
    private final VBox contenedorRutas;
    private final ScrollPane scrollRutas;
    private Consumer<CaminoSimulacion> onCaminoSeleccionado;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye la vista de recorrido de cadena con sus estilos institucionales y texto en negro.
     */
    public VistaRecorridoCadena() {
        super(8);
        getStyleClass().add("vista-recorrido-cadena");
        setPadding(new Insets(8, 12, 8, 12));
        setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 6px; -fx-border-color: #E5E7EB; -fx-border-radius: 6px; -fx-text-fill: #000000;");

        // Paso 1: Fila de encabezado
        HBox filaEncabezado = new HBox(10);
        filaEncabezado.setAlignment(Pos.CENTER_LEFT);

        this.etiquetaTitulo = new Label("Recorrido de la Cadena:");
        this.etiquetaTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        this.etiquetaTitulo.setTextFill(Color.BLACK);
        this.etiquetaTitulo.setStyle("-fx-text-fill: #000000 !important;");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        this.etiquetaResumenRutas = new Label("");
        this.etiquetaResumenRutas.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        this.etiquetaResumenRutas.setTextFill(Color.BLACK);
        this.etiquetaResumenRutas.setStyle("-fx-text-fill: #000000 !important;");
        this.etiquetaResumenRutas.setVisible(false);

        filaEncabezado.getChildren().addAll(etiquetaTitulo, espaciador, etiquetaResumenRutas);

        // Paso 2: Contenedor con scroll para las tarjetas de cada ruta
        this.contenedorRutas = new VBox(8);
        this.contenedorRutas.setFillWidth(true);

        this.scrollRutas = new ScrollPane(contenedorRutas);
        this.scrollRutas.setFitToWidth(true);
        this.scrollRutas.setPrefHeight(130);
        this.scrollRutas.setMaxHeight(180);
        this.scrollRutas.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollRutas.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.scrollRutas.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        // Paso 3: Estado inicial vacío
        limpiar();

        getChildren().addAll(filaEncabezado, scrollRutas);
    }

    // =========================================================================
    // 3. MÉTODOS DE MUTACIÓN Y ACTUALIZACIÓN VISUAL
    // =========================================================================

    /**
     * Muestra el recorrido paso a paso de todos los caminos explorados durante la simulación.
     *
     * @param resultado Resultado formal de la simulación {@link ResultadoSimulacion}.
     */
    public void mostrarResultado(ResultadoSimulacion resultado) {
        contenedorRutas.getChildren().clear();

        if (resultado == null) {
            limpiar();
            return;
        }

        List<CaminoSimulacion> caminos = new ArrayList<>(resultado.getCaminos());
        caminos.sort((c1, c2) -> {
            if (c1.esExitoso() != c2.esExitoso()) {
                return c1.esExitoso() ? -1 : 1;
            }
            return Boolean.compare(c1.esCaminoMuerto(), c2.esCaminoMuerto());
        });

        String palabra = resultado.getCadenaProcesada().isEmpty() ? "λ (vacía)" : "\"" + resultado.getCadenaProcesada() + "\"";

        etiquetaResumenRutas.setText("Cadena: " + palabra + " | Rutas: " + caminos.size() + " | Veredicto: " + resultado.getVeredicto());
        etiquetaResumenRutas.setStyle("-fx-text-fill: #000000 !important;");
        etiquetaResumenRutas.setVisible(true);

        if (caminos.isEmpty()) {
            Label lblVacio = new Label("No se exploraron rutas para la cadena evaluada.");
            lblVacio.setFont(Font.font("Segoe UI", 12));
            lblVacio.setTextFill(Color.BLACK);
            lblVacio.setStyle("-fx-text-fill: #000000 !important;");
            contenedorRutas.getChildren().add(lblVacio);
            return;
        }

        for (int i = 0; i < caminos.size(); i++) {
            CaminoSimulacion camino = caminos.get(i);
            VBox tarjetaRuta = crearTarjetaRuta(camino, i + 1, caminos.size() > 1);
            contenedorRutas.getChildren().add(tarjetaRuta);
        }
    }

    private VBox crearTarjetaRuta(CaminoSimulacion camino, int numeroRuta, boolean esMultiple) {
        VBox tarjeta = new VBox(6);
        tarjeta.getStyleClass().add("tarjeta-recorrido");
        tarjeta.setPadding(new Insets(8, 12, 8, 12));

        String colorBorde = "#E5E7EB";
        String colorFondo = "#FFFFFF";

        if (camino.esExitoso()) {
            colorBorde = PaletaColor.HEX_ACEPTACION;
            colorFondo = PaletaColor.HEX_ACEPTACION_CLARO;
        } else if (camino.esCaminoMuerto()) {
            colorBorde = PaletaColor.HEX_ADVERTENCIA;
            colorFondo = PaletaColor.HEX_ADVERTENCIA_CLARO;
        }

        tarjeta.setStyle(
                "-fx-background-color: " + colorFondo + "; "
                        + "-fx-background-radius: 6px; "
                        + "-fx-border-color: " + colorBorde + "; "
                        + "-fx-border-radius: 6px; "
                        + "-fx-text-fill: #000000 !important; "
                        + "-fx-cursor: hand;"
        );

        // 1. Fila de título y badge de estado de la ruta
        HBox filaTitulo = new HBox(8);
        filaTitulo.setAlignment(Pos.CENTER_LEFT);

        String tituloTexto = esMultiple ? "Ruta " + numeroRuta : "Recorrido";
        Label lblTituloRuta = new Label(tituloTexto);
        lblTituloRuta.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTituloRuta.setTextFill(Color.BLACK);
        lblTituloRuta.setStyle("-fx-text-fill: #000000 !important; -fx-font-weight: bold;");

        Label badgeEstado = new Label();
        badgeEstado.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badgeEstado.setTextFill(Color.BLACK);
        if (camino.esExitoso()) {
            badgeEstado.setText("«Aceptada»");
            badgeEstado.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #000000 !important; -fx-border-color: #10B981; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 2px 8px; -fx-font-weight: bold;");
        } else if (camino.esCaminoMuerto()) {
            badgeEstado.setText("«Camino Muerto»");
            badgeEstado.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #000000 !important; -fx-border-color: #F59E0B; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 2px 8px; -fx-font-weight: bold;");
        } else {
            badgeEstado.setText("«No Aceptada»");
            badgeEstado.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #000000 !important; -fx-border-color: #EF4444; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 2px 8px; -fx-font-weight: bold;");
        }

        filaTitulo.getChildren().addAll(lblTituloRuta, badgeEstado);

        // 2. Fila visual con el flujo secuencial de estados y transiciones
        HBox flujoVisual = new HBox(6);
        flujoVisual.setAlignment(Pos.CENTER_LEFT);

        // Estado inicial
        flujoVisual.getChildren().add(crearChipEstado(camino.getEstadoInicial(), true, camino.getPasos().isEmpty() && camino.esExitoso()));

        // Pasos intermedios
        List<PasoTransicion> pasos = camino.getPasos();
        for (int p = 0; p < pasos.size(); p++) {
            PasoTransicion paso = pasos.get(p);
            boolean esUltimo = (p == pasos.size() - 1);

            // Flecha con símbolo
            Label flecha = new Label("── " + paso.getSimbolo() + " ──►");
            flecha.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            flecha.setTextFill(Color.BLACK);
            flecha.setStyle("-fx-text-fill: #000000 !important; -fx-font-weight: bold;");

            // Estado destino
            boolean destinoAceptacion = esUltimo && camino.esExitoso();
            Label chipDestino = crearChipEstado(paso.getDestino(), false, destinoAceptacion);

            flujoVisual.getChildren().addAll(flecha, chipDestino);
        }

        // Si es camino muerto, agregar indicador de bloqueo
        if (camino.esCaminoMuerto()) {
            Label lblBloqueo = new Label("──✖ (Bloqueo)");
            lblBloqueo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            lblBloqueo.setTextFill(Color.BLACK);
            lblBloqueo.setStyle("-fx-text-fill: #000000 !important; -fx-font-weight: bold;");
            flujoVisual.getChildren().add(lblBloqueo);
        }

        // 3. Detalle explicativo de la ruta (texto en negro)
        Label lblDescripcion = new Label(camino.getDescripcion());
        lblDescripcion.setFont(Font.font("Segoe UI", 11));
        lblDescripcion.setTextFill(Color.BLACK);
        lblDescripcion.setStyle("-fx-text-fill: #000000 !important;");

        tarjeta.getChildren().addAll(filaTitulo, flujoVisual, lblDescripcion);

        // Acción al hacer clic en la tarjeta de ruta: invocar callback de selección
        tarjeta.setOnMouseClicked(e -> {
            if (onCaminoSeleccionado != null) {
                onCaminoSeleccionado.accept(camino);
            }
        });

        return tarjeta;
    }

    private Label crearChipEstado(Estado estado, boolean esInicial, boolean esAceptacionFinal) {
        String texto = (esInicial ? "▶ " : "") + estado.getNombre();
        Label chip = new Label(texto);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        chip.setTextFill(Color.BLACK);

        String borde = esAceptacionFinal ? PaletaColor.HEX_ACEPTACION : "#9CA3AF";
        String fondo = esAceptacionFinal ? "#ECFDF5" : "#FFFFFF";

        chip.setStyle(
                "-fx-background-color: " + fondo + "; "
                        + "-fx-text-fill: #000000 !important; "
                        + "-fx-border-color: " + borde + "; "
                        + "-fx-border-radius: 12px; "
                        + "-fx-background-radius: 12px; "
                        + "-fx-padding: 2px 10px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-border-width: " + (esAceptacionFinal ? "2px" : "1px") + ";"
        );
        return chip;
    }

    /**
     * Muestra un mensaje explicativo ante errores que impiden la simulación (ej. falta de inicial o símbolos ajenos).
     *
     * @param mensaje Mensaje de advertencia o error.
     */
    public void mostrarError(String mensaje) {
        contenedorRutas.getChildren().clear();
        etiquetaResumenRutas.setText("Evaluación no realizada");
        etiquetaResumenRutas.setStyle("-fx-text-fill: #000000 !important;");
        etiquetaResumenRutas.setVisible(true);

        Label lblError = new Label("⚠ " + (mensaje != null ? mensaje : ""));
        lblError.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblError.setTextFill(Color.BLACK);
        lblError.setStyle("-fx-text-fill: #000000 !important; -fx-font-weight: bold;");
        lblError.setWrapText(true);

        contenedorRutas.getChildren().add(lblError);
    }

    /**
     * Restablece la vista al estado inicial sin recorrido activo con texto en negro.
     */
    public void limpiar() {
        contenedorRutas.getChildren().clear();
        etiquetaResumenRutas.setText("");
        etiquetaResumenRutas.setStyle("-fx-text-fill: #000000 !important;");
        etiquetaResumenRutas.setVisible(false);

        Label lblPlaceholder = new Label("Ingrese una cadena de prueba y presione «Comprobar» para visualizar el recorrido de estados.");
        lblPlaceholder.setFont(Font.font("Segoe UI", 12));
        lblPlaceholder.setTextFill(Color.BLACK);
        lblPlaceholder.setStyle("-fx-text-fill: #000000 !important;");

        contenedorRutas.getChildren().add(lblPlaceholder);
    }

    // =========================================================================
    // 4. GETTERS Y SETTERS
    // =========================================================================

    public VBox getContenedorRutas() {
        return contenedorRutas;
    }

    public Label getEtiquetaResumenRutas() {
        return etiquetaResumenRutas;
    }

    public void setOnCaminoSeleccionado(Consumer<CaminoSimulacion> onCaminoSeleccionado) {
        this.onCaminoSeleccionado = onCaminoSeleccionado;
    }
}
