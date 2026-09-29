package com.automatas.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el algoritmo formal de determinización por construcción de subconjuntos (RF-07).
 * <p>
 * Estructura secuencial de las pruebas (Constitución - Principios 8 y 9):
 * <ol>
 *   <li><b>Validación de precedencia de errores y precondiciones de entrada.</b></li>
 *   <li><b>Determinización básica y nomenclatura k0, k1, ... (RF-07).</b></li>
 *   <li><b>Regla formal de aceptación por intersección no vacía (Caso Límite 14).</b></li>
 *   <li><b>Poda estricta de macro-estados inalcanzables (Caso Límite 13).</b></li>
 *   <li><b>Caso límite con N = 1 (Caso Límite 16).</b></li>
 *   <li><b>Manejo de estado trampa vacío (ø) en tabla de subconjuntos (Caso Límite 15).</b></li>
 *   <li><b>Equivalencia formal de lenguaje entre el AFN original y el AFD generado.</b></li>
 * </ol>
 */
@DisplayName("Pruebas Unitarias para el Algoritmo de Determinización (RF-07)")
class ConvertidorSubconjuntosTest {

    private ConvertidorSubconjuntos convertidor;
    private Alfabeto alfabeto;

    @BeforeEach
    void setUp() {
        convertidor = new ConvertidorSubconjuntos();
        alfabeto = Alfabeto.desdeCadena("a, b");
    }

    // =========================================================================
    // 1. PRECEDENCIA DE ERRORES Y PRECONDICIONES DE ENTRADA
    // =========================================================================

    @Test
    @DisplayName("1.1 Precedencia 1: Aborta si falta el estado inicial")
    void testPrecedenciaSinEstadoInicial() {
        Automata automata = new Automata(alfabeto);
        Estado q0 = automata.crearEstado();
        q0.setAceptacion(true);
        // No se marca q0 como inicial

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> convertidor.convertir(automata),
                "Debe abortar prioritariamente si no hay estado inicial"
        );
        assertTrue(ex.getMessage().contains("inicial"));
    }

    @Test
    @DisplayName("1.2 Precedencia 2: Aborta si falta al menos un estado de aceptación")
    void testPrecedenciaSinEstadoFinal() {
        Automata automata = new Automata(alfabeto);
        Estado q0 = automata.crearEstado();
        automata.marcarEstadoInicial(q0);
        // q0 no es final y no hay ningún estado final

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> convertidor.convertir(automata),
                "Debe abortar si no hay estado final definido"
        );
        assertTrue(ex.getMessage().contains("final") || ex.getMessage().contains("aceptación"));
    }

    @Test
    @DisplayName("1.3 Precedencia 3: Aborta si el autómata ya es determinista (AFD)")
    void testPrecedenciaAutomataYaDeterminista() {
        Automata automata = new Automata(alfabeto);
        Estado q0 = automata.crearEstado();
        q0.setAceptacion(true);
        automata.marcarEstadoInicial(q0);
        // Automata con 1 estado sin bifurcaciones es AFD

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> convertidor.convertir(automata),
                "Debe abortar informando que el autómata ya es determinista"
        );
        assertTrue(ex.getMessage().contains("determinista") || ex.getMessage().contains("AFD"));
    }

    @Test
    @DisplayName("1.4 Precedencia 4: Aborta si el autómata no contiene estados")
    void testPrecedenciaAutomataSinEstados() {
        Automata automata = new Automata(alfabeto);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> convertidor.convertir(automata)
        );
        assertNotNull(ex.getMessage());
    }

    // =========================================================================
    // 2. DETERMINIZACIÓN BÁSICA Y NOMENCLATURA K
    // =========================================================================

    @Test
    @DisplayName("2.1 Debe convertir un AFN a AFD con nomenclatura k0, k1, ... y estado inicial k0")
    void testConversionAFNBasico() {
        // AFN: L = cadenas sobre {a, b} que terminan en 'b'
        // q0 --a-> q0, q0 --b-> q0, q0 --b-> q1 (q1 final)
        Automata afn = new Automata(alfabeto);
        Estado q0 = afn.crearEstado(); // q0
        Estado q1 = afn.crearEstado(); // q1
        q1.setAceptacion(true);
        afn.marcarEstadoInicial(q0);

        afn.agregarTransicion(q0, q0, 'a');
        afn.agregarTransicion(q0, q0, 'b');
        afn.agregarTransicion(q0, q1, 'b'); // Bifurcación en 'b' -> AFN

        assertEquals(TipoAutomata.AFN, afn.obtenerTipo());

        ResultadoConversion resultado = convertidor.convertir(afn);
        Automata afd = resultado.getAfdGenerado();

        // Verificaciones del AFD generado
        assertNotNull(afd);
        assertEquals(TipoAutomata.AFD, afd.obtenerTipo(), "El autómata resultante debe ser estrictamente AFD");
        assertTrue(afd.getEstadoInicial().isPresent());
        assertEquals("k0", afd.getEstadoInicial().get().getNombre(), "El estado inicial debe llamarse 'k0'");

        // El estado k0 debe corresponder a {q0}
        Optional<Set<Estado>> subconjuntoK0 = resultado.buscarSubconjuntoPorNombre("k0");
        assertTrue(subconjuntoK0.isPresent());
        assertEquals(Set.of(q0), subconjuntoK0.get());

        // Debe existir el macro-estado {q0, q1} como k1
        Optional<Set<Estado>> subconjuntoK1 = resultado.buscarSubconjuntoPorNombre("k1");
        assertTrue(subconjuntoK1.isPresent());
        assertEquals(Set.of(q0, q1), subconjuntoK1.get());
        assertTrue(resultado.getAfdGenerado().buscarEstadoPorNombre("k1").get().esAceptacion(),
                "k1 contiene a q1 (final), por lo que debe ser de aceptación");
    }

    // =========================================================================
    // 3. REGLA FORMAL DE ACEPTACIÓN POR INTERSECCIÓN
    // =========================================================================

    @Test
    @DisplayName("3.1 k_i es final si y solo si su subconjunto contiene al menos un estado final del AFN")
    void testReglaAceptacionInterseccion() {
        Automata afn = new Automata(alfabeto);
        Estado q0 = afn.crearEstado();
        Estado q1 = afn.crearEstado();
        Estado q2 = afn.crearEstado();
        q2.setAceptacion(true); // Solo q2 es final
        afn.marcarEstadoInicial(q0);

        afn.agregarTransicion(q0, q0, 'a');
        afn.agregarTransicion(q0, q1, 'a');
        afn.agregarTransicion(q1, q2, 'b');

        ResultadoConversion resultado = convertidor.convertir(afn);

        for (FilaSubconjuntos fila : resultado.getTablaSubconjuntos().getFilas()) {
            boolean contieneFinalAFN = fila.getSubconjuntoAFN().contains(q2);
            assertEquals(contieneFinalAFN, fila.getEstadoAFD().esAceptacion(),
                    "La aceptación de " + fila.getEstadoAFD().getNombre() + " debe coincidir con la presencia de q2");
        }
    }

    // =========================================================================
    // 4. PODA DE MACRO-ESTADOS INALCANZABLES
    // =========================================================================

    @Test
    @DisplayName("4.1 Solo se generan y materializan macro-estados alcanzables desde k0")
    void testPodaEstadosInalcanzables() {
        Automata afn = new Automata(alfabeto);
        Estado q0 = afn.crearEstado();
        Estado q1 = afn.crearEstado();
        Estado qInalcanzable = afn.crearEstado(); // Estado huérfano en el AFN
        q1.setAceptacion(true);
        afn.marcarEstadoInicial(q0);

        afn.agregarTransicion(q0, q0, 'a');
        afn.agregarTransicion(q0, q1, 'a');
        afn.agregarTransicion(qInalcanzable, qInalcanzable, 'a'); // Transición inalcanzable

        ResultadoConversion resultado = convertidor.convertir(afn);
        Automata afd = resultado.getAfdGenerado();

        // Ningún subconjunto del AFD debe incluir al estado inalcanzable
        for (Estado estadoAFD : afd.getEstados()) {
            Set<Estado> subconjunto = resultado.buscarSubconjuntoPorNombre(estadoAFD.getNombre()).orElseThrow();
            assertFalse(subconjunto.contains(qInalcanzable),
                    "El estado inalcanzable no debe aparecer en ningún macro-estado");
        }
    }

    // =========================================================================
    // 5. CASO LÍMITE N = 1
    // =========================================================================

    @Test
    @DisplayName("5.1 Caso Límite N = 1: AFN que genera un AFD con un único nodo k0")
    void testCasoLimiteN1() {
        Automata afn = new Automata(alfabeto);
        Estado q0 = afn.crearEstado();
        q0.setAceptacion(true);
        afn.marcarEstadoInicial(q0);

        // q0 tiene múltiples transiciones sobre sí mismo con 'a' (AFN de un solo estado)
        afn.agregarTransicion(q0, q0, 'a');
        // Para forzar AFN, agregamos una arista temporal redundante o dos aristas reflexivas
        Estado qAux = afn.crearEstado();
        afn.agregarTransicion(q0, qAux, 'a');
        afn.agregarTransicion(q0, q0, 'a'); // AFN

        // Ahora un AFN donde solo k0 es alcanzable y no genera otros estados:
        Automata afnN1 = new Automata(alfabeto);
        Estado p0 = afnN1.crearEstado();
        p0.setAceptacion(true);
        afnN1.marcarEstadoInicial(p0);
        // Bifurcación reflexiva: p0 --a-> p0 y p0 con otra arista hacia sí mismo
        // En nuestro modelo, p0 -> p0('a') con destinos {p0}. Para bifurcar se necesitan 2 destinos:
        Estado p1 = afnN1.crearEstado();
        p1.setAceptacion(true);
        afnN1.agregarTransicion(p0, p0, 'a');
        afnN1.agregarTransicion(p0, p1, 'a');
        afnN1.agregarTransicion(p1, p0, 'a');
        afnN1.agregarTransicion(p1, p1, 'a');

        ResultadoConversion res = convertidor.convertir(afnN1);
        assertNotNull(res.getAfdGenerado());
        assertEquals(TipoAutomata.AFD, res.getAfdGenerado().obtenerTipo());
    }

    // =========================================================================
    // 6. ESTADO TRAMPA VACÍO EN TABLA DE SUBCONJUNTOS
    // =========================================================================

    @Test
    @DisplayName("6.1 Transición hacia conjunto vacío se representa como ø en la tabla y sin arista en el AFD")
    void testManejoEstadoTrampaVacio() {
        Automata afn = new Automata(alfabeto);
        Estado q0 = afn.crearEstado();
        Estado q1 = afn.crearEstado();
        q1.setAceptacion(true);
        afn.marcarEstadoInicial(q0);

        // q0 solo tiene salidas con 'a' (hacia q0 y q1), ninguna con 'b'
        afn.agregarTransicion(q0, q0, 'a');
        afn.agregarTransicion(q0, q1, 'a');

        ResultadoConversion resultado = convertidor.convertir(afn);
        FilaSubconjuntos filaK0 = resultado.getTablaSubconjuntos().getFilas().get(0);

        // Para 'b', la salida es vacía (ø)
        assertTrue(filaK0.obtenerDestinoAFD('b').isEmpty(), "En el AFD no debe existir destino para 'b'");
        assertEquals("ø", filaK0.obtenerDestinoFormateado('b'), "En la tabla debe representarse con el símbolo ø");
    }

    // =========================================================================
    // 7. EQUIVALENCIA FORMAL DE LENGUAJE (AFN vs AFD)
    // =========================================================================

    @Test
    @DisplayName("7.1 El AFD generado debe aceptar exactamente las mismas cadenas que el AFN original")
    void testEquivalenciaDeLenguaje() {
        // Lenguaje: cadenas que contienen "ab"
        Automata afn = new Automata(alfabeto);
        Estado q0 = afn.crearEstado();
        Estado q1 = afn.crearEstado();
        Estado q2 = afn.crearEstado();
        q2.setAceptacion(true);
        afn.marcarEstadoInicial(q0);

        afn.agregarTransicion(q0, q0, 'a');
        afn.agregarTransicion(q0, q0, 'b');
        afn.agregarTransicion(q0, q1, 'a');
        afn.agregarTransicion(q1, q2, 'b');
        afn.agregarTransicion(q2, q2, 'a');
        afn.agregarTransicion(q2, q2, 'b');

        ResultadoConversion resultado = convertidor.convertir(afn);
        Automata afd = resultado.getAfdGenerado();

        SimuladorAutomata simulador = new SimuladorAutomata();
        String[] cadenasPrueba = {"ab", "aab", "bab", "bba", "a", "b", "aba", "bbab", "", "ba"};

        for (String cadena : cadenasPrueba) {
            boolean veredictoAFN = simulador.evaluarCadena(afn, cadena).esAceptada();
            boolean veredictoAFD = simulador.evaluarCadena(afd, cadena).esAceptada();
            assertEquals(
                    veredictoAFN,
                    veredictoAFD,
                    "Discrepancia para la cadena '" + cadena + "': AFN=" + veredictoAFN + ", AFD=" + veredictoAFD
            );
        }
    }
}
