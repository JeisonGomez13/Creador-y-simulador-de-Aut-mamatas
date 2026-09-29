package com.automatas.view.componente;

import com.automatas.model.Alfabeto;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el diálogo asistido de selección de símbolos (RF-04, Casos Límite 7 y 8).
 */
@DisplayName("Pruebas Unitarias de SelectorSimbolosDialog (RF-04, Casos Límite 7 y 8)")
class SelectorSimbolosDialogTest {

    @BeforeAll
    static void inicializarJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ya inicializado
        }
    }

    /**
     * Utilidad para ejecutar acciones en el hilo de aplicación de JavaFX (FX Application Thread),
     * requerido por el constructor de Stage/HeavyweightDialog en controles Dialog.
     */
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
    // 1. GENERACIÓN DINÁMICA DE CASILLAS SEGÚN ALFABETO FORMAL
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe generar una casilla (CheckBox) por cada símbolo del alfabeto activo")
    void testGeneracionDinamicaCasillas() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b, c");
            SelectorSimbolosDialog dialogo = new SelectorSimbolosDialog(alfabeto);

            assertEquals(3, dialogo.getCasillas().size(), "Debe haber 3 casillas para el alfabeto a, b, c");

            List<String> textos = dialogo.getCasillas().stream()
                    .map(CheckBox::getText)
                    .toList();

            assertTrue(textos.contains("a"));
            assertTrue(textos.contains("b"));
            assertTrue(textos.contains("c"));
        });
    }

    // =========================================================================
    // 2. CONFIRMACIÓN CON SELECCIÓN DE SÍMBOLOS
    // =========================================================================

    @Test
    @DisplayName("2.1 Al confirmar con selección, debe retornar la lista de símbolos marcados")
    void testConfirmacionConSeleccion() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("x, y, z");
            SelectorSimbolosDialog dialogo = new SelectorSimbolosDialog(alfabeto);

            // Seleccionar 'x' y 'z'
            dialogo.seleccionarSimbolo('x', true);
            dialogo.seleccionarSimbolo('z', true);

            List<Character> seleccion = dialogo.getResultConverter().call(ButtonType.OK);

            assertNotNull(seleccion);
            assertEquals(2, seleccion.size());
            assertEquals(List.of('x', 'z'), seleccion);
        });
    }

    // =========================================================================
    // 3. CANCELACIÓN Y CIERRE (CASO LÍMITE 8)
    // =========================================================================

    @Test
    @DisplayName("3.1 Al cancelar o cerrar el diálogo, debe retornar una lista vacía que impida crear aristas")
    void testCancelacionRetornaListaVacia() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            SelectorSimbolosDialog dialogo = new SelectorSimbolosDialog(alfabeto);

            dialogo.seleccionarSimbolo('a', true);

            // Simular pulsar Cancelar
            List<Character> resultadoCancelar = dialogo.getResultConverter().call(ButtonType.CANCEL);
            assertNotNull(resultadoCancelar);
            assertTrue(resultadoCancelar.isEmpty(), "Al presionar cancelar debe retornar lista vacía");

            // Simular cerrar ventana (botón nulo)
            List<Character> resultadoCerrar = dialogo.getResultConverter().call(null);
            assertNotNull(resultadoCerrar);
            assertTrue(resultadoCerrar.isEmpty(), "Al cerrar sin presionar OK debe retornar lista vacía");
        });
    }

    @Test
    @DisplayName("3.2 Al presionar OK sin haber marcado ninguna casilla, debe retornar lista vacía")
    void testConfirmacionSinSeleccionRetornaListaVacia() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
            SelectorSimbolosDialog dialogo = new SelectorSimbolosDialog(alfabeto);

            // Ninguna casilla marcada
            List<Character> resultado = dialogo.getResultConverter().call(ButtonType.OK);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty(), "Sin selección debe retornar lista vacía");
        });
    }

    // =========================================================================
    // 4. PRESELECCIÓN INICIAL DE SÍMBOLOS
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe soportar inicializarse con símbolos preseleccionados")
    void testPreseleccionInicial() {
        ejecutarEnFx(() -> {
            Alfabeto alfabeto = Alfabeto.desdeCadena("a, b, c");
            SelectorSimbolosDialog dialogo = new SelectorSimbolosDialog(alfabeto, Set.of('b'));

            List<Character> seleccionActual = dialogo.obtenerSimbolosSeleccionados();
            assertEquals(List.of('b'), seleccionActual);
        });
    }

    // =========================================================================
    // 5. VALIDACIÓN DE ENTRADA
    // =========================================================================

    @Test
    @DisplayName("5.1 Debe rechazar alfabeto nulo con IllegalArgumentException")
    void testAlfabetoNuloRechazado() {
        ejecutarEnFx(() -> {
            assertThrows(IllegalArgumentException.class, () -> new SelectorSimbolosDialog(null));
        });
    }
}
