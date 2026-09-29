package com.automatas.view.componente;

import com.automatas.model.Alfabeto;
import com.automatas.model.Automata;
import com.automatas.model.ConvertidorSubconjuntos;
import com.automatas.model.Estado;
import com.automatas.model.ResultadoConversion;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente visual TablaTransicionesVista (RF-08, Casos Límite 14 y 15).
 */
@DisplayName("Pruebas Unitarias de TablaTransicionesVista (RF-08)")
class TablaTransicionesVistaTest {

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
    // 1. INICIALIZACIÓN Y GENERACIÓN DINÁMICA DE COLUMNAS POR Σ (RF-08)
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe generar dinámicamente columnas según el alfabeto formal Σ")
    void testGeneracionColumnasDinamicas() {
        ejecutarEnFx(() -> {
            TablaTransicionesVista tabla = new TablaTransicionesVista();
            Alfabeto alfabeto2 = Alfabeto.desdeCadena("a, b");
            Automata automata2 = new Automata(alfabeto2);

            tabla.actualizar(automata2);

            // Debe tener 3 columnas: Estado, "a", "b"
            assertEquals(3, tabla.getColumns().size());
            assertEquals("Estado", tabla.getColumns().get(0).getText());
            assertEquals("a", tabla.getColumns().get(1).getText());
            assertEquals("b", tabla.getColumns().get(2).getText());

            // Actualizar con un alfabeto de 3 símbolos: a, b, c
            Alfabeto alfabeto3 = Alfabeto.desdeCadena("a, b, c");
            Automata automata3 = new Automata(alfabeto3);
            tabla.actualizar(automata3);

            assertEquals(4, tabla.getColumns().size());
            assertEquals("Estado", tabla.getColumns().get(0).getText());
            assertEquals("a", tabla.getColumns().get(1).getText());
            assertEquals("b", tabla.getColumns().get(2).getText());
            assertEquals("c", tabla.getColumns().get(3).getText());
        });
    }

    // =========================================================================
    // 2. REFLEJO REACTIVO DE ESTADOS Y TRANSICIONES DETERMINISTAS (AFD)
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe poblar filas con indicadores de estado inicial y final, y transiciones en AFD")
    void testReflejoTransicionesAFD() {
        ejecutarEnFx(() -> {
            TablaTransicionesVista tabla = new TablaTransicionesVista();
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);

            Estado q0 = automata.crearEstado(); // q0
            Estado q1 = automata.crearEstado(); // q1
            automata.marcarEstadoInicial(q0);
            q1.setAceptacion(true);

            automata.agregarTransicion(q0, q1, 'a');

            tabla.actualizar(automata);

            assertEquals(2, tabla.getItems().size());

            // Fila q0: inicial (-> q0)
            Optional<TablaTransicionesVista.FilaTransicionVista> filaQ0 = tabla.obtenerFila(q0);
            assertTrue(filaQ0.isPresent());
            assertTrue(filaQ0.get().getIndicadorEstado().contains("->"));
            assertTrue(filaQ0.get().getIndicadorEstado().contains("q0"));
            assertEquals("q1", tabla.obtenerValorCelda(q0, 'a'));
            assertEquals("ø", tabla.obtenerValorCelda(q0, 'b'), "Sin transición debe mostrar ø");

            // Fila q1: aceptación (* q1)
            Optional<TablaTransicionesVista.FilaTransicionVista> filaQ1 = tabla.obtenerFila(q1);
            assertTrue(filaQ1.isPresent());
            assertTrue(filaQ1.get().getIndicadorEstado().contains("*"));
            assertTrue(filaQ1.get().getIndicadorEstado().contains("q1"));
            assertEquals("ø", tabla.obtenerValorCelda(q1, 'a'));
            assertEquals("ø", tabla.obtenerValorCelda(q1, 'b'));
        });
    }

    // =========================================================================
    // 3. COMPORTAMIENTO AFN: CONJUNTOS {q1, q2} Y VACÍO ø (RF-08, CASO 14)
    // =========================================================================

    @Test
    @DisplayName("3.1 En AFN con salidas múltiples para un símbolo debe exhibir el conjunto entre llaves {q1, q2}")
    void testTransicionesMultiplesAFN() {
        ejecutarEnFx(() -> {
            TablaTransicionesVista tabla = new TablaTransicionesVista();
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);

            Estado q0 = automata.crearEstado();
            Estado q1 = automata.crearEstado();
            Estado q2 = automata.crearEstado();
            automata.marcarEstadoInicial(q0);

            // Bifurcación no determinista en 'a': q0 -> q1 y q0 -> q2
            automata.agregarTransicion(q0, q1, 'a');
            automata.agregarTransicion(q0, q2, 'a');

            tabla.actualizar(automata);

            // La celda de (q0, a) debe exhibir el conjunto ordenado entre llaves
            String valorCelda = tabla.obtenerValorCelda(q0, 'a');
            assertEquals("{q1, q2}", valorCelda);

            // En 'b' no hay transición: debe mostrar el símbolo de vacío ø
            assertEquals("ø", tabla.obtenerValorCelda(q0, 'b'));
        });
    }

    // =========================================================================
    // 4. REACTIVIDAD ANTE ADICIÓN Y ELIMINACIÓN DE TRANSICIONES
    // =========================================================================

    @Test
    @DisplayName("4.1 La tabla debe reflejar instantáneamente las transiciones añadidas o eliminadas")
    void testReactividadAdicionYEliminacion() {
        ejecutarEnFx(() -> {
            TablaTransicionesVista tabla = new TablaTransicionesVista();
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);

            Estado q0 = automata.crearEstado();
            Estado q1 = automata.crearEstado();

            tabla.actualizar(automata);
            assertEquals("ø", tabla.obtenerValorCelda(q0, 'a'));

            // Agregar transición y actualizar
            automata.agregarTransicion(q0, q1, 'a');
            tabla.actualizar(automata);
            assertEquals("q1", tabla.obtenerValorCelda(q0, 'a'));

            // Eliminar estado q1 y actualizar
            automata.eliminarEstado(q1);
            tabla.actualizar(automata);
            assertEquals(1, tabla.getItems().size());
            assertEquals("ø", tabla.obtenerValorCelda(q0, 'a'));
        });
    }

    // =========================================================================
    // 5. PESTAÑAS DE CONVERSIÓN: COLUMNA INMUTABLE DE SUBCONJUNTOS (RF-07, RF-08)
    // =========================================================================

    @Test
    @DisplayName("5.1 En pestañas de conversión debe incluir la columna inmutable de subconjuntos del AFN original")
    void testPestanaConversionSubconjuntos() {
        ejecutarEnFx(() -> {
            TablaTransicionesVista tabla = new TablaTransicionesVista();
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata afn = new Automata(alfabeto);

            Estado q0 = afn.crearEstado();
            Estado q1 = afn.crearEstado();
            afn.marcarEstadoInicial(q0);
            q1.setAceptacion(true);

            // AFN con no-determinismo
            afn.agregarTransicion(q0, q0, 'a');
            afn.agregarTransicion(q0, q1, 'a');
            afn.agregarTransicion(q1, q1, 'b');

            ConvertidorSubconjuntos convertidor = new ConvertidorSubconjuntos();
            ResultadoConversion resultado = convertidor.convertir(afn);

            // Poblar tabla en modo conversión
            tabla.actualizarConConversion(resultado);

            // Debe existir columna de Subconjuntos AFN
            assertTrue(tabla.tieneColumnaSubconjuntos(), "La tabla debe incluir la columna de subconjuntos");
            assertEquals("Subconjunto AFN", tabla.getColumns().get(1).getText());

            // Verificar contenido de k0
            Optional<TablaTransicionesVista.FilaTransicionVista> filaK0 = tabla.obtenerFilaPorNombre("k0");
            assertTrue(filaK0.isPresent());
            assertEquals("{q0}", filaK0.get().getSubconjuntoAFN());

            // k0 -> a debe ir a k1
            String destinoK0A = tabla.obtenerValorCelda("k0", 'a');
            assertEquals("k1", destinoK0A);

            // k0 -> b no tiene salida en afn para q0: debe ir a ø
            String destinoK0B = tabla.obtenerValorCelda("k0", 'b');
            assertEquals("ø", destinoK0B);
        });
    }

    // =========================================================================
    // 6. LIMPIEZA Y RESTABLECIMIENTO
    // =========================================================================

    @Test
    @DisplayName("6.1 Limpiar la tabla debe remover todos los elementos y columnas")
    void testLimpiarTabla() {
        ejecutarEnFx(() -> {
            TablaTransicionesVista tabla = new TablaTransicionesVista();
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            Automata automata = new Automata(alfabeto);
            automata.crearEstado();

            tabla.actualizar(automata);
            assertFalse(tabla.getItems().isEmpty());
            assertFalse(tabla.getColumns().isEmpty());

            tabla.limpiar();
            assertTrue(tabla.getItems().isEmpty());
            assertTrue(tabla.getColumns().isEmpty());
        });
    }
}
