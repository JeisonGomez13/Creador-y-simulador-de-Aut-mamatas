package com.automatas.view.componente;

import com.automatas.model.Automata;
import com.automatas.model.CaminoSimulacion;
import com.automatas.model.Estado;
import com.automatas.model.ResultadoSimulacion;
import com.automatas.model.SimuladorAutomata;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente VistaRecorridoCadena (RF-06).
 */
@DisplayName("Pruebas Unitarias de VistaRecorridoCadena (RF-06)")
class VistaRecorridoCadenaTest {

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

    @Test
    @DisplayName("1. Debe inicializarse con contenedor de rutas y mensaje de espera")
    void testInicializacionDefecto() {
        ejecutarEnFx(() -> {
            VistaRecorridoCadena vista = new VistaRecorridoCadena();
            assertNotNull(vista.getContenedorRutas());
            assertNotNull(vista.getEtiquetaResumenRutas());
            assertFalse(vista.getEtiquetaResumenRutas().isVisible());
            assertEquals(1, vista.getContenedorRutas().getChildren().size());
        });
    }

    @Test
    @DisplayName("2. Debe renderizar correctamente el recorrido de una cadena aceptada")
    void testMostrarResultadoAceptada() {
        ejecutarEnFx(() -> {
            Automata automata = new Automata();
            automata.asignarAlfabeto(com.automatas.model.Alfabeto.desdeCadena("a, b"));
            Estado q0 = automata.crearEstado();
            Estado q1 = automata.crearEstado();
            automata.marcarEstadoInicial(q0);
            q1.setAceptacion(true);
            automata.agregarTransicion(q0, q1, 'a');

            SimuladorAutomata simulador = new SimuladorAutomata();
            ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "a");

            VistaRecorridoCadena vista = new VistaRecorridoCadena();
            vista.mostrarResultado(resultado);

            assertTrue(vista.getEtiquetaResumenRutas().isVisible());
            assertTrue(vista.getEtiquetaResumenRutas().getText().contains("Aceptada"));
            assertEquals(1, vista.getContenedorRutas().getChildren().size());
        });
    }

    @Test
    @DisplayName("3. Debe renderizar camino muerto ante bifurcación sin salida")
    void testMostrarResultadoCaminoMuerto() {
        ejecutarEnFx(() -> {
            Automata automata = new Automata();
            automata.asignarAlfabeto(com.automatas.model.Alfabeto.desdeCadena("a, b"));
            Estado q0 = automata.crearEstado();
            Estado q1 = automata.crearEstado();
            automata.marcarEstadoInicial(q0);
            q1.setAceptacion(true);
            automata.agregarTransicion(q0, q1, 'a'); // solo transita con 'a', no con 'b'

            SimuladorAutomata simulador = new SimuladorAutomata();
            ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "b");

            VistaRecorridoCadena vista = new VistaRecorridoCadena();
            vista.mostrarResultado(resultado);

            assertTrue(vista.getEtiquetaResumenRutas().isVisible());
            assertTrue(vista.getEtiquetaResumenRutas().getText().contains("Rechazada"));
            assertEquals(1, vista.getContenedorRutas().getChildren().size());
        });
    }

    @Test
    @DisplayName("4. Debe notificar selección de camino cuando el usuario interactúa con la tarjeta")
    void testSeleccionCamino() {
        ejecutarEnFx(() -> {
            Automata automata = new Automata();
            automata.asignarAlfabeto(com.automatas.model.Alfabeto.desdeCadena("a, b"));
            Estado q0 = automata.crearEstado();
            Estado q1 = automata.crearEstado();
            automata.marcarEstadoInicial(q0);
            q1.setAceptacion(true);
            automata.agregarTransicion(q0, q1, 'a');

            SimuladorAutomata simulador = new SimuladorAutomata();
            ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "a");

            VistaRecorridoCadena vista = new VistaRecorridoCadena();
            AtomicReference<CaminoSimulacion> caminoSeleccionado = new AtomicReference<>();
            vista.setOnCaminoSeleccionado(caminoSeleccionado::set);

            vista.mostrarResultado(resultado);

            // Simular clic en la tarjeta de ruta generada
            javafx.scene.Node tarjeta = vista.getContenedorRutas().getChildren().get(0);
            tarjeta.fireEvent(new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0,
                    javafx.scene.input.MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    false, false, false, false, false, false, null
            ));

            assertNotNull(caminoSeleccionado.get());
            assertEquals("q0", caminoSeleccionado.get().getEstadoInicial().getNombre());
        });
    }

    @Test
    @DisplayName("5. Debe mostrar mensaje de error y restablecer con limpiar")
    void testMostrarErrorYLimpiar() {
        ejecutarEnFx(() -> {
            VistaRecorridoCadena vista = new VistaRecorridoCadena();
            vista.mostrarError("Falta estado inicial");

            assertTrue(vista.getEtiquetaResumenRutas().getText().contains("no realizada"));

            vista.limpiar();
            assertFalse(vista.getEtiquetaResumenRutas().isVisible());
        });
    }

    @Test
    @DisplayName("6. Debe priorizar la ruta aceptada mostrándola de primera entre múltiples bifurcaciones")
    void testRutaAceptadaAparecePrimero() {
        ejecutarEnFx(() -> {
            Automata automata = new Automata();
            automata.asignarAlfabeto(com.automatas.model.Alfabeto.desdeCadena("a, b"));
            Estado q0 = automata.crearEstado();
            Estado q1 = automata.crearEstado();
            Estado q2 = automata.crearEstado();
            automata.marcarEstadoInicial(q0);
            q2.setAceptacion(true); // q2 es el estado de aceptación

            // AFN: q0 --a-> q1 (no aceptado), y q0 --a-> q2 (aceptado)
            automata.agregarTransicion(q0, q1, 'a');
            automata.agregarTransicion(q0, q2, 'a');

            SimuladorAutomata simulador = new SimuladorAutomata();
            ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "a");

            VistaRecorridoCadena vista = new VistaRecorridoCadena();
            vista.mostrarResultado(resultado);

            assertEquals(2, vista.getContenedorRutas().getChildren().size());
            // La primera tarjeta debe ser la aceptada
            javafx.scene.layout.VBox primeraTarjeta = (javafx.scene.layout.VBox) vista.getContenedorRutas().getChildren().get(0);
            javafx.scene.layout.HBox filaTitulo = (javafx.scene.layout.HBox) primeraTarjeta.getChildren().get(0);
            javafx.scene.control.Label badge = (javafx.scene.control.Label) filaTitulo.getChildren().get(1);
            assertEquals("«Aceptada»", badge.getText());
        });
    }
}
