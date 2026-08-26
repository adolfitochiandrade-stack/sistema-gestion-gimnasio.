package Controlador;

import Modelo.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PagosController {

    @FXML private TextField txtBuscar, txtMonto, txtIdCliente;
    @FXML private DatePicker dpFechaPago, dpFechaVencimiento;
    @FXML private TableView<Pago> tblPagos;
    @FXML private TableColumn<Pago, Integer> colId, colIdCliente;
    @FXML private TableColumn<Pago, Double> colMonto;
    @FXML private TableColumn<Pago, String> colFechaPago, colFechaVencimiento, colRegistradoPor;
    @FXML private Button btnNuevo, btnAgregar, btnEditar, btnEliminar, btnLimpiar, btnVolver, btnBuscar;
    @FXML private TextField txtBuscarCliente;
    @FXML private ListView<Cliente> lvResultadosCliente;
    @FXML private Label lblErrorMonto;
    @FXML private Label lblErrorFechaPago, lblErrorFechaVencimiento;
    @FXML private Label lblErrorBuscarCliente;
    @FXML private Label lblErrorIdCliente;




    private final PagoDAO pagoDAO = new PagoDAO();
    private final ObservableList<Pago> listaObservablePagos = FXCollections.observableArrayList();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private Cliente clienteSeleccionado;

    // Validaciones de campos: máximo de caracteres y formato
    private static final int MAX_MONTO = 9;
    private static final int MAX_BUSCAR_CLIENTE = 30;
    private static final int MAX_ID_CLIENTE = 5;



    // Lista maestra (completa) de clientes y lista filtrada para el buscador del ComboBox
    private final ObservableList<Cliente> listaMaestraClientes = FXCollections.observableArrayList();
    private FilteredList<Cliente> clientesFiltrados;

    // Modo de trabajo del formulario: hasta que el usuario elige "Nuevo" o "Editar"
    // el formulario permanece bloqueado y no se puede guardar nada.
    private enum Modo { NINGUNO, NUEVO, EDITAR }
    private Modo modoActual = Modo.NINGUNO;
    private Pago pagoEnEdicion;

    // Formato de fecha con el que se guarda/lee en la base de datos: AAAA/MM/DD (ej. 2026/04/03)
    private static final DateTimeFormatter FORMATO_FECHA_GUARDADO = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    // Formatos aceptados al leer fechas ya existentes (por si estaban guardadas con otro separador)
    private static final DateTimeFormatter[] FORMATOS_LECTURA = {
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    @FXML
    public void initialize() {
        // Apuntamos a las propiedades reales del modelo Pago.java
        colId.setCellValueFactory(new PropertyValueFactory<>("idPago"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));

        // Columnas de texto directo
        colFechaPago.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        colFechaVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));

        if (colRegistradoPor != null)
            colRegistradoPor.setCellValueFactory(new PropertyValueFactory<>("registradoPor"));

        // Formato visual de la columna ID (ej: P-003), igual que en Clientes
        colId.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("P-%03d", item));
            }
        });

        // Formato visual de la columna ID Cliente (ej: C-002), igual que en Clientes
        colIdCliente.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("C-%03d", item));
            }
        });

        // Formato visual de la columna Monto como moneda
        colMonto.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        // Carga de Clientes (tu lógica actual)
        List<Cliente> clientes = clienteDAO.obtenerTodosLosClientes();
        if (clientes != null) listaMaestraClientes.setAll(clientes);

        configurarBuscadorCliente();

        // Formato visual del DatePicker: AAAA/MM/DD
        configurarDatePicker(dpFechaPago, lblErrorFechaPago);
        configurarDatePicker(dpFechaVencimiento, lblErrorFechaVencimiento);

        // Validación del campo Monto: máximo 5 dígitos, solo números
        limitarCampo(txtMonto, lblErrorMonto, MAX_MONTO, "\\$?[0-9]{0,6}(\\.[0-9]{0,2})?", "Máximo 5 dígitos (hasta $99,999).", "Solo se permiten números y el símbolo $.");
        limitarCampo(txtBuscarCliente, lblErrorBuscarCliente, MAX_BUSCAR_CLIENTE, "[\\p{L} ]*", "Máximo 30 caracteres.", "Solo se permiten letras.");
        limitarCampo(txtIdCliente, lblErrorIdCliente, MAX_ID_CLIENTE, "[A-Za-z0-9-]*", "Máximo 5 caracteres.", "Formato de código inválido.");

        mostrarPagosEnTabla();

        //roles de usuario
        if (!SesionUsuario.getInstancia().esAdministrador()) {
            btnEditar.setDisable(true);
            btnEliminar.setDisable(true);
        }

        // El formulario inicia bloqueado hasta que se elija "Nuevo" o "Editar"
        establecerModoNinguno();
    }

    /**
     * Buscador de clientes: mientras el usuario escribe, se filtra la lista maestra
     * y se muestran los resultados en un ListView que aparece debajo. No hay un
     * desplegable fijo: la lista solo se ve cuando hay texto y coincidencias.
     */
    private void configurarBuscadorCliente() {
        lvResultadosCliente.setCellFactory(lv -> new ListCell<Cliente>() {
            @Override protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre() + "  (C-" + String.format("%03d", item.getIdCliente()) + ")");
            }
        });

        txtBuscarCliente.textProperty().addListener((obs, oldValue, newValue) -> {
            // Si el texto es el mismo que el del cliente ya seleccionado, no relanzar la búsqueda
            if (clienteSeleccionado != null && txtBuscarCliente.getText().equals(clienteSeleccionado.getNombre())) {
                return;
            }
            clienteSeleccionado = null;
            txtIdCliente.clear();

            String filtro = newValue == null ? "" : newValue.trim().toLowerCase();
            if (filtro.isEmpty()) {
                lvResultadosCliente.setVisible(false);
                lvResultadosCliente.setManaged(false);
                return;
            }

            List<Cliente> coincidencias = listaMaestraClientes.stream()
                    .filter(c -> c.getNombre().toLowerCase().contains(filtro))
                    .collect(java.util.stream.Collectors.toList());

            lvResultadosCliente.setItems(FXCollections.observableArrayList(coincidencias));
            boolean hayResultados = !coincidencias.isEmpty();
            lvResultadosCliente.setVisible(hayResultados);
            lvResultadosCliente.setManaged(hayResultados);




        });

        // Al hacer clic en un resultado, se selecciona ese cliente
        lvResultadosCliente.setOnMouseClicked(event -> {
            Cliente elegido = lvResultadosCliente.getSelectionModel().getSelectedItem();
            if (elegido != null) {
                seleccionarCliente(elegido);
            }
        });
    }

    private void seleccionarCliente(Cliente cliente) {
        clienteSeleccionado = cliente;
        txtBuscarCliente.setText(cliente.getNombre());
        txtIdCliente.setText(String.format("C-%03d", cliente.getIdCliente()));
        lvResultadosCliente.setVisible(false);
        lvResultadosCliente.setManaged(false);
    }
    /** Hace que el DatePicker muestre y acepte el formato AAAA/MM/DD (ej. 2026/04/03). */
    private void configurarDatePicker(DatePicker datePicker, Label lblError) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : FORMATO_FECHA_GUARDADO.format(fecha);
            }
            @Override
            public LocalDate fromString(String texto) {
                return parsearFecha(texto);
            }

        });
        datePicker.setPromptText("AAAA/MM/DD");

        // Paso 1: mientras se escribe, solo se permiten números y "/", máximo 10 caracteres (AAAA/MM/DD)
        limitarCampo(datePicker.getEditor(), lblError, 10, "[0-9/]*", "Máximo 10 caracteres.", "Solo se permiten números y \"/\".");

        // Paso 2: al salir del campo, se valida que la fecha esté completa y sea real (no acepta 2026/13/45, por ejemplo)
        datePicker.getEditor().focusedProperty().addListener((obs, estabaEnfocado, estaEnfocado) -> {
            if (!estaEnfocado) {
                String texto = datePicker.getEditor().getText();
                if (texto != null && !texto.trim().isEmpty()) {
                    LocalDate fecha = parsearFechaEstricta(texto.trim());
                    if (fecha == null) {
                        mostrarErrorCampo(datePicker.getEditor(), lblError, "Formato inválido. Usa AAAA/MM/DD (ej. 2026/05/12).");
                        datePicker.setValue(null);
                    } else {
                        datePicker.setValue(fecha);
                        ocultarErrorCampo(datePicker.getEditor(), lblError);
                    }
                }
            }
        });
    }

    /** Intenta interpretar una fecha ya sea en formato AAAA/MM/DD o AAAA-MM-DD (compatibilidad con datos antiguos). */
    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        for (DateTimeFormatter formato : FORMATOS_LECTURA) {
            try {
                return LocalDate.parse(texto.trim(), formato);
            } catch (DateTimeParseException ignorado) {
                // probamos con el siguiente formato
            }
        }
        return null;
    }

    //Valida que el texto tenga EXACTAMENTE el formato AAAA/MM/DD y sea una fecha real
    private LocalDate parsearFechaEstricta(String texto) {
        if (!texto.matches("\\d{4}/\\d{2}/\\d{2}")) return null;
        try {
            return LocalDate.parse(texto, FORMATO_FECHA_GUARDADO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void mostrarPagosEnTabla() {
        listaObservablePagos.setAll(pagoDAO.obtenerTodosLosPagos());
        tblPagos.setItems(listaObservablePagos);
    }

    @FXML
    void ejecutarBusqueda(ActionEvent event) {
        listaObservablePagos.setAll(pagoDAO.buscarPagos(txtBuscar.getText().trim()));
    }

    // Habilita el formulario en blanco para capturar un pago nuevo.
    @FXML
    void nuevoRegistro(ActionEvent event) {
        modoActual = Modo.NUEVO;
        pagoEnEdicion = null;
        tblPagos.getSelectionModel().clearSelection();
        limpiarCamposTexto();
        habilitarCampos(true);
        txtBuscarCliente.requestFocus();
    }

    // Toma el pago seleccionado en la tabla y habilita el formulario para editarlo.
    @FXML
    void prepararEdicion(ActionEvent event) {
        Pago seleccionado = tblPagos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un pago de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        modoActual = Modo.EDITAR;
        pagoEnEdicion = seleccionado;

        txtMonto.setText(String.valueOf(seleccionado.getMonto()));
        dpFechaPago.setValue(parsearFecha(seleccionado.getFechaPago()));
        dpFechaVencimiento.setValue(parsearFecha(seleccionado.getFechaVencimiento()));

        listaMaestraClientes.stream()
                .filter(c -> c.getIdCliente() == seleccionado.getIdCliente())
                .findFirst()
                .ifPresent(this::seleccionarCliente);

        habilitarCampos(true);
        txtMonto.requestFocus();
    }

    // Botón único de Guardar: decide si registra un pago nuevo o actualiza el seleccionado
    // según el modo de trabajo activo (Nuevo Registro / Editar).
    @FXML
    void guardar(ActionEvent event) {
        switch (modoActual) {
            case NUEVO -> registrarPago();
            case EDITAR -> actualizarPago();
            default -> mostrarAlerta("Selecciona una acción",
                    "Antes de guardar, presiona \"Nuevo Pago\" para agregar uno o selecciona uno de la tabla y presiona \"Editar\".",
                    Alert.AlertType.WARNING);
        }
    }

    private void registrarPago() {
        if (fechaConFormatoInvalido()) {
            mostrarAlerta("Formato de fecha incorrecto",
                    "Revisa la fecha marcada en rojo. Usa el formato AAAA/MM/DD, por ejemplo: 2026/05/12.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (validarCamposVacios()) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        int idUsuarioActual = SesionUsuario.getInstancia().getIdUsuarioLogueado();
        if (idUsuarioActual == 0) {
            mostrarAlerta("Error", "Sesión no encontrada", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Constructor con 8 parámetros: (id, monto, fPago, fVenc, idCliente, idUsuario, registradoPor, fechaRegistro)
            Pago nuevo = new Pago(0, Double.parseDouble(txtMonto.getText().trim()),
                    FORMATO_FECHA_GUARDADO.format(dpFechaPago.getValue()),
                    FORMATO_FECHA_GUARDADO.format(dpFechaVencimiento.getValue()),
                    clienteSeleccionado.getIdCliente(), idUsuarioActual, "", "");

            if (pagoDAO.registrarPago(nuevo)) {
                mostrarAlerta("Éxito", "Pago guardado con éxito.", Alert.AlertType.INFORMATION);
                mostrarPagosEnTabla();
                establecerModoNinguno();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Formato Erróneo", "Verifica los datos numéricos.", Alert.AlertType.ERROR);
        }
    }

    private void actualizarPago() {
        if (pagoEnEdicion == null) {
            mostrarAlerta("Atención", "Selecciona un pago de la tabla y presiona \"Editar\".", Alert.AlertType.WARNING);
            return;
        }

        if (fechaConFormatoInvalido()) {
            mostrarAlerta("Formato de fecha incorrecto",
                    "Revisa la fecha marcada en rojo. Usa el formato AAAA/MM/DD, por ejemplo: 2026/05/12.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (validarCamposVacios()) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Pago modificado = new Pago(pagoEnEdicion.getIdPago(), Double.parseDouble(txtMonto.getText().trim()),
                    FORMATO_FECHA_GUARDADO.format(dpFechaPago.getValue()),
                    FORMATO_FECHA_GUARDADO.format(dpFechaVencimiento.getValue()),
                    clienteSeleccionado.getIdCliente(), SesionUsuario.getInstancia().getIdUsuarioLogueado(), "", "");

            if (pagoDAO.modificarPago(modificado)) {
                mostrarAlerta("Éxito", "Pago editado con éxito.", Alert.AlertType.INFORMATION);
                mostrarPagosEnTabla();
                establecerModoNinguno();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Datos numéricos inválidos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void eliminarPago(ActionEvent event) {
        Pago seleccionado = tblPagos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un pago para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        ButtonType btnAceptar = new ButtonType("Aceptar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que quieres eliminar el pago?");
        confirmacion.getButtonTypes().setAll(btnAceptar, btnCancelar);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == btnAceptar) {
                if (pagoDAO.eliminarPago(seleccionado.getIdPago())) {
                    mostrarAlerta("Éxito", "Pago eliminado con éxito.", Alert.AlertType.INFORMATION);
                    mostrarPagosEnTabla();
                    establecerModoNinguno();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el pago.", Alert.AlertType.ERROR);
                }
            }
            // Si se presiona Cancelar, no se hace nada y la operación queda anulada
        });
    }

    @FXML
    void buscarPagoRealTime(KeyEvent event) {
        ejecutarBusqueda(null);
    }

    @FXML
    void limpiarFormulario() {
        establecerModoNinguno();
    }

    @FXML
    void volverAlMenu(ActionEvent event) {
        try {
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/Vista/Main-View.fxml"))));
            stage.setResizable(true);
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean validarCamposVacios() {
        return txtMonto.getText().isEmpty() || dpFechaPago.getValue() == null ||
                dpFechaVencimiento.getValue() == null || clienteSeleccionado == null;
    }

    // Detecta si alguna de las fechas quedó marcada en rojo por tener un formato inválido
    // (por ejemplo, el usuario invirtió el orden día/mes/año). En ese caso el valor del
    // DatePicker queda en null igual que si el campo estuviera vacío, así que hay que
    // distinguirlo para no mostrar el mensaje genérico de "Campos Vacíos" cuando en
    // realidad el dato sí se escribió, solo que con un formato incorrecto.
    private boolean fechaConFormatoInvalido() {
        return dpFechaPago.getEditor().getStyleClass().contains("input-field-active-error") ||
                dpFechaVencimiento.getEditor().getStyleClass().contains("input-field-active-error");
    }

    // Regresa el formulario a su estado bloqueado: sin modo activo, campos deshabilitados y vacíos.
    private void establecerModoNinguno() {
        modoActual = Modo.NINGUNO;
        pagoEnEdicion = null;
        limpiarCamposTexto();
        habilitarCampos(false);
        tblPagos.getSelectionModel().clearSelection();
    }

    private void limpiarCamposTexto() {
        txtMonto.clear();
        dpFechaPago.setValue(null);
        dpFechaVencimiento.setValue(null);
        txtIdCliente.clear();
        clienteSeleccionado = null;
        txtBuscarCliente.clear();
        lvResultadosCliente.setVisible(false);
        lvResultadosCliente.setManaged(false);
        // Quita cualquier marca en rojo que haya quedado de un intento anterior con formato inválido
        ocultarErrorCampo(dpFechaPago.getEditor(), lblErrorFechaPago);
        ocultarErrorCampo(dpFechaVencimiento.getEditor(), lblErrorFechaVencimiento);
    }

    // Habilita o bloquea visualmente los campos del formulario según haya un modo de trabajo activo
    private void habilitarCampos(boolean activo) {
        txtBuscarCliente.setDisable(!activo);
        txtMonto.setDisable(!activo);
        dpFechaPago.setDisable(!activo);
        dpFechaVencimiento.setDisable(!activo);

        aplicarEstiloCampo(txtBuscarCliente, activo);
        aplicarEstiloCampo(txtMonto, activo);
    }

    private void aplicarEstiloCampo(TextField campo, boolean activo) {
        campo.getStyleClass().remove("input-field-active");
        campo.getStyleClass().remove("input-field-disabled");
        campo.getStyleClass().add(activo ? "input-field-active" : "input-field-disabled");
    }

    private void mostrarAlerta(String t, String m, Alert.AlertType a) {
        Alert alert = new Alert(a);
        alert.setTitle(t);
        alert.setContentText(m);
        alert.showAndWait();
    }
    // Filtra en tiempo real: revierte el cambio si excede el máximo o si el carácter no es válido para ese campo
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

}