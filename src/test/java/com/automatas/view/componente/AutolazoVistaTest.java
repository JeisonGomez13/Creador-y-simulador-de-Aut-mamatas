package com.automatas.view.componente;

import com.automatas.model.Estado;
import com.automatas.view.util.PaletaColor;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente AutolazoVista (RF-04, Casos Límite 5 y 7).
 */
@DisplayName("Pruebas Unitarias de AutolazoVista (RF-04, Casos Límite 5 y 7)")
class AutolazoVistaTest {

    @BeforeAll
    static void inicializarJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ya inicializado
        }
    }

    // =========================================================================
    // 1. CREACIÓN Y UNIFICACIÓN DE SÍMBOLOS
    // =========================================================================

    @Test
    @DisplayName("1.1 El autolazo debe unificar múltiples símbolos en una sola etiqueta sobre sí mismo")
    void testCreacionYSimbolos() {
        Estado q0 = new Estado("q0");
        NodoEstadoVista nodo = new NodoEstadoVista(q0);

        AutolazoVista lazo = new AutolazoVista(nodo, Set.of('b', 'a'));

        assertEquals("a, b", lazo.getTextoSimbolos());
        assertEquals("a, b", lazo.getEtiqueta().getText());

        lazo.agregarSimbolo('0');
        assertTrue(lazo.getTextoSimbolos().contains("0"));
    }

    // =========================================================================
    // 2. GEOMETRÍA EN CUADRANTE SUPERIOR Y CABEZA DE FLECHA
    // =========================================================================

    @Test
    @DisplayName("2.1 La curva cúbica debe elevarse en el cuadrante superior por encima del nodo")
    void testGeometriaCuadranteSuperior() {
        Estado q0 = new Estado("q0");
        NodoEstadoVista nodo = new NodoEstadoVista(q0);
        nodo.setLayoutX(200);
        nodo.setLayoutY(200);

        AutolazoVista lazo = new AutolazoVista(nodo, 'a');

        assertNotNull(lazo.getCurva());
        assertNotNull(lazo.getCabezaFlecha());
        assertNotNull(lazo.getEtiqueta());

        // Colores de la paleta oficial
        assertEquals(PaletaColor.BORDE_REGULAR, lazo.getCurva().getStroke());
        assertEquals(PaletaColor.BORDE_REGULAR, lazo.getCabezaFlecha().getFill());

        double centroY = nodo.getCentroY(); // 225.0

        // Los puntos de control y la etiqueta deben estar arriba del centro del nodo (y < centroY)
        assertTrue(lazo.getCurva().getControlY1() < centroY, "El punto de control 1 debe estar por encima del nodo");
        assertTrue(lazo.getCurva().getControlY2() < centroY, "El punto de control 2 debe estar por encima del nodo");
        assertTrue(lazo.getEtiqueta().getLayoutY() < centroY, "La etiqueta debe ubicarse en la parte superior del lazo");
    }

    // =========================================================================
    // 3. REACTIVIDAD AL MOVIMIENTO DEL NODO
    // =========================================================================

    @Test
    @DisplayName("3.1 Al mover el nodo, el autolazo debe actualizar automáticamente sus coordenadas")
    void testReactividadMovimiento() {
        Estado q0 = new Estado("q0");
        NodoEstadoVista nodo = new NodoEstadoVista(q0);
        nodo.setLayoutX(100);
        nodo.setLayoutY(100);

        AutolazoVista lazo = new AutolazoVista(nodo, 'a');
        double inicioXAntes = lazo.getCurva().getStartX();

        nodo.setLayoutX(300);
        lazo.actualizarGeometria();

        double inicioXDespues = lazo.getCurva().getStartX();
        assertNotEquals(inicioXAntes, inicioXDespues, "Las coordenadas deben desplazarse junto con el nodo");
    }
}
