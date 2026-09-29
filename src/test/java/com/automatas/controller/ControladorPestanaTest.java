package com.automatas.controller;

import com.automatas.model.Alfabeto;
import com.automatas.model.Automata;
import com.automatas.model.Estado;
import com.automatas.model.TipoAutomata;
import com.automatas.view.VistaPestanaAutomata;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de integración del ControladorPestana y VistaPestanaAutomata (RF-01 a RF-06, RF-08).
 */
@DisplayName("Pruebas Unitarias de ControladorPestana (MVC - RF-01 a RF-06, RF-08)")
class ControladorPestanaTest {

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
    // 1. SINCRONIZACIÓN AL AGREGAR Y ELIMINAR ESTADOS
    // =========================================================================

    @Test
    @DisplayName("1.1 Al agregar un estado, la tabla y la clasificación deben actualizarse al unísono")
    void testSincronizacionAlAgregarEstado() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            assertEquals("Sin estados", vista.getPanelSuperior().getEtiquetaTipo().getText());
            assertEquals(0, vista.getTablaTransiciones().getItems().size());

            // Agregar estado
            Estado q0 = controlador.agregarEstado(100, 100);

            assertNotNull(q0);
            assertEquals("AFD", vista.getPanelSuperior().getEtiquetaTipo().getText(),
                    "Un autómata con 1 estado sin salidas múltiples debe clasificarse como AFD");
            assertEquals(1, vista.getTablaTransiciones().getItems().size());
            assertTrue(vista.getTablaTransiciones().obtenerFila(q0).isPresent());
        });
    }

    // =========================================================================
    // 2. SINCRONIZACIÓN AL AGREGAR TRANSICIONES Y BLOQUEO DEL ALFABETO
    // =========================================================================

    @Test
    @DisplayName("2.1 Agregar transiciones debe actualizar la tabla, clasificar AFN y bloquear edición de Σ")
    void testSincronizacionAlAgregarTransicion() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(300, 100);

            assertFalse(vista.getPanelSuperior().esAlfabetoBloqueado());

            // Transición q0 -> q1 con 'a'
            controlador.conectarEstados(q0, q1, Set.of('a'));

            assertTrue(vista.getPanelSuperior().esAlfabetoBloqueado(),
                    "El alfabeto debe bloquearse al existir transiciones (Caso Límite 10)");
            assertEquals("q1", vista.getTablaTransiciones().obtenerValorCelda(q0, 'a'));
            assertEquals("AFD", vista.getPanelSuperior().getEtiquetaTipo().getText());

            // Segunda transición saliente para 'a' (bifurcación no determinista)
            controlador.crearAutolazo(q0, Set.of('a'));

            assertEquals("AFN", vista.getPanelSuperior().getEtiquetaTipo().getText(),
                    "Debe reclasificarse como AFN al detectar múltiples salidas para el mismo símbolo");
            assertEquals("{q0, q1}", vista.getTablaTransiciones().obtenerValorCelda(q0, 'a'));
        });
    }

    // =========================================================================
    // 3. SINCRONIZACIÓN AL RENOMBRAR UN ESTADO
    // =========================================================================

    @Test
    @DisplayName("3.1 Al renombrar un estado, la tabla y el lienzo deben actualizar el identificador")
    void testSincronizacionAlRenombrarEstado() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            assertEquals("q0", q0.getNombre());

            controlador.renombrarEstado(q0, "inicio");

            assertEquals("inicio", q0.getNombre());
            assertTrue(vista.getTablaTransiciones().obtenerFilaPorNombre("inicio").isPresent());
            assertEquals("inicio", vista.getLienzo().obtenerNodoVista(q0).get().getEtiquetaNombre().getText());
        });
    }

    // =========================================================================
    // 4. COMPROBACIÓN INMEDIATA DE CADENAS (RF-06)
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe evaluar la cadena y emitir veredicto en PanelComprobacionVista")
    void testComprobacionCadena() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            Estado q1 = controlador.agregarEstado(300, 100);
            controlador.establecerEstadoInicial(q0);
            controlador.alternarAceptacion(q1);

            controlador.conectarEstados(q0, q1, Set.of('a'));

            // Comprobar cadena aceptada "a"
            controlador.comprobarCadena("a");
            assertEquals("«Aceptada»", vista.getPanelComprobacion().getEtiquetaVeredicto().getText());
            assertTrue(vista.getPanelComprobacion().getVistaRecorrido().getEtiquetaResumenRutas().getText().contains("Aceptada"));
            assertTrue(vista.getLienzo().obtenerNodoVista(q1).get().isSeleccionado(), "El nodo q1 del camino exitoso debe estar resaltado");

            // Comprobar cadena rechazada "b"
            controlador.comprobarCadena("b");
            assertEquals("«Rechazada»", vista.getPanelComprobacion().getEtiquetaVeredicto().getText());
            assertTrue(vista.getPanelComprobacion().getVistaRecorrido().getEtiquetaResumenRutas().getText().contains("Rechazada"));
        });
    }

    @Test
    @DisplayName("4.2 Debe rechazar comprobación si falta el estado inicial o hay caracteres ajenos a Σ")
    void testComprobacionErroresValidacion() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            // Sin inicial
            controlador.comprobarCadena("a");
            assertTrue(vista.getPanelComprobacion().getEtiquetaVeredicto().getText().contains("inicial"));

            // Con inicial, pero carácter ajeno 'z'
            controlador.establecerEstadoInicial(q0);
            controlador.comprobarCadena("z");
            assertTrue(vista.getPanelComprobacion().getEtiquetaVeredicto().getText().contains("alfabeto")
                    || vista.getPanelComprobacion().getEtiquetaVeredicto().getText().contains("inválid"));
        });
    }

    // =========================================================================
    // 5. AGREGAR ESTADOS DESDE BOTÓN Y GESTIÓN DE INICIAL (RF-02, RF-03)
    // =========================================================================

    @Test
    @DisplayName("5.1 Debe agregar estados mediante botón superior escalonando coordenadas y sincronizando vista")
    void testAgregarEstadoDesdeBoton() {
        ejecutarEnFx(() -> {
            Automata automata = new Automata();
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            assertEquals(0, automata.cardinalidadEstados());

            // Pulsar botón visual "+ Estado"
            vista.getPanelSuperior().getBotonAgregarEstado().fire();

            assertEquals(1, automata.cardinalidadEstados());
            assertTrue(vista.getLienzo().obtenerNodoVista(automata.getEstados().iterator().next()).isPresent());

            // Agregar un segundo estado mediante método
            Estado e2 = controlador.agregarEstadoDesdeBoton();
            assertNotNull(e2);
            assertEquals(2, automata.cardinalidadEstados());
            assertTrue(vista.getLienzo().obtenerNodoVista(e2).isPresent());
        });
    }

    @Test
    @DisplayName("5.2 Debe permitir desmarcar estado inicial sincronizando vista y modelo")
    void testDesmarcarEstadoInicial() {
        ejecutarEnFx(() -> {
            Automata automata = new Automata();
            VistaPestanaAutomata vista = new VistaPestanaAutomata();
            ControladorPestana controlador = new ControladorPestana(automata, vista);

            Estado q0 = controlador.agregarEstado(100, 100);
            controlador.establecerEstadoInicial(q0);
            assertTrue(automata.getEstadoInicial().isPresent());
            assertTrue(vista.getLienzo().obtenerNodoVista(q0).get().esInicial());

            controlador.desmarcarEstadoInicial();
            assertTrue(automata.getEstadoInicial().isEmpty());
            assertFalse(vista.getLienzo().obtenerNodoVista(q0).get().esInicial());
        });
    }
}
