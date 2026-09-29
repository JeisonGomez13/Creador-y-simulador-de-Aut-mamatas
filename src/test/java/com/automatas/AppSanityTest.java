package com.automatas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas de inicialización y sanidad del proyecto")
class AppSanityTest {

    @Test
    @DisplayName("La clase principal Main debe existir en el paquete raíz com.automatas")
    void testClasePrincipalExiste() {
        assertNotNull(Main.class, "La clase Main debe estar presente en el proyecto");
    }

    @Test
    @DisplayName("El entorno de ejecución debe soportar la especificación de Java 17 o superior")
    void testVersionJavaValida() {
        String version = System.getProperty("java.version");
        assertNotNull(version, "La propiedad java.version no debe ser nula");
        int majorVersion = Runtime.version().feature();
        assertTrue(majorVersion >= 17, "La versión de Java debe ser al menos 17. Detectada: " + majorVersion);
    }
}
