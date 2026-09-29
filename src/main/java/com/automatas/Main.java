package com.automatas;

import com.automatas.controller.ControladorPrincipal;
import com.automatas.view.VistaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Punto de entrada principal de la aplicación de diseño, simulación y conversión de autómatas finitos (Tarea 4.3).
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Autómatas (AFD / AFN) — Aplicación de Diseño y Simulación");

        // 1. Instanciar Vista y Controlador Principal (inicia en 0, sin autómata predeterminado)
        VistaPrincipal vistaPrincipal = new VistaPrincipal();
        ControladorPrincipal controladorPrincipal = new ControladorPrincipal(vistaPrincipal);

        // 2. Configurar escena con estilos institucionales
        Scene scene = new Scene(vistaPrincipal, 1100, 720);
        URL cssUrl = getClass().getResource("/css/estilos.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
