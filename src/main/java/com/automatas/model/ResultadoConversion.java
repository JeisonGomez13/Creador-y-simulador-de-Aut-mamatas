package com.automatas.model;

import java.util.Optional;
import java.util.Set;

/**
 * Objeto de transferencia de datos (DTO) inmutable que encapsula el resultado completo del algoritmo
 * de determinización por subconjuntos (RF-07).
 * <p>
 * Contiene el AFN original, el AFD determinista generado y la tabla formal de correspondencia
 * de subconjuntos alcanzables.
 */
public class ResultadoConversion {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Automata afnOriginal;
    private final Automata afdGenerado;
    private final TablaSubconjuntos tablaSubconjuntos;

    // =========================================================================
    // 2. CONSTRUCTOR
    // =========================================================================

    /**
     * Construye el resultado formal de la conversión.
     *
     * @param afnOriginal Autómata no determinista de origen.
     * @param afdGenerado Autómata determinista equivalente generado.
     * @param tablaSubconjuntos Tabla formal de correspondencia entre macro-estados y subconjuntos.
     * @throws IllegalArgumentException Si alguno de los parámetros es nulo.
     */
    public ResultadoConversion(
            Automata afnOriginal,
            Automata afdGenerado,
            TablaSubconjuntos tablaSubconjuntos
    ) {
        if (afnOriginal == null) {
            throw new IllegalArgumentException("El autómata AFN original no puede ser nulo.");
        }
        if (afdGenerado == null) {
            throw new IllegalArgumentException("El autómata AFD generado no puede ser nulo.");
        }
        if (tablaSubconjuntos == null) {
            throw new IllegalArgumentException("La tabla de subconjuntos no puede ser nula.");
        }

        this.afnOriginal = afnOriginal;
        this.afdGenerado = afdGenerado;
        this.tablaSubconjuntos = tablaSubconjuntos;
    }

    // =========================================================================
    // 3. MÉTODOS DE CONSULTA
    // =========================================================================

    /**
     * Obtiene el autómata no determinista de origen.
     *
     * @return Instancia de {@link Automata} original.
     */
    public Automata getAfnOriginal() {
        return afnOriginal;
    }

    /**
     * Obtiene el autómata determinista equivalente generado por el algoritmo.
     *
     * @return Instancia de {@link Automata} determinista con nomenclatura k0, k1, ...
     */
    public Automata getAfdGenerado() {
        return afdGenerado;
    }

    /**
     * Obtiene la tabla matemática de subconjuntos generada en el proceso.
     *
     * @return Instancia de {@link TablaSubconjuntos}.
     */
    public TablaSubconjuntos getTablaSubconjuntos() {
        return tablaSubconjuntos;
    }

    /**
     * Busca el subconjunto de estados del AFN asociado a un macro-estado del AFD por su identificador textual.
     *
     * @param nombreEstadoAFD Identificador del estado en el AFD (ej. "k0", "k1").
     * @return {@link Optional} con el conjunto inmutable de estados del AFN, o vacío si el nombre no existe.
     */
    public Optional<Set<Estado>> buscarSubconjuntoPorNombre(String nombreEstadoAFD) {
        if (nombreEstadoAFD == null) {
            return Optional.empty();
        }
        return tablaSubconjuntos.buscarFilaPorNombre(nombreEstadoAFD)
                .map(FilaSubconjuntos::getSubconjuntoAFN);
    }
}
