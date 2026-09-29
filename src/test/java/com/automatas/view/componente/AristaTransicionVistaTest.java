package com.automatas.view.componente;

import com.automatas.model.Estado;
import com.automatas.view.util.PaletaColor;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el componente AristaTransicionVista (RF-04, Casos Límite 5 y 6).
 */
@DisplayName("Pruebas Unitarias de AristaTransicionVista (RF-04, Casos Límite 5 y 6)")
class AristaTransicionVistaTest {

    @BeforeAll
    static void inicializarJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ya inicializado
        }
    }

    // =========================================================================
    // 1. UNIFICACIÓN DE SÍMBOLOS EN UNA SOLA FLECHA (CASO LÍMITE 5)
    // =========================================================================

    @Test
    @DisplayName("1.1 Múltiples símbolos entre el mismo par de nodos deben unificarse con comas (a, b)")
    void testUnificacionSimbolos() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");
        NodoEstadoVista n0 = new NodoEstadoVista(q0);
        NodoEstadoVista n1 = new NodoEstadoVista(q1);

        AristaTransicionVista arista = new AristaTransicionVista(n0, n1, Set.of('b', 'a'));

        assertEquals("a, b", arista.getTextoSimbolos(), "Los símbolos deben ordenarse y formatearse con coma");
        assertEquals("a, b", arista.getEtiqueta().getText(), "La etiqueta visual debe reflejar los símbolos unificados");

        arista.agregarSimbolo('c');
        assertEquals("a, b, c", arista.getTextoSimbolos());
        assertEquals("a, b, c", arista.getEtiqueta().getText());
    }

    // =========================================================================
    // 2. ARISTA DIRECTA Y GEOMETRÍA BÁSICA
    // =========================================================================

    @Test
    @DisplayName("2.1 La arista unidireccional debe conectar los bordes de ambos nodos con cabeza de flecha")
    void testAristaUnidireccional() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");
        NodoEstadoVista n0 = new NodoEstadoVista(q0);
        NodoEstadoVista n1 = new NodoEstadoVista(q1);

        n0.setLayoutX(100);
        n0.setLayoutY(100);
        n1.setLayoutX(300);
        n1.setLayoutY(100);

        AristaTransicionVista arista = new AristaTransicionVista(n0, n1, 'a');
        assertFalse(arista.esCurvada());

        // Verificar componentes gráficos
        assertNotNull(arista.getCurva());
        assertNotNull(arista.getCabezaFlecha());
        assertNotNull(arista.getEtiqueta());

        // Colores según la paleta neutra (sin azul ni café)
        assertEquals(PaletaColor.BORDE_REGULAR, arista.getCurva().getStroke());
        assertEquals(PaletaColor.BORDE_REGULAR, arista.getCabezaFlecha().getFill());

        // La curva debe terminar a la izquierda del centro del nodo destino (cerca del radio)
        double finX = arista.getCurva().getEndX();
        double centroDestinoX = n1.getCentroX();
        assertTrue(finX < centroDestinoX, "La flecha debe terminar en el perímetro exterior del nodo destino");
    }

    // =========================================================================
    // 3. TRANSICIONES BIDIRECCIONALES OPUESTAS (CASO LÍMITE 6)
    // =========================================================================

    @Test
    @DisplayName("3.1 Transiciones opuestas (q0->q1 y q1->q0) deben curvarse en sentidos opuestos sin solaparse")
    void testTransicionesBidireccionalesOpuestas() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");
        NodoEstadoVista n0 = new NodoEstadoVista(q0);
        NodoEstadoVista n1 = new NodoEstadoVista(q1);

        n0.setLayoutX(100);
        n0.setLayoutY(100);
        n1.setLayoutX(300);
        n1.setLayoutY(100);

        AristaTransicionVista aristaIda = new AristaTransicionVista(n0, n1, 'a');
        AristaTransicionVista aristaVuelta = new AristaTransicionVista(n1, n0, 'b');

        aristaIda.setCurvada(true);
        aristaVuelta.setCurvada(true);

        assertTrue(aristaIda.esCurvada());
        assertTrue(aristaVuelta.esCurvada());

        // Puntos de control de ambas curvas deben estar en lados opuestos del segmento
        double controlYIda = aristaIda.getCurva().getControlY();
        double controlYVuelta = aristaVuelta.getCurva().getControlY();
        double centroY = n0.getCentroY(); // 125.0

        boolean idaArriba = controlYIda < centroY;
        boolean vueltaAbajo = controlYVuelta > centroY;
        boolean idaAbajo = controlYIda > centroY;
        boolean vueltaArriba = controlYVuelta < centroY;

        assertTrue((idaArriba && vueltaAbajo) || (idaAbajo && vueltaArriba),
                "Los puntos de control deben estar en lados opuestos para evitar solapamiento visual");

        // Separación mínima garantizada entre los centros de ambas curvas
        double separacion = Math.abs(controlYIda - controlYVuelta);
        assertTrue(separacion >= 40.0, "Debe existir una separación mínima apreciable entre aristas opuestas: " + separacion);
    }

    // =========================================================================
    // 4. REACTIVIDAD ANTE ARRASTRE DE NODOS
    // =========================================================================

    @Test
    @DisplayName("4.1 Al cambiar la posición de los nodos, la geometría de la arista debe actualizarse")
    void testReactividadMovimientoNodos() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");
        NodoEstadoVista n0 = new NodoEstadoVista(q0);
        NodoEstadoVista n1 = new NodoEstadoVista(q1);

        n0.setLayoutX(100);
        n0.setLayoutY(100);
        n1.setLayoutX(200);
        n1.setLayoutY(100);

        AristaTransicionVista arista = new AristaTransicionVista(n0, n1, 'a');
        double finXInicial = arista.getCurva().getEndX();

        // Mover nodo destino
        n1.setLayoutX(400);
        arista.actualizarGeometria();

        double finXDespues = arista.getCurva().getEndX();
        assertNotEquals(finXInicial, finXDespues, "El extremo de la arista debe actualizarse tras mover el nodo");
    }
}
