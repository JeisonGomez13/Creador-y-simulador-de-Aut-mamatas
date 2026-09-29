package com.automatas.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa el resultado global formal e inmutable de la evaluación de una cadena sobre un autómata.
 * <p>
 * Reglas de diseño (Constitución - Principios 8 y 9):
 * <ul>
 *   <li>Es un Objeto de Valor (Value Object) 100% inmutable.</li>
 *   <li>Emite el veredicto binario estricto («Aceptada» o «Rechazada») según RF-06.</li>
 *   <li>Contiene la colección completa de caminos de transición explorados para permitir la inspección
 *       y visualización detallada del proceso de validación.</li>
 * </ul>
 */
public final class ResultadoSimulacion {

    // =========================================================================
    // 1. ESTADO INTERNO (INMUTABLE)
    // =========================================================================

    private final String cadenaOriginal;
    private final String cadenaProcesada;
    private final boolean aceptada;
    private final List<CaminoSimulacion> caminos;

    // =========================================================================
    // 2. CONSTRUCTOR PRINCIPAL
    // =========================================================================

    /**
     * Construye un resultado formal de simulación.
     *
     * @param cadenaOriginal Cadena ingresada por el usuario antes de procesar.
     * @param cadenaProcesada Cadena evaluada tras aplicar recorte ({@code trim()}).
     * @param aceptada {@code true} si la palabra es aceptada por el lenguaje; {@code false} si es rechazada.
     * @param caminos Lista de todos los caminos explorados durante la ejecución.
     */
    public ResultadoSimulacion(
            String cadenaOriginal,
            String cadenaProcesada,
            boolean aceptada,
            List<CaminoSimulacion> caminos) {
        this.cadenaOriginal = (cadenaOriginal != null) ? cadenaOriginal : "";
        this.cadenaProcesada = (cadenaProcesada != null) ? cadenaProcesada : "";
        this.aceptada = aceptada;
        this.caminos = (caminos != null) ? Collections.unmodifiableList(new ArrayList<>(caminos)) : Collections.emptyList();
    }

    // =========================================================================
    // 3. CONSULTAS DE DOMINIO Y VEREDICTO
    // =========================================================================

    public String getCadenaOriginal() {
        return cadenaOriginal;
    }

    public String getCadenaProcesada() {
        return cadenaProcesada;
    }

    public boolean esAceptada() {
        return aceptada;
    }

    /**
     * Retorna el veredicto binario en español como especifica RF-06: «Aceptada» o «Rechazada».
     *
     * @return Texto con el veredicto estricto.
     */
    public String getVeredicto() {
        return aceptada ? "Cadena Aceptada" : "Cadena Rechazada";
    }

    /**
     * Obtiene la lista completa de todos los caminos y ramas de ejecución explorados.
     *
     * @return Lista inmutable de {@link CaminoSimulacion}.
     */
    public List<CaminoSimulacion> getCaminos() {
        return caminos;
    }

    /**
     * Retorna el primer camino exitoso encontrado que condujo a la aceptación de la palabra.
     *
     * @return {@link Optional} con el camino aceptado, o vacío si la cadena fue rechazada.
     */
    public Optional<CaminoSimulacion> getPrimerCaminoExitoso() {
        return caminos.stream().filter(CaminoSimulacion::esExitoso).findFirst();
    }

    /**
     * Genera un reporte detallado con todos los caminos y ramas exploradas durante la validación.
     *
     * @return Resumen multilínea del proceso de simulación.
     */
    public String getResumenDetallado() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cadena: \"").append(cadenaProcesada).append("\" -> Veredicto: ").append(getVeredicto()).append("\n");
        sb.append("Caminos explorados (").append(caminos.size()).append("):\n");
        for (int i = 0; i < caminos.size(); i++) {
            sb.append("  [Ruta ").append(i + 1).append("] ").append(caminos.get(i).getDescripcion()).append("\n");
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
        ResultadoSimulacion that = (ResultadoSimulacion) o;
        return aceptada == that.aceptada &&
               Objects.equals(cadenaOriginal, that.cadenaOriginal) &&
               Objects.equals(cadenaProcesada, that.cadenaProcesada) &&
               Objects.equals(caminos, that.caminos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cadenaOriginal, cadenaProcesada, aceptada, caminos);
    }

    @Override
    public String toString() {
        return getVeredicto() + " (" + cadenaProcesada + ")";
    }
}
