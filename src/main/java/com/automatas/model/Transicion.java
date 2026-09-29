package com.automatas.model;

import java.util.Objects;

/**
 * Representa una transición formal dirigida en un autómata finito (δ(q_origen, a) = q_destino).
 * <p>
 * Reglas de diseño y restricciones (RF-04, Constitución - Principios 8 y 9):
 * <ul>
 *   <li>Es un Objeto de Valor (Value Object) 100% inmutable.</li>
 *   <li>Conecta un {@link Estado} de origen con un {@link Estado} de destino mediante un símbolo de Σ.</li>
 *   <li>Rechaza estados nulos y símbolos que no sean letras válidas ({@code a-z}, {@code A-Z}).</li>
 *   <li>La igualdad e identidad se definen por la coincidencia estricta de la terna (origen, destino, símbolo).</li>
 * </ul>
 */
public final class Transicion {

    // =========================================================================
    // 1. ESTADO INTERNO (INMUTABLE)
    // =========================================================================

    private final Estado origen;
    private final Estado destino;
    private final char simbolo;

    // =========================================================================
    // 2. CONSTRUCTOR PRINCIPAL
    // =========================================================================

    /**
     * Construye una transición dirigida formal validando sus invariantes.
     *
     * @param origen Estado origen de la transición.
     * @param destino Estado destino de la transición.
     * @param simbolo Símbolo formal de transición perteneciente a Σ.
     * @throws IllegalArgumentException Si algún estado es nulo o si el símbolo no es una letra válida.
     */
    public Transicion(Estado origen, Estado destino, char simbolo) {
        // Paso 1: Validar que los estados origen y destino no sean nulos
        if (origen == null) {
            throw new IllegalArgumentException("El estado origen de la transición no puede ser nulo.");
        }
        if (destino == null) {
            throw new IllegalArgumentException("El estado destino de la transición no puede ser nulo.");
        }

        // Paso 2: Validar que el símbolo sea alfabético válido
        if (!Alfabeto.esSimboloValido(simbolo)) {
            throw new IllegalArgumentException(
                    "El símbolo de transición '" + simbolo + "' no es una letra válida (a-z, A-Z)."
            );
        }

        // Paso 3: Asignación inmutable definitiva
        this.origen = origen;
        this.destino = destino;
        this.simbolo = simbolo;
    }

    // =========================================================================
    // 3. CONSULTAS Y PROPIEDADES DE ESTADO
    // =========================================================================

    /**
     * Obtiene el estado origen de la transición.
     *
     * @return Instancia del estado origen.
     */
    public Estado getOrigen() {
        return origen;
    }

    /**
     * Obtiene el estado destino de la transición.
     *
     * @return Instancia del estado destino.
     */
    public Estado getDestino() {
        return destino;
    }

    /**
     * Obtiene el símbolo formal asociado a la transición.
     *
     * @return Carácter del símbolo de Σ.
     */
    public char getSimbolo() {
        return simbolo;
    }

    /**
     * Determina si la transición es un autolazo reflexivo (origen y destino coinciden).
     *
     * @return {@code true} si origen es igual a destino; {@code false} en caso contrario.
     */
    public boolean esAutolazo() {
        return origen.equals(destino);
    }

    // =========================================================================
    // 4. MÉTODOS CANÓNICOS DE IDENTIDAD (OBJECT)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transicion that = (Transicion) o;
        return simbolo == that.simbolo &&
               Objects.equals(origen, that.origen) &&
               Objects.equals(destino, that.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origen, destino, simbolo);
    }

    @Override
    public String toString() {
        return "(" + origen.getNombre() + ", " + simbolo + ") -> " + destino.getNombre();
    }
}
