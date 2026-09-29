package com.automatas.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Motor formal de simulación y evaluación de cadenas sobre autómatas finitos (AFD y AFN).
 * <p>
 * Reglas de simulación y diseño (RF-06, Casos Límite 1, 2, 9 y Constitución - Principios 8 y 9):
 * <ul>
 *   <li><b>Recorte automático:</b> Aplica {@code trim()} a los extremos de la cadena antes de evaluar.</li>
 *   <li><b>Validación previa de Σ:</b> Rechaza cualquier carácter ajeno al alfabeto antes de simular.</li>
 *   <li><b>Validación de estado inicial:</b> Aborta si el autómata carece de un estado inicial definido.</li>
 *   <li><b>Evaluación de palabra vacía (λ):</b> Acepta si y solo si el estado inicial es de aceptación.</li>
 *   <li><b>Exploración de ramas (caminos de transición):</b> Explora exhaustivamente todos los caminos posibles
 *       (sin ε-transiciones), registrando cada transición y detectando caminos muertos.</li>
 *   <li><b>Veredicto estricto:</b> Emite «Aceptada» si al menos una rama finaliza en aceptación tras consumir
 *       toda la palabra; «Rechazada» en caso contrario.</li>
 * </ul>
 */
public class SimuladorAutomata {

    // =========================================================================
    // 1. MÉTODO PRINCIPAL DE EVALUACIÓN
    // =========================================================================

    /**
     * Evalúa una cadena sobre un autómata finito y retorna el resultado detallado con todos sus caminos.
     *
     * @param automata Autómata formal sobre el cual evaluar la cadena.
     * @param entrada Cadena de texto a evaluar.
     * @return {@link ResultadoSimulacion} con el veredicto binario y la trazabilidad de caminos.
     * @throws IllegalStateException Si el autómata no tiene un estado inicial definido.
     * @throws IllegalArgumentException Si el autómata o la entrada son nulos, o si la cadena contiene
     *                                  caracteres que no pertenecen al alfabeto Σ del autómata.
     */
    public ResultadoSimulacion evaluarCadena(Automata automata, String entrada) {
        // Paso 1: Validar nulidad de parámetros
        if (automata == null) {
            throw new IllegalArgumentException("El autómata a evaluar no puede ser nulo.");
        }
        if (entrada == null) {
            throw new IllegalArgumentException("La cadena de entrada no puede ser nula.");
        }

        // Paso 2: Validar precondición obligatoria de estado inicial y estado final (RF-03, RF-06)
        if (automata.getEstadoInicial().isEmpty()) {
            throw new IllegalStateException("Debe definir un estado inicial antes de evaluar cadenas.");
        }
        if (automata.getEstadosAceptacion().isEmpty()) {
            throw new IllegalStateException("Debe definir al menos un estado final (de aceptación) antes de evaluar cadenas.");
        }
        Estado estadoInicial = automata.getEstadoInicial().get();

        // Paso 3: Aplicar recorte de espacios en blanco en los extremos (trim)
        String cadenaProcesada = entrada.trim();

        // Paso 4: Validar pertenencia estricta de todos los caracteres al alfabeto Σ
        if (automata.getAlfabeto() != null) {
            automata.getAlfabeto().validarCadena(cadenaProcesada);
        }

        // Paso 5: Caso especial - Cadena vacía (λ)
        if (cadenaProcesada.isEmpty()) {
            boolean aceptada = estadoInicial.esAceptacion();
            CaminoSimulacion caminoLambda = new CaminoSimulacion(
                    estadoInicial,
                    Collections.emptyList(),
                    estadoInicial,
                    true,
                    false
            );
            return new ResultadoSimulacion(
                    entrada,
                    cadenaProcesada,
                    aceptada,
                    List.of(caminoLambda)
            );
        }

        // Paso 6: Caso general - Exploración de ramas y caminos de transición
        List<CaminoSimulacion> caminosExplorados = explorarCaminos(automata, estadoInicial, cadenaProcesada);

        // Paso 7: Priorizar caminos para que los aceptados aparezcan de primero (RF-06)
        caminosExplorados.sort((c1, c2) -> {
            if (c1.esExitoso() != c2.esExitoso()) {
                return c1.esExitoso() ? -1 : 1;
            }
            return Boolean.compare(c1.esCaminoMuerto(), c2.esCaminoMuerto());
        });

        // Paso 8: Determinación del veredicto formal
        boolean aceptada = caminosExplorados.stream().anyMatch(CaminoSimulacion::esExitoso);

        return new ResultadoSimulacion(entrada, cadenaProcesada, aceptada, caminosExplorados);
    }

    // =========================================================================
    // 2. MOTOR DE EXPLORACIÓN DE CAMINOS Y RAMAS
    // =========================================================================

    /**
     * Explora todas las ramas posibles de ejecución generadas por el consumo de la palabra en el grafo.
     *
     * @param automata Autómata formal que contiene el grafo de estados y transiciones.
     * @param estadoInicial Estado donde inicia la evaluación.
     * @param cadena Palabra formal a consumir carácter por carácter.
     * @return Lista de todos los caminos finalizados (exitosos, rechazados o muertos).
     */
    private List<CaminoSimulacion> explorarCaminos(Automata automata, Estado estadoInicial, String cadena) {
        List<CaminoSimulacion> caminosFinalizados = new ArrayList<>();
        Queue<NodoRama> colaExploracion = new ArrayDeque<>();

        // Iniciar con la rama raíz en el estado inicial y posición 0
        colaExploracion.add(new NodoRama(estadoInicial, 0, new ArrayList<>()));

        while (!colaExploracion.isEmpty()) {
            NodoRama ramaActual = colaExploracion.poll();

            // Si la rama consumió toda la palabra
            if (ramaActual.indiceSimbolo == cadena.length()) {
                CaminoSimulacion camino = new CaminoSimulacion(
                        estadoInicial,
                        ramaActual.pasos,
                        ramaActual.estadoActual,
                        true,
                        false
                );
                caminosFinalizados.add(camino);
                continue;
            }

            // Consumir el siguiente símbolo de la palabra
            char simbolo = cadena.charAt(ramaActual.indiceSimbolo);
            Set<Estado> destinos = automata.obtenerDestinos(ramaActual.estadoActual, simbolo);

            // Si no existen transiciones para el símbolo actual, la rama muere (camino muerto)
            if (destinos.isEmpty()) {
                CaminoSimulacion caminoMuerto = new CaminoSimulacion(
                        estadoInicial,
                        ramaActual.pasos,
                        ramaActual.estadoActual,
                        false,
                        true
                );
                caminosFinalizados.add(caminoMuerto);
                continue;
            }

            // Bifurcar la ejecución hacia cada estado destino posible
            for (Estado destino : destinos) {
                List<PasoTransicion> nuevosPasos = new ArrayList<>(ramaActual.pasos);
                nuevosPasos.add(new PasoTransicion(ramaActual.estadoActual, simbolo, destino));

                colaExploracion.add(new NodoRama(
                        destino,
                        ramaActual.indiceSimbolo + 1,
                        nuevosPasos
                ));
            }
        }

        return caminosFinalizados;
    }

    // =========================================================================
    // 3. ESTRUCTURA INTERNA AUXILIAR DE EXPLORACIÓN
    // =========================================================================

    /**
     * Nodo de estado temporal para el seguimiento de una rama activa en la cola de simulación.
     */
    private static final class NodoRama {
        private final Estado estadoActual;
        private final int indiceSimbolo;
        private final List<PasoTransicion> pasos;

        private NodoRama(Estado estadoActual, int indiceSimbolo, List<PasoTransicion> pasos) {
            this.estadoActual = estadoActual;
            this.indiceSimbolo = indiceSimbolo;
            this.pasos = pasos;
        }
    }
    // NOTA DIDÁCTICA SOBRE NodoRama:
    // Funciona como una "ficha de seguimiento" para cada bifurcación del autómata:
    // 1. estadoActual: en qué nodo está parada esta rama en este momento.
    // 2. indiceSimbolo: qué posición de la palabra le toca leer a continuación.
    // 3. pasos: el historial de transiciones que recorrió para llegar hasta aquí (trazabilidad visual).
    // Al usarlo con una cola (Queue), exploramos todas las ramas posibles de forma iterativa y segura.
}
