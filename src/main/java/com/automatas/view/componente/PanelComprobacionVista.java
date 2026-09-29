package com.automatas.view.componente;

import com.automatas.model.ResultadoSimulacion;
import com.automatas.view.util.PaletaColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * Componente visual de la barra inferior para la comprobación inmediata de cadenas y
 * visualización del recorrido completo de procesamiento (RF-06).
 * <p>
 * Reglas de diseño y casos límite (RF-06, RNF-02, Constitución Principio 7):
 * <ul>
 *   <li><b>Entrada con recorte automático:</b> Campo de texto para la palabra de prueba, aplicando {@code trim()}.</li>
 *   <li><b>Veredicto binario inmediato:</b> Emite sin parpadeos {@code «Aceptada»} (en verde esmeralda)
 *       o {@code «Rechazada»} (en rojo neutro o ámbar).</li>
 *   <li><b>Recorrido completo interactivo:</b> Integra {@link VistaRecorridoCadena} para desplegar el flujo
 *       de estados visitados y ramas exploradas.</li>
 *   <li><b>Restricción cromática estricta:</b> 0% de componentes en tonos azul o café.</li>
 * </ul>
 */
public class PanelComprobacionVista extends VBox {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final TextField campoCadena;
    private final Button btnComprobar;
    private final Label etiquetaVeredicto;
    private final Button btnAlternarRecorrido;
    private final VistaRecorridoCadena vistaRecorrido;

    private Consumer<String> onComprobar;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye el panel de comprobación con sus controles y estilos institucionales.
     */
    public PanelComprobacionVista() {
        super(10);
        setPadding(new Insets(10, 15, 10, 15));
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 6px; -fx-border-color: #E5E7EB; -fx-border-radius: 6px;");

        // Paso 1: Fila superior de controles (HBox)
        HBox filaControles = new HBox(12);
        filaControles.setAlignment(Pos.CENTER_LEFT);

        Label lblCadena = new Label("Cadena de prueba:");
        lblCadena.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblCadena.setTextFill(javafx.scene.paint.Color.BLACK);
        lblCadena.setStyle("-fx-text-fill: #000000 !important;");

        this.campoCadena = new TextField();
        this.campoCadena.setPromptText("ej. abba (o vacío para λ)");
        this.campoCadena.setPrefWidth(220);
        this.campoCadena.setFont(Font.font("Segoe UI", 12));
        this.campoCadena.setStyle("-fx-text-fill: #000000 !important;");

        this.btnComprobar = new Button("Comprobar");
        this.btnComprobar.getStyleClass().add("button-exito");
        this.btnComprobar.setOnAction(ev -> ejecutarComprobacion());
        this.campoCadena.setOnAction(ev -> ejecutarComprobacion());

        this.etiquetaVeredicto = new Label("");
        this.etiquetaVeredicto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        this.btnAlternarRecorrido = new Button("Ocultar recorrido");
        this.btnAlternarRecorrido.getStyleClass().add("button-secundario");
        this.btnAlternarRecorrido.setStyle("-fx-text-fill: #000000 !important;");
        this.btnAlternarRecorrido.setFont(Font.font("Segoe UI", 11));
        this.btnAlternarRecorrido.setOnAction(ev -> alternarVisibilidadRecorrido());

        filaControles.getChildren().addAll(lblCadena, campoCadena, btnComprobar, etiquetaVeredicto, espaciador, btnAlternarRecorrido);

        // Paso 2: Vista del recorrido detallado de la cadena
        this.vistaRecorrido = new VistaRecorridoCadena();

        // Paso 3: Ensamblar contenedor principal
        getChildren().addAll(filaControles, vistaRecorrido);
    }

    private void ejecutarComprobacion() {
        if (onComprobar != null) {
            onComprobar.accept(getCadena());
        }
    }

    private void alternarVisibilidadRecorrido() {
        boolean nuevoEstado = !vistaRecorrido.isVisible();
        vistaRecorrido.setVisible(nuevoEstado);
        vistaRecorrido.setManaged(nuevoEstado);
        btnAlternarRecorrido.setText(nuevoEstado ? "Ocultar recorrido" : "Ver recorrido");
    }

    // =========================================================================
    // 3. MÉTODOS DE MUTACIÓN Y ACTUALIZACIÓN VISUAL
    // =========================================================================

    /**
     * Muestra el veredicto binario con estilos de alto contraste (RF-06).
     *
     * @param aceptada {@code true} si la cadena es aceptada; {@code false} si es rechazada.
     */
    public void mostrarVeredicto(boolean aceptada) {
        if (aceptada) {
            etiquetaVeredicto.setText("«Aceptada»");
            etiquetaVeredicto.setStyle(
                    "-fx-background-color: " + PaletaColor.HEX_ACEPTACION_CLARO + "; "
                            + "-fx-text-fill: #065F46; "
                            + "-fx-padding: 4px 14px; "
                            + "-fx-background-radius: 12px; "
                            + "-fx-border-color: " + PaletaColor.HEX_ACEPTACION + "; "
                            + "-fx-border-radius: 12px;"
            );
        } else {
            etiquetaVeredicto.setText("«Rechazada»");
            etiquetaVeredicto.setStyle(
                    "-fx-background-color: " + PaletaColor.HEX_PELIGRO_CLARO + "; "
                            + "-fx-text-fill: #991B1B; "
                            + "-fx-padding: 4px 14px; "
                            + "-fx-background-radius: 12px; "
                            + "-fx-border-color: " + PaletaColor.HEX_PELIGRO + "; "
                            + "-fx-border-radius: 12px;"
            );
        }
    }

    /**
     * Muestra el resultado completo de la simulación incluyendo el veredicto y el recorrido paso a paso.
     *
     * @param resultado Resultado formal de la evaluación {@link ResultadoSimulacion}.
     */
    public void mostrarResultado(ResultadoSimulacion resultado) {
        if (resultado == null) {
            limpiar();
            return;
        }
        mostrarVeredicto(resultado.esAceptada());
        vistaRecorrido.mostrarResultado(resultado);

        // Asegurar que el recorrido sea visible si estaba colapsado
        vistaRecorrido.setVisible(true);
        vistaRecorrido.setManaged(true);
        btnAlternarRecorrido.setText("Ocultar recorrido");
    }

    /**
     * Muestra un mensaje de advertencia o error en la evaluación (ej. símbolos ajenos o falta de estado inicial).
     *
     * @param mensaje Mensaje explicativo en español.
     */
    public void mostrarError(String mensaje) {
        etiquetaVeredicto.setText(mensaje != null ? mensaje : "");
        etiquetaVeredicto.setStyle(
                "-fx-background-color: " + PaletaColor.HEX_ADVERTENCIA_CLARO + "; "
                        + "-fx-text-fill: #92400E; "
                        + "-fx-padding: 4px 14px; "
                        + "-fx-background-radius: 12px; "
                        + "-fx-border-color: " + PaletaColor.HEX_ADVERTENCIA + "; "
                        + "-fx-border-radius: 12px;"
        );
        vistaRecorrido.mostrarError(mensaje);
    }

    /**
     * Limpia la entrada de texto y restablece el veredicto y el recorrido.
     */
    public void limpiar() {
        campoCadena.setText("");
        etiquetaVeredicto.setText("");
        etiquetaVeredicto.setStyle("");
        vistaRecorrido.limpiar();
    }

    // =========================================================================
    // 4. GETTERS Y SETTERS
    // =========================================================================

    /**
     * Obtiene el texto ingresado tras aplicar recorte de espacios en blanco ({@code trim()}).
     *
     * @return Cadena normalizada de prueba.
     */
    public String getCadena() {
        String texto = campoCadena.getText();
        return texto != null ? texto.trim() : "";
    }

    public void setCadena(String cadena) {
        campoCadena.setText(cadena != null ? cadena : "");
    }

    public TextField getCampoCadena() {
        return campoCadena;
    }

    public Button getBotonComprobar() {
        return btnComprobar;
    }

    public Label getEtiquetaVeredicto() {
        return etiquetaVeredicto;
    }

    public Button getBotonAlternarRecorrido() {
        return btnAlternarRecorrido;
    }

    public VistaRecorridoCadena getVistaRecorrido() {
        return vistaRecorrido;
    }

    public void setOnComprobar(Consumer<String> onComprobar) {
        this.onComprobar = onComprobar;
    }
}
