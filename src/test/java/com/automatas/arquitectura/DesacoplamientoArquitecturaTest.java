package com.automatas.arquitectura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Pruebas de Arquitectura y Desacoplamiento (Constitución Principio 3)")
class DesacoplamientoArquitecturaTest {

    private static final Pattern PATRON_IMPORT_JAVAFX = Pattern.compile("^\\s*import\\s+javafx\\..*");
    private static final Pattern PATRON_REFERENCIA_JAVAFX = Pattern.compile(".*\\bjavafx\\.[a-zA-Z0-9_.]+\\b.*");

    @Test
    @DisplayName("El paquete 'com.automatas.model' debe existir y ninguna clase debe importar o referenciar 'javafx.*'")
    void testPaqueteModelNoImportaJavaFX() throws IOException {
        Path rutaModel = Paths.get("src", "main", "java", "com", "automatas", "model");

        assertTrue(Files.exists(rutaModel), 
                "El paquete 'com.automatas.model' debe existir en 'src/main/java/com/automatas/model'");
        assertTrue(Files.isDirectory(rutaModel), 
                "La ruta 'src/main/java/com/automatas/model' debe ser un directorio");

        List<String> violaciones = new ArrayList<>();

        try (Stream<Path> archivos = Files.walk(rutaModel)) {
            archivos.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(archivoJava -> {
                        try {
                            List<String> lineas = Files.readAllLines(archivoJava);
                            for (int i = 0; i < lineas.size(); i++) {
                                String linea = lineas.get(i);
                                if (PATRON_IMPORT_JAVAFX.matcher(linea).matches() || PATRON_REFERENCIA_JAVAFX.matcher(linea).matches()) {
                                    violaciones.add(String.format("Archivo: %s (Línea %d): %s",
                                            archivoJava.getFileName(), i + 1, linea.trim()));
                                }
                            }
                        } catch (IOException e) {
                            fail("Error al leer el archivo fuente: " + archivoJava, e);
                        }
                    });
        }

        if (!violaciones.isEmpty()) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("\n[VIOLACIÓN DE LA CONSTITUCIÓN - PRINCIPIO 3]\n");
            mensaje.append("El paquete 'model' contiene la lógica matemática formal y tiene estrictamente prohibido importar o referenciar paquetes de JavaFX ('javafx.*'):\n");
            for (String v : violaciones) {
                mensaje.append("  - ").append(v).append("\n");
            }
            fail(mensaje.toString());
        }
    }
}
