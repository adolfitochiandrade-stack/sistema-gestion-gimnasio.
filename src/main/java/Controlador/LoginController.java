package Controlador;

import Modelo.SesionUsuario;
import Modelo.User;
import Modelo.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;


import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasenia;
    @FXML private Button btnIngresar;
    @FXML private Label lblErrorUsuario;
    @FXML private Label lblErrorContrasenia;

    //Validaciones del login de los el usuario y la contraseña, para que no se exceda el límite de caracteres.
    private static final int MAX_USUARIO = 30;
    private static final int MAX_PASSWORD = 8;

    private final UserDAO userDAO = new UserDAO();

    //metodo para inicializar los campos de texto y limitar la cantidad de caracteres que se pueden ingresar en ellos.
    @FXML
    public void initialize() {
        limitarCampo(txtUsuario, lblErrorUsuario, MAX_USUARIO, "Máximo " + MAX_USUARIO + " caracteres.");
        limitarCampo(txtContrasenia, lblErrorContrasenia, MAX_PASSWORD, "Máximo " + MAX_PASSWORD + " caracteres.");
    }

    private void limitarCampo(TextField campo, Label lblError, int maxCaracteres, String mensaje) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > maxCaracteres) {
                campo.setText(oldValue); // no deja escribir más
                mostrarErrorCampo(campo, lblError, mensaje);
            } else {
                ocultarErrorCampo(campo, lblError);
            }
        });
    }

    private void mostrarErrorCampo(TextField campo, Label lblError, String mensaje) {
        if (!campo.getStyleClass().contains("custom-field-error")) {
            campo.getStyleClass().add("custom-field-error");
        }
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarErrorCampo(TextField campo, Label lblError) {
        campo.getStyleClass().remove("custom-field-error");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    @FXML
    void handleIngresar(ActionEvent event) {
        String identificador = txtUsuario.getText().trim();
        String password = txtContrasenia.getText().trim();

        if (identificador.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor, introduce tu usuario/correo y contraseña.");
            return;
        }

        // Ejecución de la consulta inteligente en el DAO
        User usuarioValido = userDAO.login(identificador, password);

        if (usuarioValido != null) {
            SesionUsuario.getInstancia().login(usuarioValido);
            System.out.println("Sesión iniciada con éxito. Bienvenido: " + usuarioValido.getUsername());
            abrirMenuPrincipal();
        } else {
            mostrarAlerta("No pudiste ingresar", "El usuario/correo o la contraseña son incorrectos.");
        }
    }

    private void abrirMenuPrincipal() {
        try {
            // Cargamos la vista del Menú Principal desde el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/Main-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnIngresar.getScene().getWindow();

            // Reemplazamos los controles viejos por los del Menú Principal sin abrir ventanas dobles
            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Panel Principal");
            stage.setResizable(true); // El menú se puede estirar si el usuario lo desea
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error crítico al procesar el salto al Menú Principal: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}