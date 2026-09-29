package com.automatas.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Representa formalmente un autómata finito (AFD o AFN) como la 5-tupla matemática:
 * M = (Q, Σ, δ, q0, F).
 * <p>
 * Reglas arquitectónicas y de diseño (Constitución - Principios 8 y 9):
 * <ul>
 *   <li><b>Agregador Raíz (Aggregate Root):</b> Encapsula y protege la integridad de estados y transiciones.</li>
 *   <li><b>Gestión de Identificadores:</b> Asigna automáticamente el menor índice libre (q0, q1, ...).</li>
 *   <li><b>Integridad Referencial:</b> Eliminación de estados en cascada para evitar aristas huérfanas.</li>
 *   <li><b>Estado Inicial Único:</b> Soporte para a lo sumo un estado inicial con reemplazo automático.</li>
 *   <li><b>Bloqueo de Alfabeto:</b> Impide alterar Σ mientras existan transiciones trazadas.</li>
 * </ul>
 */
public class Automata {

    // =========================================================================
    // 1. ESTADO INTERNO DEL MODELO FORMAL
    // =========================================================================

    private Alfabeto alfabeto;
    private final Set<Estado> estados;
    private Estado estadoInicial;
    private final Set<Transicion> transiciones;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye un autómata formal inicializado con un alfabeto activo.
     *
     * @param alfabeto Alfabeto formal Σ no nulo.
     * @throws IllegalArgumentException Si el alfabeto proporcionado es nulo.
     */
    public Automata(Alfabeto alfabeto) {
        if (alfabeto == null) {
            throw new IllegalArgumentException("El alfabeto formal del autómata no puede ser nulo.");
        }
        this.alfabeto = alfabeto;
        this.estados = new LinkedHashSet<>();
        this.transiciones = new LinkedHashSet<>();
        this.estadoInicial = null;
    }

    /**
     * Construye un autómata en blanco sin alfabeto inicial configurado.
     */
    public Automata() {
        this.alfabeto = null;
        this.estados = new LinkedHashSet<>();
        this.transiciones = new LinkedHashSet<>();
        this.estadoInicial = null;
    }

    // =========================================================================
    // 3. GESTIÓN DEL ALFABETO FORMAL (Σ)
    // =========================================================================

    /**
     * Obtiene el alfabeto formal activo del autómata.
     *
     * @return Instancia de {@link Alfabeto} o {@code null} si aún no ha sido definido.
     */
    public Alfabeto getAlfabeto() {
        return alfabeto;
    }

    /**
     * Asigna o reconfigura el alfabeto formal del autómata.
     *
     * @param nuevoAlfabeto Nuevo alfabeto formal a asignar.
     * @throws IllegalStateException Si el autómata ya cuenta con transiciones activas.
     * @throws IllegalArgumentException Si el alfabeto proporcionado es nulo.
     */
    public void asignarAlfabeto(Alfabeto nuevoAlfabeto) {
        // Paso 1: Validar no nulidad
        if (nuevoAlfabeto == null) {
            throw new IllegalArgumentException("El nuevo alfabeto no puede ser nulo.");
        }

        // Paso 2: Validar precondición de bloqueo (Caso Límite 10)
        if (tieneTransiciones()) {
            throw new IllegalStateException(
                    "No se puede modificar el alfabeto mientras existan transiciones activas en el autómata."
            );
        }

        // Paso 3: Asignación
        this.alfabeto = nuevoAlfabeto;
    }

    // =========================================================================
    // 4. GESTIÓN DEL CONJUNTO DE ESTADOS (Q)
    // =========================================================================

    /**
     * Crea un nuevo estado asignándole automáticamente el menor índice entero libre disponible (q0, q1, ...).
     *
     * @return Nuevo {@link Estado} creado y registrado en el autómata.
     */
    public Estado crearEstado() {
        String siguienteNombre = generarSiguienteNombreEstado();
        Estado nuevoEstado = new Estado(siguienteNombre);
        agregarEstado(nuevoEstado);
        return nuevoEstado;
    }

    /**
     * Calcula el menor identificador libre disponible con el prefijo "q" (q0, q1, q2, ...).
     *
     * @return Nombre disponible garantizado sin colisiones en Q.
     */
    public String generarSiguienteNombreEstado() {
        int indice = 0;
        while (existeEstadoConNombre("q" + indice)) {
            indice++;
        }
        return "q" + indice;
    }

    /**
     * Determina si existe algún estado en Q con el identificador textual especificado.
     *
     * @param nombre Identificador a comprobar.
     * @return {@code true} si ya existe un estado con ese nombre; {@code false} en caso contrario.
     */
    public boolean existeEstadoConNombre(String nombre) {
        return buscarEstadoPorNombre(nombre).isPresent();
    }

    /**
     * Busca un estado en Q por su identificador textual.
     *
     * @param nombre Identificador a buscar.
     * @return {@link Optional} con el estado si fue encontrado, o vacío en caso contrario.
     */
    public Optional<Estado> buscarEstadoPorNombre(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        String buscado = nombre.trim();
        for (Estado estado : estados) {
            if (estado.getNombre().equals(buscado)) {
                return Optional.of(estado);
            }
        }
        return Optional.empty();
    }

    /**
     * Agrega un estado formal al conjunto Q garantizando la unicidad de identificador.
     *
     * @param estado Estado a registrar.
     * @throws IllegalArgumentException Si el estado es nulo o si ya existe un estado con el mismo nombre.
     */
    public void agregarEstado(Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado a agregar no puede ser nulo.");
        }
        if (existeEstadoConNombre(estado.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un estado con el identificador '" + estado.getNombre() + "' en el autómata."
            );
        }
        estados.add(estado);
    }

    /**
     * Elimina un estado del autómata, aplicando borrado en cascada de todas sus transiciones incidentes
     * y salientes, y desmarcando el estado inicial si coincidía con el eliminado.
     *
     * @param estado Estado a remover de Q.
     */
    public void eliminarEstado(Estado estado) {
        if (estado == null || !estados.contains(estado)) {
            return;
        }

        // Paso 1: Si era el estado inicial, dejar el autómata temporalmente sin inicial
        if (estado.equals(this.estadoInicial)) {
            this.estadoInicial = null;
        }

        // Paso 2: Borrado en cascada de transiciones incidentes y salientes (RF-02)
        transiciones.removeIf(t -> t.getOrigen().equals(estado) || t.getDestino().equals(estado));

        // Paso 3: Remover el nodo del conjunto Q
        estados.remove(estado);
    }

    /**
     * Renombra un estado existente en el autómata validando que el nuevo nombre no colisione con otro estado de Q.
     *
     * @param estado Estado a renombrar.
     * @param nuevoNombre Nuevo identificador textual deseado.
     * @throws IllegalArgumentException Si el nuevo nombre colisiona con otro estado o es inválido.
     */
    public void renombrarEstado(Estado estado, String nuevoNombre) {
        if (estado == null || !contieneEstado(estado)) {
            throw new IllegalArgumentException("El estado a renombrar no pertenece a este autómata.");
        }
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo nombre no puede ser nulo ni estar en blanco.");
        }

        String normalizado = nuevoNombre.trim();
        if (normalizado.equals(estado.getNombre())) {
            return; // Sin cambios
        }

        if (existeEstadoConNombre(normalizado)) {
            throw new IllegalArgumentException(
                    "No se puede renombrar: el identificador '" + normalizado + "' ya está en uso."
            );
        }

        estado.renombrar(normalizado);
    }

    /**
     * Determina si un estado específico forma parte del conjunto Q.
     *
     * @param estado Estado a comprobar.
     * @return {@code true} si pertenece a Q; {@code false} en caso contrario.
     */
    public boolean contieneEstado(Estado estado) {
        return estados.contains(estado);
    }

    /**
     * Obtiene la vista inmutable del conjunto de estados Q.
     *
     * @return Conjunto inmutable de estados.
     */
    public Set<Estado> getEstados() {
        return Collections.unmodifiableSet(estados);
    }

    /**
     * Retorna la cantidad de estados registrados en Q (|Q|).
     *
     * @return Número de estados en el autómata.
     */
    public int cardinalidadEstados() {
        return estados.size();
    }

    // =========================================================================
    // 5. GESTIÓN DE ESTADO INICIAL Y ACEPTACIÓN (q0, F)
    // =========================================================================

    /**
     * Designa un estado como el estado inicial único del autómata, reemplazando automáticamente cualquier
     * designación previa.
     *
     * @param estado Estado de Q a marcar como inicial.
     * @throws IllegalArgumentException Si el estado no pertenece al autómata.
     */
    public void marcarEstadoInicial(Estado estado) {
        if (estado == null || !contieneEstado(estado)) {
            throw new IllegalArgumentException(
                    "El estado inicial debe pertenecer al conjunto de estados Q del autómata."
            );
        }
        this.estadoInicial = estado;
    }

    /**
     * Desmarca el estado inicial, dejando al autómata sin inicial definido.
     */
    public void desmarcarEstadoInicial() {
        this.estadoInicial = null;
    }

    /**
     * Obtiene el estado inicial del autómata si está configurado.
     *
     * @return {@link Optional} con el estado inicial o vacío si no se ha designado ninguno.
     */
    public Optional<Estado> getEstadoInicial() {
        return Optional.ofNullable(estadoInicial);
    }

    /**
     * Obtiene la vista inmutable del conjunto de estados de aceptación F (F ⊆ Q).
     *
     * @return Conjunto inmutable de estados marcados como finales.
     */
    public Set<Estado> getEstadosAceptacion() {
        Set<Estado> aceptacion = new LinkedHashSet<>();
        for (Estado e : estados) {
            if (e.esAceptacion()) {
                aceptacion.add(e);
            }
        }
        return Collections.unmodifiableSet(aceptacion);
    }

    // =========================================================================
    // 6. GESTIÓN DEL CONJUNTO DE TRANSICIONES (δ)
    // =========================================================================

    /**
     * Agrega una transición formal dirigida entre dos estados pertenecientes a Q mediante un símbolo de Σ.
     *
     * @param origen Estado origen de la transición.
     * @param destino Estado destino de la transición.
     * @param simbolo Símbolo de transición perteneciente al alfabeto Σ.
     * @return Instancia de {@link Transicion} registrada.
     * @throws IllegalArgumentException Si los estados o el símbolo violan las reglas del autómata.
     */
    public Transicion agregarTransicion(Estado origen, Estado destino, char simbolo) {
        // Paso 1: Validar pertenencia de estados a Q
        if (origen == null || !contieneEstado(origen)) {
            throw new IllegalArgumentException("El estado origen debe pertenecer al conjunto Q del autómata.");
        }
        if (destino == null || !contieneEstado(destino)) {
            throw new IllegalArgumentException("El estado destino debe pertenecer al conjunto Q del autómata.");
        }

        // Paso 2: Validar pertenencia del símbolo al alfabeto formal Σ
        if (alfabeto == null || !alfabeto.contiene(simbolo)) {
            throw new IllegalArgumentException(
                    "El símbolo '" + simbolo + "' no pertenece al alfabeto formal Σ activo del autómata."
            );
        }

        // Paso 3: Crear e insertar la transición (Set ignora duplicados redundantes de forma nativa)
        Transicion transicion = new Transicion(origen, destino, simbolo);
        transiciones.add(transicion);
        return transicion;
    }

    /**
     * Remueve una transición formal del autómata.
     *
     * @param transicion Transición a eliminar.
     */
    public void eliminarTransicion(Transicion transicion) {
        if (transicion != null) {
            transiciones.remove(transicion);
        }
    }

    /**
     * Obtiene los estados destino alcanzables a partir de un estado origen consumiendo un símbolo específico (δ(q, a)).
     *
     * @param origen Estado origen a consultar.
     * @param simbolo Símbolo consumido.
     * @return Conjunto inmutable de estados destino (vacío si no existe transición para dicho símbolo).
     */
    public Set<Estado> obtenerDestinos(Estado origen, char simbolo) {
        if (origen == null) {
            return Collections.emptySet();
        }
        Set<Estado> destinos = new LinkedHashSet<>();
        for (Transicion t : transiciones) {
            if (t.getOrigen().equals(origen) && t.getSimbolo() == simbolo) {
                destinos.add(t.getDestino());
            }
        }
        return Collections.unmodifiableSet(destinos);
    }

    /**
     * Obtiene todas las transiciones que parten de un estado origen determinado.
     *
     * @param origen Estado origen a consultar.
     * @return Conjunto inmutable de transiciones salientes de dicho estado.
     */
    public Set<Transicion> obtenerTransicionesSalientes(Estado origen) {
        if (origen == null) {
            return Collections.emptySet();
        }
        Set<Transicion> salientes = new LinkedHashSet<>();
        for (Transicion t : transiciones) {
            if (t.getOrigen().equals(origen)) {
                salientes.add(t);
            }
        }
        return Collections.unmodifiableSet(salientes);
    }

    /**
     * Obtiene la vista inmutable del conjunto completo de transiciones δ.
     *
     * @return Conjunto inmutable de transiciones.
     */
    public Set<Transicion> getTransiciones() {
        return Collections.unmodifiableSet(transiciones);
    }

    /**
     * Retorna la cantidad total de transiciones registradas en el autómata (|δ|).
     *
     * @return Número de transiciones.
     */
    public int cardinalidadTransiciones() {
        return transiciones.size();
    }

    /**
     * Indica si el autómata contiene al menos una transición registrada.
     *
     * @return {@code true} si hay transiciones; {@code false} si el conjunto está vacío.
     */
    public boolean tieneTransiciones() {
        return !transiciones.isEmpty();
    }

    // =========================================================================
    // 7. CLASIFICACIÓN DINÁMICA DEL TIPO DE AUTÓMATA (RF-05)
    // =========================================================================

    /**
     * Evalúa dinámicamente la estructura del autómata y determina su clasificación formal.
     * <p>
     * Reglas formales (RF-05, Caso Límite 19):
     * <ol>
     *   <li>Si el conjunto Q está vacío (|Q| = 0), retorna {@link TipoAutomata#SIN_ESTADOS}.</li>
     *   <li>Si existe al menos un estado con dos o más transiciones salientes para el mismo símbolo,
     *       retorna {@link TipoAutomata#AFN}.</li>
     *   <li>En caso contrario (|Q| &gt; 0 sin salidas múltiples para ningún símbolo),
     *       retorna {@link TipoAutomata#AFD} (los autómatas parciales o incompletos se consideran deterministas).</li>
     * </ol>
     *
     * @return {@link TipoAutomata} correspondiente al estado actual del grafo.
     */
    public TipoAutomata obtenerTipo() {
        // Paso 1: Si no hay estados registrados en el lienzo
        if (estados.isEmpty()) {
            return TipoAutomata.SIN_ESTADOS;
        }

        // Paso 2: Evaluar transiciones salientes de cada estado en busca de bifurcaciones
        for (Estado q : estados) {
            Set<Character> simbolosSalientes = new java.util.HashSet<>();
            for (Transicion t : transiciones) {
                if (t.getOrigen().equals(q)) {
                    if (!simbolosSalientes.add(t.getSimbolo())) {
                        return TipoAutomata.AFN;
                    }
                }
            }
        }

        // Paso 3: Ningún estado posee salidas múltiples para el mismo símbolo
        return TipoAutomata.AFD;
    }

    // =========================================================================
    // 8. MÉTODOS CANÓNICOS DE IDENTIDAD (OBJECT)
    // =========================================================================

    @Override
    public String toString() {
        return "Automata{" +
                "Q=" + estados +
                ", Σ=" + alfabeto +
                ", q0=" + (estadoInicial != null ? estadoInicial.getNombre() : "ø") +
                ", F=" + getEstadosAceptacion() +
                ", |δ|=" + transiciones.size() +
                ", Tipo=" + obtenerTipo() +
                '}';
    }
}
