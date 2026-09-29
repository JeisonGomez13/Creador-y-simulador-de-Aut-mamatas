package com.automatas.controller;

import com.automatas.model.Alfabeto;
import com.automatas.model.Automata;
import com.automatas.model.ConvertidorSubconjuntos;
import com.automatas.model.Estado;
import com.automatas.model.ResultadoConversion;
import com.automatas.model.ResultadoSimulacion;
import com.automatas.model.SimuladorAutomata;
import com.automatas.model.TipoAutomata;
import com.automatas.model.Transicion;
import com.automatas.view.VistaPestanaAutomata;
import com.automatas.view.componente.NodoEstadoVista;
import com.automatas.view.componente.SelectorSimbolosDialog;
import com.automatas.view.util.DisposicionCircular;
import javafx.geometry.Point2D;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Controlador MVC que coordina el modelo {@link Automata} con la vista de pestaña {@link VistaPestanaAutomata}
 * (RF-01 a RF-08).
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Sincronización en tiempo real del modelo y la tabla de transiciones viva (RF-08).</li>
 *   <li>Gestión del alfabeto formal y su bloqueo automático ante transiciones (RF-01, Caso Límite 10).</li>
 *   <li>Clasificación dinámica continua del autómata (Sin estados, AFD, AFN) (RF-05).</li>
 *   <li>Orquestación del motor de simulación inmediata y emisión de veredictos binarios (RF-06).</li>
 *   <li>Flujo completo de conversión AFN -> AFD con topología protegida y disposición circular (RF-07).</li>
 * </ul>
 */
public class ControladorPestana {

    // =========================================================================
    // 1. ATRIBUTOS DE INSTANCIA
    // =========================================================================

    private final Automata automata;
    private final VistaPestanaAutomata vista;
    private final SimuladorAutomata simulador;

    private String titulo;
    private int contadorConversiones;
    private boolean modoProtegido;
    private Consumer<ControladorPestana> onNuevaPestanaGenerada;

    // =========================================================================
    // 2. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye y vincula el controlador de pestaña con título predeterminado "Autómata 1".
     *
     * @param automata Modelo formal {@link Automata}.
     * @param vista Contenedor visual {@link VistaPestanaAutomata}.
     */
    public ControladorPestana(Automata automata, VistaPestanaAutomata vista) {
        this(automata, vista, "Autómata 1");
    }

    /**
     * Construye y vincula el controlador de pestaña especificando su título de pestaña.
     *
     * @param automata Modelo formal {@link Automata}.
     * @param vista Contenedor visual {@link VistaPestanaAutomata}.
     * @param titulo Título identificador de la pestaña.
     * @throws IllegalArgumentException Si el modelo o la vista son nulos.
     */
    public ControladorPestana(Automata automata, VistaPestanaAutomata vista, String titulo) {
        if (automata == null || vista == null) {
            throw new IllegalArgumentException("El autómata y la vista de pestaña no pueden ser nulos.");
        }

        this.automata = automata;
        this.vista = vista;
        this.simulador = new SimuladorAutomata();
        this.titulo = (titulo != null && !titulo.trim().isEmpty()) ? titulo.trim() : "Autómata 1";
        this.contadorConversiones = 0;
        this.modoProtegido = false;

        inicializarControlador();
    }

    private void inicializarControlador() {
        // Paso 1: Configurar alfabeto en lienzo si ya existe en el modelo
        if (automata.getAlfabeto() != null) {
            vista.getLienzo().setAlfabeto(automata.getAlfabeto());
            vista.getPanelSuperior().getCampoAlfabeto().setText(
                    String.join(", ", automata.getAlfabeto().getSimbolos().stream().map(String::valueOf).toList())
            );
        }

        // Paso 2: Escuchadores de eventos de la vista
        vista.getPanelSuperior().setOnDefinirAlfabeto(this::definirAlfabeto);
        vista.getPanelSuperior().setOnConvertir(this::convertirAAfd);
        vista.getPanelSuperior().setOnAgregarEstado(this::agregarEstadoDesdeBoton);

        vista.getLienzo().setOnSolicitudTransicion(this::manejarSolicitudTransicion);
        vista.getLienzo().setOnCrearEstadoEnPosicion(this::agregarEstado);
        vista.getLienzo().setOnSolicitudEliminarEstado(this::eliminarEstado);
        vista.getLienzo().setOnSolicitudEstablecerInicial(this::establecerEstadoInicial);
        vista.getLienzo().setOnSolicitudDesmarcarInicial(this::desmarcarEstadoInicial);
        vista.getLienzo().setOnSolicitudAlternarAceptacion(this::alternarAceptacion);
        vista.getLienzo().setOnSolicitudRenombrarEstado(this::renombrarEstado);

        vista.getPanelComprobacion().setOnComprobar(this::comprobarCadena);
        vista.getPanelComprobacion().getVistaRecorrido().setOnCaminoSeleccionado(camino -> {
            vista.getLienzo().resaltarCamino(camino);
        });

        // Paso 3: Sincronización inicial
        sincronizar();
    }

    private void validarModoEdicionPermitido() {
        if (modoProtegido) {
            throw new IllegalStateException("Operación no permitida: la pestaña se encuentra en modo protegido de topología.");
        }
    }

    // =========================================================================
    // 3. GESTIÓN DEL ALFABETO FORMAL (RF-01, CASO LÍMITE 10)
    // =========================================================================

    /**
     * Procesa la solicitud del usuario para definir o actualizar el alfabeto formal Σ.
     *
     * @param texto Cadena con símbolos separados por comas.
     */
    public void definirAlfabeto(String texto) {
        validarModoEdicionPermitido();

        if (automata.tieneTransiciones()) {
            vista.getPanelSuperior().mostrarMensaje(
                    "Debe eliminar las transiciones existentes antes de reconfigurar el alfabeto."
            );
            return;
        }

        try {
            Alfabeto nuevoAlfabeto = Alfabeto.desdeCadena(texto);
            automata.asignarAlfabeto(nuevoAlfabeto);
            vista.getLienzo().setAlfabeto(nuevoAlfabeto);
            vista.getPanelSuperior().limpiarMensaje();
            sincronizar();
        } catch (IllegalArgumentException e) {
            vista.getPanelSuperior().mostrarMensaje(e.getMessage());
        }
    }

    // =========================================================================
    // 4. GESTIÓN DE ESTADOS (RF-02, RF-03)
    // =========================================================================

    /**
     * Agrega un nuevo estado al autómata invocando la acción desde el botón superior,
     * calculando una posición visual escalonada en el lienzo.
     *
     * @return Entidad formal {@link Estado} creada.
     */
    public Estado agregarEstadoDesdeBoton() {
        validarModoEdicionPermitido();
        int cantidad = automata.cardinalidadEstados();
        double x = 120.0 + ((cantidad * 80.0) % 560.0);
        double y = 140.0 + (((cantidad / 7) * 90.0) % 360.0);
        return agregarEstado(x, y);
    }

    /**
     * Agrega un nuevo estado al autómata y lo posiciona en el lienzo gráfico.
     *
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @return Entidad formal {@link Estado} creada.
     */
    public Estado agregarEstado(double x, double y) {
        validarModoEdicionPermitido();

        Estado nuevoEstado = automata.crearEstado();
        vista.getLienzo().agregarEstado(nuevoEstado, x, y);
        sincronizar();
        return nuevoEstado;
    }

    /**
     * Elimina un estado del autómata y del lienzo en cascada.
     *
     * @param estado Estado a remover.
     */
    public void eliminarEstado(Estado estado) {
        validarModoEdicionPermitido();

        if (estado == null) {
            return;
        }
        automata.eliminarEstado(estado);
        vista.getLienzo().eliminarEstado(estado);
        sincronizar();
    }

    /**
     * Renombra un estado existente en el modelo y actualiza la vista viva.
     *
     * @param estado Estado formal a renombrar.
     * @param nuevoNombre Nuevo identificador textual.
     */
    public void renombrarEstado(Estado estado, String nuevoNombre) {
        validarModoEdicionPermitido();

        if (estado == null || nuevoNombre == null) {
            return;
        }
        try {
            String nombreAnterior = estado.getNombre();
            automata.renombrarEstado(estado, nuevoNombre);
            vista.getLienzo().renombrarEstado(estado, nombreAnterior, nuevoNombre);
            vista.getPanelSuperior().limpiarMensaje();
            sincronizar();
        } catch (IllegalArgumentException e) {
            vista.getPanelSuperior().mostrarMensaje(e.getMessage());
        }
    }

    /**
     * Establece el estado inicial único del autómata (RF-03).
     *
     * @param estado Estado formal inicial.
     */
    public void establecerEstadoInicial(Estado estado) {
        validarModoEdicionPermitido();

        automata.marcarEstadoInicial(estado);
        vista.getLienzo().establecerEstadoInicial(estado);
        sincronizar();
    }

    /**
     * Desmarca el estado inicial del autómata si existía uno configurado (RF-03).
     */
    public void desmarcarEstadoInicial() {
        validarModoEdicionPermitido();

        automata.desmarcarEstadoInicial();
        vista.getLienzo().desmarcarEstadoInicial();
        sincronizar();
    }

    /**
     * Alterna la condición de aceptación de un estado.
     *
     * @param estado Estado a modificar.
     */
    public void alternarAceptacion(Estado estado) {
        validarModoEdicionPermitido();

        if (estado == null) {
            return;
        }
        estado.setAceptacion(!estado.esAceptacion());
        vista.getLienzo().obtenerNodoVista(estado).ifPresent(NodoEstadoVista::actualizarVista);
        sincronizar();
    }

    // =========================================================================
    // 5. GESTIÓN DE TRANSICIONES (RF-04)
    // =========================================================================

    /**
     * Registra una transición entre dos estados (o autolazo si origen == destino) para un símbolo único.
     *
     * @param origen Estado origen.
     * @param destino Estado destino.
     * @param simbolo Símbolo de transición.
     */
    public void agregarTransicion(Estado origen, Estado destino, char simbolo) {
        agregarTransicion(origen, destino, Set.of(simbolo));
    }

    /**
     * Registra transiciones entre dos estados (o autolazo si origen == destino) para un conjunto de símbolos.
     *
     * @param origen Estado origen.
     * @param destino Estado destino.
     * @param simbolos Conjunto de símbolos asociados.
     */
    public void agregarTransicion(Estado origen, Estado destino, Set<Character> simbolos) {
        validarModoEdicionPermitido();

        if (origen == null || destino == null || simbolos == null || simbolos.isEmpty()) {
            return;
        }
        if (origen.equals(destino)) {
            crearAutolazo(origen, simbolos);
        } else {
            conectarEstados(origen, destino, simbolos);
        }
    }

    /**
     * Conecta dos estados distintos mediante un conjunto de símbolos del alfabeto formal.
     *
     * @param origen Estado origen.
     * @param destino Estado destino.
     * @param simbolos Símbolos formalmente asociados.
     */
    public void conectarEstados(Estado origen, Estado destino, Set<Character> simbolos) {
        validarModoEdicionPermitido();

        if (origen == null || destino == null || simbolos == null || simbolos.isEmpty()) {
            return;
        }

        for (char s : simbolos) {
            automata.agregarTransicion(origen, destino, s);
        }
        vista.getLienzo().conectarEstados(origen, destino, simbolos);
        sincronizar();
    }

    /**
     * Crea o amplía un autolazo reflexivo sobre un estado (Caso Límite 7).
     *
     * @param estado Estado formal.
     * @param simbolos Símbolos de transición.
     */
    public void crearAutolazo(Estado estado, Set<Character> simbolos) {
        validarModoEdicionPermitido();

        if (estado == null || simbolos == null || simbolos.isEmpty()) {
            return;
        }

        for (char s : simbolos) {
            automata.agregarTransicion(estado, estado, s);
        }
        vista.getLienzo().crearAutolazo(estado, simbolos);
        sincronizar();
    }

    private void manejarSolicitudTransicion(Estado origen, Estado destino) {
        if (modoProtegido) {
            return;
        }

        if (automata.getAlfabeto() == null || automata.getAlfabeto().getSimbolos().isEmpty()) {
            vista.getPanelSuperior().mostrarMensaje("Debe definir primero el alfabeto formal Σ.");
            return;
        }

        SelectorSimbolosDialog dialog = new SelectorSimbolosDialog(automata.getAlfabeto());
        Optional<List<Character>> resultado = dialog.showAndWait();

        if (resultado.isPresent() && !resultado.get().isEmpty()) {
            Set<Character> seleccionados = new HashSet<>(resultado.get());
            if (origen.equals(destino)) {
                crearAutolazo(origen, seleccionados);
            } else {
                conectarEstados(origen, destino, seleccionados);
            }
        }
    }

    // =========================================================================
    // 6. COMPROBACIÓN INMEDIATA DE CADENAS (RF-06, CASO LÍMITE 1)
    // =========================================================================

    /**
     * Evalúa una cadena en el autómata y refleja el veredicto binario en el panel inferior.
     *
     * @param cadena Entrada a simular.
     */
    public void comprobarCadena(String cadena) {
        if (automata.getEstadoInicial().isEmpty()) {
            vista.getPanelComprobacion().mostrarError(
                    "No se puede evaluar: el autómata no posee un estado inicial designado."
            );
            return;
        }

        String normalizada = (cadena == null) ? "" : cadena.trim();

        Alfabeto alfabeto = automata.getAlfabeto();
        if (alfabeto != null) {
            for (int i = 0; i < normalizada.length(); i++) {
                char c = normalizada.charAt(i);
                if (!alfabeto.contiene(c)) {
                    vista.getPanelComprobacion().mostrarError(
                            "El carácter '" + c + "' es inválido (no pertenece al alfabeto Σ)."
                    );
                    return;
                }
            }
        }

        try {
            ResultadoSimulacion resultado = simulador.evaluarCadena(automata, normalizada);
            vista.getPanelComprobacion().mostrarResultado(resultado);

            // Resaltar en el lienzo el camino exitoso o la primera ruta explorada
            if (resultado.getPrimerCaminoExitoso().isPresent()) {
                vista.getLienzo().resaltarCamino(resultado.getPrimerCaminoExitoso().get());
            } else if (!resultado.getCaminos().isEmpty()) {
                vista.getLienzo().resaltarCamino(resultado.getCaminos().get(0));
            } else {
                vista.getLienzo().limpiarResaltado();
            }
        } catch (Exception ex) {
            vista.getPanelComprobacion().mostrarError(ex.getMessage());
            vista.getLienzo().limpiarResaltado();
        }
    }

    // =========================================================================
    // 7. FLUJO COMPLETO DE CONVERSIÓN AFN A AFD (RF-07, CASOS LÍMITE 11, 12, 19, 22)
    // =========================================================================

    /**
     * Ejecuta el flujo formal de determinización por construcción de subconjuntos.
     * <p>
     * Valida sucesivamente la precedencia:
     * <ol>
     *   <li>Presencia de estados en el autómata.</li>
     *   <li>Existencia de estado inicial (Caso Límite 12.1 / 18).</li>
     *   <li>Existencia de al menos un estado de aceptación.</li>
     *   <li>Rechazo si el autómata ya clasifica como AFD (Caso Límite 12.2).</li>
     *   <li>Definición activa de alfabeto formal Σ.</li>
     * </ol>
     *
     * @return {@link Optional} con el nuevo {@link ControladorPestana} generado en modo protegido,
     *         o vacío si fallaron las validaciones.
     */
    public Optional<ControladorPestana> convertirAAfd() {
        // Validación 1: Presencia de estados
        if (automata.cardinalidadEstados() == 0) {
            vista.getPanelSuperior().mostrarMensaje("El autómata no contiene estados para convertir.");
            return Optional.empty();
        }

        // Validación 2: Precedencia 1 - Existencia obligatoria de estado inicial (RF-07, Caso Límite 12.1)
        if (automata.getEstadoInicial().isEmpty()) {
            vista.getPanelSuperior().mostrarMensaje("Debe designar un estado inicial antes de convertir.");
            return Optional.empty();
        }

        // Validación 3: Existencia de estados de aceptación
        if (automata.getEstadosAceptacion().isEmpty()) {
            vista.getPanelSuperior().mostrarMensaje("Debe definir al menos un estado final (de aceptación) antes de realizar la conversión.");
            return Optional.empty();
        }

        // Validación 4: Precedencia 2 - Rechazar si ya es determinista (RF-07, Caso Límite 12.2)
        if (automata.obtenerTipo() == TipoAutomata.AFD) {
            vista.getPanelSuperior().mostrarMensaje("El autómata ya es determinista y no requiere conversión.");
            return Optional.empty();
        }

        // Validación 5: Alfabeto no nulo
        if (automata.getAlfabeto() == null || automata.getAlfabeto().getSimbolos().isEmpty()) {
            vista.getPanelSuperior().mostrarMensaje("Debe definir primero el alfabeto formal Σ.");
            return Optional.empty();
        }

        // Ejecutar algoritmo formal de subconjuntos
        ConvertidorSubconjuntos convertidor = new ConvertidorSubconjuntos();
        ResultadoConversion resultado = convertidor.convertir(automata);
        Automata afdGenerado = resultado.getAfdGenerado();

        // Crear nueva vista y controlador
        VistaPestanaAutomata nuevaVista = new VistaPestanaAutomata();
        ControladorPestana nuevoControlador = new ControladorPestana(afdGenerado, nuevaVista);

        // Generar título correlativo diferenciado (Caso Límite 22)
        this.contadorConversiones++;
        String nuevoTitulo = (this.contadorConversiones == 1)
                ? "AFD (de " + this.titulo + ")"
                : "AFD " + this.contadorConversiones + " (de " + this.titulo + ")";
        nuevoControlador.setTitulo(nuevoTitulo);

        // Disposición circular uniforme limpia (RF-07, Caso Límite 15)
        double centroX = nuevaVista.getLienzo().getAnchoVirtual() / 2.0;
        double centroY = nuevaVista.getLienzo().getAltoVirtual() / 2.0;
        Map<Estado, Point2D> posiciones = DisposicionCircular.calcularDisposicion(
                afdGenerado.getEstados(), centroX, centroY
        );

        Estado inicialAfd = afdGenerado.getEstadoInicial().orElse(null);
        for (Estado estado : afdGenerado.getEstados()) {
            Point2D pos = posiciones.get(estado);
            boolean esInicial = estado.equals(inicialAfd);
            nuevaVista.getLienzo().agregarEstado(estado, pos.getX(), pos.getY(), esInicial);
        }

        // Poblar transiciones unificadas en el lienzo del nuevo AFD
        Map<String, Set<Character>> transicionesAgrupadas = new LinkedHashMap<>();
        for (Transicion t : afdGenerado.getTransiciones()) {
            String clave = t.getOrigen().getNombre() + "->" + t.getDestino().getNombre();
            transicionesAgrupadas.computeIfAbsent(clave, k -> new TreeSet<>()).add(t.getSimbolo());
        }

        for (Map.Entry<String, Set<Character>> entry : transicionesAgrupadas.entrySet()) {
            String[] partes = entry.getKey().split("->");
            Estado origen = afdGenerado.buscarEstadoPorNombre(partes[0]).orElseThrow();
            Estado destino = afdGenerado.buscarEstadoPorNombre(partes[1]).orElseThrow();
            Set<Character> simbolos = entry.getValue();

            if (origen.equals(destino)) {
                nuevaVista.getLienzo().crearAutolazo(origen, simbolos);
            } else {
                nuevaVista.getLienzo().conectarEstados(origen, destino, simbolos);
            }
        }

        // Actualizar tabla con subconjuntos inmutables (RF-08)
        nuevaVista.getTablaTransiciones().actualizarConConversion(resultado);
        nuevaVista.getEtiquetaTituloTabla().setText("Tabla de Subconjuntos");

        // Configurar modo protegido (Caso Límite 20)
        nuevoControlador.setModoProtegido(true);

        // Limpiar mensajes previos en la vista fuente
        vista.getPanelSuperior().limpiarMensaje();

        // Notificar al escuchador de nueva pestaña si está presente
        if (onNuevaPestanaGenerada != null) {
            onNuevaPestanaGenerada.accept(nuevoControlador);
        }

        return Optional.of(nuevoControlador);
    }

    // =========================================================================
    // 8. SINCRONIZACIÓN REACTIVA GLOBAL (RF-05, RF-08)
    // =========================================================================

    /**
     * Sincroniza al unísono todos los componentes de la vista con el estado actual del modelo formal:
     * clasificación del autómata, tabla de transiciones y bloqueo de alfabeto.
     */
    public void sincronizar() {
        // 1. Sincronizar clasificación viva del autómata (RF-05)
        vista.getPanelSuperior().actualizarTipoAutomata(automata.obtenerTipo());

        // 2. Sincronizar bloqueo de alfabeto (RF-01, Caso Límite 10)
        vista.getPanelSuperior().bloquearAlfabeto(modoProtegido || automata.tieneTransiciones());

        // 3. Sincronizar tabla de transiciones lateral si no está en modo protegido (RF-08)
        if (!modoProtegido) {
            vista.getTablaTransiciones().actualizar(automata);
        }
    }

    // =========================================================================
    // 9. GETTERS Y SETTERS
    // =========================================================================

    public Automata getAutomata() {
        return automata;
    }

    public VistaPestanaAutomata getVista() {
        return vista;
    }

    public SimuladorAutomata getSimulador() {
        return simulador;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo != null && !titulo.trim().isEmpty()) {
            this.titulo = titulo.trim();
        }
    }

    public boolean esModoProtegido() {
        return modoProtegido;
    }

    public void setModoProtegido(boolean modoProtegido) {
        this.modoProtegido = modoProtegido;
        vista.setModoProtegido(modoProtegido);
    }

    public void setOnNuevaPestanaGenerada(Consumer<ControladorPestana> onNuevaPestanaGenerada) {
        this.onNuevaPestanaGenerada = onNuevaPestanaGenerada;
    }

    /**
     * Determina si el autómata actual posee contenido (estados o transiciones) susceptible de confirmación antes de cierre (Caso Límite 23).
     *
     * @return {@code true} si posee al menos un estado o transición; {@code false} en caso contrario.
     */
    public boolean tieneContenido() {
        return automata.cardinalidadEstados() > 0 || automata.tieneTransiciones();
    }
}
