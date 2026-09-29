package com.automatas.view.componente;

import com.automatas.model.Estado;
import com.automatas.view.util.PaletaColor;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente visual del nodo de estado (RF-02, RF-03, RNF-04).
 * <p>
 * Verifica que el componente visual refleje fielmente las condiciones de estado:
 * regular, inicial, de aceptación y simultáneamente inicial+aceptación,
 * además de selección y sincronización con el modelo de dominio.
 */
@DisplayName("Pruebas Unitarias del Componente NodoEstadoVista (RF-02, RF-03)")
class NodoEstadoVistaTest {

    @BeforeAll
    static void inicializarJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit ya inicializado en ejecuciones previas
        }
    }

    // =========================================================================
    // 1. ESTADO REGULAR (NO INICIAL, NO ACEPTACIÓN)
    // =========================================================================

    @Test
    @DisplayName("1.1 El nodo regular debe mostrar círculo base y etiqueta, sin doble anillo ni flecha inicial")
    void testEstadoRegular() {
        Estado q0 = new Estado("q0", false);
        NodoEstadoVista vista = new NodoEstadoVista(q0, false);

        assertNotNull(vista.getEstado());
        assertEquals("q0", vista.getEstado().getNombre());
        assertFalse(vista.esInicial());
        assertFalse(vista.esAceptacion());

        // Verificación de elementos visuales
        assertNotNull(vista.getCirculoBase());
        assertEquals(NodoEstadoVista.RADIO_BASE, vista.getCirculoBase().getRadius(), 0.001);
        assertEquals(PaletaColor.FONDO_BLANCO, vista.getCirculoBase().getFill());
        assertEquals(PaletaColor.BORDE_REGULAR, vista.getCirculoBase().getStroke());

        assertNotNull(vista.getEtiquetaNombre());
        assertEquals("q0", vista.getEtiquetaNombre().getText());

        assertNotNull(vista.getCirculoAceptacion());
        assertFalse(vista.getCirculoAceptacion().isVisible(), "El anillo concéntrico de aceptación debe estar oculto");

        assertNotNull(vista.getFlechaInicial());
        assertFalse(vista.getFlechaInicial().isVisible(), "La flecha inicial debe estar oculta");
    }

    // =========================================================================
    // 2. ESTADO INICIAL
    // =========================================================================

    @Test
    @DisplayName("2.1 El nodo marcado como inicial debe mostrar la flecha triangular entrante")
    void testEstadoInicial() {
        Estado q0 = new Estado("q0", false);
        NodoEstadoVista vista = new NodoEstadoVista(q0, true);

        assertTrue(vista.esInicial());
        assertFalse(vista.esAceptacion());

        assertTrue(vista.getFlechaInicial().isVisible(), "La flecha inicial debe ser visible");
        assertFalse(vista.getCirculoAceptacion().isVisible(), "El anillo concéntrico no debe ser visible");
    }

    // =========================================================================
    // 3. ESTADO DE ACEPTACIÓN
    // =========================================================================

    @Test
    @DisplayName("3.1 El nodo de aceptación debe mostrar el círculo concéntrico interior en verde esmeralda")
    void testEstadoAceptacion() {
        Estado q1 = new Estado("q1", true);
        NodoEstadoVista vista = new NodoEstadoVista(q1, false);

        assertFalse(vista.esInicial());
        assertTrue(vista.esAceptacion());

        assertTrue(vista.getCirculoAceptacion().isVisible(), "El anillo de aceptación debe ser visible");
        assertEquals(NodoEstadoVista.RADIO_ACEPTACION, vista.getCirculoAceptacion().getRadius(), 0.001);
        assertEquals(PaletaColor.ACEPTACION, vista.getCirculoAceptacion().getStroke(),
                "El borde concéntrico debe ser de color verde esmeralda (#10B981)");
        assertEquals(Color.TRANSPARENT, vista.getCirculoAceptacion().getFill());

        assertFalse(vista.getFlechaInicial().isVisible(), "La flecha inicial no debe ser visible");
    }

    // =========================================================================
    // 4. ESTADO SIMULTÁNEAMENTE INICIAL Y DE ACEPTACIÓN
    // =========================================================================

    @Test
    @DisplayName("4.1 El nodo inicial y de aceptación debe mostrar simultáneamente flecha y doble anillo")
    void testEstadoInicialYAceptacionSimultaneos() {
        Estado q0 = new Estado("q0", true);
        NodoEstadoVista vista = new NodoEstadoVista(q0, true);

        assertTrue(vista.esInicial());
        assertTrue(vista.esAceptacion());

        assertTrue(vista.getFlechaInicial().isVisible(), "La flecha inicial debe ser visible");
        assertTrue(vista.getCirculoAceptacion().isVisible(), "El anillo concéntrico de aceptación debe ser visible");
    }

    // =========================================================================
    // 5. ESTADO DE SELECCIÓN Y ACTIVACIÓN
    // =========================================================================

    @Test
    @DisplayName("5.1 Al seleccionar el nodo, su borde debe adoptar el color violeta de selección (#8B5CF6)")
    void testSeleccionNodo() {
        Estado q0 = new Estado("q0");
        NodoEstadoVista vista = new NodoEstadoVista(q0, false);

        assertFalse(vista.esSeleccionado());
        assertEquals(PaletaColor.BORDE_REGULAR, vista.getCirculoBase().getStroke());

        vista.setSeleccionado(true);
        assertTrue(vista.esSeleccionado());
        assertEquals(PaletaColor.SELECCION, vista.getCirculoBase().getStroke(),
                "El borde seleccionado debe ser violeta neutro (#8B5CF6)");

        vista.setSeleccionado(false);
        assertFalse(vista.esSeleccionado());
        assertEquals(PaletaColor.BORDE_REGULAR, vista.getCirculoBase().getStroke());
    }

    // =========================================================================
    // 6. SINCRONIZACIÓN REACTIVA CON EL MODELO
    // =========================================================================

    @Test
    @DisplayName("6.1 Modificaciones en el modelo subyacente deben actualizar la representación visual")
    void testSincronizacionConModelo() {
        Estado q0 = new Estado("q0", false);
        NodoEstadoVista vista = new NodoEstadoVista(q0, false);

        assertFalse(vista.getCirculoAceptacion().isVisible());

        // Modificar aceptación en el modelo
        q0.setAceptacion(true);
        vista.actualizarVista();
        assertTrue(vista.esAceptacion());
        assertTrue(vista.getCirculoAceptacion().isVisible());

        // Modificar nombre en el modelo
        q0.renombrar("inicio");
        vista.actualizarVista();
        assertEquals("inicio", vista.getEtiquetaNombre().getText());

        // Cambiar flag inicial mediante el componente
        vista.setInicial(true);
        assertTrue(vista.esInicial());
        assertTrue(vista.getFlechaInicial().isVisible());
    }

    // =========================================================================
    // 7. RENDERIZADO DENTRO DE UN PANE Y PROPIEDADES DE CENTRO
    // =========================================================================

    @Test
    @DisplayName("7.1 El nodo debe integrarse en un Pane contenedor y exponer coordenadas de centro")
    void testIntegracionEnPaneYCentro() {
        Pane lienzoPrueba = new Pane();
        Estado q0 = new Estado("q0");
        NodoEstadoVista vista = new NodoEstadoVista(q0, false);

        lienzoPrueba.getChildren().add(vista);
        assertEquals(1, lienzoPrueba.getChildren().size());

        // Asignar posición en el lienzo
        vista.setLayoutX(100.0);
        vista.setLayoutY(150.0);

        // Coordenadas centrales esperadas para trazado de aristas
        assertEquals(100.0, vista.getLayoutX(), 0.001);
        assertEquals(150.0, vista.getLayoutY(), 0.001);

        assertNotNull(vista.centroXProperty());
        assertNotNull(vista.centroYProperty());
    }
}
