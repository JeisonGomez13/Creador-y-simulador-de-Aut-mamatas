package com.automatas.model;

/**
 * Representa la clasificación formal del tipo de autómata finito según sus características estructurales.
 * <p>
 * Reglas de clasificación según RF-05 y Caso Límite 19:
 * <ul>
 *   <li><b>SIN_ESTADOS:</b> Cuando el lienzo activo contiene 0 estados (|Q| = 0).</li>
 *   <li><b>AFD:</b> Cuando |Q| &gt; 0 y ningún estado posee múltiples transiciones salientes para el mismo símbolo.</li>
 *   <li><b>AFN:</b> Cuando |Q| &gt; 0 y existe al menos un estado con dos o más transiciones salientes para el mismo símbolo.</li>
 * </ul>
 */
public enum TipoAutomata {

    // =========================================================================
    // 1. CONSTANTES DEL ENUM
    // =========================================================================

    /** Estado neutro exhibido cuando el autómata no posee nodos en Q. */
    SIN_ESTADOS("Sin estados"),

    /** Autómata Finito Determinista: cada estado tiene a lo sumo una salida por símbolo. */
    AFD("AFD"),

    /** Autómata Finito No Determinista: existe al menos una bifurcación con el mismo símbolo. */
    AFN("AFN");

    // =========================================================================
    // 2. ATRIBUTO INMUTABLE
    // =========================================================================

    private final String etiqueta;

    // =========================================================================
    // 3. CONSTRUCTOR
    // =========================================================================

    TipoAutomata(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // =========================================================================
    // 4. CONSULTAS Y COMPORTAMIENTO DE DOMINIO
    // =========================================================================

    /**
     * Retorna la etiqueta textual en español representativa del tipo de autómata.
     *
     * @return Texto descriptivo (ej. "Sin estados", "AFD", "AFN").
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Indica si el tipo corresponde a un autómata determinista.
     *
     * @return {@code true} si es AFD; {@code false} en caso contrario.
     */
    public boolean esDeterminista() {
        return this == AFD;
    }

    /**
     * Indica si el autómata cuenta con al menos un estado registrado.
     *
     * @return {@code true} si es AFD o AFN; {@code false} si es SIN_ESTADOS.
     */
    public boolean tieneEstados() {
        return this != SIN_ESTADOS;
    }

    // =========================================================================
    // 5. MÉTODOS CANÓNICOS (OBJECT)
    // =========================================================================

    @Override
    public String toString() {
        return etiqueta;
    }
}
