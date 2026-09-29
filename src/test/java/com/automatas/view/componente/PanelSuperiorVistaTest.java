package com.automatas.view.componente;

import com.automatas.model.TipoAutomata;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente visual PanelSuperiorVista (RF-01, RF-05, Casos Límite 10 y 18).
 */
@DisplayName("Pruebas Unitarias de PanelSuperiorVista (RF-01, RF-05)")
class PanelSuperiorVistaTest {

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
    // 1. INICIALIZACIÓN POR DEFECTO Y ESTADOS VISUALES
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe inicializarse con indicador '«Sin estados»' y campo de alfabeto desbloqueado")
    void testInicializacionDefecto() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();

            assertNotNull(panel.getCampoAlfabeto());
            assertNotNull(panel.getBotonEstablecer());
            assertNotNull(panel.getEtiquetaTipo());

            assertFalse(panel.esAlfabetoBloqueado());
            assertFalse(panel.getCampoAlfabeto().isDisable());
            assertFalse(panel.getBotonEstablecer().isDisable());

            assertTrue(panel.getEtiquetaTipo().getText().contains("Sin estados"));
        });
    }

    // =========================================================================
    // 2. ACTUALIZACIÓN DINÁMICA DE CLASIFICACIÓN (RF-05)
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe reflejar correctamente la etiqueta del tipo de autómata (Sin estados, AFD, AFN)")
    void testActualizarTipoAutomata() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();

            panel.actualizarTipoAutomata(TipoAutomata.SIN_ESTADOS);
            assertEquals(TipoAutomata.SIN_ESTADOS.getEtiqueta(), panel.getEtiquetaTipo().getText());

            panel.actualizarTipoAutomata(TipoAutomata.AFD);
            assertEquals(TipoAutomata.AFD.getEtiqueta(), panel.getEtiquetaTipo().getText());

            panel.actualizarTipoAutomata(TipoAutomata.AFN);
            assertEquals(TipoAutomata.AFN.getEtiqueta(), panel.getEtiquetaTipo().getText());
        });
    }

    // =========================================================================
    // 3. BLOQUEO AUTOMÁTICO DEL ALFABETO (RF-01, CASO LÍMITE 10)
    // =========================================================================

    @Test
    @DisplayName("3.1 Debe habilitar y deshabilitar el campo de alfabeto según el estado de bloqueo")
    void testBloqueoAlfabeto() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();

            panel.bloquearAlfabeto(true);
            assertTrue(panel.esAlfabetoBloqueado());
            assertTrue(panel.getCampoAlfabeto().isDisable());
            assertTrue(panel.getBotonEstablecer().isDisable());

            panel.bloquearAlfabeto(false);
            assertFalse(panel.esAlfabetoBloqueado());
            assertFalse(panel.getCampoAlfabeto().isDisable());
            assertFalse(panel.getBotonEstablecer().isDisable());
        });
    }

    // =========================================================================
    // 4. MENSAJES DE ADVERTENCIA Y ERROR
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe mostrar y limpiar mensajes informativos o de error")
    void testMensajesInformativos() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();

            panel.mostrarMensaje("Debe eliminar las transiciones existentes antes de reconfigurar.");
            assertEquals("Debe eliminar las transiciones existentes antes de reconfigurar.", panel.getMensaje());
            assertTrue(panel.getEtiquetaMensaje().isVisible());

            panel.limpiarMensaje();
            assertEquals("", panel.getMensaje());
            assertFalse(panel.getEtiquetaMensaje().isVisible());
        });
    }

    // =========================================================================
    // 5. CALLBACK DE DEFINICIÓN DE ALFABETO
    // =========================================================================

    @Test
    @DisplayName("5.1 Debe invocar el callback registrado al presionar el botón de confirmación")
    void testCallbackDefinirAlfabeto() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();
            AtomicReference<String> recibido = new AtomicReference<>();

            panel.setOnDefinirAlfabeto(recibido::set);

            panel.getCampoAlfabeto().setText("a, b, c");
            panel.getBotonEstablecer().fire();

            assertEquals("a, b, c", recibido.get());
        });
    }

    // =========================================================================
    // 6. ACCIÓN DE AGREGAR ESTADO (RF-02)
    // =========================================================================

    @Test
    @DisplayName("6.1 Debe contener botón 'Agregar estado' habilitado y disparar callback onAgregarEstado")
    void testBotonAgregarEstado() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();
            assertNotNull(panel.getBotonAgregarEstado());
            assertEquals("Agregar estado", panel.getBotonAgregarEstado().getText());
            assertFalse(panel.getBotonAgregarEstado().isDisable());

            java.util.concurrent.atomic.AtomicBoolean disparado = new java.util.concurrent.atomic.AtomicBoolean(false);
            panel.setOnAgregarEstado(() -> disparado.set(true));

            panel.getBotonAgregarEstado().fire();
            assertTrue(disparado.get());
        });
    }

    @Test
    @DisplayName("6.2 Debe habilitar y deshabilitar botón de agregar estado")
    void testHabilitarCreacionEstados() {
        ejecutarEnFx(() -> {
            PanelSuperiorVista panel = new PanelSuperiorVista();

            panel.habilitarCreacionEstados(false);
            assertTrue(panel.getBotonAgregarEstado().isDisable());
            assertFalse(panel.getBotonAgregarEstado().isVisible());

            panel.habilitarCreacionEstados(true);
            assertFalse(panel.getBotonAgregarEstado().isDisable());
            assertTrue(panel.getBotonAgregarEstado().isVisible());
        });
    }
}
