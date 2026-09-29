package com.automatas.view.util;

import com.automatas.model.Estado;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Algoritmo geométrico determinista para la disposición espacial de estados en grafos (RF-07, Caso Límite 15).
 * <p>
 * Reglas de distribución:
 * <ul>
 *   <li><b>Caso N = 1 (Caso Límite 15):</b> El nodo se sitúa exactamente en el centro geométrico {@code (centroX, centroY)}.</li>
 *   <li><b>Caso N > 1:</b> Los nodos se distribuyen uniformemente a lo largo de una circunferencia centrada en
 *       {@code (centroX, centroY)}, con radio escalable en función de N para evitar colisiones y superposición de aristas.</li>
 *   <li><b>Orientación trigonométrica:</b> El primer nodo (índice 0) se ubica en el cenit (ángulo -π/2, posición superior),
 *       progresando en sentido horario para máxima legibilidad visual.</li>
 * </ul>
 */
public final class DisposicionCircular {

    // =========================================================================
    // 1. CONSTANTES GEOMÉTRICAS PREDETERMINADAS
    // =========================================================================

    public static final double RADIO_BASE_PREDETERMINADO = 180.0;
    public static final double SEPARACION_MINIMA_ENTRE_NODOS = 120.0;

    // =========================================================================
    // 2. CONSTRUCTOR PRIVADO
    // =========================================================================

    private DisposicionCircular() {
        // Clase de utilidades estática; prevenir instanciación externa
    }

    // =========================================================================
    // 3. CÁLCULO DE RADIO DINÁMICO
    // =========================================================================

    /**
     * Calcula el radio adecuado para alojar N nodos garantizando una separación mínima de arco entre ellos.
     *
     * @param n Número de nodos a distribuir.
     * @param radioBase Radio mínimo deseado.
     * @return Radio geométrico adaptado.
     */
    public static double calcularRadio(int n, double radioBase) {
        if (n <= 1) {
            return 0.0;
        }
        double radioRequeridoPorSeparacion = (n * SEPARACION_MINIMA_ENTRE_NODOS) / (2.0 * Math.PI);
        return Math.max(radioBase, radioRequeridoPorSeparacion);
    }

    // =========================================================================
    // 4. GENERACIÓN DE COORDENADAS PUNTUALES
    // =========================================================================

    /**
     * Calcula la lista de coordenadas geométricas para N puntos en disposición circular centrada.
     *
     * @param n Cantidad de puntos a generar.
     * @param centroX Coordenada X del centro geométrico.
     * @param centroY Coordenada Y del centro geométrico.
     * @param radioBase Radio base de distribución.
     * @return Lista inmutable con las coordenadas {@link Point2D}.
     * @throws IllegalArgumentException Si N es negativo.
     */
    public static List<Point2D> calcularPuntos(int n, double centroX, double centroY, double radioBase) {
        if (n < 0) {
            throw new IllegalArgumentException("La cantidad de nodos no puede ser negativa: " + n);
        }
        if (n == 0) {
            return Collections.emptyList();
        }
        if (n == 1) {
            return List.of(new Point2D(centroX, centroY));
        }

        double radio = calcularRadio(n, radioBase);
        List<Point2D> puntos = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            double angulo = (2.0 * Math.PI * i / n) - (Math.PI / 2.0);
            double x = centroX + radio * Math.cos(angulo);
            double y = centroY + radio * Math.sin(angulo);
            puntos.add(new Point2D(x, y));
        }

        return Collections.unmodifiableList(puntos);
    }

    /**
     * Calcula la lista de coordenadas geométricas para N puntos empleando el radio base predeterminado.
     *
     * @param n Cantidad de puntos a generar.
     * @param centroX Coordenada X del centro geométrico.
     * @param centroY Coordenada Y del centro geométrico.
     * @return Lista inmutable con las coordenadas {@link Point2D}.
     * @throws IllegalArgumentException Si N es negativo.
     */
    public static List<Point2D> calcularPuntos(int n, double centroX, double centroY) {
        return calcularPuntos(n, centroX, centroY, RADIO_BASE_PREDETERMINADO);
    }

    // =========================================================================
    // 5. ASIGNACIÓN ASOCIATIVA A ESTADOS FORMALES
    // =========================================================================

    /**
     * Asigna a cada estado de la colección una posición espacial en disposición circular centrada.
     *
     * @param estados Colección ordenada de estados formales.
     * @param centroX Coordenada X del centro geométrico.
     * @param centroY Coordenada Y del centro geométrico.
     * @param radioBase Radio base de distribución.
     * @return Mapa asociativo inmutable de {@link Estado} a {@link Point2D}.
     * @throws IllegalArgumentException Si la colección de estados es nula.
     */
    public static Map<Estado, Point2D> calcularDisposicion(Collection<Estado> estados, double centroX, double centroY, double radioBase) {
        if (estados == null) {
            throw new IllegalArgumentException("La colección de estados no puede ser nula.");
        }
        if (estados.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Estado> listaEstados = new ArrayList<>(estados);
        List<Point2D> puntos = calcularPuntos(listaEstados.size(), centroX, centroY, radioBase);

        Map<Estado, Point2D> mapa = new LinkedHashMap<>();
        for (int i = 0; i < listaEstados.size(); i++) {
            mapa.put(listaEstados.get(i), puntos.get(i));
        }

        return Collections.unmodifiableMap(mapa);
    }

    /**
     * Asigna a cada estado de la colección una posición espacial empleando el radio base predeterminado.
     *
     * @param estados Colección de estados formales.
     * @param centroX Coordenada X del centro geométrico.
     * @param centroY Coordenada Y del centro geométrico.
     * @return Mapa asociativo inmutable de {@link Estado} a {@link Point2D}.
     * @throws IllegalArgumentException Si la colección de estados es nula.
     */
    public static Map<Estado, Point2D> calcularDisposicion(Collection<Estado> estados, double centroX, double centroY) {
        return calcularDisposicion(estados, centroX, centroY, RADIO_BASE_PREDETERMINADO);
    }
}
