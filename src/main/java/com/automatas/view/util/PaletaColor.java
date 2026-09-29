package com.automatas.view.util;

import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tokens centralizados de color para la interfaz gráfica de usuario (RNF-06, Constitución Principio 7).
 * <p>
 * Reglas de diseño y restricciones cromáticas:
 * <ul>
 *   <li><b>Restricción Absoluta:</b> Prohibición estricta de tonos de color azul (espectro HSL 190°–250°)
 *       y café/marrón (espectro HSL 25°–45° oscuro).</li>
 *   <li><b>Paleta Neutra:</b> Tonos grafito (#1E1E24, #2B2D42), gris perla (#F4F4F6), blanco y negro carbón (#111827).</li>
 *   <li><b>Acentos Permitidos:</b> Verde esmeralda (#10B981) para aceptación, violeta neutro (#8B5CF6) para selección/activo,
 *       ámbar (#F59E0B) para advertencias y AFN, y rojo neutro (#EF4444) para errores.</li>
 * </ul>
 */
public final class PaletaColor {

    // =========================================================================
    // 1. CONSTANTES HEXADECIMALES
    // =========================================================================

    // Fondos y Paneles
    public static final String HEX_FONDO_PRINCIPAL = "#F4F4F6";
    public static final String HEX_FONDO_BLANCO = "#FFFFFF";
    public static final String HEX_GRAFITO_OSCURO = "#1E1E24";
    public static final String HEX_GRAFITO_SECUNDARIO = "#2B2D42";

    // Textos y Bordes
    public static final String HEX_TEXTO_PRINCIPAL = "#121212";
    public static final String HEX_TEXTO_SECUNDARIO = "#6B7280";
    public static final String HEX_BORDE_REGULAR = "#4A4E69";
    public static final String HEX_BORDE_SUAVE = "#E5E7EB";

    // Acentos y Estados Visuales
    public static final String HEX_ACEPTACION = "#10B981";
    public static final String HEX_ACEPTACION_CLARO = "#D1FAE5";
    public static final String HEX_SELECCION = "#8B5CF6";
    public static final String HEX_SELECCION_CLARO = "#EDE9FE";
    public static final String HEX_ADVERTENCIA = "#F59E0B";
    public static final String HEX_ADVERTENCIA_CLARO = "#FEF3C7";
    public static final String HEX_PELIGRO = "#EF4444";
    public static final String HEX_PELIGRO_CLARO = "#FEE2E2";

    // =========================================================================
    // 2. INSTANCIAS DE COLOR (JAVAFX COLOR)
    // =========================================================================

    // Fondos y Paneles
    public static final Color FONDO_PRINCIPAL = Color.web(HEX_FONDO_PRINCIPAL);
    public static final Color FONDO_BLANCO = Color.web(HEX_FONDO_BLANCO);
    public static final Color GRAFITO_OSCURO = Color.web(HEX_GRAFITO_OSCURO);
    public static final Color GRAFITO_SECUNDARIO = Color.web(HEX_GRAFITO_SECUNDARIO);

    // Textos y Bordes
    public static final Color TEXTO_PRINCIPAL = Color.web(HEX_TEXTO_PRINCIPAL);
    public static final Color TEXTO_SECUNDARIO = Color.web(HEX_TEXTO_SECUNDARIO);
    public static final Color BORDE_REGULAR = Color.web(HEX_BORDE_REGULAR);
    public static final Color BORDE_SUAVE = Color.web(HEX_BORDE_SUAVE);

    // Acentos y Estados Visuales
    public static final Color ACEPTACION = Color.web(HEX_ACEPTACION);
    public static final Color ACEPTACION_CLARO = Color.web(HEX_ACEPTACION_CLARO);
    public static final Color SELECCION = Color.web(HEX_SELECCION);
    public static final Color SELECCION_CLARO = Color.web(HEX_SELECCION_CLARO);
    public static final Color ADVERTENCIA = Color.web(HEX_ADVERTENCIA);
    public static final Color ADVERTENCIA_CLARO = Color.web(HEX_ADVERTENCIA_CLARO);
    public static final Color PELIGRO = Color.web(HEX_PELIGRO);
    public static final Color PELIGRO_CLARO = Color.web(HEX_PELIGRO_CLARO);

    // =========================================================================
    // 3. REGISTRO Y CONSULTA DE TOKENS
    // =========================================================================

    private static final Map<String, Color> MAPA_COLORES;

    static {
        Map<String, Color> mapa = new LinkedHashMap<>();
        mapa.put("FONDO_PRINCIPAL", FONDO_PRINCIPAL);
        mapa.put("FONDO_BLANCO", FONDO_BLANCO);
        mapa.put("GRAFITO_OSCURO", GRAFITO_OSCURO);
        mapa.put("GRAFITO_SECUNDARIO", GRAFITO_SECUNDARIO);
        mapa.put("TEXTO_PRINCIPAL", TEXTO_PRINCIPAL);
        mapa.put("TEXTO_SECUNDARIO", TEXTO_SECUNDARIO);
        mapa.put("BORDE_REGULAR", BORDE_REGULAR);
        mapa.put("BORDE_SUAVE", BORDE_SUAVE);
        mapa.put("ACEPTACION", ACEPTACION);
        mapa.put("ACEPTACION_CLARO", ACEPTACION_CLARO);
        mapa.put("SELECCION", SELECCION);
        mapa.put("SELECCION_CLARO", SELECCION_CLARO);
        mapa.put("ADVERTENCIA", ADVERTENCIA);
        mapa.put("ADVERTENCIA_CLARO", ADVERTENCIA_CLARO);
        mapa.put("PELIGRO", PELIGRO);
        mapa.put("PELIGRO_CLARO", PELIGRO_CLARO);
        MAPA_COLORES = Collections.unmodifiableMap(mapa);
    }

    /**
     * Constructor privado para prevenir instanciación de clase de utilidades.
     */
    private PaletaColor() {
        throw new UnsupportedOperationException("PaletaColor es una clase de constantes de diseño y no debe instanciarse.");
    }

    /**
     * Obtiene todos los tokens de color registrados en la paleta oficial.
     *
     * @return Mapa inmutable de nombre de token a instancia {@link Color}.
     */
    public static Map<String, Color> obtenerTodosLosColores() {
        return MAPA_COLORES;
    }

    // =========================================================================
    // 4. MÉTODOS DE ANÁLISIS HSL Y DETECCIÓN DE COLORES PROHIBIDOS
    // =========================================================================

    /**
     * Convierte un {@link Color} de JavaFX a sus componentes matemáticos HSL (Hue, Saturation, Lightness).
     *
     * @param color Color a analizar.
     * @return Arreglo de 3 elementos: [0] = Hue (0.0 a 360.0), [1] = Saturation (0.0 a 1.0), [2] = Lightness (0.0 a 1.0).
     */
    public static double[] calcularHSL(Color color) {
        if (color == null) {
            return new double[]{0.0, 0.0, 0.0};
        }

        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double c = max - min;
        double l = (max + min) / 2.0;

        double h = 0.0;
        if (c > 0.00001) {
            if (max == r) {
                h = 60.0 * (((g - b) / c) % 6);
            } else if (max == g) {
                h = 60.0 * (((b - r) / c) + 2);
            } else {
                h = 60.0 * (((r - g) / c) + 4);
            }
            if (h < 0) {
                h += 360.0;
            }
        }

        double s = 0.0;
        if (c > 0.00001 && l > 0.00001 && l < 0.99999) {
            s = c / (1.0 - Math.abs(2.0 * l - 1.0));
        }

        return new double[]{h, s, l};
    }

    /**
     * Determina si un color pertenece al espectro cromático del AZUL (HSL 190°–250° con saturación perceptible).
     * <p>
     * Se descartan los tonos puramente neutros (grises, negro, blanco) con saturación negligible.
     *
     * @param color Color a comprobar.
     * @return {@code true} si cae en el espectro azul; {@code false} en caso contrario.
     */
    public static boolean esAzul(Color color) {
        if (color == null) {
            return false;
        }
        double[] hsl = calcularHSL(color);
        double h = hsl[0];
        double s = hsl[1];
        double l = hsl[2];

        // Rango formal de azul: Hue entre 190° y 250°, con saturación cromática > 25% y luminosidad distinguible del negro puro (L > 0.15)
        return h >= 190.0 && h <= 250.0 && s > 0.25 && l > 0.15 && l < 0.95;
    }

    /**
     * Determina si un color pertenece al espectro cromático del CAFÉ o MARRÓN (HSL 25°–45° oscuro con saturación).
     * <p>
     * Los tonos amarillos y ámbar brillantes (alta luminosidad o brillo) quedan expresamente excluidos.
     *
     * @param color Color a comprobar.
     * @return {@code true} si cae en el espectro café; {@code false} en caso contrario.
     */
    public static boolean esCafe(Color color) {
        if (color == null) {
            return false;
        }
        double[] hsl = calcularHSL(color);
        double h = hsl[0];
        double s = hsl[1];
        double l = hsl[2];

        // Rango formal de café: Hue entre 25° y 45°, saturación moderada/alta y luminosidad baja (oscuro)
        return h >= 25.0 && h <= 45.0 && l <= 0.48 && s >= 0.20;
    }
}
