package com.automatas.controller;

import com.automatas.model.Automata;
import com.automatas.view.VistaPestanaAutomata;
import com.automatas.view.VistaPrincipal;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Controlador orquestador global de la aplicación (RF-08).
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Gestión multi-lienzo reactiva sobre el {@link javafx.scene.control.TabPane} principal.</li>
 *   <li>Rotulación correlativa de pestañas manuales ({@code "Autómata 1"}, {@code "Autómata 2"}, ...).</li>
 *   <li>Confirmación preventiva antes de cerrar pestañas con estados o transiciones en memoria (Caso Límite 23).</li>
 *   <li>Regeneración automática de {@code "Autómata 1"} si se cierra la última pestaña activa (Caso Límite 24).</li>
 *   <li>Integración automática de nuevas pestañas resultantes de conversión AFN -> AFD (RF-07).</li>
 * </ul>
 */
public class ControladorPrincipal {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final VistaPrincipal vistaPrincipal;
    private final Map<Tab, ControladorPestana> pestanasActivas;
    private int contadorPestanasManuales;
    private BiPredicate<Tab, ControladorPestana> confirmadorCierre;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye el controlador principal vinculándolo a la vista raíz.
     *
     * @param vistaPrincipal Contenedor visual de la ventana principal.
     * @throws IllegalArgumentException Si la vista principal es nula.
     */
    public ControladorPrincipal(VistaPrincipal vistaPrincipal) {
        if (vistaPrincipal == null) {
            throw new IllegalArgumentException("La vista principal no puede ser nula.");
        }

        this.vistaPrincipal = vistaPrincipal;
        this.pestanasActivas = new LinkedHashMap<>();
        this.contadorPestanasManuales = 1;

        // Confirmador preventivo predeterminado mediante Alert modal institucional (Caso Límite 23)
        this.confirmadorCierre = (tab, controlador) -> {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Confirmar Cierre de Pestaña");
            alerta.setHeaderText("¿Desea cerrar esta pestaña?");
            alerta.setContentText("Se perderán los datos del autómata en memoria.");

            ButtonType btnCerrar = new ButtonType("Cerrar Pestaña", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerta.getButtonTypes().setAll(btnCerrar, btnCancelar);

            Optional<ButtonType> respuesta = alerta.showAndWait();
            return respuesta.isPresent() && respuesta.get() == btnCerrar;
        };

        // Conectar acción rápida de nuevo autómata
        this.vistaPrincipal.setOnNuevoAutomata(this::crearNuevaPestanaManual);

        // Conectar alternancia de Modo América para sincronizar marca de agua en lienzos
        this.vistaPrincipal.setOnModoAmericaCambiado(this::aplicarModoAmerica);

        // Crear la primera pestaña inicial obligatoria ("Autómata 1")
        crearNuevaPestanaManual();
    }

    // =========================================================================
    // 3. GESTIÓN DE PESTAÑAS (RF-08, CASOS LÍMITE 23 Y 24)
    // =========================================================================

    /**
     * Crea una nueva pestaña manual en blanco con rotulación correlativa («Autómata N») y la agrega al TabPane.
     *
     * @return El {@link ControladorPestana} correspondiente a la pestaña creada.
     */
    public ControladorPestana crearNuevaPestanaManual() {
        String titulo = "Autómata " + contadorPestanasManuales++;
        Automata automata = new Automata();
        VistaPestanaAutomata vistaPestana = new VistaPestanaAutomata();
        ControladorPestana controlador = new ControladorPestana(automata, vistaPestana, titulo);

        agregarPestana(controlador);
        return controlador;
    }

    /**
     * Inserta un nuevo controlador de pestaña en el TabPane, registrando sus escuchadores de cierre y conversión.
     *
     * @param controlador Instancia de {@link ControladorPestana} a hospedar.
     * @return {@link Tab} creado y seleccionado.
     */
    public Tab agregarPestana(ControladorPestana controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de pestaña a agregar no puede ser nulo.");
        }

        // Si el Modo América está activo globalmente, activar marca de agua en la nueva pestaña
        if (vistaPrincipal.isModoAmericaActivo() && controlador.getVista() != null && controlador.getVista().getLienzo() != null) {
            controlador.getVista().getLienzo().activarMarcaDeAguaAmerica(true);
        }

        Tab tab = new Tab(controlador.getTitulo(), controlador.getVista());
        pestanasActivas.put(tab, controlador);

        // Conectar listener de conversión: al convertir, se agrega automáticamente la nueva pestaña AFD
        controlador.setOnNuevaPestanaGenerada(this::agregarPestana);

        // Interceptar solicitud interactiva de cierre de pestaña (clic en la 'x' nativa de la pestaña)
        tab.setOnCloseRequest(event -> {
            if (controlador.tieneContenido()) {
                boolean confirmado = (confirmadorCierre != null) && confirmadorCierre.test(tab, controlador);
                if (!confirmado) {
                    event.consume();
                    return;
                }
            }
            alCerrarPestana(tab);
        });

        vistaPrincipal.getTabPane().getTabs().add(tab);
        vistaPrincipal.getTabPane().getSelectionModel().select(tab);

        return tab;
    }

    private void aplicarModoAmerica(boolean activo) {
        for (ControladorPestana ctrl : pestanasActivas.values()) {
            if (ctrl.getVista() != null && ctrl.getVista().getLienzo() != null) {
                ctrl.getVista().getLienzo().activarMarcaDeAguaAmerica(activo);
            }
        }
    }

    /**
     * Cierra programáticamente una pestaña activa respetando la confirmación preventiva si contiene datos.
     *
     * @param tab Pestaña a cerrar.
     */
    public void cerrarPestana(Tab tab) {
        if (tab == null || !vistaPrincipal.getTabPane().getTabs().contains(tab)) {
            return;
        }

        ControladorPestana controlador = pestanasActivas.get(tab);
        if (controlador != null && controlador.tieneContenido()) {
            boolean confirmado = (confirmadorCierre != null) && confirmadorCierre.test(tab, controlador);
            if (!confirmado) {
                return;
            }
        }

        vistaPrincipal.getTabPane().getTabs().remove(tab);
        alCerrarPestana(tab);
    }

    private void alCerrarPestana(Tab tab) {
        pestanasActivas.remove(tab);

        // Regeneración automática obligatoria si se cierra la última pestaña (Caso Límite 24)
        if (vistaPrincipal.getTabPane().getTabs().isEmpty()) {
            contadorPestanasManuales = 1;
            crearNuevaPestanaManual();
        }
    }

    // =========================================================================
    // 4. CONSULTAS Y CONFIGURACIÓN
    // =========================================================================

    /**
     * Obtiene el controlador asociado a una pestaña gráfica dada.
     *
     * @param tab Pestaña a consultar.
     * @return {@link ControladorPestana} asociado, o {@code null} si no existe.
     */
    public ControladorPestana obtenerControladorDePestana(Tab tab) {
        return pestanasActivas.get(tab);
    }

    /**
     * Permite inyectar un predicado de confirmación de cierre personalizado (útil para pruebas unitarias).
     *
     * @param confirmadorCierre Función predicado que evalúa si el cierre es autorizado.
     */
    public void setConfirmadorCierre(BiPredicate<Tab, ControladorPestana> confirmadorCierre) {
        this.confirmadorCierre = confirmadorCierre;
    }

    public VistaPrincipal getVistaPrincipal() {
        return vistaPrincipal;
    }

    public int getCantidadPestanasActivas() {
        return pestanasActivas.size();
    }
}
