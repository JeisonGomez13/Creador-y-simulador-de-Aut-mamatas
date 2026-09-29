package com.automatas.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad y agregador formal Automata.
 * <p>
 * Estructura secuencial de las pruebas (Constitución - Principios 8 y 9):
 * <ol>
 *   <li><b>Asignación secuencial del menor índice libre disponible (Caso Límite 17).</b></li>
 *   <li><b>Eliminación de estados y borrado en cascada de transiciones (RF-02).</b></li>
 *   <li><b>Designación, reemplazo y desmarcado del estado inicial único (RF-03).</b></li>
 *   <li><b>Renombrado y control de colisiones de identificadores en Q (RF-02).</b></li>
 *   <li><b>Gestión e integridad de transiciones respecto a Σ y Q (RF-04).</b></li>
 *   <li><b>Bloqueo del alfabeto formal ante transiciones activas (Caso Límite 10).</b></li>
 * </ol>
 */
@DisplayName("Pruebas Unitarias para la Entidad Automata")
class AutomataTest {

    private Alfabeto alfabetoBase;
    private Automata automata;

    @BeforeEach
    void setUp() {
        alfabetoBase = Alfabeto.desdeCadena("a, b");
        automata = new Automata(alfabetoBase);
    }

    // =========================================================================
    // 1. ASIGNACIÓN SECUENCIAL DEL MENOR ÍNDICE LIBRE DISPONIBLE
    // =========================================================================

    @Test
    @DisplayName("1.1 testAsignacionSecuencialMenorIndiceLibre: Debe reutilizar siempre el menor índice entero libre")
    void testAsignacionSecuencialMenorIndiceLibre() {
        // Paso 1: Creación secuencial inicial (q0, q1, q2)
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        Estado q2 = automata.crearEstado();

        assertEquals("q0", q0.getNombre());
        assertEquals("q1", q1.getNombre());
        assertEquals("q2", q2.getNombre());
        assertEquals(3, automata.cardinalidadEstados());

        // Paso 2: Eliminación de un estado intermedio (q1)
        automata.eliminarEstado(q1);
        assertEquals(2, automata.cardinalidadEstados());
        assertFalse(automata.contieneEstado(q1));

        // Paso 3: El siguiente estado creado debe reutilizar el menor índice libre (q1)
        Estado qReutilizado = automata.crearEstado();
        assertEquals("q1", qReutilizado.getNombre(), "Debe reasignar el índice libre menor 'q1'");

        // Paso 4: El siguiente estado debe continuar con q3
        Estado q3 = automata.crearEstado();
        assertEquals("q3", q3.getNombre(), "Una vez ocupados q0, q1 y q2, debe asignar 'q3'");
    }

    // =========================================================================
    // 2. BORRADO EN CASCADA DE TRANSICIONES AL ELIMINAR ESTADOS
    // =========================================================================

    @Test
    @DisplayName("2.1 testBorradoEnCascada: Eliminar un estado debe remover todas sus transiciones incidentes y salientes")
    void testBorradoEnCascada() {
        Estado q0 = automata.crearEstado(); // q0
        Estado q1 = automata.crearEstado(); // q1
        Estado q2 = automata.crearEstado(); // q2

        // Paso 1: Configurar transiciones
        // q0 -> q1 ('a')  [incidente en q1]
        // q1 -> q2 ('b')  [saliente de q1]
        // q1 -> q1 ('a')  [autolazo en q1]
        // q2 -> q0 ('a')  [independiente de q1]
        Transicion t01 = automata.agregarTransicion(q0, q1, 'a');
        Transicion t12 = automata.agregarTransicion(q1, q2, 'b');
        Transicion t11 = automata.agregarTransicion(q1, q1, 'a');
        Transicion t20 = automata.agregarTransicion(q2, q0, 'a');

        assertEquals(4, automata.cardinalidadTransiciones());

        // Paso 2: Eliminar q1 del autómata
        automata.eliminarEstado(q1);

        // Paso 3: Verificar que solo sobrevive la transición entre q2 y q0
        assertEquals(1, automata.cardinalidadTransiciones(), "Deben quedar solo las transiciones ajenas a q1");
        assertTrue(automata.getTransiciones().contains(t20), "t20 no conecta con q1 y debe permanecer");
        assertFalse(automata.getTransiciones().contains(t01), "t01 incide en q1 y debió ser eliminada");
        assertFalse(automata.getTransiciones().contains(t12), "t12 sale de q1 y debió ser eliminada");
        assertFalse(automata.getTransiciones().contains(t11), "t11 es autolazo de q1 y debió ser eliminada");
    }

    // =========================================================================
    // 3. DESIGNACIÓN Y REEMPLAZO AUTOMÁTICO DE ESTADO INICIAL
    // =========================================================================

    @Test
    @DisplayName("3.1 Debe permitir marcar un único estado inicial y reemplazar el anterior automáticamente")
    void testUnicoEstadoInicialYReemplazo() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();

        // Inicialmente el autómata no tiene estado inicial (HU-04, criterio formal)
        assertTrue(automata.getEstadoInicial().isEmpty());

        // Marcar q0 como inicial
        automata.marcarEstadoInicial(q0);
        assertEquals(Optional.of(q0), automata.getEstadoInicial());

        // Marcar q1 como inicial reemplaza a q0 automáticamente
        automata.marcarEstadoInicial(q1);
        assertEquals(Optional.of(q1), automata.getEstadoInicial());

        // Desmarcar deja el autómata sin inicial
        automata.desmarcarEstadoInicial();
        assertTrue(automata.getEstadoInicial().isEmpty());
    }

    @Test
    @DisplayName("3.2 Eliminar el estado inicial deja al autómata temporalmente sin inicial")
    void testEliminacionEstadoInicialDejaAutomataSinInicial() {
        Estado q0 = automata.crearEstado();
        automata.marcarEstadoInicial(q0);
        assertEquals(Optional.of(q0), automata.getEstadoInicial());

        automata.eliminarEstado(q0);
        assertTrue(automata.getEstadoInicial().isEmpty(), "Al eliminar el nodo inicial, getEstadoInicial debe ser empty");
    }

    @Test
    @DisplayName("3.3 Debe rechazar marcar como inicial a un estado que no pertenezca a Q")
    void testRechazaEstadoInicialAjeno() {
        Estado estadoExterno = new Estado("qx");
        assertThrows(IllegalArgumentException.class, () -> automata.marcarEstadoInicial(estadoExterno));
    }

    // =========================================================================
    // 4. RENOMBRADO Y UNICIDAD DE NOMBRES EN Q
    // =========================================================================

    @Test
    @DisplayName("4.1 Debe permitir renombrar un estado y rechazar colisiones con otros estados de Q")
    void testRenombradoYUnicidadDeNombres() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();

        // Renombrado exitoso
        automata.renombrarEstado(q0, "inicio");
        assertEquals("inicio", q0.getNombre());
        assertTrue(automata.buscarEstadoPorNombre("inicio").isPresent());

        // Intento de renombrar q1 a "inicio" debe fallar por colisión
        assertThrows(
                IllegalArgumentException.class,
                () -> automata.renombrarEstado(q1, "inicio"),
                "Debe rechazar nombres duplicados en el mismo autómata"
        );
        assertEquals("q1", q1.getNombre(), "q1 debe preservar su nombre original tras el fallo");
    }

    // =========================================================================
    // 5. GESTIÓN E INTEGRIDAD DE TRANSICIONES
    // =========================================================================

    @Test
    @DisplayName("5.1 Debe rechazar transiciones con símbolos ajenos a Σ o estados ajenos a Q")
    void testIntegridadDeTransiciones() {
        Estado q0 = automata.crearEstado();
        Estado qExterno = new Estado("qExt");

        // Rechaza símbolo ajeno al alfabeto Σ = {a, b}
        assertThrows(IllegalArgumentException.class, () -> automata.agregarTransicion(q0, q0, 'c'));

        // Rechaza estado no registrado en Q
        assertThrows(IllegalArgumentException.class, () -> automata.agregarTransicion(q0, qExterno, 'a'));
        assertThrows(IllegalArgumentException.class, () -> automata.agregarTransicion(qExterno, q0, 'a'));
    }

    @Test
    @DisplayName("5.2 Debe calcular correctamente los estados destino de transiciones múltiples y deterministas")
    void testObtenerDestinos() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        Estado q2 = automata.crearEstado();

        automata.agregarTransicion(q0, q1, 'a');
        automata.agregarTransicion(q0, q2, 'a'); // Salida no determinista para 'a'

        Set<Estado> destinosA = automata.obtenerDestinos(q0, 'a');
        assertEquals(2, destinosA.size());
        assertTrue(destinosA.contains(q1));
        assertTrue(destinosA.contains(q2));

        Set<Estado> destinosB = automata.obtenerDestinos(q0, 'b');
        assertTrue(destinosB.isEmpty(), "Si no hay transición, retorna conjunto vacío");
    }

    // =========================================================================
    // 6. BLOQUEO DEL ALFABETO ANTE TRANSICIONES ACTIVAS
    // =========================================================================

    @Test
    @DisplayName("6.1 No debe permitir cambiar el alfabeto mientras existan transiciones activas")
    void testBloqueoAlfabetoConTransiciones() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();

        // Sin transiciones: permite cambiar el alfabeto libremente
        Alfabeto nuevoAlfabeto = Alfabeto.desdeCadena("x, y, z");
        assertDoesNotThrow(() -> automata.asignarAlfabeto(nuevoAlfabeto));
        assertEquals(nuevoAlfabeto, automata.getAlfabeto());

        // Con transiciones: debe bloquear cualquier intento de cambiar el alfabeto
        automata.agregarTransicion(q0, q1, 'x');
        Alfabeto otroAlfabeto = Alfabeto.desdeCadena("a, b");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> automata.asignarAlfabeto(otroAlfabeto),
                "Debe fallar si el autómata tiene transiciones activas"
        );
        assertTrue(ex.getMessage().contains("transiciones"));
    }

    // =========================================================================
    // 7. CLASIFICACIÓN DINÁMICA DE TIPO DE AUTÓMATA (RF-05)
    // =========================================================================

    @Test
    @DisplayName("7.1 testClasificacionSinEstados: Un lienzo con 0 estados debe clasificarse como SIN_ESTADOS")
    void testClasificacionSinEstados() {
        assertEquals(0, automata.cardinalidadEstados());
        assertEquals(TipoAutomata.SIN_ESTADOS, automata.obtenerTipo());

        Estado q0 = automata.crearEstado();
        assertNotEquals(TipoAutomata.SIN_ESTADOS, automata.obtenerTipo());

        automata.eliminarEstado(q0);
        assertEquals(TipoAutomata.SIN_ESTADOS, automata.obtenerTipo());
    }

    @Test
    @DisplayName("7.2 testClasificacionAFD: Grafo sin salidas múltiples para el mismo símbolo debe clasificarse como AFD")
    void testClasificacionAFD() {
        Estado q0 = automata.crearEstado();
        // Con al menos 1 estado y 0 transiciones se clasifica como AFD
        assertEquals(TipoAutomata.AFD, automata.obtenerTipo());

        Estado q1 = automata.crearEstado();
        automata.agregarTransicion(q0, q1, 'a');
        automata.agregarTransicion(q0, q0, 'b'); // Autolazo con diferente símbolo
        automata.agregarTransicion(q1, q1, 'a');

        assertEquals(TipoAutomata.AFD, automata.obtenerTipo(), "Sin bifurcaciones duplicadas para el mismo símbolo es AFD");
    }

    @Test
    @DisplayName("7.3 testClasificacionAFN: Estado con dos o más salidas para el mismo símbolo debe clasificarse como AFN")
    void testClasificacionAFN() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();
        Estado q2 = automata.crearEstado();

        automata.agregarTransicion(q0, q1, 'a');
        assertEquals(TipoAutomata.AFD, automata.obtenerTipo());

        // Se agrega una segunda salida para 'a' desde q0
        Transicion tNoDet = automata.agregarTransicion(q0, q2, 'a');
        assertEquals(TipoAutomata.AFN, automata.obtenerTipo(), "q0 tiene dos salidas para 'a', debe ser AFN");

        // Si se elimina la transición no determinista, vuelve a ser AFD
        automata.eliminarTransicion(tNoDet);
        assertEquals(TipoAutomata.AFD, automata.obtenerTipo(), "Al eliminar la salida repetida, debe volver a ser AFD");
    }

    @Test
    @DisplayName("7.4 testClasificacionAFNConAutolazo: Autolazo y arista externa con el mismo símbolo es no determinista")
    void testClasificacionAFNConAutolazo() {
        Estado q0 = automata.crearEstado();
        Estado q1 = automata.crearEstado();

        automata.agregarTransicion(q0, q0, 'a'); // Autolazo con 'a'
        automata.agregarTransicion(q0, q1, 'a'); // Salida externa también con 'a'

        assertEquals(TipoAutomata.AFN, automata.obtenerTipo());
    }
}
