package com.automatas.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad inmutable Alfabeto (Σ).
 * <p>
 * Estructura secuencial de las pruebas (Constitución - Principio 9):
 * <ol>
 *   <li><b>Construcción válida y distinción de mayúsculas/minúsculas.</b></li>
 *   <li><b>Tratamiento sintáctico y recorte de espacios.</b></li>
 *   <li><b>Rechazo de caracteres inválidos y casos límite.</b></li>
 *   <li><b>Protección de inmutabilidad del estado.</b></li>
 *   <li><b>Operaciones de dominio: validación de palabras y redefinición.</b></li>
 * </ol>
 */
@DisplayName("Pruebas Unitarias para la Entidad Alfabeto (Σ)")
class AlfabetoTest {

    // =========================================================================
    // 1. CONSTRUCCIÓN VÁLIDA Y CASOS FELICES
    // =========================================================================

    @Test
    @DisplayName("1.1 Debe crear un alfabeto válido con letras únicas respetando mayúsculas y minúsculas")
    void testCreacionValidaConLetrasUnicas() {
        Alfabeto alfabeto = Alfabeto.desdeCadena("a, b, C");

        assertNotNull(alfabeto, "La instancia del alfabeto no debe ser nula");
        assertEquals(3, alfabeto.cardinalidad(), "La cardinalidad debe ser 3");
        assertTrue(alfabeto.contiene('a'), "Debe contener el símbolo 'a'");
        assertTrue(alfabeto.contiene('b'), "Debe contener el símbolo 'b'");
        assertTrue(alfabeto.contiene('C'), "Debe contener el símbolo 'C'");
        assertFalse(alfabeto.contiene('c'), "No debe contener 'c' minúscula si solo se definió 'C' mayúscula");
    }

    @Test
    @DisplayName("1.2 Debe distinguir estrictamente entre mayúsculas y minúsculas como símbolos distintos")
    void testDistingueMayusculasDeMinusculas() {
        Alfabeto alfabeto = Alfabeto.desdeCadena("a, A");

        assertEquals(2, alfabeto.cardinalidad(), "Deben coexistir 'a' y 'A' como dos símbolos distintos");
        assertTrue(alfabeto.contiene('a'), "Debe contener 'a'");
        assertTrue(alfabeto.contiene('A'), "Debe contener 'A'");
    }

    // =========================================================================
    // 2. DELIMITACIÓN SINTÁCTICA Y TRATAMIENTO DE ESPACIOS
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe ignorar espacios en blanco alrededor de las comas delimitadoras")
    void testRecortaEspaciosAlrededorDeComas() {
        Alfabeto alfabeto = Alfabeto.desdeCadena("   x  ,   y   ,   Z   ");

        assertEquals(3, alfabeto.cardinalidad());
        assertTrue(alfabeto.contiene('x'));
        assertTrue(alfabeto.contiene('y'));
        assertTrue(alfabeto.contiene('Z'));
    }

    @Test
    @DisplayName("2.2 Debe rechazar comas consecutivas, al inicio o al final sin símbolo")
    void testRechazaComasConsecutivas() {
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena("a,,b"));
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena(",a,b"));
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena("a,b,"));
    }

    // =========================================================================
    // 3. RECHAZO DE ENTRADAS INVÁLIDAS Y CASOS LÍMITE
    // =========================================================================

    @Test
    @DisplayName("3.1 Debe rechazar definiciones nulas o vacías")
    void testRechazaCadenaNulaOVacia() {
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena(null));
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena(""));
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "9", "a, 2, b", "1, 2, 3"})
    @DisplayName("3.2 Debe rechazar números en la definición del alfabeto")
    void testRechazaNumeros(String entradaInvalida) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Alfabeto.desdeCadena(entradaInvalida),
                "Debe lanzar IllegalArgumentException ante la presencia de números"
        );
        assertTrue(ex.getMessage().contains("letra"), "El mensaje de error debe indicar que solo se admiten letras");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a, +, b", "@, b", "a, #", "a, -, c", "*, /"})
    @DisplayName("3.3 Debe rechazar operadores aritméticos y caracteres especiales")
    void testRechazaOperadoresYCaracteresEspeciales(String entradaInvalida) {
        assertThrows(
                IllegalArgumentException.class,
                () -> Alfabeto.desdeCadena(entradaInvalida),
                "Debe lanzar IllegalArgumentException ante operadores o caracteres especiales"
        );
    }

    @Test
    @DisplayName("3.4 Debe rechazar el espacio en blanco como símbolo del alfabeto")
    void testRechazaEspacioComoSimbolo() {
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.desdeCadena("a,  , b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab, c", "a, xyz", "palabra"})
    @DisplayName("3.5 Debe rechazar cadenas multicarácter como símbolos")
    void testRechazaCadenasMulticaracter(String entradaInvalida) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Alfabeto.desdeCadena(entradaInvalida),
                "Debe lanzar IllegalArgumentException ante símbolos multicarácter"
        );
        assertTrue(ex.getMessage().contains("única letra") || ex.getMessage().contains("un solo carácter"));
    }

    @Test
    @DisplayName("3.6 Debe rechazar símbolos duplicados en la definición")
    void testRechazaSimbolosDuplicados() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Alfabeto.desdeCadena("a, b, a"),
                "Debe rechazar símbolos duplicados"
        );
        assertTrue(ex.getMessage().contains("duplicado"), "El mensaje debe indicar que el símbolo está duplicado");
    }

    // =========================================================================
    // 4. INMUTABILIDAD Y PROTECCIÓN DE ESTADO
    // =========================================================================

    @Test
    @DisplayName("4.1 El conjunto de símbolos expuesto debe ser estrictamente inmutable")
    void testConjuntoSimbolosEsInmutable() {
        Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");
        Set<Character> simbolos = alfabeto.getSimbolos();

        assertThrows(UnsupportedOperationException.class, () -> simbolos.add('c'),
                "No debe permitir modificar el conjunto retornado externamente");
    }

    // =========================================================================
    // 5. OPERACIONES DE DOMINIO (LENGUAJE Y REDEFINICIÓN)
    // =========================================================================

    @Test
    @DisplayName("5.1 Debe validar si una palabra está formada exclusivamente por símbolos de Σ")
    void testValidacionDeCadenas() {
        Alfabeto alfabeto = Alfabeto.desdeCadena("a, b");

        assertDoesNotThrow(() -> alfabeto.validarCadena("ababbaba"));
        assertDoesNotThrow(() -> alfabeto.validarCadena("")); // Cadena vacía (λ) no tiene símbolos ajenos

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> alfabeto.validarCadena("abc")
        );
        assertTrue(ex.getMessage().contains("'c'"), "El mensaje debe señalar el carácter inválido 'c'");
    }

    @Test
    @DisplayName("5.2 Debe bloquear la redefinición ante transiciones activas y generar nueva instancia inmutable en caso libre")
    void testBloqueoConTransicionesActivas() {
        Alfabeto original = Alfabeto.desdeCadena("a, b");

        // Paso 1: Falla si existen transiciones activas en el autómata
        assertThrows(
                IllegalStateException.class,
                () -> original.redefinir("c, d", true),
                "Debe fallar si existen transiciones activas"
        );

        // Paso 2: Si no hay transiciones, retorna una nueva instancia inmutable
        Alfabeto nuevo = original.redefinir("c, d", false);
        assertNotSame(original, nuevo, "Debe retornar una nueva instancia inmutable");

        // Paso 3: Verificar que el nuevo alfabeto tiene los símbolos actualizados
        assertEquals(2, nuevo.cardinalidad());
        assertTrue(nuevo.contiene('c'));
        assertTrue(nuevo.contiene('d'));
        assertFalse(nuevo.contiene('a'));

        // Paso 4: Verificar que el alfabeto original no fue mutado
        assertEquals(2, original.cardinalidad());
        assertTrue(original.contiene('a'));
        assertTrue(original.contiene('b'));
        assertFalse(original.contiene('c'));
    }
}
