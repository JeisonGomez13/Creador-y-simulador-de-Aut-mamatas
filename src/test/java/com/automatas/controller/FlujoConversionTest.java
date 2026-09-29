package com.automatas.controller;

import com.automatas.model.Alfabeto;
import com.automatas.model.Automata;
import com.automatas.model.Estado;
import com.automatas.view.VistaPestanaAutomata;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del flujo completo de conversión AFN a AFD y topología protegida (Tarea 4.2, RF-07, Casos Límite 11, 12, 19, 22).
 */
@DisplayName("Pruebas del Flujo Completo de Conversión AFN -> AFD (Tarea 4.2, RF-07)")
class FlujoConversionTest {

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
    // 1. VALIDACIÓN DE PRECEDENCIA (RF-07, Casos Límite 12, 18, 19)
    // =========================================================================

    @Test
    @DisplayName("1.1 Si el autómata no tiene estado inicial, aborta con mensaje instructivo (Caso Límite 12.1)")
    void testPrecedenciaSinEstadoInicial() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(200, 100);
            controlador.alternarAceptacion(q1);
            controlador.agregarTransicion(q0, q1, 'a');

            // Intento de conversión sin inicial
            Optional<ControladorPestana> resultado = controlador.convertirAAfd();

            assertTrue(resultado.isEmpty(), "No debe realizar conversión sin estado inicial.");
            assertTrue(vista.getPanelSuperior().getEtiquetaMensaje().isVisible());
            assertEquals("Debe designar un estado inicial antes de convertir.",
                    vista.getPanelSuperior().getEtiquetaMensaje().getText());
        });
    }

    @Test
    @DisplayName("1.2 Si el autómata ya clasifica como AFD, aborta con mensaje informativo (Caso Límite 12.2)")
    void testPrecedenciaYaEsDeterminista() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            controlador.establecerEstadoInicial(q0);
            controlador.alternarAceptacion(q0);
            controlador.agregarTransicion(q0, q0, 'a');

            // Es AFD (sin bifurcaciones)
            Optional<ControladorPestana> resultado = controlador.convertirAAfd();

            assertTrue(resultado.isEmpty(), "No debe convertir un autómata que ya es AFD.");
            assertTrue(vista.getPanelSuperior().getEtiquetaMensaje().isVisible());
            assertEquals("El autómata ya es determinista y no requiere conversión.",
                    vista.getPanelSuperior().getEtiquetaMensaje().getText());
        });
    }

    @Test
    @DisplayName("1.3 Si el autómata no tiene estados de aceptación, aborta con mensaje instructivo")
    void testPrecedenciaSinEstadosAceptacion() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(200, 100);
            controlador.establecerEstadoInicial(q0);
            // Bifurcación AFN sobre 'a' hacia q0 y q1
            controlador.agregarTransicion(q0, q0, 'a');
            controlador.agregarTransicion(q0, q1, 'a');

            Optional<ControladorPestana> resultado = controlador.convertirAAfd();

            assertTrue(resultado.isEmpty());
            assertTrue(vista.getPanelSuperior().getEtiquetaMensaje().isVisible());
            assertEquals("Debe definir al menos un estado final (de aceptación) antes de realizar la conversión.",
                    vista.getPanelSuperior().getEtiquetaMensaje().getText());
        });
    }

    // =========================================================================
    // 2. CONVERSIÓN EXITOSA Y TOPOLOGÍA PROTEGIDA (RF-07, Casos Límite 20, 22)
    // =========================================================================

    @Test
    @DisplayName("2.1 Conversión exitosa genera nueva pestaña con topología protegida y DisposicionCircular")
    void testConversionExitosaGeneraNuevoControladorProtegido() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);
            controlador.setTitulo("Autómata 1");

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(200, 100);
            controlador.establecerEstadoInicial(q0);
            controlador.alternarAceptacion(q1);

            // Bifurcación AFN: q0 tiene transiciones con 'a' hacia q0 y q1
            controlador.agregarTransicion(q0, q0, 'a');
            controlador.agregarTransicion(q0, q1, 'a');

            Optional<ControladorPestana> resultadoOpt = controlador.convertirAAfd();

            assertTrue(resultadoOpt.isPresent(), "La conversión debe completarse con éxito.");
            ControladorPestana afdControlador = resultadoOpt.get();

            // 1. Título correlativo
            assertEquals("AFD (de Autómata 1)", afdControlador.getTitulo());

            // 2. Topología protegida activa (Caso Límite 20)
            assertTrue(afdControlador.esModoProtegido(), "El controlador generado debe estar en modo protegido.");
            assertTrue(afdControlador.getVista().getLienzo().esModoProtegido());
            assertTrue(afdControlador.getVista().getPanelSuperior().isAlfabetoBloqueado());
            assertFalse(afdControlador.getVista().getPanelSuperior().getBotonConvertir().isVisible());

            // 3. Mutaciones prohibidas en modo protegido
            assertThrows(IllegalStateException.class, () -> afdControlador.agregarEstado(150, 150));
            assertThrows(IllegalStateException.class, () -> afdControlador.definirAlfabeto("c, d"));

            // 4. Estados creados en el lienzo del AFD con DisposicionCircular
            assertFalse(afdControlador.getVista().getLienzo().getNodosVista().isEmpty(),
                    "El lienzo del nuevo AFD debe contener nodos.");

            // 5. Tabla de subconjuntos viva poblada (RF-08)
            assertEquals("Tabla de Subconjuntos",
                    afdControlador.getVista().getEtiquetaTituloTabla().getText());

            // 6. Comprobación de cadenas habilitada sobre el nuevo AFD
            afdControlador.comprobarCadena("a");
            assertEquals("«Aceptada»", afdControlador.getVista().getPanelComprobacion().getEtiquetaVeredicto().getText());
        });
    }

    // =========================================================================
    // 3. CONVERSIONES SUCESIVAS DE LA MISMA FUENTE (Caso Límite 22)
    // =========================================================================

    @Test
    @DisplayName("3.1 Conversiones sucesivas sobre la misma pestaña generan títulos correlativos diferenciados")
    void testConversionesSucesivasTitulosCorrelativos() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);
            controlador.setTitulo("Autómata 2");

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(200, 100);
            controlador.establecerEstadoInicial(q0);
            controlador.alternarAceptacion(q1);

            controlador.agregarTransicion(q0, q0, 'a');
            controlador.agregarTransicion(q0, q1, 'a');

            // Conversión 1
            Optional<ControladorPestana> conv1 = controlador.convertirAAfd();
            assertTrue(conv1.isPresent());
            assertEquals("AFD (de Autómata 2)", conv1.get().getTitulo());

            // Conversión 2
            Optional<ControladorPestana> conv2 = controlador.convertirAAfd();
            assertTrue(conv2.isPresent());
            assertEquals("AFD 2 (de Autómata 2)", conv2.get().getTitulo());

            // Conversión 3
            Optional<ControladorPestana> conv3 = controlador.convertirAAfd();
            assertTrue(conv3.isPresent());
            assertEquals("AFD 3 (de Autómata 2)", conv3.get().getTitulo());
        });
    }

    // =========================================================================
    // 4. ESCUCHADOR DE NOTIFICACIÓN DE NUEVA PESTAÑA GENERADA
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe notificar al listener registrado cuando la conversión crea una nueva pestaña")
    void testNotificacionListenerPestanaGenerada() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(200, 100);
            controlador.establecerEstadoInicial(q0);
            controlador.alternarAceptacion(q1);
            controlador.agregarTransicion(q0, q0, 'a');
            controlador.agregarTransicion(q0, q1, 'a');

            AtomicReference<ControladorPestana> pestanaGenerada = new AtomicReference<>();
            controlador.setOnNuevaPestanaGenerada(pestanaGenerada::set);

            controlador.convertirAAfd();

            assertNotNull(pestanaGenerada.get(), "El listener debió recibir el nuevo controlador generado.");
            assertEquals("AFD (de Autómata 1)", pestanaGenerada.get().getTitulo());
        });
    }
}
