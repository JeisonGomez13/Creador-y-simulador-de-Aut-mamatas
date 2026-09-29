package com.automatas.view.componente;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente PanelComprobacionVista (RF-06).
 */
@DisplayName("Pruebas Unitarias de PanelComprobacionVista (RF-06)")
class PanelComprobacionVistaTest {

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
    // 1. INICIALIZACIÓN POR DEFECTO
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe inicializarse con campo de texto, botón 'Comprobar' y etiqueta de veredicto")
    void testInicializacionDefecto() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();

            assertNotNull(panel.getCampoCadena());
            assertNotNull(panel.getBotonComprobar());
            assertNotNull(panel.getEtiquetaVeredicto());
            assertEquals("Comprobar", panel.getBotonComprobar().getText());
        });
    }

    // =========================================================================
    // 2. VEREDICTOS BINARIOS CON ESTILOS CONTRASTADOS (RF-06)
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe exhibir veredicto '«Aceptada»' en verde esmeralda ante simulación exitosa")
    void testVeredictoAceptada() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();

            panel.mostrarVeredicto(true);

            assertEquals("«Aceptada»", panel.getEtiquetaVeredicto().getText());
            assertTrue(panel.getEtiquetaVeredicto().getStyle().contains("#10B981")
                    || panel.getEtiquetaVeredicto().getStyle().contains("#065F46"));
        });
    }

    @Test
    @DisplayName("2.2 Debe exhibir veredicto '«Rechazada»' en rojo o ámbar neutro ante simulación fallida")
    void testVeredictoRechazada() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();

            panel.mostrarVeredicto(false);

            assertEquals("«Rechazada»", panel.getEtiquetaVeredicto().getText());
            assertTrue(panel.getEtiquetaVeredicto().getStyle().contains("#EF4444")
                    || panel.getEtiquetaVeredicto().getStyle().contains("#991B1B"));
        });
    }

    // =========================================================================
    // 3. MANEJO DE ERRORES DE EVALUACIÓN
    // =========================================================================

    @Test
    @DisplayName("3.1 Debe exhibir mensajes de advertencia si la cadena o autómata no cumplen condiciones")
    void testMostrarError() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();

            panel.mostrarError("El símbolo 'x' no pertenece al alfabeto.");
            assertEquals("El símbolo 'x' no pertenece al alfabeto.", panel.getEtiquetaVeredicto().getText());
        });
    }

    // =========================================================================
    // 4. CALLBACK DE COMPROBACIÓN Y RECORTE TRIM()
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe invocar el callback con el texto recortado al presionar el botón Comprobar")
    void testCallbackComprobarConTrim() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();
            AtomicReference<String> recibido = new AtomicReference<>();

            panel.setOnComprobar(recibido::set);

            panel.getCampoCadena().setText("  abb  ");
            panel.getBotonComprobar().fire();

            assertEquals("abb", recibido.get(), "Debe enviar la cadena tras aplicar trim()");
        });
    }

    // =========================================================================
    // 5. LIMPIEZA
    // =========================================================================

    @Test
    @DisplayName("5.1 Limpiar debe vaciar el campo de texto y restablecer el veredicto")
    void testLimpiar() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();

            panel.getCampoCadena().setText("abc");
            panel.mostrarVeredicto(true);

            panel.limpiar();
            assertEquals("", panel.getCadena());
            assertEquals("", panel.getEtiquetaVeredicto().getText());
        });
    }

    // =========================================================================
    // 6. INTEGRACIÓN DE RECORRIDO DE CADENA (RF-06)
    // =========================================================================

    @Test
    @DisplayName("6.1 Debe mostrar el resultado completo actualizando veredicto y vista de recorrido")
    void testMostrarResultadoCompleto() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();
            assertNotNull(panel.getVistaRecorrido());
            assertNotNull(panel.getBotonAlternarRecorrido());

            com.automatas.model.Automata automata = new com.automatas.model.Automata();
            automata.asignarAlfabeto(com.automatas.model.Alfabeto.desdeCadena("a, b"));
            com.automatas.model.Estado q0 = automata.crearEstado();
            com.automatas.model.Estado q1 = automata.crearEstado();
            automata.marcarEstadoInicial(q0);
            q1.setAceptacion(true);
            automata.agregarTransicion(q0, q1, 'a');

            com.automatas.model.SimuladorAutomata simulador = new com.automatas.model.SimuladorAutomata();
            com.automatas.model.ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "a");

            panel.mostrarResultado(resultado);

            assertEquals("«Aceptada»", panel.getEtiquetaVeredicto().getText());
            assertTrue(panel.getVistaRecorrido().isVisible());
            assertEquals("Ocultar recorrido", panel.getBotonAlternarRecorrido().getText());
        });
    }

    @Test
    @DisplayName("6.2 Debe alternar la visibilidad de la vista de recorrido al accionar el botón")
    void testAlternarVisibilidadRecorrido() {
        ejecutarEnFx(() -> {
            PanelComprobacionVista panel = new PanelComprobacionVista();
            assertTrue(panel.getVistaRecorrido().isVisible());

            panel.getBotonAlternarRecorrido().fire();
            assertFalse(panel.getVistaRecorrido().isVisible());
            assertEquals("Ver recorrido", panel.getBotonAlternarRecorrido().getText());

            panel.getBotonAlternarRecorrido().fire();
            assertTrue(panel.getVistaRecorrido().isVisible());
            assertEquals("Ocultar recorrido", panel.getBotonAlternarRecorrido().getText());
        });
    }
}
