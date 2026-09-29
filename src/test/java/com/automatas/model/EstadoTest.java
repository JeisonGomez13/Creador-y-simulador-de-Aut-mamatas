package com.automatas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para las entidades Estado y Transicion del modelo formal.
 * <p>
 * Estructura secuencial de las pruebas (Constitución - Principios 8 y 9):
 * <ol>
 *   <li><b>Construcción válida y normalización de Estado.</b></li>
 *   <li><b>Rechazo de identificadores inválidos en Estado.</b></li>
 *   <li><b>Renombrado y preservación de estado ante fallos.</b></li>
 *   <li><b>Gestión del flag de aceptación en Estado.</b></li>
 *   <li><b>Identidad y comparación formal de Estado.</b></li>
 *   <li><b>Construcción, inmutabilidad y comparación de Transicion.</b></li>
 * </ol>
 */
@DisplayName("Pruebas Unitarias para Estado y Transicion")
class EstadoTest {

    // =========================================================================
    // 1. CONSTRUCCIÓN VÁLIDA Y NORMALIZACIÓN DE ESTADO
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe crear un estado válido por defecto como no final con trim aplicado")
    void testCreacionEstadoValido() {
        Estado estado = new Estado("  q0  ");

        assertEquals("q0", estado.getNombre(), "El nombre debe almacenarse con trim aplicado");
        assertFalse(estado.esAceptacion(), "Por defecto no debe ser estado de aceptación");
    }

    @Test
    @DisplayName("1.2 Debe crear un estado especificando explícitamente el flag de aceptación")
    void testCreacionEstadoConAceptacion() {
        Estado estado = new Estado("q_final", true);

        assertEquals("q_final", estado.getNombre());
        assertTrue(estado.esAceptacion(), "Debe marcarse como estado de aceptación");
    }

    @Test
    @DisplayName("1.3 Debe admitir nombres en el límite superior de 8 caracteres")
    void testNombreEnLimiteMaximoDe8Caracteres() {
        assertDoesNotThrow(() -> new Estado("12345678"));
        assertDoesNotThrow(() -> new Estado("abcdefgh"));

        Estado estado = new Estado("  12345678  ");
        assertEquals("12345678", estado.getNombre());
    }

    // =========================================================================
    // 2. RECHAZO DE IDENTIFICADORES INVÁLIDOS EN ESTADO
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe rechazar identificadores nulos o vacíos")
    void testRechazaNombreNuloOVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Estado(null));
        assertThrows(IllegalArgumentException.class, () -> new Estado(""));
        assertThrows(IllegalArgumentException.class, () -> new Estado("    "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "estado_largo", "q012345678"})
    @DisplayName("2.2 Debe rechazar identificadores mayores a 8 caracteres")
    void testRechazaNombreMayorA8Caracteres(String nombreInvalido) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Estado(nombreInvalido),
                "Debe rechazar nombres mayores a 8 caracteres tras trim"
        );
        assertTrue(ex.getMessage().contains("8 caracteres"), "El mensaje debe especificar el límite de 8 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = {"q0\nq1", "q0\r", "q\n", "\rq0"})
    @DisplayName("2.3 Debe rechazar identificadores con saltos de línea")
    void testRechazaNombresConSaltosDeLinea(String nombreConSalto) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Estado(nombreConSalto),
                "Debe rechazar nombres que contengan saltos de línea"
        );
        assertTrue(ex.getMessage().contains("salto"), "El mensaje debe indicar la prohibición de saltos de línea");
    }

    // =========================================================================
    // 3. RENOMBRADO Y PRESERVACIÓN DE ESTADO ANTE FALLOS
    // =========================================================================

    @Test
    @DisplayName("3.1 Debe permitir renombrar un estado con un identificador válido")
    void testRenombradoValido() {
        Estado estado = new Estado("q0");
        estado.renombrar("  inicio  ");

        assertEquals("inicio", estado.getNombre(), "El nuevo nombre debe actualizarse con trim aplicado");
    }

    @Test
    @DisplayName("3.2 Debe preservar el nombre anterior si el renombrado falla por validación")
    void testPreservaNombreAnteriorAnteFalloDeRenombrado() {
        Estado estado = new Estado("q0");

        assertThrows(IllegalArgumentException.class, () -> estado.renombrar("nombre_demasiado_largo"));
        assertEquals("q0", estado.getNombre(), "Debe preservar el nombre previo si supera 8 caracteres");

        assertThrows(IllegalArgumentException.class, () -> estado.renombrar("   "));
        assertEquals("q0", estado.getNombre(), "Debe preservar el nombre previo si está en blanco");

        assertThrows(IllegalArgumentException.class, () -> estado.renombrar(null));
        assertEquals("q0", estado.getNombre(), "Debe preservar el nombre previo si es nulo");
    }

    // =========================================================================
    // 4. GESTIÓN DEL FLAG DE ACEPTACIÓN EN ESTADO
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe permitir alternar el flag de aceptación")
    void testModificarAceptacion() {
        Estado estado = new Estado("q0");
        assertFalse(estado.esAceptacion());

        estado.setAceptacion(true);
        assertTrue(estado.esAceptacion());

        estado.setAceptacion(false);
        assertFalse(estado.esAceptacion());
    }

    // =========================================================================
    // 5. IDENTIDAD, HASHCODE Y COMPARABILIDAD DE ESTADO
    // =========================================================================

    @Test
    @DisplayName("5.1 Dos estados con el mismo identificador son iguales independientemente del flag de aceptación")
    void testIgualdadYHashCodeDeEstado() {
        Estado e1 = new Estado("q0", false);
        Estado e2 = new Estado("q0", true);
        Estado e3 = new Estado("q1", false);

        assertEquals(e1, e2, "Dos estados con mismo nombre son formalmente idénticos en el autómata");
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }

    @Test
    @DisplayName("5.2 Debe implementar orden natural por nombre para soporte en TreeSet")
    void testComparabilidadDeEstado() {
        Estado e0 = new Estado("q0");
        Estado e1 = new Estado("q1");

        assertTrue(e0.compareTo(e1) < 0);
        assertTrue(e1.compareTo(e0) > 0);
        assertEquals(0, e0.compareTo(new Estado("q0")));
    }

    // =========================================================================
    // 6. CONSTRUCCIÓN, INMUTABILIDAD Y COMPARACIÓN DE TRANSICIÓN
    // =========================================================================

    @Test
    @DisplayName("6.1 Debe crear una transición válida entre dos estados con un símbolo formal")
    void testCreacionTransicionValida() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");
        Transicion t = new Transicion(q0, q1, 'a');

        assertSame(q0, t.getOrigen());
        assertSame(q1, t.getDestino());
        assertEquals('a', t.getSimbolo());
        assertFalse(t.esAutolazo());
    }

    @Test
    @DisplayName("6.2 Debe detectar correctamente un autolazo reflexivo")
    void testTransicionAutolazo() {
        Estado q0 = new Estado("q0");
        Transicion autolazo = new Transicion(q0, q0, 'b');

        assertTrue(autolazo.esAutolazo(), "Debe identificar que origen y destino son el mismo estado");
    }

    @Test
    @DisplayName("6.3 Debe rechazar transiciones con estados nulos o símbolos no alfabéticos")
    void testRechazoParametrosInvalidosEnTransicion() {
        Estado q0 = new Estado("q0");

        assertThrows(IllegalArgumentException.class, () -> new Transicion(null, q0, 'a'));
        assertThrows(IllegalArgumentException.class, () -> new Transicion(q0, null, 'a'));
        assertThrows(IllegalArgumentException.class, () -> new Transicion(q0, q0, '1'),
                "Debe rechazar números como símbolos de transición");
        assertThrows(IllegalArgumentException.class, () -> new Transicion(q0, q0, '+'),
                "Debe rechazar operadores como símbolos de transición");
        assertThrows(IllegalArgumentException.class, () -> new Transicion(q0, q0, ' '),
                "Debe rechazar espacios como símbolos de transición");
    }

    @Test
    @DisplayName("6.4 Dos transiciones con mismo origen, destino y símbolo son idénticas (Caso Límite 25)")
    void testComparacionTransicionesIdenticas() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");

        Transicion t1 = new Transicion(q0, q1, 'a');
        Transicion t2 = new Transicion(q0, q1, 'a');
        Transicion t3 = new Transicion(q0, q1, 'A'); // Sensible a mayúsculas
        Transicion t4 = new Transicion(q1, q0, 'a'); // Sentido opuesto

        assertEquals(t1, t2, "Transiciones con misma terna (origen, destino, símbolo) deben ser iguales");
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, t3, "Debe distinguir entre 'a' y 'A'");
        assertNotEquals(t1, t4, "Debe distinguir sentido de transición");
    }
}
