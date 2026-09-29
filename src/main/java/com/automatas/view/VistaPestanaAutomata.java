package com.automatas.view;

import com.automatas.view.componente.PanelComprobacionVista;
import com.automatas.view.componente.PanelSuperiorVista;
import com.automatas.view.componente.TablaTransicionesVista;
import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Contenedor visual compuesto que ensambla una pestaña de trabajo de autómata (RF-08).
 * <p>
 * Estructura jerárquica nativa basada en {@link BorderPane}:
 * <ul>
 *   <li><b>Superior (Top):</b> {@link PanelSuperiorVista} con configuración de alfabeto y clasificación viva.</li>
 *   <li><b>Centro (Center):</b> {@link SplitPane} colapsable con {@link LienzoGrafo} a la izquierda
 *       y {@link TablaTransicionesVista} a la derecha.</li>
 *   <li><b>Inferior (Bottom):</b> {@link PanelComprobacionVista} para evaluación de palabras y veredicto.</li>
 * </ul>
 */
public class VistaPestanaAutomata extends BorderPane {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final PanelSuperiorVista panelSuperior;
    private final LienzoGrafo lienzo;
    private final TablaTransicionesVista tablaTransiciones;
    private final PanelComprobacionVista panelComprobacion;
    private final SplitPane splitPane;
    private final VBox contenedorTabla;
    private final Label etiquetaTituloTabla;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye y ensambla la vista completa de la pestaña de autómata.
     */
    public VistaPestanaAutomata() {
        setPadding(new Insets(10));
        setStyle("-fx-background-color: " + PaletaColor.HEX_FONDO_PRINCIPAL + ";");

        // Paso 1: Barra superior (Alfabeto y Clasificación)
        this.panelSuperior = new PanelSuperiorVista();
        this.panelSuperior.getStyleClass().add("panel-superior");
        setTop(panelSuperior);
        BorderPane.setMargin(panelSuperior, new Insets(0, 0, 10, 0));

        // Paso 2: Panel central con SplitPane
        this.lienzo = new LienzoGrafo();

        this.tablaTransiciones = new TablaTransicionesVista();
        VBox.setVgrow(tablaTransiciones, Priority.ALWAYS);

        this.etiquetaTituloTabla = new Label("Tabla de Transiciones");
        this.etiquetaTituloTabla.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        this.etiquetaTituloTabla.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        this.etiquetaTituloTabla.setStyle("-fx-text-fill: #000000 !important;");

        this.contenedorTabla = new VBox(8, etiquetaTituloTabla, tablaTransiciones);
        this.contenedorTabla.getStyleClass().add("contenedor-tabla");
        this.contenedorTabla.setPadding(new Insets(10));
        this.contenedorTabla.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 6px; -fx-border-color: #E5E7EB; -fx-border-radius: 6px;");

        this.splitPane = new SplitPane(lienzo, contenedorTabla);
        this.splitPane.setDividerPositions(0.65);
        setCenter(splitPane);

        // Paso 3: Barra inferior (Comprobación de Cadenas)
        this.panelComprobacion = new PanelComprobacionVista();
        this.panelComprobacion.getStyleClass().add("panel-comprobacion");
        setBottom(panelComprobacion);
        BorderPane.setMargin(panelComprobacion, new Insets(10, 0, 0, 0));

        getStyleClass().add("vista-pestana-automata");
    }

    // =========================================================================
    // 3. GETTERS Y ACCESORES
    // =========================================================================

    public PanelSuperiorVista getPanelSuperior() {
        return panelSuperior;
    }

    public LienzoGrafo getLienzo() {
        return lienzo;
    }

    public TablaTransicionesVista getTablaTransiciones() {
        return tablaTransiciones;
    }

    public PanelComprobacionVista getPanelComprobacion() {
        return panelComprobacion;
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    public VBox getContenedorTabla() {
        return contenedorTabla;
    }

    public Label getEtiquetaTituloTabla() {
        return etiquetaTituloTabla;
    }

    /**
     * Configura el modo protegido de inspección sobre la vista (Caso Límite 20).
     *
     * @param modoProtegido {@code true} para bloquear mutaciones de topología y alfabeto.
     */
    public void setModoProtegido(boolean modoProtegido) {
        lienzo.setModoProtegido(modoProtegido);
        panelSuperior.bloquearAlfabeto(modoProtegido);
        panelSuperior.habilitarConversion(!modoProtegido);
        panelSuperior.habilitarCreacionEstados(!modoProtegido);
    }

    public boolean esModoProtegido() {
        return lienzo.esModoProtegido();
    }
}
