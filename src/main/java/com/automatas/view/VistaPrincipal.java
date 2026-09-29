package com.automatas.view;

import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * Ventana raíz y contenedor visual principal de la aplicación multi-pestaña (RF-08).
 * <p>
 * Estructura:
 * <ul>
 *   <li><b>Superior (Top):</b> Barra de herramientas con título institucional, botón «Modo América» y botón de acción rápida «+ Nuevo Autómata».</li>
 *   <li><b>Centro (Center):</b> {@link TabPane} con políticas de cierre de pestañas habilitadas y navegación desacoplada.</li>
 *   <li><b>Estética (Constitución Principio 7):</b> Tonos neutros, grafito y esmeralda, 0% azul y 0% café.</li>
 * </ul>
 */
public class VistaPrincipal extends BorderPane {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final TabPane tabPane;
    private final Button btnNuevoAutomata;
    private final Button btnModoAmerica;
    private Runnable onNuevoAutomata;
    private Consumer<Boolean> onModoAmericaCambiado;
    private boolean modoAmericaActivo;

    // =========================================================================
    // 2. CONSTRUCTOR
    // =========================================================================

    /**
     * Construye y ensambla la vista principal de la aplicación.
     */
    public VistaPrincipal() {
        this.modoAmericaActivo = false;
        setStyle("-fx-background-color: " + PaletaColor.HEX_FONDO_PRINCIPAL + ";");

        // Paso 1: Barra de herramientas superior
        HBox barraSuperior = new HBox(12);
        barraSuperior.getStyleClass().add("barra-superior-app");
        barraSuperior.setAlignment(Pos.CENTER_LEFT);
        barraSuperior.setPadding(new Insets(10, 16, 10, 16));
        barraSuperior.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1px 0;");

        Label lblTituloApp = new Label("Autómatas (AFD / AFN)");
        lblTituloApp.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTituloApp.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        lblTituloApp.setStyle("-fx-text-fill: #000000 !important;");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        this.btnModoAmerica = new Button("Modo América");
        this.btnModoAmerica.getStyleClass().addAll("button-secundario", "btn-modo-america");
        this.btnModoAmerica.setOnAction(ev -> alternarModoAmerica());

        this.btnNuevoAutomata = new Button("+ Nuevo Autómata");
        this.btnNuevoAutomata.getStyleClass().add("button-primario");
        this.btnNuevoAutomata.setOnAction(ev -> {
            if (onNuevoAutomata != null) {
                onNuevoAutomata.run();
            }
        });

        barraSuperior.getChildren().addAll(lblTituloApp, espaciador, btnModoAmerica, btnNuevoAutomata);
        setTop(barraSuperior);

        // Paso 2: TabPane central para albergar múltiples lienzos independientes (RF-08)
        this.tabPane = new TabPane();
        this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        this.tabPane.setStyle("-fx-background-color: " + PaletaColor.HEX_FONDO_PRINCIPAL + ";");

        setCenter(tabPane);
    }

    // =========================================================================
    // 3. MODO AMÉRICA (TEMA ROJO Y BLANCO)
    // =========================================================================

    /**
     * Alterna la activación del Modo América (rojo y blanco).
     */
    public void alternarModoAmerica() {
        setModoAmerica(!modoAmericaActivo);
    }

    /**
     * Establece explícitamente el estado del Modo América.
     *
     * @param activo {@code true} para activar el tema rojo y blanco, {@code false} para regresar al clásico.
     */
    public void setModoAmerica(boolean activo) {
        this.modoAmericaActivo = activo;
        if (activo) {
            if (!getStyleClass().contains("modo-america")) {
                getStyleClass().add("modo-america");
            }
            btnModoAmerica.setText("Modo América (Activo)");
        } else {
            getStyleClass().remove("modo-america");
            btnModoAmerica.setText("Modo América");
        }

        if (onModoAmericaCambiado != null) {
            onModoAmericaCambiado.accept(activo);
        }
    }

    public boolean isModoAmericaActivo() {
        return modoAmericaActivo;
    }

    public Button getBotonModoAmerica() {
        return btnModoAmerica;
    }

    public void setOnModoAmericaCambiado(Consumer<Boolean> onModoAmericaCambiado) {
        this.onModoAmericaCambiado = onModoAmericaCambiado;
    }

    // =========================================================================
    // 4. GETTERS Y SETTERS
    // =========================================================================

    public TabPane getTabPane() {
        return tabPane;
    }

    public Button getBotonNuevoAutomata() {
        return btnNuevoAutomata;
    }

    public void setOnNuevoAutomata(Runnable onNuevoAutomata) {
        this.onNuevoAutomata = onNuevoAutomata;
    }
}
