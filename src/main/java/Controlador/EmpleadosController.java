package Controlador;

import Modelo.Empleado;
import Modelo.EmpleadoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class EmpleadosController {

    @FXML private TextField txtId, txtBuscar, txtUsername, txtCorreo;
    @FXML private PasswordField txtContrasenia;
    @FXML private ComboBox<String> cmbRol;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colUsername, colCorreo, colRol;
    @FXML private Button btnNuevo, btnAgregar, btnEditar, btnEliminar, btnLimpiar, btnVolver, btnBuscar;
    @FXML private Label lblErrorUsername, lblErrorCorreo, lblErrorContrasenia;


    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final ObservableList<Empleado> listaObservableEmpleados = FXCollections.observableArrayList();

    // Validaciones de los campos de texto para limitar la cantidad de caracteres que se pueden ingresar.
    private static final int MAX_USERNAME = 30;
    private static final int MAX_PASSWORD = 8;
    private static final int MAX_CORREO = 30;

    // Modo de trabajo del formulario: hasta que el usuario elige "Nuevo" o "Editar"
    // el formulario permanece bloqueado y no se puede guardar nada.
    private enum Modo { NINGUNO, NUEVO, EDITAR }
    private Modo modoActual = Modo.NINGUNO;
    private Empleado empleadoEnEdicion;


    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        colId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("E-%03d", item));
            }
        });

        if (cmbRol != null) {
            cmbRol.setItems(FXCollections.observableArrayList("Administrador", "Encargado"));
        }

        mostrarEmpleadosEnTabla();

        // Validaciones de los campos de texto para limitar la cantidad de caracteres que se pueden ingresar.
        limitarCampo(txtUsername, lblErrorUsername, MAX_USERNAME, "[\\p{L} ]*", "Máximo 30 caracteres.", "Solo se permiten letras.");
        limitarCampo(txtCorreo, lblErrorCorreo, MAX_CORREO, "[A-Za-z0-9@._-]*", "Máximo 30 caracteres.", "Caracteres no válidos en el correo.");
        limitarCampoContrasenia(txtContrasenia, lblErrorContrasenia, MAX_PASSWORD, "Máximo 8 caracteres.");

        // El formulario inicia bloqueado hasta que se elija "Nuevo" o "Editar"
        establecerModoNinguno();
    }

    private void mostrarEmpleadosEnTabla() {
        listaObservableEmpleados.clear();
        List<Empleado> deBD = empleadoDAO.obtenerTodosLosEmpleados();
        if (deBD != null) listaObservableEmpleados.addAll(deBD);
        tblEmpleados.setItems(listaObservableEmpleados);
    }

    @FXML
    void ejecutarBusqueda(ActionEvent event) {
        String texto = txtBuscar.getText().trim();
        listaObservableEmpleados.clear();
        listaObservableEmpleados.addAll(texto.isEmpty()
                ? empleadoDAO.obtenerTodosLosEmpleados()
                : empleadoDAO.buscarEmpleados(texto));
    }

    // Habilita el formulario en blanco para capturar un empleado nuevo.
    @FXML
    void nuevoRegistro(ActionEvent event) {
        modoActual = Modo.NUEVO;
        empleadoEnEdicion = null;
        tblEmpleados.getSelectionModel().clearSelection();
        limpiarCamposTexto();
        habilitarCampos(true);
        txtUsername.requestFocus();
    }

    // Toma el empleado seleccionado en la tabla y habilita el formulario para editarlo.
    @FXML
    void prepararEdicion(ActionEvent event) {
        Empleado seleccionado = tblEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un empleado de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        modoActual = Modo.EDITAR;
        empleadoEnEdicion = seleccionado;
        txtId.setText(String.format("E-%03d", seleccionado.getIdUsuario()));
        txtUsername.setText(seleccionado.getUsername());
        txtCorreo.setText(seleccionado.getCorreo());
        txtContrasenia.clear();
        cmbRol.setValue(seleccionado.getRol());
        habilitarCampos(true);
        txtUsername.requestFocus();
    }

    // Botón único de Guardar: decide si registra un empleado nuevo o actualiza el seleccionado
    // según el modo de trabajo activo (Nuevo Registro / Editar).
    @FXML
    void guardar(ActionEvent event) {
        switch (modoActual) {
            case NUEVO -> registrarEmpleado();
            case EDITAR -> actualizarEmpleado();
            default -> mostrarAlerta("Selecciona una acción",
                    "Antes de guardar, presiona \"Nuevo Empleado\" para agregar uno o selecciona uno de la tabla y presiona \"Editar\".",
                    Alert.AlertType.WARNING);
        }
    }

    private void registrarEmpleado() {
        if (validarCamposVacios(true)) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }
        // Validación de formato de correo
        if (!validarFormatoCorreo(txtCorreo.getText().trim())) {
            mostrarAlerta("Correo inválido", "Ingresa un correo válido, por ejemplo: nombre@gmail.com", Alert.AlertType.WARNING);
            return;
        }

        Empleado nuevo = new Empleado(
                0,
                txtUsername.getText().trim(),
                txtCorreo.getText().trim(),
                txtContrasenia.getText().trim(),
                cmbRol.getValue()
        );

        if (empleadoDAO.registrarEmpleado(nuevo)) {
            mostrarAlerta("Éxito", "Empleado agregado con éxito.", Alert.AlertType.INFORMATION);
            mostrarEmpleadosEnTabla();
            establecerModoNinguno();
        } else {
            mostrarAlerta("Error", "No se pudo guardar el registro.", Alert.AlertType.ERROR);
        }
    }

    private void actualizarEmpleado() {
        if (empleadoEnEdicion == null) {
            mostrarAlerta("Atención", "Selecciona un empleado de la tabla y presiona \"Editar\".", Alert.AlertType.WARNING);
            return;
        }
        if (validarCamposVacios(false)) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }
        if (!validarFormatoCorreo(txtCorreo.getText().trim())) {
            mostrarAlerta("Correo inválido", "Ingresa un correo válido, por ejemplo: nombre@gmail.com", Alert.AlertType.WARNING);
            return;
        }

        String nuevaContrasenia = txtContrasenia.getText().trim();

        Empleado empleadoModificado = new Empleado(
                empleadoEnEdicion.getIdUsuario(),
                txtUsername.getText().trim(),
                txtCorreo.getText().trim(),
                nuevaContrasenia,
                cmbRol.getValue()
        );

        boolean actualizado = nuevaContrasenia.isEmpty()
                ? empleadoDAO.modificarEmpleadoSinContrasenia(empleadoModificado)
                : empleadoDAO.modificarEmpleado(empleadoModificado);

        if (actualizado) {
            mostrarAlerta("Éxito", "Empleado editado con éxito.", Alert.AlertType.INFORMATION);
            mostrarEmpleadosEnTabla();
            establecerModoNinguno();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void eliminarEmpleado(ActionEvent event) {
        Empleado seleccionado = tblEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un empleado para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        if (empleadoDAO.tieneMovimientosRegistrados(seleccionado.getIdUsuario())) {
            mostrarAlerta("No se puede eliminar", "No se puede eliminar este usuario porque tiene movimientos a su nombre.", Alert.AlertType.WARNING);
            return;
        }

        ButtonType btnAceptar = new ButtonType("Aceptar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que quieres eliminar al empleado \"" + seleccionado.getUsername() + "\"?");
        confirmacion.getButtonTypes().setAll(btnAceptar, btnCancelar);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == btnAceptar) {
                if (empleadoDAO.eliminarEmpleado(seleccionado.getIdUsuario())) {
                    mostrarAlerta("Éxito", "Empleado eliminado correctamente.", Alert.AlertType.INFORMATION);
                    mostrarEmpleadosEnTabla();
                    establecerModoNinguno();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el registro.", Alert.AlertType.ERROR);
                }
            }
            // Si se presiona Cancelar, no se hace nada y la operación queda anulada
        });
    }

    @FXML
    void buscarEmpleadoRealTime(KeyEvent event) {
        String texto = txtBuscar.getText().trim();
        listaObservableEmpleados.clear();
        listaObservableEmpleados.addAll(texto.isEmpty()
                ? empleadoDAO.obtenerTodosLosEmpleados()
                : empleadoDAO.buscarEmpleados(texto));
    }

    @FXML
    void limpiarFormulario() {
        establecerModoNinguno();
    }

    @FXML
    void volverAlMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Main-View.fxml"));
            Stage stage = (Stage) btnVolver.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Panel Principal");
            stage.setResizable(true);
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al regresar: " + e.getMessage());
        }
    }

    private boolean validarCamposVacios(boolean requerirContrasenia) {
        boolean base = txtUsername.getText().trim().isEmpty() ||
                txtCorreo.getText().trim().isEmpty() ||
                cmbRol.getValue() == null;
        return requerirContrasenia ? (base || txtContrasenia.getText().trim().isEmpty()) : base;
    }

    // Regresa el formulario a su estado bloqueado: sin modo activo, campos deshabilitados y vacíos.
    private void establecerModoNinguno() {
        modoActual = Modo.NINGUNO;
        empleadoEnEdicion = null;
        limpiarCamposTexto();
        habilitarCampos(false);
        tblEmpleados.getSelectionModel().clearSelection();
    }

    private void limpiarCamposTexto() {
        txtId.clear();
        txtUsername.clear();
        txtCorreo.clear();
        txtContrasenia.clear();
        cmbRol.setValue(null);
    }

    // Habilita o bloquea visualmente los campos del formulario según haya un modo de trabajo activo
    private void habilitarCampos(boolean activo) {
        txtUsername.setDisable(!activo);
        txtCorreo.setDisable(!activo);
        txtContrasenia.setDisable(!activo);
        cmbRol.setDisable(!activo);

        aplicarEstiloCampo(txtUsername, activo);
        aplicarEstiloCampo(txtCorreo, activo);
    }

    private void aplicarEstiloCampo(TextField campo, boolean activo) {
        campo.getStyleClass().remove("input-field-active");
        campo.getStyleClass().remove("input-field-disabled");
        campo.getStyleClass().add(activo ? "input-field-active" : "input-field-disabled");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void limitarCampo(TextField campo, Label lblError, int maxCaracteres,
                              String regexPermitido, String mensajeLongitud, String mensajeFormato) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > maxCaracteres) {
                campo.setText(oldValue);
                mostrarErrorCampo(campo, lblError, mensajeLongitud);
                return;
            }
            if (!newValue.isEmpty() && !newValue.matches(regexPermitido)) {
                campo.setText(oldValue);
                mostrarErrorCampo(campo, lblError, mensajeFormato);
                return;
            }
            ocultarErrorCampo(campo, lblError);
        });
    }

    // Igual que limitarCampo, pero para PasswordField (no puede compartir tipo TextField porque
    // aunque PasswordField hereda de TextField, aquí lo separamos para dejarlo más claro)
    private void limitarCampoContrasenia(PasswordField campo, Label lblError, int maxCaracteres, String mensaje) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > maxCaracteres) {
                campo.setText(oldValue);
                mostrarErrorCampo(campo, lblError, mensaje);
            } else {
                ocultarErrorCampo(campo, lblError);
            }
        });
    }

    private void mostrarErrorCampo(TextField campo, Label lblError, String mensaje) {
        if (!campo.getStyleClass().contains("input-field-active-error")) {
            campo.getStyleClass().add("input-field-active-error");
        }
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarErrorCampo(TextField campo, Label lblError) {
        campo.getStyleClass().remove("input-field-active-error");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private boolean validarFormatoCorreo(String correo) {
        return correo.matches("^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

}