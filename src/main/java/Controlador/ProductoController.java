package Controlador;

import Modelo.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ProductoController {

    @FXML private TextField txtBuscar, txtNombre, txtDescripcion, txtPrecioCompra, txtPrecioVenta, txtStock;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId, colStock;
    @FXML private TableColumn<Producto, Double> colPrecioCompra, colPrecioVenta;
    @FXML private TableColumn<Producto, String> colNombre, colDescripcion, colRegistradoPor;
    @FXML private Button btnNuevo, btnAgregar, btnEditar, btnEliminar, btnLimpiar, btnVolver, btnBuscar;
    @FXML private Label lblErrorNombre, lblErrorDescripcion, lblErrorPrecioCompra, lblErrorPrecioVenta, lblErrorStock;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ObservableList<Producto> listaObservableProductos = FXCollections.observableArrayList();

    // Validaciones de los campos de texto para limitar la cantidad de caracteres que se pueden ingresar en ellos.
    private static final int MAX_NOMBRE = 30;
    private static final int MAX_DESCRIPCION = 150;
    private static final int MAX_PRECIO = 9;
    private static final int MAX_STOCK = 5;

    // Modo de trabajo del formulario: hasta que el usuario elige "Nuevo" o "Editar"
    // el formulario permanece bloqueado y no se puede guardar nada.
    private enum Modo { NINGUNO, NUEVO, EDITAR }
    private Modo modoActual = Modo.NINGUNO;
    private Producto productoEnEdicion;

    @FXML
    public void initialize() {
        // Apuntamos a las propiedades reales del modelo Producto.java
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        if (colRegistradoPor != null)
            colRegistradoPor.setCellValueFactory(new PropertyValueFactory<>("registradoPor"));

        // Formato visual de la columna ID (ej: PR-003), igual que en Clientes y Pagos
        colId.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("PR-%03d", item));
            }
        });

        // Formato visual de las columnas de precio como moneda
        colPrecioCompra.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colPrecioVenta.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        mostrarProductosEnTabla();
        // Validaciones de los campos de texto para limitar la cantidad de caracteres que se pueden ingresar en ellos.
        limitarCampo(txtNombre, lblErrorNombre, MAX_NOMBRE, "[\\p{L} ]*", "Máximo 30 caracteres.", "Solo se permiten letras.");
        limitarCampo(txtDescripcion, lblErrorDescripcion, MAX_DESCRIPCION, ".*", "Máximo 150 caracteres.", "Carácter no válido.");
        limitarCampo(txtPrecioCompra, lblErrorPrecioCompra, MAX_PRECIO, "\\$?[0-9]{0,6}(\\.[0-9]{0,2})?", "Máximo 5 dígitos (hasta $99,999).", "Solo se permiten números y el símbolo $.");
        limitarCampo(txtPrecioVenta, lblErrorPrecioVenta, MAX_PRECIO, "\\$?[0-9]{0,6}(\\.[0-9]{0,2})?", "Máximo 5 dígitos (hasta $99,999).", "Solo se permiten números y el símbolo $.");
        limitarCampo(txtStock, lblErrorStock, MAX_STOCK, "[0-9]*", "Máximo 5 dígitos (hasta 99,999 unidades).", "Solo se permiten números.");

        //roles de usuarios

        if (!SesionUsuario.getInstancia().esAdministrador()) {
            btnEliminar.setDisable(true);

        }

        // El formulario inicia bloqueado hasta que se elija "Nuevo" o "Editar"
        establecerModoNinguno();
    }

    private void mostrarProductosEnTabla() {
        listaObservableProductos.setAll(productoDAO.obtenerTodosLosProductos());
        tblProductos.setItems(listaObservableProductos);
    }

    @FXML
    void ejecutarBusqueda(ActionEvent event) {
        listaObservableProductos.setAll(productoDAO.buscarProductos(txtBuscar.getText().trim()));
    }

    // Habilita el formulario en blanco para capturar un producto nuevo.
    @FXML
    void nuevoRegistro(ActionEvent event) {
        modoActual = Modo.NUEVO;
        productoEnEdicion = null;
        tblProductos.getSelectionModel().clearSelection();
        limpiarCamposTexto();
        habilitarCampos(true);
        txtNombre.requestFocus();
    }

    // Toma el producto seleccionado en la tabla y habilita el formulario para editarlo.
    @FXML
    void prepararEdicion(ActionEvent event) {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un producto de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        modoActual = Modo.EDITAR;
        productoEnEdicion = seleccionado;
        txtNombre.setText(seleccionado.getNombreProducto());
        txtDescripcion.setText(seleccionado.getDescripcion() != null ? seleccionado.getDescripcion() : "");
        txtPrecioCompra.setText(String.valueOf(seleccionado.getPrecioCompra()));
        txtPrecioVenta.setText(String.valueOf(seleccionado.getPrecioVenta()));
        txtStock.setText(String.valueOf(seleccionado.getStock()));
        habilitarCampos(true);
        txtNombre.requestFocus();
    }

    // Botón único de Guardar: decide si registra un producto nuevo o actualiza el seleccionado
    // según el modo de trabajo activo (Nuevo Registro / Editar).
    @FXML
    void guardar(ActionEvent event) {
        switch (modoActual) {
            case NUEVO -> registrarProducto();
            case EDITAR -> actualizarProducto();
            default -> mostrarAlerta("Selecciona una acción",
                    "Antes de guardar, presiona \"Nuevo Producto\" para agregar uno o selecciona uno de la tabla y presiona \"Editar\".",
                    Alert.AlertType.WARNING);
        }
    }

    private void registrarProducto() {
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
            // Constructor con 8 parámetros: (id, nombre, descripcion, precioCompra, precioVenta, stock, idUsuario, registradoPor)
            Producto nuevo = new Producto(0, txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    Double.parseDouble(txtPrecioCompra.getText().trim()),
                    Double.parseDouble(txtPrecioVenta.getText().trim()),
                    Integer.parseInt(txtStock.getText().trim()),
                    idUsuarioActual, "");

            if (productoDAO.registrarProducto(nuevo)) {
                mostrarAlerta("Éxito", "Producto guardado con éxito.", Alert.AlertType.INFORMATION);
                mostrarProductosEnTabla();
                establecerModoNinguno();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Formato Erróneo", "Verifica los datos numéricos.", Alert.AlertType.ERROR);
        }
    }

    private void actualizarProducto() {
        if (productoEnEdicion == null) {
            mostrarAlerta("Atención", "Selecciona un producto de la tabla y presiona \"Editar\".", Alert.AlertType.WARNING);
            return;
        }

        if (validarCamposVacios()) {
            mostrarAlerta("Campos Vacíos", "Por favor, llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Producto modificado = new Producto(productoEnEdicion.getIdProducto(), txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    Double.parseDouble(txtPrecioCompra.getText().trim()),
                    Double.parseDouble(txtPrecioVenta.getText().trim()),
                    Integer.parseInt(txtStock.getText().trim()),
                    SesionUsuario.getInstancia().getIdUsuarioLogueado(), "");

            if (productoDAO.modificarProducto(modificado)) {
                mostrarAlerta("Éxito", "Producto editado con éxito.", Alert.AlertType.INFORMATION);
                mostrarProductosEnTabla();
                establecerModoNinguno();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Datos numéricos inválidos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void eliminarProducto(ActionEvent event) {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un producto para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        ButtonType btnAceptar = new ButtonType("Aceptar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que quieres eliminar el producto?");
        confirmacion.getButtonTypes().setAll(btnAceptar, btnCancelar);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == btnAceptar) {
                if (productoDAO.eliminarProducto(seleccionado.getIdProducto())) {
                    mostrarAlerta("Éxito", "Producto eliminado con éxito.", Alert.AlertType.INFORMATION);
                    mostrarProductosEnTabla();
                    establecerModoNinguno();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el producto.", Alert.AlertType.ERROR);
                }
            }
            // Si se presiona Cancelar, no se hace nada y la operación queda anulada
        });
    }

    @FXML
    void buscarProductoRealTime(KeyEvent event) {
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
        return txtNombre.getText().isEmpty() || txtPrecioCompra.getText().isEmpty() ||
                txtPrecioVenta.getText().isEmpty() || txtStock.getText().isEmpty();
    }

    // Regresa el formulario a su estado bloqueado: sin modo activo, campos deshabilitados y vacíos.
    private void establecerModoNinguno() {
        modoActual = Modo.NINGUNO;
        productoEnEdicion = null;
        limpiarCamposTexto();
        habilitarCampos(false);
        tblProductos.getSelectionModel().clearSelection();
    }

    private void limpiarCamposTexto() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecioCompra.clear();
        txtPrecioVenta.clear();
        txtStock.clear();
    }

    // Habilita o bloquea visualmente los campos del formulario según haya un modo de trabajo activo
    private void habilitarCampos(boolean activo) {
        txtNombre.setDisable(!activo);
        txtDescripcion.setDisable(!activo);
        txtPrecioCompra.setDisable(!activo);
        txtPrecioVenta.setDisable(!activo);
        txtStock.setDisable(!activo);

        aplicarEstiloCampo(txtNombre, activo);
        aplicarEstiloCampo(txtDescripcion, activo);
        aplicarEstiloCampo(txtPrecioCompra, activo);
        aplicarEstiloCampo(txtPrecioVenta, activo);
        aplicarEstiloCampo(txtStock, activo);
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