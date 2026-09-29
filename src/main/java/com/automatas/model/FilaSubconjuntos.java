package com.automatas.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Representa una fila individual dentro de la tabla de determinización por subconjuntos (RF-07).
 * <p>
 * Asocia un macro-estado del autómata finito determinista (k_i) con su correspondiente
 * subconjunto matemático de estados del AFN original, así como las transiciones salientes
 * tanto en el modelo del AFN como en el AFD resultante.
 */
public class FilaSubconjuntos {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Estado estadoAFD;
    private final Set<Estado> subconjuntoAFN;
    private final Map<Character, Set<Estado>> transicionesAFN;
    private final Map<Character, Estado> transicionesAFD;

    // =========================================================================
    // 2. CONSTRUCTOR
    // =========================================================================

    /**
     * Construye una fila de la tabla de subconjuntos con sus mapeos formales.
     *
     * @param estadoAFD Macro-estado k_i generado en el AFD.
     * @param subconjuntoAFN Subconjunto matemático no vacío de estados del AFN correspondiente a k_i.
     * @param transicionesAFN Mapeo de transiciones salientes agrupadas por símbolo hacia subconjuntos del AFN.
     * @param transicionesAFD Mapeo de transiciones salientes hacia macro-estados k_j del AFD (o null si es ø).
     */
    public FilaSubconjuntos(
            Estado estadoAFD,
            Set<Estado> subconjuntoAFN,
            Map<Character, Set<Estado>> transicionesAFN,
            Map<Character, Estado> transicionesAFD
    ) {
        if (estadoAFD == null) {
            throw new IllegalArgumentException("El estado del AFD no puede ser nulo.");
        }
        if (subconjuntoAFN == null || subconjuntoAFN.isEmpty()) {
            throw new IllegalArgumentException("El subconjunto de estados del AFN no puede ser nulo ni vacío.");
        }

        this.estadoAFD = estadoAFD;
        this.subconjuntoAFN = Collections.unmodifiableSet(new java.util.TreeSet<>(subconjuntoAFN));
        this.transicionesAFN = transicionesAFN != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(transicionesAFN))
                : Collections.emptyMap();
        this.transicionesAFD = transicionesAFD != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(transicionesAFD))
                : Collections.emptyMap();
    }

    // =========================================================================
    // 3. MÉTODOS DE CONSULTA Y FORMATEO
    // =========================================================================

    /**
     * Obtiene el macro-estado k_i del AFD.
     *
     * @return Estado correspondiente en el AFD.
     */
    public Estado getEstadoAFD() {
        return estadoAFD;
    }

    /**
     * Obtiene el conjunto inmutable de estados del AFN original que conforman este macro-estado.
     *
     * @return Conjunto ordenado de estados del AFN.
     */
    public Set<Estado> getSubconjuntoAFN() {
        return subconjuntoAFN;
    }

    /**
     * Retorna una representación formateada del subconjunto AFN (ej. "{q0, q1}" o "ø").
     *
     * @return Cadena formateada para su despliegue en tabla o diagnóstico.
     */
    public String getSubconjuntoFormateado() {
        if (subconjuntoAFN.isEmpty()) {
            return "ø";
        }
        return formatearSubconjunto(subconjuntoAFN);
    }

    /**
     * Obtiene el conjunto de estados destino en el AFN al consumir el símbolo especificado.
     *
     * @param simbolo Símbolo del alfabeto evaluado.
     * @return Conjunto inmutable de estados destino (vacío si no hay transición).
     */
    public Set<Estado> obtenerDestinoAFN(char simbolo) {
        Set<Estado> destinos = transicionesAFN.get(simbolo);
        return destinos != null ? destinos : Collections.emptySet();
    }

    /**
     * Retorna la representación formateada del conjunto de estados destino en el AFN (ej. "{q1, q2}" o "ø").
     *
     * @param simbolo Símbolo del alfabeto evaluado.
     * @return Cadena con el subconjunto formateado o "ø" si es vacío.
     */
    public String obtenerDestinoAFNFormateado(char simbolo) {
        Set<Estado> destinos = transicionesAFN.get(simbolo);
        if (destinos == null || destinos.isEmpty()) {
            return "ø";
        }
        return formatearSubconjunto(destinos);
    }

    /**
     * Obtiene el estado destino en el AFD al consumir el símbolo especificado.
     *
     * @param simbolo Símbolo del alfabeto evaluado.
     * @return {@link Optional} con el estado destino k_j, o vacío si no hay transición definida (estado trampa ø).
     */
    public Optional<Estado> obtenerDestinoAFD(char simbolo) {
        return Optional.ofNullable(transicionesAFD.get(simbolo));
    }

    /**
     * Retorna el identificador del estado destino en el AFD o el símbolo "ø" si la transición conduce al vacío.
     *
     * @param simbolo Símbolo del alfabeto evaluado.
     * @return Nombre del estado k_j o "ø".
     */
    public String obtenerDestinoFormateado(char simbolo) {
        Estado destino = transicionesAFD.get(simbolo);
        return destino != null ? destino.getNombre() : "ø";
    }

    /**
     * Obtiene el mapa completo de transiciones salientes hacia subconjuntos del AFN.
     *
     * @return Mapa inmutable de símbolo a subconjunto del AFN.
     */
    public Map<Character, Set<Estado>> getTransicionesAFN() {
        return transicionesAFN;
    }

    /**
     * Obtiene el mapa completo de transiciones salientes en el AFD.
     *
     * @return Mapa inmutable de símbolo a estado del AFD (puede contener valores nulos para ø).
     */
    public Map<Character, Estado> getTransicionesAFD() {
        return transicionesAFD;
    }

    // =========================================================================
    // 4. MÉTODOS AUXILIARES PRIVADOS Y CANÓNICOS
    // =========================================================================

    private static String formatearSubconjunto(Set<Estado> conjunto) {
        return "{" + conjunto.stream()
                .map(Estado::getNombre)
                .sorted()
                .collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilaSubconjuntos that = (FilaSubconjuntos) o;
        return Objects.equals(estadoAFD, that.estadoAFD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estadoAFD);
    }

    @Override
    public String toString() {
        return estadoAFD.getNombre() + " = " + getSubconjuntoFormateado();
    }
}
