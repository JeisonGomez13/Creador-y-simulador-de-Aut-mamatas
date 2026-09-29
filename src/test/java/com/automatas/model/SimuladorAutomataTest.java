package com.automatas.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el motor de simulación de autómatas y trazabilidad de caminos.
 * <p>
 * Estructura secuencial de las pruebas (Constitución - Principios 8 y 9):
 * <ol>
 *   <li><b>Validación de precondiciones y excepciones de entrada (inicial y final requeridos).</b></li>
 *   <li><b>Evaluación de la cadena vacía (λ).</b></li>
 *   <li><b>Simulación sobre AFD (caminos deterministas, éxitos y rechazos).</b></li>
 *   <li><b>Simulación sobre AFN (exploración de múltiples caminos y ramas).</b></li>
 *   <li><b>Trazabilidad e inspección detallada de pasos y transiciones.</b></li>
 * </ol>
 */
@DisplayName("Pruebas Unitarias para el Motor de Simulación y Caminos (RF-06)")
class SimuladorAutomataTest {

    private SimuladorAutomata simulador;
    private Alfabeto alfabeto;
    private Automata automata;

    @BeforeEach
    void setUp() {
        simulador = new SimuladorAutomata();
        alfabeto = Alfabeto.desdeCadena("a, b");
        automata = new Automata(alfabeto);
    }

    // =========================================================================
    // 1. VALIDACIÓN DE PRECONDICIONES Y EXCEPCIONES
    // =========================================================================

    @Test
    @DisplayName("1.1 testAbortoSinEstadoInicial: Simular sin estado inicial debe lanzar IllegalStateException")
    void testAbortoSinEstadoInicial() {
        Estado q0 = automata.crearEstado(); // q0 existe pero no es inicial
        q0.setAceptacion(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> simulador.evaluarCadena(automata, "a"),
                "Debe abortar si el autómata carece de estado inicial"
        );
        assertTrue(ex.getMessage().contains("inicial"));
    }

    @Test
    @DisplayName("1.2 testAbortoSinEstadoFinal: Simular sin al menos un estado de aceptación debe lanzar IllegalStateException")
    void testAbortoSinEstadoFinal() {
        Estado q0 = automata.crearEstado();
        automata.marcarEstadoInicial(q0);
        // q0 no es final y no existe ningún otro estado final

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> simulador.evaluarCadena(automata, "a"),
                "Debe abortar si el autómata carece de al menos un estado de aceptación"
        );
        assertTrue(ex.getMessage().contains("final") || ex.getMessage().contains("aceptación"));
    }

    @Test
    @DisplayName("1.3 testRechazoCaracteresAjenosAlAlfabeto: Símbolo ajeno a Σ debe lanzar IllegalArgumentException")
    void testRechazoCaracteresAjenosAlAlfabeto() {
        Estado q0 = automata.crearEstado();
        q0.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> simulador.evaluarCadena(automata, "abxc"),
                "Debe abortar antes de evaluar si hay símbolos ajenos a Σ"
        );
        assertTrue(ex.getMessage().contains("'x'"));
    }

    @Test
    @DisplayName("1.4 testRecorteDeEspaciosTrim: Aplica trim() automático antes de evaluar")
    void testRecorteDeEspaciosTrim() {
        Estado q0 = automata.crearEstado();
        q0.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        // Espacios en blanco tras trim resultan en la cadena vacía λ
        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "   ");
        assertTrue(resultado.esAceptada());
        assertEquals("", resultado.getCadenaProcesada());
    }

    // =========================================================================
    // 2. EVALUACIÓN FORMAL DE LA CADENA VACÍA (λ)
    // =========================================================================

    @Test
    @DisplayName("2.1 testCadenaVaciaAceptadaSiInicialEsFinal: λ sobre q0 ∈ F se acepta")
    void testCadenaVaciaAceptadaSiInicialEsFinal() {
        Estado q0 = automata.crearEstado();
        q0.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "");
        assertTrue(resultado.esAceptada(), "La cadena vacía debe ser aceptada si q0 es de aceptación");
        assertEquals("Cadena Aceptada", resultado.getVeredicto());
        assertEquals(1, resultado.getCaminos().size());
        assertTrue(resultado.getCaminos().get(0).esExitoso());
    }

    @Test
    @DisplayName("2.2 testCadenaVaciaRechazadaSiInicialNoEsFinal: λ sobre q0 ∉ F se rechaza")
    void testCadenaVaciaRechazadaSiInicialNoEsFinal() {
        Estado q0 = automata.crearEstado();
        q0.setAceptacion(false);
        automata.marcarEstadoInicial(q0);

        // Se define un estado q1 final para cumplir la precondición de existencia de estado final
        Estado q1 = automata.crearEstado();
        q1.setAceptacion(true);

        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "");
        assertFalse(resultado.esAceptada(), "La cadena vacía debe ser rechazada si q0 no es de aceptación");
        assertEquals("Cadena Rechazada", resultado.getVeredicto());
    }

    // =========================================================================
    // 3. SIMULACIÓN SOBRE AFD DETERMINISTA
    // =========================================================================

    @Test
    @DisplayName("3.1 testSimulacionAFDExito: Cadena que finaliza en aceptación es aceptada con su camino")
    void testSimulacionAFDExito() {
        // Lenguaje: cadenas que terminan en 'b' sobre Σ={a, b}
        Estado q0 = automata.crearEstado(); // inicial
        Estado q1 = automata.crearEstado(); // final
        q1.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        automata.agregarTransicion(q0, q0, 'a');
        automata.agregarTransicion(q0, q1, 'b');
        automata.agregarTransicion(q1, q0, 'a');
        automata.agregarTransicion(q1, q1, 'b');

        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "aab");
        assertTrue(resultado.esAceptada());
        assertEquals("Cadena Aceptada", resultado.getVeredicto());

        // Verificación de la secuencia exacta del camino
        assertEquals(1, resultado.getCaminos().size());
        CaminoSimulacion camino = resultado.getCaminos().get(0);
        assertTrue(camino.esExitoso());
        assertEquals(3, camino.getPasos().size());
        assertEquals(q0, camino.getPasos().get(0).getOrigen());
        assertEquals(q0, camino.getPasos().get(0).getDestino()); // q0 --a-> q0
        assertEquals(q1, camino.getPasos().get(2).getDestino()); // q0 --b-> q1
    }

    @Test
    @DisplayName("3.2 testSimulacionAFDRechazoCaminoMuerto: Transición inexistente conduce a camino muerto")
    void testSimulacionAFDRechazoCaminoMuerto() {
        Estado q0 = automata.crearEstado();
        automata.marcarEstadoInicial(q0);
        // q0 solo tiene salida para 'a'
        Estado q1 = automata.crearEstado();
        q1.setAceptacion(true);
        automata.agregarTransicion(q0, q1, 'a');

        // Evaluar "b": no hay transición para 'b' desde q0
        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "b");
        assertFalse(resultado.esAceptada());
        assertEquals("Cadena Rechazada", resultado.getVeredicto());

        CaminoSimulacion camino = resultado.getCaminos().get(0);
        assertTrue(camino.esCaminoMuerto(), "El camino debió bloquearse inmediatamente");
    }

    @Test
    @DisplayName("3.3 testSimulacionAFDRechazoConsumeCadenaSinAceptacion: Consumir sin llegar a F rechaza")
    void testSimulacionAFDRechazoConsumeCadenaSinAceptacion() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        q0.setAceptacion(false);
        q1.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        automata.agregarTransicion(q0, q1, 'a');
        automata.agregarTransicion(q1, q0, 'b'); // regresa a no final

        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "ab");
        assertFalse(resultado.esAceptada(), "Termina en q0 (no aceptación)");
        assertEquals("Cadena Rechazada", resultado.getVeredicto());
    }

    // =========================================================================
    // 4. SIMULACIÓN SOBRE AFN (BIFURCACIONES Y MÚLTIPLES CAMINOS)
    // =========================================================================

    @Test
    @DisplayName("4.1 testSimulacionAFNMultiplesCaminos: Se acepta si al menos una rama llega a aceptación")
    void testSimulacionAFNMultiplesCaminos() {
        // AFN: q0 tiene dos salidas para 'a': q0 --a-> q0 y q0 --a-> q1
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        q1.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        automata.agregarTransicion(q0, q0, 'a');
        automata.agregarTransicion(q0, q1, 'a');

        // Para la cadena "a", hay 2 caminos:
        // Camino 1: q0 --a-> q0 (no final)
        // Camino 2: q0 --a-> q1 (final -> éxito!)
        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "a");
        assertTrue(resultado.esAceptada(), "Debe aceptarse porque el camino a q1 llega a aceptación");

        List<CaminoSimulacion> caminos = resultado.getCaminos();
        assertTrue(caminos.size() >= 2, "Deben haberse explorado las múltiples ramas");

        boolean hayExitoso = caminos.stream().anyMatch(CaminoSimulacion::esExitoso);
        assertTrue(hayExitoso, "Al menos uno de los caminos debe ser exitoso");
        assertTrue(caminos.get(0).esExitoso(), "El camino exitoso debe ubicarse de primero en la lista");
    }

    @Test
    @DisplayName("4.2 testSimulacionAFNTodasLasRamasMueren: Rechaza si todas las ramas se bloquean")
    void testSimulacionAFNTodasLasRamasMueren() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        Estado q2 = automata.crearEstado();
        q2.setAceptacion(true); // Se define estado final en el autómata
        automata.marcarEstadoInicial(q0);

        automata.agregarTransicion(q0, q1, 'a');
        automata.agregarTransicion(q0, q2, 'a');
        // Ninguno de los estados q1 o q2 tiene salida para 'b'

        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "ab");
        assertFalse(resultado.esAceptada());
        assertEquals("Cadena Rechazada", resultado.getVeredicto());
        assertTrue(resultado.getCaminos().stream().allMatch(CaminoSimulacion::esCaminoMuerto));
    }

    // =========================================================================
    // 5. TRAZABILIDAD Y VISUALIZACIÓN DE CAMINOS DE TRANSICIÓN
    // =========================================================================

    @Test
    @DisplayName("5.1 testTrazabilidadDeCaminos: Debe permitir inspeccionar cada transición recorrida")
    void testTrazabilidadDeCaminos() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        q1.setAceptacion(true);
        automata.marcarEstadoInicial(q0);

        automata.agregarTransicion(q0, q1, 'a');
        automata.agregarTransicion(q1, q0, 'b');

        ResultadoSimulacion resultado = simulador.evaluarCadena(automata, "aba");
        assertTrue(resultado.esAceptada());

        CaminoSimulacion camino = resultado.getCaminos().get(0);
        List<PasoTransicion> pasos = camino.getPasos();
        assertEquals(3, pasos.size());

        // Paso 0: q0 --a-> q1
        assertEquals("q0", pasos.get(0).getOrigen().getNombre());
        assertEquals('a', pasos.get(0).getSimbolo());
        assertEquals("q1", pasos.get(0).getDestino().getNombre());

        // Paso 1: q1 --b-> q0
        assertEquals("q1", pasos.get(1).getOrigen().getNombre());
        assertEquals('b', pasos.get(1).getSimbolo());
        assertEquals("q0", pasos.get(1).getDestino().getNombre());

        // Paso 2: q0 --a-> q1
        assertEquals("q0", pasos.get(2).getOrigen().getNombre());
        assertEquals('a', pasos.get(2).getSimbolo());
        assertEquals("q1", pasos.get(2).getDestino().getNombre());

        // Representación textual del camino completa y descriptiva
        assertNotNull(camino.getDescripcion());
        assertTrue(camino.getDescripcion().contains("q0"));
        assertTrue(camino.getDescripcion().contains("q1"));
        assertTrue(camino.getDescripcion().contains("CADENA ACEPTADA"));
    }
}
