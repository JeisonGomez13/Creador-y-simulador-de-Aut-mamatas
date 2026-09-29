package com.automatas.view.util;

import com.automatas.model.Estado;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas del Algoritmo de Disposición Geométrica Circular (Tarea 4.1, RF-07, Caso Límite 15)")
class DisposicionCircularTest {

    private static final double EPSILON = 1e-9;
    private static final double CENTRO_X = 600.0;
    private static final double CENTRO_Y = 400.0;
    private static final double RADIO_PRUEBA = 200.0;

    // =========================================================================
    // 1. CASO N = 1: CENTRO GEOMÉTRICO EXACTO (Caso Límite 15)
    // =========================================================================

    @Test
    @DisplayName("1.1 Para N = 1, las coordenadas deben coincidir exactamente con el centro geométrico (Xc, Yc)")
    void testCasoBaseUnSoloEstadoCentroExacto() {
        List<Point2D> puntos = DisposicionCircular.calcularPuntos(1, CENTRO_X, CENTRO_Y, RADIO_PRUEBA);

        assertEquals(1, puntos.size(), "Debe retornar exactamente un punto.");
        Point2D p = puntos.get(0);
        assertEquals(CENTRO_X, p.getX(), EPSILON, "La coordenada X debe ser exactamente el centro X.");
        assertEquals(CENTRO_Y, p.getY(), EPSILON, "La coordenada Y debe ser exactamente el centro Y.");
    }

    @Test
    @DisplayName("1.2 Mapeo con un único Estado formal asignado al centro geométrico")
    void testMapeoUnSoloEstadoFormal() {
        Estado q0 = new Estado("q0");
        Map<Estado, Point2D> disposicion = DisposicionCircular.calcularDisposicion(
                List.of(q0), CENTRO_X, CENTRO_Y, RADIO_PRUEBA
        );

        assertEquals(1, disposicion.size());
        assertTrue(disposicion.containsKey(q0));
        Point2D pos = disposicion.get(q0);
        assertEquals(CENTRO_X, pos.getX(), EPSILON);
        assertEquals(CENTRO_Y, pos.getY(), EPSILON);
    }

    // =========================================================================
    // 2. CASO N = 4: SIMETRÍA TRIGONOMÉTRICA EN LOS 4 CUADRANTES
    // =========================================================================

    @Test
    @DisplayName("2.1 Para N = 4, los puntos deben ubicarse en las 4 direcciones cardinales simétricas")
    void testCuatroEstadosSimetriaTrigonometrica() {
        List<Point2D> puntos = DisposicionCircular.calcularPuntos(4, CENTRO_X, CENTRO_Y, RADIO_PRUEBA);

        assertEquals(4, puntos.size(), "Deben generarse 4 puntos.");

        // Punto 0: Cenit / Superior (x = centroX, y = centroY - radio)
        assertEquals(CENTRO_X, puntos.get(0).getX(), EPSILON);
        assertEquals(CENTRO_Y - RADIO_PRUEBA, puntos.get(0).getY(), EPSILON);

        // Punto 1: Este / Derecha (x = centroX + radio, y = centroY)
        assertEquals(CENTRO_X + RADIO_PRUEBA, puntos.get(1).getX(), EPSILON);
        assertEquals(CENTRO_Y, puntos.get(1).getY(), EPSILON);

        // Punto 2: Nadir / Inferior (x = centroX, y = centroY + radio)
        assertEquals(CENTRO_X, puntos.get(2).getX(), EPSILON);
        assertEquals(CENTRO_Y + RADIO_PRUEBA, puntos.get(2).getY(), EPSILON);

        // Punto 3: Oeste / Izquierda (x = centroX - radio, y = centroY)
        assertEquals(CENTRO_X - RADIO_PRUEBA, puntos.get(3).getX(), EPSILON);
        assertEquals(CENTRO_Y, puntos.get(3).getY(), EPSILON);

        // Verificar que todos están exactamente a la distancia RADIO_PRUEBA del centro
        for (Point2D p : puntos) {
            double distanciaAlCentro = p.distance(CENTRO_X, CENTRO_Y);
            assertEquals(RADIO_PRUEBA, distanciaAlCentro, EPSILON);
        }

        // Verificar que la distancia entre puntos consecutivos adyacentes es idéntica
        double dist01 = puntos.get(0).distance(puntos.get(1));
        double dist12 = puntos.get(1).distance(puntos.get(2));
        double dist23 = puntos.get(2).distance(puntos.get(3));
        double dist30 = puntos.get(3).distance(puntos.get(0));

        assertEquals(dist01, dist12, EPSILON);
        assertEquals(dist12, dist23, EPSILON);
        assertEquals(dist23, dist30, EPSILON);
    }

    // =========================================================================
    // 3. EQUIDISTANCIA Y SIMETRÍA PARA DIVERSOS N (3, 5, 8)
    // =========================================================================

    @Test
    @DisplayName("3.1 Para cualquier N > 1, todos los puntos deben ser equidistantes al centro y a sus vecinos")
    void testEquidistanciaTrigonometricaGenerica() {
        int[] valoresN = {3, 5, 6, 8};

        for (int n : valoresN) {
            List<Point2D> puntos = DisposicionCircular.calcularPuntos(n, CENTRO_X, CENTRO_Y, RADIO_PRUEBA);
            assertEquals(n, puntos.size());

            double radioEfectivo = DisposicionCircular.calcularRadio(n, RADIO_PRUEBA);

            // Todos equidistantes al centro
            for (Point2D p : puntos) {
                assertEquals(radioEfectivo, p.distance(CENTRO_X, CENTRO_Y), EPSILON);
            }

            // Distancia entre vecinos consecutivos debe ser idéntica
            double distanciaPrimerPar = puntos.get(0).distance(puntos.get(1));
            for (int i = 0; i < n; i++) {
                Point2D actual = puntos.get(i);
                Point2D siguiente = puntos.get((i + 1) % n);
                assertEquals(distanciaPrimerPar, actual.distance(siguiente), EPSILON,
                        "La distancia entre nodos adyacentes debe ser constante para N = " + n);
            }
        }
    }

    // =========================================================================
    // 4. ESCALABILIDAD DINÁMICA DEL RADIO ANTE ALTO NÚMERO DE NODOS
    // =========================================================================

    @Test
    @DisplayName("4.1 El radio debe escalar dinámicamente para preservar separación mínima cuando N es alto")
    void testEscalabilidadDeRadioConMuchosNodos() {
        int nPequeno = 4;
        int nGrande = 25;

        double radioPequeno = DisposicionCircular.calcularRadio(nPequeno, RADIO_PRUEBA);
        double radioGrande = DisposicionCircular.calcularRadio(nGrande, RADIO_PRUEBA);

        assertEquals(RADIO_PRUEBA, radioPequeno, EPSILON, "Para N pequeño debe respetar el radio base.");
        assertTrue(radioGrande > radioPequeno, "Para N grande el radio debe crecer proporcionalmente.");

        List<Point2D> puntosGrandes = DisposicionCircular.calcularPuntos(nGrande, CENTRO_X, CENTRO_Y, RADIO_PRUEBA);
        double distanciaVecinos = puntosGrandes.get(0).distance(puntosGrandes.get(1));
        assertTrue(distanciaVecinos >= DisposicionCircular.SEPARACION_MINIMA_ENTRE_NODOS * 0.95,
                "La distancia entre nodos vecinos debe ser suficiente para no colisionar visualmente.");
    }

    // =========================================================================
    // 5. VALIDACIÓN DE ENTRADAS Y CASOS LÍMITE
    // =========================================================================

    @Test
    @DisplayName("5.1 Manejo de colección vacía y argumentos inválidos")
    void testCasosLimiteYValidaciones() {
        assertTrue(DisposicionCircular.calcularPuntos(0, CENTRO_X, CENTRO_Y).isEmpty());
        assertTrue(DisposicionCircular.calcularDisposicion(Collections.emptyList(), CENTRO_X, CENTRO_Y).isEmpty());

        assertThrows(IllegalArgumentException.class, () ->
                DisposicionCircular.calcularPuntos(-1, CENTRO_X, CENTRO_Y)
        );

        assertThrows(IllegalArgumentException.class, () ->
                DisposicionCircular.calcularDisposicion(null, CENTRO_X, CENTRO_Y)
        );
    }

    // =========================================================================
    // 6. ASIGNACIÓN ASOCIATIVA ORDENADA A ESTADOS FORMALES
    // =========================================================================

    @Test
    @DisplayName("6.1 El mapa retornado debe mantener la correspondencia y orden original de los estados")
    void testMapeoAsociativoOrdenado() {
        List<Estado> estados = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            estados.add(new Estado("q" + i));
        }

        Map<Estado, Point2D> resultado = DisposicionCircular.calcularDisposicion(estados, CENTRO_X, CENTRO_Y);

        assertNotNull(resultado);
        assertEquals(5, resultado.size());

        int indice = 0;
        for (Map.Entry<Estado, Point2D> entry : resultado.entrySet()) {
            assertEquals(estados.get(indice), entry.getKey(), "Debe preservar el orden de iteración.");
            indice++;
        }
    }
}
