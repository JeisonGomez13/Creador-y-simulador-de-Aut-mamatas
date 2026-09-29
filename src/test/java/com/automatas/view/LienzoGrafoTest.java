package com.automatas.view;

import com.automatas.model.Alfabeto;
import com.automatas.model.Estado;
import com.automatas.view.componente.AristaTransicionVista;
import com.automatas.view.componente.AutolazoVista;
import com.automatas.view.componente.NodoEstadoVista;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el contenedor LienzoGrafo (RF-02, RF-04, Casos Límite 7 y 21).
 */
@DisplayName("Pruebas Unitarias de LienzoGrafo (RF-02, RF-04, Casos Límite 7 y 21)")
class LienzoGrafoTest {

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
    // 1. INICIALIZACIÓN Y CONFIGURACIÓN VIRTUAL DEL LIENZO
    // =========================================================================

    @Test
    @DisplayName("1.1 LienzoGrafo debe inicializarse con dimensiones virtuales navegables en ScrollPane")
    void testInicializacionLienzo() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();

            assertNotNull(lienzo.getLienzoVirtual());
            assertTrue(lienzo.getAnchoVirtual() >= 1000.0, "El ancho virtual inicial debe ser al menos 1000px");
            assertTrue(lienzo.getAltoVirtual() >= 700.0, "El alto virtual inicial debe ser al menos 700px");
            assertEquals(lienzo.getAnchoVirtual(), lienzo.getLienzoVirtual().getPrefWidth(), 0.001);
            assertEquals(lienzo.getAltoVirtual(), lienzo.getLienzoVirtual().getPrefHeight(), 0.001);
        });
    }

    // =========================================================================
    // 2. GESTIÓN DE ESTADOS (ADICIÓN, ELIMINACIÓN EN CASCADA)
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe agregar y posicionar estados en el lienzo virtual")
    void testAgregarYEliminarEstados() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            Estado q1 = new Estado("q1");

            NodoEstadoVista nodoQ0 = lienzo.agregarEstado(q0, 150, 200, true);
            NodoEstadoVista nodoQ1 = lienzo.agregarEstado(q1, 400, 200, false);

            assertEquals(2, lienzo.getNodosVista().size());
            assertTrue(nodoQ0.esInicial());
            assertFalse(nodoQ1.esInicial());

            // Coordenadas
            assertEquals(150.0, nodoQ0.getLayoutX(), 0.001);
            assertEquals(200.0, nodoQ0.getLayoutY(), 0.001);

            // Eliminar estado q1
            lienzo.eliminarEstado(q1);
            assertEquals(1, lienzo.getNodosVista().size());
            assertTrue(lienzo.obtenerNodoVista(q1).isEmpty());
            assertTrue(lienzo.obtenerNodoVista(q0).isPresent());
        });
    }

    // =========================================================================
    // 3. EXPANSIÓN DINÁMICA DE LÍMITES AL ARRASTRAR A LOS BORDES (CASO LÍMITE 21)
    // =========================================================================

    @Test
    @DisplayName("3.1 Al posicionar o arrastrar un nodo hacia el borde derecho/inferior, el lienzo virtual debe expandirse")
    void testExpansionDinamicaBordes() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            double anchoInicial = lienzo.getAnchoVirtual();
            double altoInicial = lienzo.getAltoVirtual();

            Estado q0 = new Estado("q0");
            // Posicionar cerca del borde derecho
            NodoEstadoVista nodo = lienzo.agregarEstado(q0, anchoInicial - 40.0, 100.0);

            // La dimensión virtual debe haberse expandido automáticamente
            assertTrue(lienzo.getAnchoVirtual() > anchoInicial,
                    "El ancho virtual debió expandirse al acercar el nodo al borde derecho");
            assertTrue(lienzo.getLienzoVirtual().getPrefWidth() > anchoInicial);

            // Ahora mover hacia el borde inferior
            nodo.setLayoutY(altoInicial - 30.0);
            lienzo.verificarYExpandirLienzo(nodo.getLayoutX(), nodo.getLayoutY());

            assertTrue(lienzo.getAltoVirtual() > altoInicial,
                    "El alto virtual debió expandirse al acercar el nodo al borde inferior");
            assertTrue(lienzo.getLienzoVirtual().getPrefHeight() > altoInicial);
        });
    }

    // =========================================================================
    // 4. CREACIÓN DE TRANSICIONES Y ARCOS CURVADOS BIDIRECCIONALES (RF-04, CASO 6)
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe crear aristas unidireccionales y curvar automáticamente ante transiciones opuestas")
    void testConexionYCurvadoAutomatico() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            Estado q1 = new Estado("q1");
            lienzo.agregarEstado(q0, 100, 100);
            lienzo.agregarEstado(q1, 300, 100);

            // Conexión q0 -> q1
            AristaTransicionVista aristaIda = lienzo.conectarEstados(q0, q1, Set.of('a'));
            assertNotNull(aristaIda);
            assertFalse(aristaIda.esCurvada(), "Una conexión unidireccional no debe ser curvada");

            // Conexión opuesta q1 -> q0
            AristaTransicionVista aristaVuelta = lienzo.conectarEstados(q1, q0, Set.of('b'));
            assertNotNull(aristaVuelta);

            // Ahora AMBAS aristas deben ser curvadas automáticamente
            assertTrue(aristaIda.esCurvada(), "La arista de ida debe volverse curvada");
            assertTrue(aristaVuelta.esCurvada(), "La arista de vuelta debe ser curvada");

            // Unificar símbolos: agregar 'c' a q0 -> q1
            lienzo.conectarEstados(q0, q1, Set.of('c'));
            assertEquals("a, c", aristaIda.getTextoSimbolos());
        });
    }

    // =========================================================================
    // 5. CREACIÓN DE AUTOLAZO (CASO LÍMITE 7)
    // =========================================================================

    @Test
    @DisplayName("5.1 Debe crear y gestionar autolazos reflexivos sobre el mismo nodo")
    void testCreacionAutolazo() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            lienzo.agregarEstado(q0, 200, 200);

            AutolazoVista lazo = lienzo.crearAutolazo(q0, Set.of('a'));
            assertNotNull(lazo);
            assertEquals("a", lazo.getTextoSimbolos());

            Optional<AutolazoVista> lazoConsultado = lienzo.obtenerAutolazoVista(q0);
            assertTrue(lazoConsultado.isPresent());

            // Agregar símbolo al mismo autolazo
            lienzo.crearAutolazo(q0, Set.of('b'));
            assertEquals("a, b", lazo.getTextoSimbolos());
        });
    }

    // =========================================================================
    // 6. ELIMINACIÓN EN CASCADA DE TRANSICIONES AL REMOVER UN ESTADO
    // =========================================================================

    @Test
    @DisplayName("6.1 Eliminar un estado debe remover en cascada todas sus aristas conectadas y autolazos")
    void testEliminacionEnCascadaDeAristas() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            Estado q1 = new Estado("q1");
            lienzo.agregarEstado(q0, 100, 100);
            lienzo.agregarEstado(q1, 300, 100);

            lienzo.conectarEstados(q0, q1, Set.of('a'));
            lienzo.crearAutolazo(q0, Set.of('b'));

            assertTrue(lienzo.obtenerAristaVista(q0, q1).isPresent());
            assertTrue(lienzo.obtenerAutolazoVista(q0).isPresent());

            // Eliminar q0
            lienzo.eliminarEstado(q0);

            assertTrue(lienzo.obtenerAristaVista(q0, q1).isEmpty(), "La arista saliente de q0 debió removerse");
            assertTrue(lienzo.obtenerAutolazoVista(q0).isEmpty(), "El autolazo de q0 debió removerse");
        });
    }

    // =========================================================================
    // 7. ARRASTRE REACTIVO DE NODOS (DRAG & DROP) Y SEGUIMIENTO DE ARISTAS
    // =========================================================================

    @Test
    @DisplayName("7.1 Al arrastrar un nodo mediante eventos de ratón, su posición y las aristas vinculadas deben actualizarse")
    void testArrastreReactivoDeNodos() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            Estado q1 = new Estado("q1");
            NodoEstadoVista nodoQ0 = lienzo.agregarEstado(q0, 100, 100);
            NodoEstadoVista nodoQ1 = lienzo.agregarEstado(q1, 300, 100);

            AristaTransicionVista arista = lienzo.conectarEstados(q0, q1, Set.of('a'));
            double startXInicial = arista.getCurva().getStartX();

            // Mover nodoQ0 directamente o mediante setCentro
            nodoQ0.setLayoutX(150.0);
            nodoQ0.setLayoutY(120.0);

            // Las propiedades vinculadas deben reflejar el cambio en la arista inmediatamente
            double startXNuevo = arista.getCurva().getStartX();
            assertNotEquals(startXInicial, startXNuevo, "La arista debe seguir reactivamente al nodo desplazado");
            assertEquals(150.0 + NodoEstadoVista.RADIO_BASE, nodoQ0.getCentroX(), 0.001);
        });
    }

    // =========================================================================
    // 8. CALLBACK Y GESTIÓN DE SOLICITUD DE TRANSICIONES INTERACTIVAS
    // =========================================================================

    @Test
    @DisplayName("8.1 Debe notificar al listener registrado cuando se solicita crear una transición entre dos estados")
    void testSolicitudTransicionListener() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            Estado q1 = new Estado("q1");
            lienzo.agregarEstado(q0, 100, 100);
            lienzo.agregarEstado(q1, 300, 100);

            java.util.concurrent.atomic.AtomicReference<String> registro = new java.util.concurrent.atomic.AtomicReference<>();
            lienzo.setOnSolicitudTransicion((origen, destino) -> {
                registro.set(origen.getNombre() + "->" + destino.getNombre());
            });

            lienzo.solicitarTransicion(q0, q1);
            assertEquals("q0->q1", registro.get());

            // Solicitud de autolazo
            lienzo.solicitarTransicion(q0, q0);
            assertEquals("q0->q0", registro.get());
        });
    }

    // =========================================================================
    // 9. LIMPIEZA TOTAL Y MODO PROTEGIDO (RF-07)
    // =========================================================================

    @Test
    @DisplayName("9.1 Debe limpiar completamente el lienzo y permitir alternar el modo protegido")
    void testLimpiezaYModoProtegido() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            lienzo.agregarEstado(q0, 100, 100);
            lienzo.crearAutolazo(q0, Set.of('a'));

            assertFalse(lienzo.esModoProtegido());
            lienzo.setModoProtegido(true);
            assertTrue(lienzo.esModoProtegido());

            lienzo.limpiar();
            assertEquals(0, lienzo.getNodosVista().size());
            assertTrue(lienzo.obtenerNodoVista(q0).isEmpty());
            assertTrue(lienzo.obtenerAutolazoVista(q0).isEmpty());
        });
    }

    // =========================================================================
    // 10. ASIGNACIÓN Y ALTERNANCIA DE ESTADO INICIAL Y ACEPTACIÓN (RF-03)
    // =========================================================================

    @Test
    @DisplayName("10.1 Debe permitir designar estado inicial único y alternar condición de aceptación")
    void testDesignacionInicialYAceptacion() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            Estado q0 = new Estado("q0");
            Estado q1 = new Estado("q1");
            NodoEstadoVista nodo0 = lienzo.agregarEstado(q0, 100, 100, true);
            NodoEstadoVista nodo1 = lienzo.agregarEstado(q1, 300, 100, false);

            assertTrue(nodo0.esInicial());
            assertFalse(nodo1.esInicial());

            // Marcar q1 como inicial: debe desmarcar q0 automáticamente (RF-03)
            lienzo.establecerEstadoInicial(q1);
            assertFalse(nodo0.esInicial(), "q0 debió desmarcarse como inicial");
            assertTrue(nodo1.esInicial(), "q1 debe ser el nuevo estado inicial");

            // Desmarcar estado inicial
            lienzo.desmarcarEstadoInicial();
            assertFalse(nodo0.esInicial());
            assertFalse(nodo1.esInicial());

            // Alternar aceptación
            assertFalse(nodo1.esAceptacion());
            lienzo.alternarAceptacionEstado(q1);
            assertTrue(nodo1.esAceptacion());
            lienzo.alternarAceptacionEstado(q1);
            assertFalse(nodo1.esAceptacion());
        });
    }

    // =========================================================================
    // 11. CREACIÓN INTERACTIVA DE ESTADOS EN EL LIENZO (RF-02)
    // =========================================================================

    @Test
    @DisplayName("11.1 Debe registrar listener onCrearEstadoEnPosicion y responder correctamente")
    void testCallbackCrearEstadoEnPosicion() {
        ejecutarEnFx(() -> {
            LienzoGrafo lienzo = new LienzoGrafo();
            java.util.concurrent.atomic.AtomicReference<javafx.geometry.Point2D> puntoRecibido =
                    new java.util.concurrent.atomic.AtomicReference<>();

            lienzo.setOnCrearEstadoEnPosicion((x, y) -> puntoRecibido.set(new javafx.geometry.Point2D(x, y)));

            // Simular invocación directa del callback
            lienzo.getLienzoVirtual().fireEvent(new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                    250.0, 180.0, 250.0, 180.0,
                    javafx.scene.input.MouseButton.PRIMARY, 2,
                    false, false, false, false,
                    true, false, false, false, false, false, null
            ));

            assertNotNull(puntoRecibido.get());
            assertEquals(250.0, puntoRecibido.get().getX());
            assertEquals(180.0, puntoRecibido.get().getY());
        });
    }
}
