package com.automatas.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa el alfabeto formal (Σ) de un autómata finito como un Objeto de Valor (Value Object) inmutable.
 * <p>
 * Reglas de diseño y orden secuencial (Constitución - Principios 8 y 9):
 * <ol>
 *   <li><b>Estado inmutable:</b> Colección interna final e inmutable de símbolos.</li>
 *   <li><b>Construcción y validación:</b> Precondiciones estrictas que impiden cualquier estado inválido.</li>
 *   <li><b>Consultas de dominio:</b> Métodos de inspección (cardinalidad, pertenencia).</li>
 *   <li><b>Operaciones del lenguaje:</b> Validación de palabras y redefinición inmutable.</li>
 *   <li><b>Métodos canónicos:</b> Identidad estructural (equals, hashCode, toString).</li>
 * </ol>
 */
public final class Alfabeto {

    // =========================================================================
    // 1. ESTADO INTERNO (INMUTABLE)
    // =========================================================================

    private final Set<Character> simbolos;

    // =========================================================================
    // 2. CONSTRUCTOR PRINCIPAL
    // =========================================================================

    /**
     * Construye un alfabeto formal a partir de un conjunto de símbolos ya validados.
     *
     * @param simbolos Conjunto de caracteres válidos (letras a-z, A-Z).
     * @throws IllegalArgumentException Si el conjunto es nulo, vacío o contiene caracteres no permitidos.
     */
    public Alfabeto(Set<Character> simbolos) {
        // Paso 1: Validación de precondición básica
        if (simbolos == null || simbolos.isEmpty()) {
            throw new IllegalArgumentException("El alfabeto formal Σ no puede ser nulo ni vacío.");
        }

        // Paso 2: Validación individual y ordenamiento preservado
        Set<Character> copiaOrdenada = new LinkedHashSet<>();
        for (Character c : simbolos) {
            if (c == null || !esSimboloValido(c)) {
                throw new IllegalArgumentException("El carácter '" + c + "' no es una letra válida (a-z, A-Z).");
            }
            copiaOrdenada.add(c);
        }

        // Paso 3: Asignación inmutable definitiva
        this.simbolos = Collections.unmodifiableSet(copiaOrdenada);
    }

    // =========================================================================
    // 3. FÁBRICA ESTÁTICA Y REGLAS DE PARSEO
    // =========================================================================

    /**
     * Crea una instancia inmutable de {@link Alfabeto} a partir de una cadena de texto separada por comas.
     * <p>
     * Secuencia de parseo:
     * <ol>
     *   <li>Verificación de nulidad y contenido en blanco.</li>
     *   <li>Verificación de delimitadores sintácticos en los extremos.</li>
     *   <li>Tokenización por comas y limpieza de espacios adyacentes.</li>
     *   <li>Validación unaria de cada símbolo y detección de duplicados.</li>
     *   <li>Instanciación del objeto final.</li>
     * </ol>
     *
     * @param definicion Cadena con símbolos separados por comas (ej. {@code "a, b, C"}).
     * @return Nueva instancia inmutable de {@link Alfabeto}.
     * @throws IllegalArgumentException Si la especificación viola las reglas formales de Σ.
     */
    public static Alfabeto desdeCadena(String definicion) {
        // Paso 1: Validar nulidad y contenido no vacío
        if (definicion == null || definicion.trim().isEmpty()) {
            throw new IllegalArgumentException("La definición del alfabeto no puede ser nula ni vacía.");
        }

        // Paso 2: Validar que no existan comas en los extremos
        String textoRecortado = definicion.trim();
        if (textoRecortado.startsWith(",") || textoRecortado.endsWith(",")) {
            throw new IllegalArgumentException("La definición del alfabeto no puede iniciar ni terminar con comas.");
        }

        // Paso 3: Dividir en tokens preservando posiciones vacías
        String[] tokens = textoRecortado.split(",", -1);
        Set<Character> simbolosTemp = new LinkedHashSet<>();

        // Paso 4: Validar secuencialmente cada token extraído
        for (String token : tokens) {
            String limpio = token.trim();

            if (limpio.isEmpty()) {
                throw new IllegalArgumentException("El alfabeto no puede contener entradas vacías entre comas.");
            }
            if (limpio.length() != 1) {
                throw new IllegalArgumentException("Cada símbolo debe ser una única letra: '" + limpio + "'.");
            }

            char c = limpio.charAt(0);
            if (!esSimboloValido(c)) {
                throw new IllegalArgumentException("El carácter '" + c + "' no es una letra válida. El alfabeto solo admite letras a-z y A-Z.");
            }
            if (!simbolosTemp.add(c)) {
                throw new IllegalArgumentException("El símbolo '" + c + "' está duplicado en la definición del alfabeto.");
            }
        }

        // Paso 5: Instanciar el alfabeto con los símbolos validados
        return new Alfabeto(simbolosTemp);
    }

    /**
     * Determina si un carácter cumple con la regla formal de símbolo válido ({@code a-z}, {@code A-Z}).
     *
     * @param c Carácter a verificar.
     * @return {@code true} si es una letra del abecedario; {@code false} en caso contrario.
     */
    public static boolean esSimboloValido(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // =========================================================================
    // 4. CONSULTAS Y PROPIEDADES DE ESTADO
    // =========================================================================

    /**
     * Retorna la vista inmutable del conjunto de símbolos que conforman el alfabeto.
     *
     * @return Conjunto inmutable de caracteres.
     */
    public Set<Character> getSimbolos() {
        return simbolos;
    }

    /**
     * Retorna la cardinalidad (|Σ|) del alfabeto.
     *
     * @return Cantidad de símbolos definidos.
     */
    public int cardinalidad() {
        return simbolos.size();
    }

    /**
     * Determina si un símbolo específico pertenece al alfabeto formal.
     *
     * @param simbolo Carácter a comprobar.
     * @return {@code true} si el símbolo pertenece a Σ; {@code false} en caso contrario.
     */
    public boolean contiene(char simbolo) {
        return simbolos.contains(simbolo);
    }

    // =========================================================================
    // 5. OPERACIONES DE DOMINIO Y LENGUAJE
    // =========================================================================

    /**
     * Valida que todos los caracteres de una cadena pertenezcan a este alfabeto formal (w ∈ Σ*).
     *
     * @param cadena Cadena de entrada a evaluar.
     * @throws IllegalArgumentException Si la cadena es nula o contiene caracteres ajenos a Σ.
     */
    public void validarCadena(String cadena) {
        if (cadena == null) {
            throw new IllegalArgumentException("La cadena a evaluar no puede ser nula.");
        }
        for (int i = 0; i < cadena.length(); i++) {
            char c = cadena.charAt(i);
            if (!contiene(c)) {
                throw new IllegalArgumentException("La cadena contiene el símbolo '" + c + "' que no pertenece al alfabeto Σ.");
            }
        }
    }

    /**
     * Crea una nueva instancia inmutable de {@link Alfabeto} a partir de una nueva definición textual,
     * garantizando que el alfabeto no sea redefinido si existen transiciones activas en el autómata.
     *
     * @param nuevaDefinicion Cadena de símbolos separados por comas.
     * @param existenTransicionesActivas Indica si el autómata posee transiciones trazadas en el lienzo.
     * @return Nueva instancia inmutable de {@link Alfabeto}.
     * @throws IllegalStateException Si existen transiciones activas.
     * @throws IllegalArgumentException Si la nueva definición sintáctica es inválida.
     */
    public Alfabeto redefinir(String nuevaDefinicion, boolean existenTransicionesActivas) {
        if (existenTransicionesActivas) {
            throw new IllegalStateException("No se puede modificar el alfabeto mientras existan transiciones activas.");
        }
        return desdeCadena(nuevaDefinicion);
    }

    // =========================================================================
    // 6. MÉTODOS CANÓNICOS DE IDENTIDAD (OBJECT)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alfabeto alfabeto = (Alfabeto) o;
        return Objects.equals(simbolos, alfabeto.simbolos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simbolos);
    }

    @Override
    public String toString() {
        return "Σ = " + simbolos;
    }
}
