package Controlador;

import Modelo.Cliente;
import Modelo.ClienteDAO;
import Modelo.SesionUsuario;
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
import java.time.LocalDate;
import java.util.List;

public class ClientesController {

    @FXML private TextField txtId, txtBuscar, txtNombre, txtTelefono, txtCorreo;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre, colTelefono, colCorreo, colVencimiento, colRegistradoPor;
    @FXML private Button btnNuevo, btnAgregar, btnEditar, btnEliminar, btnLimpiar, btnVolver, btnBuscar;
    @FXML private Label lblErrorNombre, lblErrorTelefono, lblErrorCorreo;


    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ObservableList<Cliente> listaObservableClientes = FXCollections.observableArrayList();
    //validaciones de campos
    private static final int MAX_NOMBRE = 30;
    private static final int MAX_TELEFONO = 10;
    private static final int MAX_CORREO = 30;

    // Modo de trabajo del formulario: hasta que el usuario elige "Nuevo" o "Editar"
    // el formulario permanece bloqueado y no se puede guardar nada.
    private enum Modo { NINGUNO, NUEVO, EDITAR }
    private Modo modoActual = Modo.NINGUNO;
    private Cliente clienteEnEdicion;


    // Configura las columnas de la tabla y carga los datos iniciales
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));

        if (colRegistradoPor != null) {
            colRegistradoPor.setCellValueFactory(new PropertyValueFactory<>("registradoPor"));
        }

        // Colorea la fecha de vencimiento: rojo si ya venció, verde si sigue vigente, gris si nunca ha pagado
        colVencimiento.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if ("Sin pagos".equals(item)) {
                    setStyle("-fx-text-fill: #7F8C8D;");
                } else {
                    try {
                        LocalDate vencimiento = LocalDate.parse(item);
                        if (vencimiento.isBefore(LocalDate.now())) {
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        colId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("C-%03d", item));
                }
            }
        });

        mostrarClientesEnTabla();

        //VALIDACIONES DE TIPOS DE DATOS
        limitarCampo(txtNombre, lblErrorNombre, MAX_NOMBRE, "[\\p{L} ]*", "Máximo 30 caracteres.", "Solo se permiten letras.");
        limitarCampo(txtTelefono, lblErrorTelefono, MAX_TELEFONO, "[0-9]*", "Máximo 10 dígitos.", "Solo se permiten números.");
        limitarCampo(txtCorreo, lblErrorCorreo, MAX_CORREO, "[A-Za-z0-9@._-]*", "Máximo 30 caracteres.", "Caracteres no válidos en el correo.");


        //Roles de usuario
        if (!SesionUsuario.getInstancia().esAdministrador()) {
            btnEditar.setDisable(true);
            btnEliminar.setDisable(true);
        }

        // El formulario inicia bloqueado hasta que se elija "Nuevo" o "Editar"
        establecerModoNinguno();
    }



    // Refresca la tabla obteniendo la información actual desde el DAO
    private void mostrarClientesEnTabla() {
        listaObservableClientes.clear();
        List<Cliente> deBD = clienteDAO.obtenerTodosLosClientes();
        if (deBD != null) {
            listaObservableClientes.addAll(deBD);
        }
        tblClientes.setItems(listaObservableClientes);
    }

    // Filtra la lista de clientes según el texto ingresado en el buscador
    @FXML
    void ejecutarBusqueda(ActionEvent event) {
        String texto = txtBuscar.getText().trim();
        listaObservableClientes.clear();
        if (texto.isEmpty()) {
            listaObservableClientes.addAll(clienteDAO.obtenerTodosLosClientes());
        } else {
            listaObservableClientes.addAll(clienteDAO.buscarClientes(texto));
        }
    }

    // Habilita el formulario en blanco para capturar un cliente nuevo.
    @FXML
    void nuevoRegistro(ActionEvent event) {
        modoActual = Modo.NUEVO;
        clienteEnEdicion = null;
        tblClientes.getSelectionModel().clearSelection();
        limpiarCamposTexto();
        habilitarCampos(true);
        txtNombre.requestFocus();
    }

    // Toma el cliente seleccionado en la tabla y habilita el formulario para editarlo.
    @FXML
    void prepararEdicion(ActionEvent event) {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un cliente de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        modoActual = Modo.EDITAR;
        clienteEnEdicion = seleccionado;
        txtId.setText(String.format("C-%03d", seleccionado.getIdCliente()));
        txtNombre.setText(seleccionado.getNombre());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());
        habilitarCampos(true);
        txtNombre.requestFocus();
    }

    // Botón único de Guardar: decide si registra un cliente nuevo o actualiza el seleccionado
    // según el modo de trabajo activo (Nuevo Registro / Editar).
    @FXML
    void guardar(ActionEvent event) {
        switch (modoActual) {
            case NUEVO -> registrarCliente();
            case EDITAR -> actualizarCliente();
            default -> mostrarAlerta("Selecciona una acción",
                    "Antes de guardar, presiona \"Nuevo Registro\" para agregar un cliente o selecciona uno de la tabla y presiona \"Editar\".",
                    Alert.AlertType.WARNING);
        }
    }

    // Valida los campos y envía un nuevo cliente al DAO para guardarlo
    private void registrarCliente() {
        if (validarCamposVacios()) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }
        //VALIDAR CORREO

        if (!validarFormatoCorreo(txtCorreo.getText().trim())) {
            mostrarAlerta("Correo inválido", "Ingresa un correo válido, por ejemplo: nombre@gmail.com", Alert.AlertType.WARNING);
            return;
        }


        int idUsuarioActual = SesionUsuario.getInstancia().getIdUsuarioLogueado();
        if (idUsuarioActual == 0) {
            mostrarAlerta("Error", "Sesión no encontrada", Alert.AlertType.ERROR);
            return;
        }

        Cliente nuevo = new Cliente(
                0,
                txtNombre.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                idUsuarioActual,
                ""
        );

        if (clienteDAO.registrarCliente(nuevo)) {
            mostrarAlerta("Éxito", "Cliente guardado con éxito.", Alert.AlertType.INFORMATION);
            mostrarClientesEnTabla();
            establecerModoNinguno();
        } else {
            mostrarAlerta("Error", "No se pudo guardar el registro.", Alert.AlertType.ERROR);
        }
    }

    // Actualiza los datos del cliente que se dejó preparado con el botón Editar
    private void actualizarCliente() {
        if (clienteEnEdicion == null) {
            mostrarAlerta("Atención", "Selecciona un cliente de la tabla y presiona \"Editar\".", Alert.AlertType.WARNING);
            return;
        }

        //VALIDAR CAMPOS
        if (validarCamposVacios()) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        //VALIDAR CORREO
        if (!validarFormatoCorreo(txtCorreo.getText().trim())) {
            mostrarAlerta("Correo inválido", "Ingresa un correo válido, por ejemplo: nombre@gmail.com", Alert.AlertType.WARNING);
            return;
        }


        int idParaActualizar = clienteEnEdicion.getIdCliente();
        int idUsuarioActual = (SesionUsuario.getInstancia().getUsuarioActivo() != null)
                ? SesionUsuario.getInstancia().getUsuarioActivo().getIdUsuario() : 2;

        Cliente clienteModificado = new Cliente(
                idParaActualizar,
                txtNombre.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                idUsuarioActual,
                ""
        );

        if (clienteDAO.modificarCliente(clienteModificado)) {
            mostrarAlerta("Éxito", "Cliente editado con éxito.", Alert.AlertType.INFORMATION);
            mostrarClientesEnTabla();
            establecerModoNinguno();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar.", Alert.AlertType.ERROR);
        }
    }

    // Elimina el cliente seleccionado en la tabla tras confirmación
    @FXML
    void eliminarCliente(ActionEvent event) {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un cliente para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        if (clienteDAO.tienePagosRegistrados(seleccionado.getIdCliente())) {
            mostrarAlerta("No se puede eliminar", "No puedes eliminar este cliente porque tiene pagos registrados.", Alert.AlertType.WARNING);
            return;
        }

        ButtonType btnAceptar = new ButtonType("Aceptar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que quieres eliminar al cliente \"" + seleccionado.getNombre() + "\"?");
        confirmacion.getButtonTypes().setAll(btnAceptar, btnCancelar);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == btnAceptar) {
                if (clienteDAO.eliminarCliente(seleccionado.getIdCliente())) {
                    mostrarAlerta("Éxito", "Cliente eliminado correctamente.", Alert.AlertType.INFORMATION);
                    mostrarClientesEnTabla();
                    establecerModoNinguno();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el registro.", Alert.AlertType.ERROR);
                }
            }
            // Si se presiona Cancelar, no se hace nada y la operación queda anulada
        });
    }


    // Permite la búsqueda automática mientras el usuario escribe en el campo
    @FXML
    void buscarClienteRealTime(KeyEvent event) {
        String texto = txtBuscar.getText().trim();
        listaObservableClientes.clear();
        if (texto.isEmpty()) {
            listaObservableClientes.addAll(clienteDAO.obtenerTodosLosClientes());
        } else {
            listaObservableClientes.addAll(clienteDAO.buscarClientes(texto));
        }
    }

    // Vacía todos los campos del formulario, quita cualquier selección y regresa al modo bloqueado
    @FXML
    void limpiarFormulario() {
        establecerModoNinguno();
    }

    // Cambia de escena a la ventana principal del sistema
    @FXML
    void volverAlMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Main-View.fxml"));
            Stage stage = (Stage) btnVolver.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Panel Principal");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al regresar: " + e.getMessage());
        }
    }

    // Verifica que ningún campo obligatorio esté vacío antes de procesar
    private boolean validarCamposVacios() {
        return txtNombre.getText().trim().isEmpty() ||
                txtTelefono.getText().trim().isEmpty() ||
                txtCorreo.getText().trim().isEmpty();
    }

    // Regresa el formulario a su estado bloqueado: sin modo activo, campos deshabilitados y vacíos.
    private void establecerModoNinguno() {
        modoActual = Modo.NINGUNO;
        clienteEnEdicion = null;
        limpiarCamposTexto();
        habilitarCampos(false);
        tblClientes.getSelectionModel().clearSelection();
    }

    // Limpia únicamente el contenido de los campos de texto del formulario
    private void limpiarCamposTexto() {
        txtId.clear();
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
    }

    // Habilita o bloquea visualmente los campos del formulario según haya un modo de trabajo activo
    private void habilitarCampos(boolean activo) {
        txtNombre.setDisable(!activo);
        txtTelefono.setDisable(!activo);
        txtCorreo.setDisable(!activo);

        aplicarEstiloCampo(txtNombre, activo);
        aplicarEstiloCampo(txtTelefono, activo);
        aplicarEstiloCampo(txtCorreo, activo);
    }

    private void aplicarEstiloCampo(TextField campo, boolean activo) {
        campo.getStyleClass().remove("input-field-active");
        campo.getStyleClass().remove("input-field-disabled");
        campo.getStyleClass().add(activo ? "input-field-active" : "input-field-disabled");
    }

    // Muestra ventanas de mensaje de sistema al usuario
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Filtra la entrada del usuario en tiempo real, limitando caracteres y validando formato
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

    // Valida que el correo tenga formato usuario@dominio.extensión (arroba + punto + al menos 2 letras)
    private boolean validarFormatoCorreo(String correo) {
        return correo.matches("^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }


}