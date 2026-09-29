package com.automatas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias dedicadas para la entidad inmutable Transicion.
 * <p>
 * Estructura secuencial de las pruebas (Constitución - Principios 8 y 9):
 * <ol>
 *   <li><b>Construcción válida y consultas de propiedades.</b></li>
 *   <li><b>Rechazo de parámetros nulos o símbolos no alfabéticos.</b></li>
 *   <li><b>Detección de autolazos.</b></li>
 *   <li><b>Identidad estructural y comparación canónica.</b></li>
 * </ol>
 */
@DisplayName("Pruebas Unitarias para la Entidad Transicion")
class TransicionTest {

    // =========================================================================
    // 1. CONSTRUCCIÓN VÁLIDA Y CONSULTAS
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe instanciar una transición válida entre dos estados con símbolo de Σ")
    void testCreacionValida() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");
        Transicion t = new Transicion(q0, q1, 'a');

        assertSame(q0, t.getOrigen());
        assertSame(q1, t.getDestino());
        assertEquals('a', t.getSimbolo());
        assertFalse(t.esAutolazo());
        assertEquals("(q0, a) -> q1", t.toString());
    }

    // =========================================================================
    // 2. RECHAZO DE PARÁMETROS INVÁLIDOS
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe rechazar estado origen o destino nulo")
    void testRechazaEstadosNulos() {
        Estado q0 = new Estado("q0");

        assertThrows(IllegalArgumentException.class, () -> new Transicion(null, q0, 'a'));
        assertThrows(IllegalArgumentException.class, () -> new Transicion(q0, null, 'a'));
    }

    @ParameterizedTest
    @ValueSource(chars = {'0', '1', '9', '+', '-', '*', ' ', '@', '#'})
    @DisplayName("2.2 Debe rechazar caracteres que no pertenezcan al rango alfabético a-z o A-Z")
    void testRechazaSimbolosInvalidos(char simboloInvalido) {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");

        assertThrows(IllegalArgumentException.class, () -> new Transicion(q0, q1, simboloInvalido));
    }

    // =========================================================================
    // 3. DETECCIÓN DE AUTOLAZOS
    // =========================================================================

    @Test
    @DisplayName("3.1 Debe detectar si la transición conecta un estado consigo mismo")
    void testAutolazo() {
        Estado q0 = new Estado("q0");
        Transicion autolazo = new Transicion(q0, q0, 'b');

        assertTrue(autolazo.esAutolazo());
    }

    // =========================================================================
    // 4. IDENTIDAD ESTRUCTURAL (OBJECT)
    // =========================================================================

    @Test
    @DisplayName("4.1 Dos transiciones con misma terna (origen, destino, símbolo) deben ser iguales")
    void testIgualdadEstructural() {
        Estado q0 = new Estado("q0");
        Estado q1 = new Estado("q1");

        Transicion t1 = new Transicion(q0, q1, 'x');
        Transicion t2 = new Transicion(q0, q1, 'x');
        Transicion tMayuscula = new Transicion(q0, q1, 'X');
        Transicion tInvertida = new Transicion(q1, q0, 'x');

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, tMayuscula, "Debe ser sensible a mayúsculas/minúsculas");
        assertNotEquals(t1, tInvertida, "Debe ser sensible a la dirección");
    }
}
