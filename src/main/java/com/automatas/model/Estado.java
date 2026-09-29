package com.automatas.model;

import java.util.Objects;

/**
 * Representa un estado formal de un autómata finito (q ∈ Q).
 * <p>
 * Reglas de diseño y restricciones (RF-02, RF-03, Constitución):
 * <ul>
 *   <li>Posee un identificador textual único dentro del autómata (ej. {@code "q0"}).</li>
 *   <li>Longitud máxima de 8 caracteres tras aplicar recorte ({@code trim()}).</li>
 *   <li>No puede ser nulo, vacío ni contener saltos de línea.</li>
 *   <li>Mantiene un flag booleano indicando si es estado de aceptación (final).</li>
 *   <li>La igualdad e identidad formal se basan exclusivamente en su identificador textual.</li>
 * </ul>
 */
public class Estado implements Comparable<Estado> {

    // =========================================================================
    // 1. CONSTANTES
    // =========================================================================

    public static final int MAX_LONGITUD_NOMBRE = 8;

    // =========================================================================
    // 2. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private String nombre;
    private boolean aceptacion;

    // =========================================================================
    // 3. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye un estado no de aceptación a partir de su identificador textual.
     *
     * @param nombre Identificador textual del estado.
     * @throws IllegalArgumentException Si el nombre viola las restricciones formales.
     */
    public Estado(String nombre) {
        this(nombre, false);
    }

    /**
     * Construye un estado especificando su identificador textual y su condición de aceptación.
     *
     * @param nombre Identificador textual del estado.
     * @param aceptacion {@code true} si es estado de aceptación; {@code false} en caso contrario.
     * @throws IllegalArgumentException Si el nombre viola las restricciones formales.
     */
    public Estado(String nombre, boolean aceptacion) {
        this.nombre = validarYNormalizarNombre(nombre);
        this.aceptacion = aceptacion;
    }

    // =========================================================================
    // 4. MÉTODOS DE VALIDACIÓN Y NORMALIZACIÓN
    // =========================================================================

    /**
     * Valida que un identificador cumpla con los requisitos formales y retorna su versión recortada.
     *
     * @param nombreEntrada Texto a validar.
     * @return Identificador recortado ({@code trim()}).
     * @throws IllegalArgumentException Si el texto es nulo, blanco, supera 8 caracteres o contiene saltos de línea.
     */
    private static String validarYNormalizarNombre(String nombreEntrada) {
        // Paso 1: Validar no nulidad y contenido no vacío
        if (nombreEntrada == null || nombreEntrada.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del estado no puede ser nulo ni estar en blanco.");
        }

        // Paso 2: Rechazar saltos de línea
        if (nombreEntrada.contains("\n") || nombreEntrada.contains("\r")) {
            throw new IllegalArgumentException("El identificador del estado no puede contener saltos de línea.");
        }

        // Paso 3: Aplicar recorte de espacios y validar longitud máxima
        String normalizado = nombreEntrada.trim();
        if (normalizado.length() > MAX_LONGITUD_NOMBRE) {
            throw new IllegalArgumentException(
                    "El identificador del estado no puede exceder " + MAX_LONGITUD_NOMBRE +
                    " caracteres (recibido: '" + normalizado + "')."
            );
        }

        return normalizado;
    }

    // =========================================================================
    // 5. CONSULTAS Y MODIFICADORES DE ESTADO
    // =========================================================================

    /**
     * Obtiene el identificador textual del estado.
     *
     * @return Nombre normalizado del estado.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Modifica el identificador textual del estado preservando el valor anterior si la validación falla.
     *
     * @param nuevoNombre Nuevo identificador textual a asignar.
     * @throws IllegalArgumentException Si el nuevo nombre viola las restricciones formales.
     */
    public void renombrar(String nuevoNombre) {
        this.nombre = validarYNormalizarNombre(nuevoNombre);
    }

    /**
     * Indica si el estado es de aceptación (final).
     *
     * @return {@code true} si pertenece a F; {@code false} en caso contrario.
     */
    public boolean esAceptacion() {
        return aceptacion;
    }

    /**
     * Establece si el estado es o no de aceptación.
     *
     * @param aceptacion {@code true} para marcar como final; {@code false} para desmarcar.
     */
    public void setAceptacion(boolean aceptacion) {
        this.aceptacion = aceptacion;
    }

    // =========================================================================
    // 6. MÉTODOS CANÓNICOS DE IDENTIDAD (OBJECT Y COMPARABLE)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estado estado = (Estado) o;
        return Objects.equals(nombre, estado.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public int compareTo(Estado otro) {
        return this.nombre.compareTo(otro.nombre);
    }

    @Override
    public String toString() {
        return aceptacion ? nombre + "*" : nombre;
    }
}
