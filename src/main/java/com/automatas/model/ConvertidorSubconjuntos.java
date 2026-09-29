package com.automatas.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Motor formal del algoritmo de determinización por construcción de subconjuntos (RF-07).
 * <p>
 * Reglas de diseño y algoritmos (RF-07, Casos Límite 11-15, y Constitución - Principios 8 y 9):
 * <ul>
 *   <li><b>Validación de precedencia estricta:</b> Valida sucesivamente la presencia de estado inicial,
 *       la existencia de al menos un estado de aceptación, y que el autómata no sea ya determinista (AFD).</li>
 *   <li><b>Nomenclatura formal k:</b> Asigna identificadores correlativos {@code "k0", "k1", "k2", ...},
 *       siendo {@code "k0"} obligatoriamente el macro-estado que contiene al estado inicial.</li>
 *   <li><b>Regla de aceptación por intersección:</b> Un macro-estado k_i es de aceptación si y solo si su
 *       subconjunto contiene al menos un estado de aceptación del AFN original (k_i ∩ F_AFN ≠ ∅).</li>
 *   <li><b>Poda estricta de inalcanzables:</b> Solo se materializan y registran aquellos macro-estados
 *       alcanzables a partir de k0 mediante exploración en anchura (BFS).</li>
 *   <li><b>Manejo de estado trampa vacío (ø):</b> Si la unión de transiciones para un símbolo es vacía, no se
 *       crea arista en el AFD generado y se registra como «ø» en la tabla de transiciones.</li>
 * </ul>
 */
public class ConvertidorSubconjuntos {

    // =========================================================================
    // 1. CONSTANTES
    // =========================================================================

    public static final String PREFIJO_ESTADO_AFD = "k";

    // =========================================================================
    // 2. MÉTODO PRINCIPAL DE CONVERSIÓN
    // =========================================================================

    /**
     * Convierte un Autómata Finito No Determinista (AFN) en un Autómata Finito Determinista (AFD) equivalente
     * mediante el método formal de construcción de subconjuntos.
     *
     * @param afn Autómata no determinista de entrada a convertir.
     * @return {@link ResultadoConversion} con el AFD generado y la tabla de subconjuntos.
     * @throws IllegalArgumentException Si el autómata proporcionado es nulo.
     * @throws IllegalStateException Si el autómata viola alguna de las precondiciones formales
     *                               (sin inicial, sin finales o si ya es determinista).
     */
    public ResultadoConversion convertir(Automata afn) {
        // Paso 1: Validar no nulidad
        if (afn == null) {
            throw new IllegalArgumentException("El autómata a convertir no puede ser nulo.");
        }

        // Paso 2: Validar precedencia 1 - Existencia obligatoria de estado inicial
        if (afn.getEstadoInicial().isEmpty()) {
            throw new IllegalStateException("Debe definir un estado inicial antes de realizar la conversión a AFD.");
        }

        // Paso 3: Validar precedencia 2 - Existencia de al menos un estado de aceptación
        if (afn.getEstadosAceptacion().isEmpty()) {
            throw new IllegalStateException("Debe definir al menos un estado final (de aceptación) antes de realizar la conversión.");
        }

        // Paso 4: Validar cardinalidad de estados
        if (afn.cardinalidadEstados() == 0) {
            throw new IllegalStateException("El autómata no contiene estados para convertir.");
        }

        // Paso 5: Validar precedencia 3 - No ser ya determinista
        if (afn.obtenerTipo() == TipoAutomata.AFD) {
            throw new IllegalStateException("El autómata ya es determinista (AFD), no requiere conversión.");
        }

        // Paso 6: Validar alfabeto formal activo
        if (afn.getAlfabeto() == null || afn.getAlfabeto().getSimbolos().isEmpty()) {
            throw new IllegalStateException("El autómata debe poseer un alfabeto formal con símbolos definidos.");
        }

        // Paso 7: Ejecutar algoritmo de construcción de subconjuntos
        return ejecutarConstruccionSubconjuntos(afn);
    }

    // =========================================================================
    // 3. MOTOR DE DETERMINIZACIÓN Y EXPLORACIÓN EN ANCHURA (BFS)
    // =========================================================================

    /**
     * Ejecuta la construcción iterativa de macro-estados alcanzables a partir de k0.
     *
     * @param afn Autómata no determinista verificado.
     * @return Resultado formal con el AFD generado y la tabla de subconjuntos.
     */
    private ResultadoConversion ejecutarConstruccionSubconjuntos(Automata afn) {
        Alfabeto alfabeto = afn.getAlfabeto();
        Automata afd = new Automata(alfabeto);

        // Estructuras de mapeo e indexación matemática de subconjuntos
        Map<Set<Estado>, Estado> subconjuntoAEstadoAFD = new LinkedHashMap<>();
        Queue<Set<Estado>> colaExploracion = new ArrayDeque<>();
        List<FilaSubconjuntos> filasTabla = new ArrayList<>();

        int contadorEstadosK = 0;

        // Paso 7.1: Construir el macro-estado inicial k0 = {q0} (sin ε-transiciones)
        Estado q0Afn = afn.getEstadoInicial().get();
        Set<Estado> subconjuntoInicial = normalizarSubconjunto(Set.of(q0Afn));

        String nombreK0 = PREFIJO_ESTADO_AFD + (contadorEstadosK++);
        boolean esAceptacionK0 = esSubconjuntoAceptacion(subconjuntoInicial, afn.getEstadosAceptacion());
        Estado estadoK0 = new Estado(nombreK0, esAceptacionK0);

        afd.agregarEstado(estadoK0);
        afd.marcarEstadoInicial(estadoK0);

        subconjuntoAEstadoAFD.put(subconjuntoInicial, estadoK0);
        colaExploracion.add(subconjuntoInicial);

        // Paso 7.2: Exploración iterativa por BFS garantizando poda de inalcanzables
        while (!colaExploracion.isEmpty()) {
            Set<Estado> subconjuntoActual = colaExploracion.poll();
            Estado estadoAfdActual = subconjuntoAEstadoAFD.get(subconjuntoActual);

            Map<Character, Set<Estado>> transicionesAFN = new LinkedHashMap<>();
            Map<Character, Estado> transicionesAFD = new LinkedHashMap<>();

            for (char simbolo : alfabeto.getSimbolos()) {
                // Calcular unión de transiciones salientes: T = ⋃_{q ∈ S} δ(q, simbolo)
                Set<Estado> destinosUnidos = new TreeSet<>();
                for (Estado q : subconjuntoActual) {
                    destinosUnidos.addAll(afn.obtenerDestinos(q, simbolo));
                }

                Set<Estado> subconjuntoDestino = normalizarSubconjunto(destinosUnidos);
                transicionesAFN.put(simbolo, subconjuntoDestino);

                if (subconjuntoDestino.isEmpty()) {
                    // Estado trampa vacío (ø): no se agrega arista al AFD
                    transicionesAFD.put(simbolo, null);
                } else {
                    // Verificar si el macro-estado ya fue previamente descubierto
                    Estado estadoDestinoAFD = subconjuntoAEstadoAFD.get(subconjuntoDestino);

                    if (estadoDestinoAFD == null) {
                        // Descubrimiento de nuevo macro-estado alcanzable
                        String nombreNuevoK = PREFIJO_ESTADO_AFD + (contadorEstadosK++);
                        boolean esAceptacion = esSubconjuntoAceptacion(subconjuntoDestino, afn.getEstadosAceptacion());
                        estadoDestinoAFD = new Estado(nombreNuevoK, esAceptacion);

                        afd.agregarEstado(estadoDestinoAFD);
                        subconjuntoAEstadoAFD.put(subconjuntoDestino, estadoDestinoAFD);
                        colaExploracion.add(subconjuntoDestino);
                    }

                    // Registrar transición determinista en el grafo del AFD
                    afd.agregarTransicion(estadoAfdActual, estadoDestinoAFD, simbolo);
                    transicionesAFD.put(simbolo, estadoDestinoAFD);
                }
            }

            // Construir y registrar la fila de la tabla de subconjuntos
            FilaSubconjuntos fila = new FilaSubconjuntos(
                    estadoAfdActual,
                    subconjuntoActual,
                    transicionesAFN,
                    transicionesAFD
            );
            filasTabla.add(fila);
        }

        // Paso 7.3: Ensamblar la tabla y retornar el resultado inmutable
        TablaSubconjuntos tabla = new TablaSubconjuntos(filasTabla, alfabeto);
        return new ResultadoConversion(afn, afd, tabla);
    }

    // =========================================================================
    // 4. MÉTODOS AUXILIARES DE TEORÍA DE CONJUNTOS
    // =========================================================================

    /**
     * Normaliza un subconjunto de estados asegurando ordenación canónica e inmutabilidad estricta.
     *
     * @param conjunto Conjunto arbitrario de estados.
     * @return Conjunto inmutable ordenado alfabéticamente por identificador de estado.
     */
    private Set<Estado> normalizarSubconjunto(Set<Estado> conjunto) {
        return Collections.unmodifiableSet(new TreeSet<>(conjunto));
    }

    /**
     * Determina si un subconjunto es de aceptación mediante intersección no vacía con F_AFN.
     *
     * @param subconjunto Subconjunto de estados evaluado.
     * @param estadosAceptacionAFN Conjunto de estados de aceptación del autómata original.
     * @return {@code true} si subconjunto ∩ F_AFN ≠ ∅; {@code false} en caso contrario.
     */
    private boolean esSubconjuntoAceptacion(Set<Estado> subconjunto, Set<Estado> estadosAceptacionAFN) {
        return !Collections.disjoint(subconjunto, estadosAceptacionAFN);
    }
}
