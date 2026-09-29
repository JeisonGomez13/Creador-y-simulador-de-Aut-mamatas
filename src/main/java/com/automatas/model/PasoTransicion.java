package com.automatas.model;

import java.util.Objects;

/**
 * Representa un paso atómico individual de transición recorrido durante la simulación de una cadena.
 * <p>
 * Reglas de diseño (Constitución - Principios 8 y 9):
 * <ul>
 *   <li>Es un Objeto de Valor (Value Object) 100% inmutable.</li>
 *   <li>Registra el estado origen, el símbolo consumido y el estado destino alcanzado.</li>
 * </ul>
 */
public final class PasoTransicion {

    // =========================================================================
    // 1. ESTADO INTERNO (INMUTABLE)
    // =========================================================================

    private final Estado origen;
    private final char simbolo;
    private final Estado destino;

    // =========================================================================
    // 2. CONSTRUCTOR PRINCIPAL
    // =========================================================================

    /**
     * Construye un paso de transición validando sus componentes.
     *
     * @param origen Estado desde donde parte la transición.
     * @param simbolo Símbolo consumido en este paso.
     * @param destino Estado alcanzado.
     * @throws IllegalArgumentException Si algún estado es nulo.
     */
    public PasoTransicion(Estado origen, char simbolo, Estado destino) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Los estados origen y destino del paso no pueden ser nulos.");
        }
        this.origen = origen;
        this.simbolo = simbolo;
        this.destino = destino;
    }

    // =========================================================================
    // 3. CONSULTAS Y FORMATO
    // =========================================================================

    public Estado getOrigen() {
        return origen;
    }

    public char getSimbolo() {
        return simbolo;
    }

    public Estado getDestino() {
        return destino;
    }

    /**
     * Retorna una representación legible del paso (ej. {@code "q0 --a-> q1"}).
     *
     * @return Cadena descriptiva del paso.
     */
    public String getDescripcion() {
        return origen.getNombre() + " --" + simbolo + "-> " + destino.getNombre();
    }

    // =========================================================================
    // 4. MÉTODOS CANÓNICOS (OBJECT)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasoTransicion that = (PasoTransicion) o;
        return simbolo == that.simbolo &&
               Objects.equals(origen, that.origen) &&
               Objects.equals(destino, that.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origen, simbolo, destino);
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
