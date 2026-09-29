package com.automatas.view.componente;

import com.automatas.model.TipoAutomata;
import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * Componente visual de la barra superior de cada pestaña de autómata (RF-01, RF-05, Casos Límite 10 y 18).
 * <p>
 * Funcionalidades y reglas de diseño:
 * <ul>
 *   <li><b>Definición del Alfabeto (RF-01):</b> Campo para ingresar los símbolos de $\Sigma$ y botón de confirmación.</li>
 *   <li><b>Bloqueo estricto (Caso Límite 10):</b> Inhabilita automáticamente la edición de $\Sigma$ cuando
 *       existan transiciones activas en el lienzo.</li>
 *   <li><b>Indicador reactivo de clasificación (RF-05):</b> Etiqueta destacada en tiempo real
 *       con los estados {@code «Sin estados»}, {@code «AFD»} o {@code «AFN»}.</li>
 *   <li><b>Restricción cromática estricta (Constitución Principio 7):</b> Prohibición absoluta de tonos azul y café.</li>
 * </ul>
 */
public class PanelSuperiorVista extends HBox {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final TextField campoAlfabeto;
    private final Button btnEstablecer;
    private final Button btnAgregarEstado;
    private final Label etiquetaTipo;
    private final Button btnConvertir;
    private final Label etiquetaMensaje;

    private boolean alfabetoBloqueado;
    private Consumer<String> onDefinirAlfabeto;
    private Runnable onAgregarEstado;
    private Runnable onConvertir;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye la barra superior con sus controles y estilos institucionales.
     */
    public PanelSuperiorVista() {
        super(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 15, 10, 15));
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 6px; -fx-border-color: #E5E7EB; -fx-border-radius: 6px;");

        this.alfabetoBloqueado = false;

        // Paso 1: Sección de Alfabeto (Σ)
        Label lblAlfabeto = new Label("Alfabeto (Σ):");
        lblAlfabeto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblAlfabeto.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        lblAlfabeto.setStyle("-fx-text-fill: #000000 !important;");

        this.campoAlfabeto = new TextField();
        this.campoAlfabeto.setPromptText("ej. a, b");
        this.campoAlfabeto.setPrefWidth(130);
        this.campoAlfabeto.setFont(Font.font("Segoe UI", 12));
        this.campoAlfabeto.setStyle("-fx-text-fill: #000000 !important;");

        this.btnEstablecer = new Button("Definir");
        this.btnEstablecer.getStyleClass().add("button-primario");
        this.btnEstablecer.setOnAction(ev -> confirmarAlfabeto());
        this.campoAlfabeto.setOnAction(ev -> confirmarAlfabeto());

        // Paso 2: Acción rápida de agregar estado (RF-02)
        Separator separador1 = new Separator(Orientation.VERTICAL);

        this.btnAgregarEstado = new Button("Agregar estado");
        this.btnAgregarEstado.getStyleClass().add("button-secundario");
        this.btnAgregarEstado.setStyle("-fx-text-fill: #000000 !important;");
        this.btnAgregarEstado.setOnAction(ev -> {
            if (onAgregarEstado != null) {
                onAgregarEstado.run();
            }
        });

        // Paso 3: Separador visual
        Separator separador2 = new Separator(Orientation.VERTICAL);

        // Paso 4: Sección de Clasificación del Autómata (RF-05)
        Label lblClasificacion = new Label("Clasificación:");
        lblClasificacion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblClasificacion.setTextFill(PaletaColor.TEXTO_PRINCIPAL);
        lblClasificacion.setStyle("-fx-text-fill: #000000 !important;");

        this.etiquetaTipo = new Label(TipoAutomata.SIN_ESTADOS.getEtiqueta());
        this.etiquetaTipo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        actualizarEstiloBadgeTipo(TipoAutomata.SIN_ESTADOS);

        // Paso 5: Botón de Conversión AFN -> AFD (RF-07)
        this.btnConvertir = new Button("Convertir a AFD");
        this.btnConvertir.getStyleClass().add("button-secundario");
        this.btnConvertir.setStyle("-fx-text-fill: #000000 !important;");
        this.btnConvertir.setOnAction(ev -> {
            if (onConvertir != null) {
                onConvertir.run();
            }
        });

        // Paso 6: Etiqueta de advertencia o información contextual
        this.etiquetaMensaje = new Label("");
        this.etiquetaMensaje.setFont(Font.font("Segoe UI", 11));
        this.etiquetaMensaje.setTextFill(PaletaColor.PELIGRO);
        this.etiquetaMensaje.setVisible(false);
        HBox.setHgrow(this.etiquetaMensaje, Priority.ALWAYS);

        // Paso 7: Ensamblar componentes
        getChildren().addAll(
                lblAlfabeto,
                campoAlfabeto,
                btnEstablecer,
                separador1,
                btnAgregarEstado,
                separador2,
                lblClasificacion,
                etiquetaTipo,
                btnConvertir,
                etiquetaMensaje
        );
    }

    private void confirmarAlfabeto() {
        if (alfabetoBloqueado) {
            mostrarMensaje("Debe eliminar las transiciones existentes antes de reconfigurar el alfabeto.");
            return;
        }

        if (onDefinirAlfabeto != null) {
            String texto = campoAlfabeto.getText();
            onDefinirAlfabeto.accept(texto != null ? texto : "");
        }
    }

    // =========================================================================
    // 3. MÉTODOS DE MUTACIÓN Y ACTUALIZACIÓN VISUAL
    // =========================================================================

    /**
     * Actualiza la clasificación visible del autómata (RF-05).
     *
     * @param tipo Clasificación formal {@link TipoAutomata}.
     */
    public void actualizarTipoAutomata(TipoAutomata tipo) {
        if (tipo == null) {
            tipo = TipoAutomata.SIN_ESTADOS;
        }
        etiquetaTipo.setText(tipo.getEtiqueta());
        actualizarEstiloBadgeTipo(tipo);
    }

    private void actualizarEstiloBadgeTipo(TipoAutomata tipo) {
        switch (tipo) {
            case AFD -> etiquetaTipo.setStyle(
                    "-fx-background-color: " + PaletaColor.HEX_ACEPTACION_CLARO + "; "
                            + "-fx-text-fill: #065F46; "
                            + "-fx-padding: 3px 10px; "
                            + "-fx-background-radius: 12px; "
                            + "-fx-border-color: " + PaletaColor.HEX_ACEPTACION + "; "
                            + "-fx-border-radius: 12px;"
            );
            case AFN -> etiquetaTipo.setStyle(
                    "-fx-background-color: " + PaletaColor.HEX_ADVERTENCIA_CLARO + "; "
                            + "-fx-text-fill: #92400E; "
                            + "-fx-padding: 3px 10px; "
                            + "-fx-background-radius: 12px; "
                            + "-fx-border-color: " + PaletaColor.HEX_ADVERTENCIA + "; "
                            + "-fx-border-radius: 12px;"
            );
            default -> etiquetaTipo.setStyle(
                    "-fx-background-color: #F3F4F6; "
                            + "-fx-text-fill: #6B7280; "
                            + "-fx-padding: 3px 10px; "
                            + "-fx-background-radius: 12px; "
                            + "-fx-border-color: #D1D5DB; "
                            + "-fx-border-radius: 12px;"
            );
        }
    }

    /**
     * Bloquea o desbloquea los controles de edición del alfabeto formal (Caso Límite 10).
     *
     * @param bloquear {@code true} para inhabilitar; {@code false} para habilitar.
     */
    public void bloquearAlfabeto(boolean bloquear) {
        this.alfabetoBloqueado = bloquear;
        this.campoAlfabeto.setDisable(bloquear);
        this.btnEstablecer.setDisable(bloquear);

        if (bloquear) {
            Tooltip tip = new Tooltip("El alfabeto está bloqueado porque existen transiciones en el lienzo.");
            Tooltip.install(campoAlfabeto, tip);
        } else {
            Tooltip.uninstall(campoAlfabeto, null);
            limpiarMensaje();
        }
    }

    /**
     * Despliega un mensaje informativo o de advertencia al usuario.
     *
     * @param mensaje Texto del mensaje.
     */
    public void mostrarMensaje(String mensaje) {
        etiquetaMensaje.setText(mensaje != null ? mensaje : "");
        etiquetaMensaje.setVisible(mensaje != null && !mensaje.isEmpty());
    }

    /**
     * Oculta y limpia el mensaje informativo.
     */
    public void limpiarMensaje() {
        mostrarMensaje("");
    }

    // =========================================================================
    // 4. GETTERS Y SETTERS
    // =========================================================================

    public TextField getCampoAlfabeto() {
        return campoAlfabeto;
    }

    public Button getBotonEstablecer() {
        return btnEstablecer;
    }

    public Label getEtiquetaTipo() {
        return etiquetaTipo;
    }

    public Label getEtiquetaMensaje() {
        return etiquetaMensaje;
    }

    public String getMensaje() {
        return etiquetaMensaje.getText();
    }

    public boolean esAlfabetoBloqueado() {
        return alfabetoBloqueado;
    }

    public boolean isAlfabetoBloqueado() {
        return alfabetoBloqueado;
    }

    public Button getBotonConvertir() {
        return btnConvertir;
    }

    public Button getBotonAgregarEstado() {
        return btnAgregarEstado;
    }

    public void setOnAgregarEstado(Runnable onAgregarEstado) {
        this.onAgregarEstado = onAgregarEstado;
    }

    public void habilitarCreacionEstados(boolean habilitar) {
        this.btnAgregarEstado.setDisable(!habilitar);
        this.btnAgregarEstado.setVisible(habilitar);
    }

    public void setOnConvertir(Runnable onConvertir) {
        this.onConvertir = onConvertir;
    }

    public void habilitarConversion(boolean habilitar) {
        this.btnConvertir.setDisable(!habilitar);
        this.btnConvertir.setVisible(habilitar);
    }

    public void setOnDefinirAlfabeto(Consumer<String> onDefinirAlfabeto) {
        this.onDefinirAlfabeto = onDefinirAlfabeto;
    }
}
