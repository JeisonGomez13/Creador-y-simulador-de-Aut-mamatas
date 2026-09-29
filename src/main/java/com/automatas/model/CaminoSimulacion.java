package com.automatas.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa una ruta o rama completa de ejecución explorada durante la evaluación de una cadena.
 * <p>
 * Reglas de diseño (Constitución - Principios 8 y 9):
 * <ul>
 *   <li>Es un Objeto de Valor (Value Object) 100% inmutable.</li>
 *   <li>Registra la secuencia de estados visitados y transiciones ejecutadas.</li>
 *   <li>Determina si la rama culminó con éxito en aceptación, si fue rechazada o si quedó bloqueada (camino muerto).</li>
 * </ul>
 */
public final class CaminoSimulacion {

    // =========================================================================
    // 1. ESTADO INTERNO (INMUTABLE)
    // =========================================================================

    private final Estado estadoInicial;
    private final List<PasoTransicion> pasos;
    private final Estado estadoFinal;
    private final boolean consumioTodaLaCadena;
    private final boolean caminoMuerto;
    private final boolean exitoso;

    // =========================================================================
    // 2. CONSTRUCTOR PRINCIPAL
    // =========================================================================

    /**
     * Construye un camino de simulación inmutable con su trazabilidad de pasos.
     *
     * @param estadoInicial Estado desde donde comenzó la rama.
     * @param pasos Lista secuencial de pasos de transición ejecutados.
     * @param estadoFinal Último estado alcanzado en la rama.
     * @param consumioTodaLaCadena {@code true} si consumió todos los símbolos de la palabra.
     * @param caminoMuerto {@code true} si la rama se bloqueó por ausencia de transición.
     */
    public CaminoSimulacion(
            Estado estadoInicial,
            List<PasoTransicion> pasos,
            Estado estadoFinal,
            boolean consumioTodaLaCadena,
            boolean caminoMuerto) {
        if (estadoInicial == null) {
            throw new IllegalArgumentException("El estado inicial del camino no puede ser nulo.");
        }
        this.estadoInicial = estadoInicial;
        this.pasos = (pasos != null) ? Collections.unmodifiableList(new ArrayList<>(pasos)) : Collections.emptyList();
        this.estadoFinal = estadoFinal;
        this.consumioTodaLaCadena = consumioTodaLaCadena;
        this.caminoMuerto = caminoMuerto;
        this.exitoso = consumioTodaLaCadena && (estadoFinal != null && estadoFinal.esAceptacion());
    }

    // =========================================================================
    // 3. CONSULTAS Y ESTADO
    // =========================================================================

    public Estado getEstadoInicial() {
        return estadoInicial;
    }

    public List<PasoTransicion> getPasos() {
        return pasos;
    }

    public Estado getEstadoFinal() {
        return estadoFinal;
    }

    public boolean consumioTodaLaCadena() {
        return consumioTodaLaCadena;
    }

    public boolean esCaminoMuerto() {
        return caminoMuerto;
    }

    public boolean esExitoso() {
        return exitoso;
    }

    /**
     * Obtiene la lista secuencial ordenada de todos los estados visitados a lo largo de este camino.
     *
     * @return Lista inmutable de estados visitados desde el inicial hasta el final.
     */
    public List<Estado> getSecuenciaEstados() {
        List<Estado> secuencia = new ArrayList<>();
        secuencia.add(estadoInicial);
        for (PasoTransicion paso : pasos) {
            secuencia.add(paso.getDestino());
        }
        return Collections.unmodifiableList(secuencia);
    }

    /**
     * Genera una descripción legible de la ruta (ej. {@code "q0 --a-> q1 --b-> q2 [Aceptado]"}).
     *
     * @return Cadena formateada del camino.
     */
    public String getDescripcion() {
        StringBuilder sb = new StringBuilder();
        sb.append(estadoInicial.getNombre());
        for (PasoTransicion p : pasos) {
            sb.append(" --").append(p.getSimbolo()).append("-> ").append(p.getDestino().getNombre());
        }
        if (exitoso) {
            sb.append(" [CADENA ACEPTADA]");
        } else if (caminoMuerto) {
            sb.append(" [CAMINO MUERTO]");
        } else {
            sb.append(" [CADENA NO ACEPTADA]");
        }
        return sb.toString();
    }

    // =========================================================================
    // 4. MÉTODOS CANÓNICOS (OBJECT)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaminoSimulacion that = (CaminoSimulacion) o;
        return consumioTodaLaCadena == that.consumioTodaLaCadena &&
               caminoMuerto == that.caminoMuerto &&
               exitoso == that.exitoso &&
               Objects.equals(estadoInicial, that.estadoInicial) &&
               Objects.equals(pasos, that.pasos) &&
               Objects.equals(estadoFinal, that.estadoFinal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estadoInicial, pasos, estadoFinal, consumioTodaLaCadena, caminoMuerto, exitoso);
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
