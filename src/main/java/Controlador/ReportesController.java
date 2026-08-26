package Controlador;

import Modelo.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReportesController {

    @FXML private Button btnVolver;
    @FXML private Label lblEstadoClientes;
    @FXML private Label lblEstadoProductos;
    @FXML private Label lblEstadoPagos;
    @FXML private Label lblEstadoTodo;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ExcelExportador excelExportador = new ExcelExportador();
    private final PdfExportador pdfExportador = new PdfExportador();

    // Filtro individual de cada cuadrito (Clientes / Productos / Pagos)
    private FiltroFecha filtroClientes = FiltroFecha.todo();
    private FiltroFecha filtroProductos = FiltroFecha.todo();
    private FiltroFecha filtroPagos = FiltroFecha.todo();

    // Filtros independientes usados únicamente por el cuadrito "Todo" (reporte completo)
    private FiltroFecha filtroTodoClientes = FiltroFecha.todo();
    private FiltroFecha filtroTodoPagos = FiltroFecha.todo();
    private FiltroFecha filtroTodoProductos = FiltroFecha.todo();

    // Evita que un clic sobre los botones de exportar (dentro del cuadrito) también
    // dispare la apertura del modal de filtro del cuadrito completo
    @FXML
    void detenerPropagacion(MouseEvent event) {
        event.consume();
    }

    // ---------------------------------------------------------------
    // Abrir los modales de filtro (clic en el cuadrito del módulo)
    // ---------------------------------------------------------------

    @FXML
    void abrirFiltroClientes(MouseEvent event) {
        FiltroFechaController controller = abrirModalFiltro("Filtrar Clientes", filtroClientes);
        if (controller != null && controller.isGuardado()) {
            filtroClientes = controller.getFiltroSeleccionado();
            lblEstadoClientes.setText(filtroClientes.textoEstado());
        }
    }

    @FXML
    void abrirFiltroProductos(MouseEvent event) {
        FiltroFechaController controller = abrirModalFiltro("Filtrar Productos", filtroProductos);
        if (controller != null && controller.isGuardado()) {
            filtroProductos = controller.getFiltroSeleccionado();
            lblEstadoProductos.setText(filtroProductos.textoEstado());
        }
    }

    @FXML
    void abrirFiltroPagos(MouseEvent event) {
        FiltroFechaController controller = abrirModalFiltro("Filtrar Pagos", filtroPagos);
        if (controller != null && controller.isGuardado()) {
            filtroPagos = controller.getFiltroSeleccionado();
            lblEstadoPagos.setText(filtroPagos.textoEstado());
        }
    }

    @FXML
    void abrirFiltroTodo(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/FiltroTodo-View.fxml"));
            Parent root = loader.load();
            FiltroTodoController controller = loader.getController();
            controller.setFiltrosActuales(filtroTodoClientes, filtroTodoPagos, filtroTodoProductos);

            Stage modal = new Stage();
            modal.initOwner(btnVolver.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle("Reporte completo");
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.showAndWait();

            if (controller.isGuardado()) {
                filtroTodoClientes = controller.getFiltroClientes();
                filtroTodoPagos = controller.getFiltroPagos();
                filtroTodoProductos = controller.getFiltroProductos();

                boolean todo = filtroTodoClientes.esTodo() && filtroTodoPagos.esTodo() && filtroTodoProductos.esTodo();
                lblEstadoTodo.setText(todo ? "Reporte completo (todos)" : "Reporte completo (con filtros)");
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de filtro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Carga el modal de filtro de un solo módulo, lo muestra y devuelve su controlador ya cerrado
    private FiltroFechaController abrirModalFiltro(String titulo, FiltroFecha filtroActual) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/FiltroFecha-View.fxml"));
            Parent root = loader.load();
            FiltroFechaController controller = loader.getController();
            controller.setTitulo(titulo);
            controller.setFiltroActual(filtroActual);

            Stage modal = new Stage();
            modal.initOwner(btnVolver.getScene().getWindow());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle(titulo);
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.showAndWait();

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de filtro: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Exportación individual por módulo (respeta el filtro guardado del cuadrito)
    // ---------------------------------------------------------------

    @FXML
    void exportarClientesExcel(ActionEvent event) {
        List<Cliente> clientes = filtroClientes.esTodo()
                ? clienteDAO.obtenerTodosLosClientes()
                : clienteDAO.obtenerClientesPorFecha(filtroClientes.getDesde(), filtroClientes.getHasta());

        File archivo = elegirArchivoDestino("Reporte-Clientes.xlsx", "Libro de Excel (.xlsx)", ".xlsx");
        if (archivo == null) return;
        try {
            excelExportador.exportarClientes(clientes, archivo);
            mostrarAlerta("Éxito", "Reporte de clientes exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo Excel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void exportarClientesPdf(ActionEvent event) {
        List<Cliente> clientes = filtroClientes.esTodo()
                ? clienteDAO.obtenerTodosLosClientes()
                : clienteDAO.obtenerClientesPorFecha(filtroClientes.getDesde(), filtroClientes.getHasta());

        File archivo = elegirArchivoDestino("Reporte-Clientes.pdf", "Documento PDF (.pdf)", ".pdf");
        if (archivo == null) return;
        try {
            pdfExportador.exportarClientes(clientes, archivo);
            mostrarAlerta("Éxito", "Reporte de clientes exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo PDF: " + (e.getMessage() != null ? e.getMessage() : e.toString()), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void exportarProductosExcel(ActionEvent event) {
        List<Producto> productos = filtroProductos.esTodo()
                ? productoDAO.obtenerTodosLosProductos()
                : productoDAO.obtenerProductosPorFecha(filtroProductos.getDesde(), filtroProductos.getHasta());

        File archivo = elegirArchivoDestino("Reporte-Productos.xlsx", "Libro de Excel (.xlsx)", ".xlsx");
        if (archivo == null) return;
        try {
            excelExportador.exportarProductos(productos, archivo);
            mostrarAlerta("Éxito", "Reporte de productos exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo Excel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void exportarProductosPdf(ActionEvent event) {
        List<Producto> productos = filtroProductos.esTodo()
                ? productoDAO.obtenerTodosLosProductos()
                : productoDAO.obtenerProductosPorFecha(filtroProductos.getDesde(), filtroProductos.getHasta());

        File archivo = elegirArchivoDestino("Reporte-Productos.pdf", "Documento PDF (.pdf)", ".pdf");
        if (archivo == null) return;
        try {
            pdfExportador.exportarProductos(productos, archivo);
            mostrarAlerta("Éxito", "Reporte de productos exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo PDF: " + (e.getMessage() != null ? e.getMessage() : e.toString()), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void exportarPagosExcel(ActionEvent event) {
        List<Pago> pagos = filtroPagos.esTodo()
                ? pagoDAO.obtenerTodosLosPagos()
                : pagoDAO.obtenerPagosPorFecha(filtroPagos.getDesde(), filtroPagos.getHasta());

        File archivo = elegirArchivoDestino("Reporte-Pagos.xlsx", "Libro de Excel (.xlsx)", ".xlsx");
        if (archivo == null) return;
        try {
            excelExportador.exportarPagos(pagos, archivo);
            mostrarAlerta("Éxito", "Reporte de pagos exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo Excel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void exportarPagosPdf(ActionEvent event) {
        List<Pago> pagos = filtroPagos.esTodo()
                ? pagoDAO.obtenerTodosLosPagos()
                : pagoDAO.obtenerPagosPorFecha(filtroPagos.getDesde(), filtroPagos.getHasta());

        File archivo = elegirArchivoDestino("Reporte-Pagos.pdf", "Documento PDF (.pdf)", ".pdf");
        if (archivo == null) return;
        try {
            pdfExportador.exportarPagos(pagos, archivo);
            mostrarAlerta("Éxito", "Reporte de pagos exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo PDF: " + (e.getMessage() != null ? e.getMessage() : e.toString()), Alert.AlertType.ERROR);
        }
    }

    // ---------------------------------------------------------------
    // Exportación del reporte completo (respeta los 3 filtros del cuadrito "Todo")
    // ---------------------------------------------------------------

    @FXML
    void exportarTodoExcel(ActionEvent event) {
        File archivo = elegirArchivoDestino("Reporte-God Warrior Gym.xlsx", "Libro de Excel (.xlsx)", ".xlsx");
        if (archivo == null) return;

        try {
            List<Cliente> clientes = obtenerClientesFiltrados(filtroTodoClientes);
            List<Producto> productos = obtenerProductosFiltrados(filtroTodoProductos);
            List<Pago> pagos = obtenerPagosFiltrados(filtroTodoPagos);

            excelExportador.exportarTodo(clientes, productos, pagos, archivo);
            mostrarAlerta("Éxito", "Reporte exportado con éxito", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo Excel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void exportarTodoPdf(ActionEvent event) {
        File archivo = elegirArchivoDestino("Reporte-God Warrior Gym.pdf", "Documento PDF (.pdf)", ".pdf");
        if (archivo == null) return;

        try {
            List<Cliente> clientes = obtenerClientesFiltrados(filtroTodoClientes);
            List<Producto> productos = obtenerProductosFiltrados(filtroTodoProductos);
            List<Pago> pagos = obtenerPagosFiltrados(filtroTodoPagos);

            pdfExportador.exportarTodo(clientes, productos, pagos, archivo);
            mostrarAlerta("Éxito", "Reporte exportado con éxito.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el archivo PDF: " + (e.getMessage() != null ? e.getMessage() : e.toString()), Alert.AlertType.ERROR);
        }
    }

    private List<Cliente> obtenerClientesFiltrados(FiltroFecha filtro) {
        return filtro.esTodo() ? clienteDAO.obtenerTodosLosClientes() : clienteDAO.obtenerClientesPorFecha(filtro.getDesde(), filtro.getHasta());
    }

    private List<Producto> obtenerProductosFiltrados(FiltroFecha filtro) {
        return filtro.esTodo() ? productoDAO.obtenerTodosLosProductos() : productoDAO.obtenerProductosPorFecha(filtro.getDesde(), filtro.getHasta());
    }

    private List<Pago> obtenerPagosFiltrados(FiltroFecha filtro) {
        return filtro.esTodo() ? pagoDAO.obtenerTodosLosPagos() : pagoDAO.obtenerPagosPorFecha(filtro.getDesde(), filtro.getHasta());
    }

    // ---------------------------------------------------------------

    private File elegirArchivoDestino(String nombreSugerido, String descripcionFiltro, String patronFiltro) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte");
        fileChooser.setInitialFileName(nombreSugerido);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(descripcionFiltro, patronFiltro));

        File archivo = fileChooser.showSaveDialog(btnVolver.getScene().getWindow());
        String extension = patronFiltro.replace("*", "");
        if (archivo != null && !archivo.getName().toLowerCase().endsWith(extension)) {
            archivo = new File(archivo.getAbsolutePath() + extension);
        }
        return archivo;
    }

    @FXML
    void volverAlMenu(ActionEvent event) {
        try {
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Main-view.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Panel Principal");
            stage.setResizable(true);
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al volver al menú: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}