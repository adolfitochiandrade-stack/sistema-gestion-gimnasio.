package App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        Application.launch(JavaFXApp.class, args);
    }

    // 2. Clase interna que se encarga del ciclo de vida gráfico de JavaFX
    public static class JavaFXApp extends Application {
        @Override
        public void start(Stage stage) throws IOException {
            // Carga tu vista del Login de forma correcta
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/Vista/Login-View.fxml"));

            // Asigna la escena y define el título de tu sistema
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("GymCentral - Iniciar Sesión");
            stage.setScene(scene);
            stage.setResizable(false); // Mantiene el diseño del login fijo y estético
            stage.show();
        }
    }
}