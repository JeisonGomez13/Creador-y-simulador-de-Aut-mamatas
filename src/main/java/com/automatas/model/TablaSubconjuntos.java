package com.automatas.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa la tabla matemática completa del proceso de determinización por construcción de subconjuntos (RF-07, RF-08).
 * <p>
 * Encapsula la lista ordenada de macro-estados alcanzables descubiertos durante el algoritmo y el alfabeto formal
 * correspondiente.
 */
public class TablaSubconjuntos {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final List<FilaSubconjuntos> filas;
    private final Alfabeto alfabeto;

    // =========================================================================
    // 2. CONSTRUCTOR
    // =========================================================================

    /**
     * Construye la tabla de subconjuntos inmutable.
     *
     * @param filas Lista ordenada de filas correspondientes a cada macro-estado k_i.
     * @param alfabeto Alfabeto formal del autómata.
     * @throws IllegalArgumentException Si la lista de filas o el alfabeto son nulos.
     */
    public TablaSubconjuntos(List<FilaSubconjuntos> filas, Alfabeto alfabeto) {
        if (filas == null) {
            throw new IllegalArgumentException("La lista de filas no puede ser nula.");
        }
        if (alfabeto == null) {
            throw new IllegalArgumentException("El alfabeto no puede ser nulo.");
        }
        this.filas = List.copyOf(filas);
        this.alfabeto = alfabeto;
    }

    // =========================================================================
    // 3. MÉTODOS DE CONSULTA Y BÚSQUEDA
    // =========================================================================

    /**
     * Obtiene la lista inmutable de filas que componen la tabla de subconjuntos.
     *
     * @return Lista de filas en orden secuencial de descubrimiento (k0, k1, ...).
     */
    public List<FilaSubconjuntos> getFilas() {
        return filas;
    }

    /**
     * Obtiene el alfabeto formal de la conversión.
     *
     * @return Instancia de {@link Alfabeto}.
     */
    public Alfabeto getAlfabeto() {
        return alfabeto;
    }

    /**
     * Busca la fila correspondiente a un estado específico del AFD.
     *
     * @param estadoAFD Estado del AFD a buscar.
     * @return {@link Optional} con la fila encontrada o vacío si no existe.
     */
    public Optional<FilaSubconjuntos> buscarFilaPorEstado(Estado estadoAFD) {
        if (estadoAFD == null) {
            return Optional.empty();
        }
        for (FilaSubconjuntos fila : filas) {
            if (fila.getEstadoAFD().equals(estadoAFD)) {
                return Optional.of(fila);
            }
        }
        return Optional.empty();
    }

    /**
     * Busca la fila correspondiente al identificador textual de un macro-estado (ej. "k0").
     *
     * @param nombre Identificador del macro-estado.
     * @return {@link Optional} con la fila encontrada o vacío si no existe.
     */
    public Optional<FilaSubconjuntos> buscarFilaPorNombre(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        String buscado = nombre.trim();
        for (FilaSubconjuntos fila : filas) {
            if (fila.getEstadoAFD().getNombre().equals(buscado)) {
                return Optional.of(fila);
            }
        }
        return Optional.empty();
    }

    /**
     * Retorna la cantidad total de macro-estados alcanzables en la tabla.
     *
     * @return Número de filas.
     */
    public int cantidadFilas() {
        return filas.size();
    }
}
