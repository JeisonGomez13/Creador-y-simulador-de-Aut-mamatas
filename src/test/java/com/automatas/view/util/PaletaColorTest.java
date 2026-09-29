package com.automatas.view.util;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para certificar la prohibición absoluta de tonos azul y café
 * y la integridad de los tokens de color del sistema de diseño (RNF-06, Constitución Principio 7).
 */
@DisplayName("Pruebas Unitarias de la Paleta de Color y Tokens de Diseño (RNF-06, Const. 7)")
class PaletaColorTest {

    // =========================================================================
    // 1. VERIFICACIÓN DE TOKENS OBLIGATORIOS ESPECIFICADOS
    // =========================================================================

    @Test
    @DisplayName("1.1 Deben existir los tokens neutros y de acento exactos requeridos en tasks.md")
    void testTokensRequeridosPresentes() {
        assertEquals("#F4F4F6", PaletaColor.HEX_FONDO_PRINCIPAL.toUpperCase());
        assertEquals("#1E1E24", PaletaColor.HEX_GRAFITO_OSCURO.toUpperCase());
        assertEquals("#2B2D42", PaletaColor.HEX_GRAFITO_SECUNDARIO.toUpperCase());
        assertEquals("#10B981", PaletaColor.HEX_ACEPTACION.toUpperCase());
        assertEquals("#8B5CF6", PaletaColor.HEX_SELECCION.toUpperCase());
        assertEquals("#F59E0B", PaletaColor.HEX_ADVERTENCIA.toUpperCase());

        assertNotNull(PaletaColor.FONDO_PRINCIPAL);
        assertNotNull(PaletaColor.GRAFITO_OSCURO);
        assertNotNull(PaletaColor.GRAFITO_SECUNDARIO);
        assertNotNull(PaletaColor.ACEPTACION);
        assertNotNull(PaletaColor.SELECCION);
        assertNotNull(PaletaColor.ADVERTENCIA);
    }

    // =========================================================================
    // 2. DETECCIÓN PRECISA DE TONOS AZUL Y CAFÉ (VALIDACIÓN DEL DETECTOR)
    // =========================================================================

    @Test
    @DisplayName("2.1 El método esAzul debe identificar fielmente colores azules conocidos (HSL 190°–250°)")
    void testDetectorAzulIdentificaColoresAzules() {
        Color azulPuro = Color.web("#0000FF");
        Color azulDodger = Color.web("#1E90FF");
        Color azulModenaJavaFX = Color.web("#0096C9");
        Color azulRoyal = Color.web("#4169E1");
        Color azulCielo = Color.web("#87CEEB");
        Color azulMarino = Color.web("#000080");

        assertTrue(PaletaColor.esAzul(azulPuro), "#0000FF debe detectarse como azul");
        assertTrue(PaletaColor.esAzul(azulDodger), "#1E90FF debe detectarse como azul");
        assertTrue(PaletaColor.esAzul(azulModenaJavaFX), "#0096C9 (Modena) debe detectarse como azul");
        assertTrue(PaletaColor.esAzul(azulRoyal), "#4169E1 debe detectarse como azul");
        assertTrue(PaletaColor.esAzul(azulCielo), "#87CEEB debe detectarse como azul");
        assertTrue(PaletaColor.esAzul(azulMarino), "#000080 debe detectarse como azul");
    }

    @Test
    @DisplayName("2.2 El método esCafe debe identificar fielmente colores café conocidos (HSL 25°–45° oscuro)")
    void testDetectorCafeIdentificaColoresCafe() {
        Color cafeSaddle = Color.web("#8B4513");
        Color cafePuro = Color.web("#804000");
        Color marronEstandar = Color.web("#964B00");
        Color cafeOscuro = Color.web("#654321");
        Color cafeCanela = Color.web("#8B5A2B");

        assertTrue(PaletaColor.esCafe(cafeSaddle), "#8B4513 debe detectarse como café");
        assertTrue(PaletaColor.esCafe(cafePuro), "#804000 debe detectarse como café");
        assertTrue(PaletaColor.esCafe(marronEstandar), "#964B00 debe detectarse como café");
        assertTrue(PaletaColor.esCafe(cafeOscuro), "#654321 debe detectarse como café");
        assertTrue(PaletaColor.esCafe(cafeCanela), "#8B5A2B debe detectarse como café");
    }

    // =========================================================================
    // 3. CERTIFICACIÓN DE QUE NINGÚN TOKEN DE LA PALETA ES AZUL NI CAFÉ
    // =========================================================================

    @Test
    @DisplayName("3.1 Certificar que el 100% de los tokens en PaletaColor no caen en azul ni café")
    void testCertificacionTokensPaletaSinAzulNiCafe() {
        Map<String, Color> todosLosColores = PaletaColor.obtenerTodosLosColores();
        assertFalse(todosLosColores.isEmpty(), "La paleta debe contener tokens registrados");

        List<String> violaciones = new ArrayList<>();

        for (Map.Entry<String, Color> entry : todosLosColores.entrySet()) {
            String nombre = entry.getKey();
            Color color = entry.getValue();

            if (PaletaColor.esAzul(color)) {
                violaciones.add(String.format("Token '%s' (%s) clasificado en el espectro AZUL (HSL)",
                        nombre, color));
            }
            if (PaletaColor.esCafe(color)) {
                violaciones.add(String.format("Token '%s' (%s) clasificado en el espectro CAFÉ (HSL)",
                        nombre, color));
            }
        }

        assertTrue(violaciones.isEmpty(),
                "Se encontraron tokens que violan la Regla 7 de la Constitución:\n" +
                String.join("\n", violaciones));
    }

    // =========================================================================
    // 4. INSPECCIÓN DEL ARCHIVO CSS (estilos.css)
    // =========================================================================

    @Test
    @DisplayName("4.1 El archivo estilos.css debe existir y no contener ningún color azul ni café")
    void testCertificacionArchivoCssSinAzulNiCafe() throws IOException {
        Path rutaCss = Paths.get("src", "main", "resources", "css", "estilos.css");
        assertTrue(Files.exists(rutaCss), "El archivo 'src/main/resources/css/estilos.css' debe existir");

        String contenidoCss = Files.readString(rutaCss);
        assertFalse(contenidoCss.isBlank(), "El archivo estilos.css no debe estar vacío");

        // Extraer todos los códigos hexadecimales (#RRGGBB o #RGB)
        Pattern patronHex = Pattern.compile("#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\\b");
        Matcher matcher = patronHex.matcher(contenidoCss);

        List<String> violaciones = new ArrayList<>();
        int coloresInspeccionados = 0;

        while (matcher.find()) {
            String hex = matcher.group();
            coloresInspeccionados++;
            Color color = Color.web(hex);

            if (PaletaColor.esAzul(color)) {
                violaciones.add(String.format("Color CSS '%s' clasificado como AZUL", hex));
            }
            if (PaletaColor.esCafe(color)) {
                violaciones.add(String.format("Color CSS '%s' clasificado como CAFÉ", hex));
            }
        }

        assertTrue(coloresInspeccionados > 0, "Se deben haber inspeccionado colores en estilos.css");
        assertTrue(violaciones.isEmpty(),
                "Se detectaron colores prohibidos en 'estilos.css':\n" +
                String.join("\n", violaciones));
    }
}
