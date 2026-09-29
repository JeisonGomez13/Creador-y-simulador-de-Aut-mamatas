package com.automatas.view.componente;

import com.automatas.model.Alfabeto;
import com.automatas.model.Automata;
import com.automatas.model.Estado;
import com.automatas.model.FilaSubconjuntos;
import com.automatas.model.ResultadoConversion;
import com.automatas.model.TablaSubconjuntos;
import com.automatas.view.util.PaletaColor;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Componente visual reactivo basado en {@link TableView} para la representación tabular
 * de la matriz de transiciones formal de un autómata finito (RF-08, Casos Límite 14 y 15).
 * <p>
 * Reglas de diseño y visualización:
 * <ul>
 *   <li><b>Columnas dinámicas:</b> Se genera automáticamente una columna por cada símbolo de $\Sigma$.</li>
 *   <li><b>Indicadores formales:</b> Prefigura con flecha ({@code "-> "}) el estado inicial y con asterisco
 *       ({@code "* "}) los estados de aceptación ($F$).</li>
 *   <li><b>Conjuntos AFN:</b> Múltiples destinos para un mismo símbolo se exhiben entre llaves (ej. {@code "{q1, q2}"}).</li>
 *   <li><b>Estado trampa vacío:</b> Celdas sin transición definida exhiben el símbolo formal {@code "ø"} (Caso Límite 15).</li>
 *   <li><b>Pestañas de conversión:</b> Incluye la columna inmutable de mapeo a subconjuntos del AFN original (RF-07).</li>
 *   <li><b>Paleta neutra (Constitución Principio 7):</b> Prohibición absoluta de colores azul y café.</li>
 * </ul>
 */
public class TablaTransicionesVista extends TableView<TablaTransicionesVista.FilaTransicionVista> {

    // =========================================================================
    // 1. CONSTANTES
    // =========================================================================

    public static final String SIMBOLO_VACIO = "ø";
    public static final String ENCABEZADO_ESTADO = "Estado";
    public static final String ENCABEZADO_SUBCONJUNTO = "Subconjunto AFN";

    // =========================================================================
    // 2. MODELO DE FILA VISUAL (DTO INMUTABLE)
    // =========================================================================

    /**
     * Representa una fila inmutable dentro de la tabla de transiciones visual.
     */
    public static class FilaTransicionVista {

        private final Estado estado;
        private final String nombreEstado;
        private final String indicadorEstado;
        private final boolean esInicial;
        private final boolean esAceptacion;
        private final String subconjuntoAFN;
        private final Map<Character, String> destinos;

        public FilaTransicionVista(
                Estado estado,
                boolean esInicial,
                boolean esAceptacion,
                String subconjuntoAFN,
                Map<Character, String> destinos
        ) {
            this.estado = estado;
            this.nombreEstado = estado != null ? estado.getNombre() : "";
            this.esInicial = esInicial;
            this.esAceptacion = esAceptacion;
            this.subconjuntoAFN = subconjuntoAFN != null ? subconjuntoAFN : "";
            this.destinos = destinos != null ? new LinkedHashMap<>(destinos) : Collections.emptyMap();

            // Construir indicador formal estándar (-> inicial, * aceptación)
            if (esInicial && esAceptacion) {
                this.indicadorEstado = "->* " + nombreEstado;
            } else if (esInicial) {
                this.indicadorEstado = "-> " + nombreEstado;
            } else if (esAceptacion) {
                this.indicadorEstado = "* " + nombreEstado;
            } else {
                this.indicadorEstado = nombreEstado;
            }
        }

        public Estado getEstado() {
            return estado;
        }

        public String getNombreEstado() {
            return nombreEstado;
        }

        public String getIndicadorEstado() {
            return indicadorEstado;
        }

        public boolean esInicial() {
            return esInicial;
        }

        public boolean esAceptacion() {
            return esAceptacion;
        }

        public String getSubconjuntoAFN() {
            return subconjuntoAFN;
        }

        public String getDestino(char simbolo) {
            return destinos.getOrDefault(simbolo, SIMBOLO_VACIO);
        }

        public Map<Character, String> getDestinos() {
            return Collections.unmodifiableMap(destinos);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilaTransicionVista that = (FilaTransicionVista) o;
            return Objects.equals(nombreEstado, that.nombreEstado);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nombreEstado);
        }
    }

    // =========================================================================
    // 3. CONSTRUCTORES
    // =========================================================================

    /**
     * Construye un componente TablaTransicionesVista vacío con estilos institucionales neutros.
     */
    public TablaTransicionesVista() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Label etiquetaVacio = new Label("No hay estados ni transiciones registradas.");
        etiquetaVacio.setTextFill(PaletaColor.TEXTO_SECUNDARIO);
        setPlaceholder(etiquetaVacio);

        setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-radius: 6px;");
    }

    // =========================================================================
    // 4. ACTUALIZACIÓN REACTIVA (AUTÓMATA GENERAL: AFD / AFN)
    // =========================================================================

    /**
     * Actualiza la tabla reflejando la estructura viva de un autómata (estados, alfabeto y transiciones).
     *
     * @param automata Entidad formal {@link Automata}.
     */
    public void actualizar(Automata automata) {
        limpiar();

        if (automata == null) {
            return;
        }

        Alfabeto alfabeto = automata.getAlfabeto();
        if (alfabeto == null) {
            return;
        }

        // Paso 1: Columna principal de Estados
        TableColumn<FilaTransicionVista, String> colEstado = crearColumna(
                ENCABEZADO_ESTADO,
                fila -> fila.getIndicadorEstado(),
                80.0,
                Pos.CENTER_LEFT
        );
        getColumns().add(colEstado);

        // Paso 2: Columnas dinámicas para cada símbolo de Σ
        for (char simbolo : alfabeto.getSimbolos()) {
            final char s = simbolo;
            TableColumn<FilaTransicionVista, String> colSimbolo = crearColumna(
                    String.valueOf(s),
                    fila -> fila.getDestino(s),
                    55.0,
                    Pos.CENTER
            );
            getColumns().add(colSimbolo);
        }

        // Paso 3: Poblar filas ordenadas por nombre de estado
        List<Estado> estadosOrdenados = new ArrayList<>(automata.getEstados());
        estadosOrdenados.sort((e1, e2) -> e1.getNombre().compareTo(e2.getNombre()));

        for (Estado q : estadosOrdenados) {
            boolean inicial = automata.getEstadoInicial().map(q::equals).orElse(false);
            boolean aceptacion = q.esAceptacion();

            Map<Character, String> destinosPorSimbolo = new LinkedHashMap<>();
            for (char simbolo : alfabeto.getSimbolos()) {
                Set<Estado> destinos = automata.obtenerDestinos(q, simbolo);
                String valorFormateado;

                if (destinos.isEmpty()) {
                    valorFormateado = SIMBOLO_VACIO;
                } else if (destinos.size() > 1) {
                    valorFormateado = "{" + destinos.stream()
                            .map(Estado::getNombre)
                            .sorted()
                            .collect(Collectors.joining(", ")) + "}";
                } else {
                    valorFormateado = destinos.iterator().next().getNombre();
                }

                destinosPorSimbolo.put(simbolo, valorFormateado);
            }

            FilaTransicionVista fila = new FilaTransicionVista(q, inicial, aceptacion, "", destinosPorSimbolo);
            getItems().add(fila);
        }
    }

    // =========================================================================
    // 5. ACTUALIZACIÓN PARA PESTAÑAS DE CONVERSIÓN (SUBCONJUNTOS)
    // =========================================================================

    /**
     * Actualiza la tabla para una pestaña de conversión a AFD, incorporando la columna inmutable
     * de correspondencia de subconjuntos del AFN original (RF-07, RF-08).
     *
     * @param resultado DTO {@link ResultadoConversion} generado por el algoritmo.
     */
    public void actualizarConConversion(ResultadoConversion resultado) {
        limpiar();

        if (resultado == null) {
            return;
        }

        TablaSubconjuntos tablaSubconjuntos = resultado.getTablaSubconjuntos();
        Automata afd = resultado.getAfdGenerado();
        Alfabeto alfabeto = afd.getAlfabeto();

        // Paso 1: Columna de Estados del AFD (k0, k1, ...)
        TableColumn<FilaTransicionVista, String> colEstado = crearColumna(
                ENCABEZADO_ESTADO,
                fila -> fila.getIndicadorEstado(),
                80.0,
                Pos.CENTER_LEFT
        );
        getColumns().add(colEstado);

        // Paso 2: Columna inmutable de Subconjunto AFN ({q0, q1})
        TableColumn<FilaTransicionVista, String> colSubconjunto = crearColumna(
                ENCABEZADO_SUBCONJUNTO,
                fila -> fila.getSubconjuntoAFN(),
                120.0,
                Pos.CENTER
        );
        getColumns().add(colSubconjunto);

        // Paso 3: Columnas de símbolos de Σ
        for (char simbolo : alfabeto.getSimbolos()) {
            final char s = simbolo;
            TableColumn<FilaTransicionVista, String> colSimbolo = crearColumna(
                    String.valueOf(s),
                    fila -> fila.getDestino(s),
                    60.0,
                    Pos.CENTER
            );
            getColumns().add(colSimbolo);
        }

        // Paso 4: Poblar filas con las correspondencias de subconjuntos
        for (FilaSubconjuntos filaSub : tablaSubconjuntos.getFilas()) {
            Estado estadoAFD = filaSub.getEstadoAFD();
            boolean inicial = afd.getEstadoInicial().map(estadoAFD::equals).orElse(false);
            boolean aceptacion = estadoAFD.esAceptacion();
            String subconjuntoFormateado = filaSub.getSubconjuntoFormateado();

            Map<Character, String> destinosAFD = new LinkedHashMap<>();
            for (char simbolo : alfabeto.getSimbolos()) {
                destinosAFD.put(simbolo, filaSub.obtenerDestinoFormateado(simbolo));
            }

            FilaTransicionVista fila = new FilaTransicionVista(
                    estadoAFD,
                    inicial,
                    aceptacion,
                    subconjuntoFormateado,
                    destinosAFD
            );
            getItems().add(fila);
        }
    }

    // =========================================================================
    // 6. MÉTODOS DE CONSULTA Y UTILIDADES
    // =========================================================================

    /**
     * Limpia completamente las columnas y los elementos de la tabla.
     */
    public void limpiar() {
        getItems().clear();
        getColumns().clear();
    }

    /**
     * Determina si la tabla tiene actualmente desplegada la columna de subconjuntos AFN.
     *
     * @return {@code true} si está en modo conversión; {@code false} en caso contrario.
     */
    public boolean tieneColumnaSubconjuntos() {
        return getColumns().stream().anyMatch(col -> ENCABEZADO_SUBCONJUNTO.equals(col.getText()));
    }

    /**
     * Obtiene la fila visual asociada a una entidad formal de estado si existe.
     *
     * @param estado Estado formal.
     * @return {@link Optional} con la fila encontrada.
     */
    public Optional<FilaTransicionVista> obtenerFila(Estado estado) {
        if (estado == null) {
            return Optional.empty();
        }
        return getItems().stream()
                .filter(f -> estado.equals(f.getEstado()))
                .findFirst();
    }

    /**
     * Obtiene la fila visual por su identificador textual.
     *
     * @param nombreEstado Nombre del estado (ej. "q0", "k1").
     * @return {@link Optional} con la fila encontrada.
     */
    public Optional<FilaTransicionVista> obtenerFilaPorNombre(String nombreEstado) {
        if (nombreEstado == null) {
            return Optional.empty();
        }
        return getItems().stream()
                .filter(f -> nombreEstado.equals(f.getNombreEstado()))
                .findFirst();
    }

    /**
     * Retorna el texto exhibido en la celda correspondiente al estado y símbolo especificados.
     *
     * @param estado Estado de la fila.
     * @param simbolo Símbolo de la columna.
     * @return Cadena contenida en la celda o "ø" si no existe.
     */
    public String obtenerValorCelda(Estado estado, char simbolo) {
        return obtenerFila(estado)
                .map(f -> f.getDestino(simbolo))
                .orElse(SIMBOLO_VACIO);
    }

    /**
     * Retorna el texto exhibido en la celda buscando por el identificador textual del estado.
     *
     * @param nombreEstado Identificador del estado.
     * @param simbolo Símbolo de la columna.
     * @return Cadena contenida en la celda o "ø" si no existe.
     */
    public String obtenerValorCelda(String nombreEstado, char simbolo) {
        return obtenerFilaPorNombre(nombreEstado)
                .map(f -> f.getDestino(simbolo))
                .orElse(SIMBOLO_VACIO);
    }

    // =========================================================================
    // 7. CREADOR DE COLUMNAS CON ESTILOS CONSISTENTES
    // =========================================================================

    private TableColumn<FilaTransicionVista, String> crearColumna(
            String titulo,
            java.util.function.Function<FilaTransicionVista, String> extractor,
            double minWidth,
            Pos alineacion
    ) {
        TableColumn<FilaTransicionVista, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(cellData -> new SimpleStringProperty(extractor.apply(cellData.getValue())));
        columna.setSortable(false);
        columna.setMinWidth(minWidth);

        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(alineacion);
                    // Si es vacío ø, atenuar ligeramente el texto
                    if (SIMBOLO_VACIO.equals(item)) {
                        setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: #111827; -fx-font-weight: bold;");
                    }
                }
            }
        });

        return columna;
    }
}
