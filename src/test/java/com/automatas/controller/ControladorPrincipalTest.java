package com.automatas.controller;

import com.automatas.model.Alfabeto;
import com.automatas.model.Estado;
import com.automatas.view.VistaPrincipal;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Tab;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de integración del ControladorPrincipal y VistaPrincipal (Tarea 4.3, RF-08, Casos Límite 23, 24).
 */
@DisplayName("Pruebas Unitarias de ControladorPrincipal y VistaPrincipal (Tarea 4.3, RF-08)")
class ControladorPrincipalTest {

    @BeforeAll
    static void inicializarJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ya inicializado
        }
    }

    private static void ejecutarEnFx(Runnable accion) {
        if (Platform.isFxApplicationThread()) {
            accion.run();
            return;
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                accion.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        }
    }

    // =========================================================================
    // 1. INICIALIZACIÓN Y CREACIÓN DE PESTAÑAS MANUALES (RF-08)
    // =========================================================================

    @Test
    @DisplayName("1.1 Al inicializar debe existir exactamente una pestaña titulada «Autómata 1»")
    void testInicializacionCreaPestanaInicialAutomata1() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            assertEquals(1, vista.getTabPane().getTabs().size(), "Debe existir exactamente 1 pestaña inicial.");
            Tab tabInicial = vista.getTabPane().getTabs().get(0);
            assertEquals("Autómata 1", tabInicial.getText());
            assertNotNull(controlador.obtenerControladorDePestana(tabInicial));
        });
    }

    @Test
    @DisplayName("1.2 Solicitar nueva pestaña manual debe generar títulos correlativos («Autómata 2», «Autómata 3»)")
    void testBotonNuevoAutomataCreaPestanasRotuladasSecuencialmente() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            ControladorPestana p2 = controlador.crearNuevaPestanaManual();
            assertEquals(2, vista.getTabPane().getTabs().size());
            assertEquals("Autómata 2", p2.getTitulo());
            assertEquals("Autómata 2", vista.getTabPane().getTabs().get(1).getText());

            ControladorPestana p3 = controlador.crearNuevaPestanaManual();
            assertEquals(3, vista.getTabPane().getTabs().size());
            assertEquals("Autómata 3", p3.getTitulo());
            assertEquals("Autómata 3", vista.getTabPane().getTabs().get(2).getText());
        });
    }

    // =========================================================================
    // 2. CIERRE DE PESTAÑAS Y CONFIRMACIÓN PREVENTIVA (Caso Límite 23)
    // =========================================================================

    @Test
    @DisplayName("2.1 Pestaña vacía (sin estados ni transiciones) se cierra directamente sin confirmación")
    void testCierrePestanaVaciaSinConfirmacion() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            controlador.crearNuevaPestanaManual(); // Ahora hay Autómata 1 y Autómata 2
            assertEquals(2, vista.getTabPane().getTabs().size());

            AtomicBoolean confirmadorInvocado = new AtomicBoolean(false);
            controlador.setConfirmadorCierre((tab, ctrl) -> {
                confirmadorInvocado.set(true);
                return true;
            });

            Tab tab2 = vista.getTabPane().getTabs().get(1);
            controlador.cerrarPestana(tab2);

            assertFalse(confirmadorInvocado.get(), "No debe pedir confirmación si la pestaña está vacía.");
            assertEquals(1, vista.getTabPane().getTabs().size());
            assertEquals("Autómata 1", vista.getTabPane().getTabs().get(0).getText());
        });
    }

    @Test
    @DisplayName("2.2 Pestaña con contenido solicita confirmación; si se rechaza, la pestaña permanece abierta (Caso Límite 23)")
    void testCierrePestanaConContenidoConfirmacionRechazadaNoCierra() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            Tab tab1 = vista.getTabPane().getTabs().get(0);
            ControladorPestana ctrl1 = controlador.obtenerControladorDePestana(tab1);

            // Agregar contenido (un estado)
            ctrl1.agregarEstado(100, 100);
            assertTrue(ctrl1.tieneContenido());

            // Usuario rechaza el cierre en el diálogo preventivo
            controlador.setConfirmadorCierre((tab, ctrl) -> false);

            controlador.cerrarPestana(tab1);

            assertEquals(1, vista.getTabPane().getTabs().size(), "La pestaña no debió cerrarse.");
            assertTrue(vista.getTabPane().getTabs().contains(tab1));
        });
    }

    @Test
    @DisplayName("2.3 Pestaña con contenido solicita confirmación; si se aprueba, la pestaña se cierra (Caso Límite 23)")
    void testCierrePestanaConContenidoConfirmacionAprobadaCierra() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            ControladorPestana ctrl2 = controlador.crearNuevaPestanaManual();
            Tab tab2 = vista.getTabPane().getTabs().get(1);

            ctrl2.agregarEstado(150, 150);
            assertTrue(ctrl2.tieneContenido());

            // Usuario confirma el cierre
            controlador.setConfirmadorCierre((tab, ctrl) -> true);

            controlador.cerrarPestana(tab2);

            assertEquals(1, vista.getTabPane().getTabs().size());
            assertFalse(vista.getTabPane().getTabs().contains(tab2));
        });
    }

    // =========================================================================
    // 3. REGENERACIÓN AUTOMÁTICA DE «Autómata 1» AL CERRAR LA ÚLTIMA (Caso Límite 24)
    // =========================================================================

    @Test
    @DisplayName("3.1 Cerrar la última pestaña abierta debe regenerar automáticamente una pestaña en blanco «Autómata 1» (Caso Límite 24)")
    void testCierreUltimaPestanaAutoRegeneraAutomata1() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            Tab tabUnica = vista.getTabPane().getTabs().get(0);

            // Cerrar la única pestaña existente
            controlador.cerrarPestana(tabUnica);

            // No debe quedar vacía; debe haber regenerado «Autómata 1»
            assertFalse(vista.getTabPane().getTabs().isEmpty(), "El editor nunca debe quedar sin pestañas.");
            assertEquals(1, vista.getTabPane().getTabs().size());
            Tab tabRegenerada = vista.getTabPane().getTabs().get(0);
            assertEquals("Autómata 1", tabRegenerada.getText());
        });
    }

    // =========================================================================
    // 4. INTEGRACIÓN DE CONVERSIÓN EN EL TABPANE (RF-07, RF-08)
    // =========================================================================

    @Test
    @DisplayName("4.1 La acción de conversión de un AFN debe insertar la nueva pestaña AFD en el TabPane y seleccionarla")
    void testConversionInsertaNuevaPestanaEnTabPane() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            Tab tab1 = vista.getTabPane().getTabs().get(0);
            ControladorPestana ctrl1 = controlador.obtenerControladorDePestana(tab1);

            ctrl1.definirAlfabeto("a, b");
            Estado q0 = ctrl1.agregarEstado(100, 100);
            Estado q1 = ctrl1.agregarEstado(200, 100);
            ctrl1.establecerEstadoInicial(q0);
            ctrl1.alternarAceptacion(q1);
            ctrl1.agregarTransicion(q0, q0, 'a');
            ctrl1.agregarTransicion(q0, q1, 'a');

            // Disparar conversión
            ctrl1.convertirAAfd();

            // Debe haber 2 pestañas ahora: «Autómata 1» y «AFD (de Autómata 1)»
            assertEquals(2, vista.getTabPane().getTabs().size());
            Tab tabAfd = vista.getTabPane().getTabs().get(1);
            assertEquals("AFD (de Autómata 1)", tabAfd.getText());
            assertEquals(tabAfd, vista.getTabPane().getSelectionModel().getSelectedItem(),
                    "La nueva pestaña de conversión debe quedar seleccionada.");
        });
    }

    // =========================================================================
    // 5. MODO AMÉRICA (TEMA ROJO Y BLANCO & MARCA DE AGUA)
    // =========================================================================

    @Test
    @DisplayName("5.1 El botón Modo América debe alternar la clase CSS y sincronizar marca de agua en lienzos")
    void testModoAmericaAlternarYSincronizarLienzos() {
        ejecutarEnFx(() -> {
            VistaPrincipal vista = new VistaPrincipal();
            ControladorPrincipal controlador = new ControladorPrincipal(vista);

            assertNotNull(vista.getBotonModoAmerica());
            assertEquals("Modo América", vista.getBotonModoAmerica().getText());
            assertFalse(vista.isModoAmericaActivo());
            assertFalse(vista.getStyleClass().contains("modo-america"));

            Tab tab1 = vista.getTabPane().getTabs().get(0);
            ControladorPestana ctrl1 = controlador.obtenerControladorDePestana(tab1);

            // Activar Modo América mediante el botón
            vista.getBotonModoAmerica().fire();

            assertTrue(vista.isModoAmericaActivo());
            assertTrue(vista.getStyleClass().contains("modo-america"));
            assertEquals("Modo América (Activo)", vista.getBotonModoAmerica().getText());
            assertTrue(ctrl1.getVista().getLienzo().isMarcaDeAguaAmericaActiva(),
                    "El lienzo debe tener la marca de agua activa en Modo América");

            // Crear una nueva pestaña con Modo América activo: debe nacer con marca de agua activa
            ControladorPestana ctrl2 = controlador.crearNuevaPestanaManual();
            assertTrue(ctrl2.getVista().getLienzo().isMarcaDeAguaAmericaActiva(),
                    "Nuevas pestañas creadas durante Modo América deben heredar la marca de agua activa");

            // Desactivar Modo América
            vista.getBotonModoAmerica().fire();
            assertFalse(vista.isModoAmericaActivo());
            assertFalse(vista.getStyleClass().contains("modo-america"));
            assertEquals("Modo América", vista.getBotonModoAmerica().getText());
            assertFalse(ctrl1.getVista().getLienzo().isMarcaDeAguaAmericaActiva());
            assertFalse(ctrl2.getVista().getLienzo().isMarcaDeAguaAmericaActiva());
        });
    }
}
