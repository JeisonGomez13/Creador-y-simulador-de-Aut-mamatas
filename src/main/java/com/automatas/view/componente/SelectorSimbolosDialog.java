package com.automatas.view.componente;

import com.automatas.model.Alfabeto;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Diálogo modal asistido para la selección interactiva de símbolos del alfabeto formal (RF-04).
 * <p>
 * Reglas de diseño y casos límite (RF-04, Casos Límite 7 y 8):
 * <ul>
 *   <li><b>Generación dinámica de casillas:</b> Presenta casillas de verificación ({@link CheckBox})
 *       correspondientes de forma exclusiva a los símbolos de $\Sigma$ activo, eliminando la posibilidad
 *       de ingresar caracteres ajenos o transiciones vacías ($\varepsilon$).</li>
 *   <li><b>Cancelación limpia (Caso Límite 8):</b> Al presionar Cancelar, pulsar Escape o cerrar la ventana,
 *       retorna una lista vacía inmutable para abortar el trazado sin crear aristas huérfanas en el grafo.</li>
 *   <li><b>Confirmación con retorno filtrado:</b> Al presionar Aceptar, retorna la lista ordenada de símbolos
 *       efectivamente marcados por el usuario.</li>
 * </ul>
 */
public class SelectorSimbolosDialog extends Dialog<List<Character>> {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Alfabeto alfabeto;
    private final List<CheckBox> casillas;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye el diálogo asistido con preselección opcional de símbolos.
     *
     * @param alfabeto Alfabeto formal activo $\Sigma$.
     * @param simbolosPreseleccionados Conjunto de símbolos que deben nacer marcados (o vacío).
     * @throws IllegalArgumentException Si el alfabeto formal proporcionado es nulo.
     */
    public SelectorSimbolosDialog(Alfabeto alfabeto, Set<Character> simbolosPreseleccionados) {
        if (alfabeto == null) {
            throw new IllegalArgumentException("El alfabeto formal no puede ser nulo.");
        }

        this.alfabeto = alfabeto;
        this.casillas = new ArrayList<>();

        // Paso 1: Configurar títulos y textos del encabezado
        setTitle("Transición - Selección de Símbolos");
        setHeaderText("Seleccione los símbolos del alfabeto formal para esta transición:");

        // Paso 2: Configurar botones estándar (Aceptar y Cancelar)
        ButtonType botonAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(botonAceptar, botonCancelar);

        // Paso 3: Construir contenedor dinámico con las casillas de verificación
        FlowPane contenedorCasillas = new FlowPane();
        contenedorCasillas.setHgap(16);
        contenedorCasillas.setVgap(12);
        contenedorCasillas.setPadding(new Insets(15, 10, 15, 10));

        Set<Character> preseleccion = simbolosPreseleccionados != null ? simbolosPreseleccionados : Collections.emptySet();

        for (char simbolo : alfabeto.getSimbolos()) {
            CheckBox casilla = new CheckBox(String.valueOf(simbolo));
            casilla.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

            if (preseleccion.contains(simbolo)) {
                casilla.setSelected(true);
            }

            casillas.add(casilla);
            contenedorCasillas.getChildren().add(casilla);
        }

        getDialogPane().setContent(contenedorCasillas);

        // Paso 4: Cargar hoja de estilos institucional si está disponible
        URL cssUrl = getClass().getResource("/css/estilos.css");
        if (cssUrl != null) {
            getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }

        // Paso 5: Conversor formal del resultado (Caso Límite 8: retorno vacío ante cancelación)
        setResultConverter(botonPulsado -> {
            if (botonPulsado != null && botonPulsado.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return obtenerSimbolosSeleccionados();
            }
            return Collections.emptyList();
        });
    }

    /**
     * Construye el diálogo asistido sin símbolos preseleccionados.
     *
     * @param alfabeto Alfabeto formal activo $\Sigma$.
     */
    public SelectorSimbolosDialog(Alfabeto alfabeto) {
        this(alfabeto, Collections.emptySet());
    }

    // =========================================================================
    // 3. MÉTODOS DE CONSULTA Y MANIPULACIÓN
    // =========================================================================

    /**
     * Retorna la lista ordenada de símbolos actualmente marcados en las casillas.
     *
     * @return Lista inmutable de caracteres seleccionados.
     */
    public List<Character> obtenerSimbolosSeleccionados() {
        List<Character> seleccionados = new ArrayList<>();
        for (CheckBox cb : casillas) {
            if (cb.isSelected()) {
                seleccionados.add(cb.getText().charAt(0));
            }
        }
        return Collections.unmodifiableList(seleccionados);
    }

    /**
     * Permite marcar o desmarcar programáticamente la casilla correspondiente a un símbolo.
     *
     * @param simbolo Símbolo a modificar.
     * @param seleccionado {@code true} para marcar; {@code false} para desmarcar.
     */
    public void seleccionarSimbolo(char simbolo, boolean seleccionado) {
        for (CheckBox cb : casillas) {
            if (cb.getText().charAt(0) == simbolo) {
                cb.setSelected(seleccionado);
                break;
            }
        }
    }

    /**
     * Obtiene la lista inmutable de casillas de verificación contenidas en el diálogo.
     *
     * @return Lista de {@link CheckBox}.
     */
    public List<CheckBox> getCasillas() {
        return Collections.unmodifiableList(casillas);
    }

    /**
     * Obtiene el alfabeto formal activo sobre el que opera el diálogo.
     *
     * @return Instancia de {@link Alfabeto}.
     */
    public Alfabeto getAlfabeto() {
        return alfabeto;
    }
}
