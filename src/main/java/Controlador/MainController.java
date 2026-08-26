package Controlador;

import Modelo.SesionUsuario;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import javafx.scene.control.Label;
import Modelo.ClienteDAO;
import Modelo.ProductoDAO;
import Modelo.PagoDAO;



import java.io.IOException;

public class MainController {

    // VINCULACIÓN CON EL FXML: Botón y Tarjetas del menú
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnEmpleados;
    @FXML private Button btnReportes;
    @FXML private VBox cardClientes;
    @FXML private VBox cardProductos;
    @FXML private VBox cardPagos;


    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();



    @FXML private Label lblCountClientes;
    @FXML private Label lblCountProductos;
    @FXML private Label lblCountPagos;

    //Al hacer clic, descarga el menú principal y vuelve a pintar la ventana del Login.
    @FXML
    void handleCerrarSesion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Login-View.fxml"));
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Iniciar Sesión");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error de transición al regresar al Login: " + e.getMessage());
        }
    }

    //rodels de ususrio
    @FXML
    private void initialize() {
        actualizarContadores();
        if (!SesionUsuario.getInstancia().esAdministrador()) {
            btnEmpleados.setDisable(true);
        }
    }



    private void actualizarContadores() {
        lblCountClientes.setText(clienteDAO.contarClientes() + " registrados");
        lblCountProductos.setText(productoDAO.contarProductos() + " registrados");
        lblCountPagos.setText(pagoDAO.contarPagos() + " registrados");
    }



    @FXML
    void handleAbrirEmpleados(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Empleados-View.fxml"));
            Stage stage = (Stage) btnEmpleados.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Gestión de Empleados");
            stage.setResizable(true);
            stage.sizeToScene(); // Ajusta la ventana al tamaño real de esta vista antes de decidir si maximizar
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar el módulo de Empleados: " + e.getMessage());
        }
    }

    // Abre el módulo de Reportes (exportación a Excel de Clientes, Productos y Pagos)
    @FXML
    void handleAbrirReportes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Reportes-View.fxml"));
            Stage stage = (Stage) btnReportes.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Reportes");
            stage.setResizable(true);
            stage.sizeToScene(); // Ajusta la ventana al tamaño real de esta vista antes de decidir si maximizar
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar el módulo de Reportes: " + e.getMessage());
        }
    }

    //Se activa al dar un clic izquierdo sobre la tarjeta azul de Clientes.
    @FXML
    void abrirModuloClientes(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Clientes-View.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Administración de Clientes");
            stage.setResizable(true);
            stage.sizeToScene(); // Ajusta la ventana al tamaño real de esta vista antes de decidir si maximizar
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar el módulo de Clientes: " + e.getMessage());
        }
    }

    //Se activa al dar clic sobre la tarjeta amarilla de inventario.
    @FXML
    void abrirModuloProductos(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Product-View.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Inventario de Productos");
            stage.setResizable(true);
            stage.sizeToScene(); // Ajusta la ventana al tamaño real de esta vista antes de decidir si maximizar
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar el módulo de Productos: " + e.getMessage());
        }
    }

    //Se activa al dar clic sobre la tarjeta verde de cobros.
    @FXML
    void abrirModuloPagos(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/Pagos-View.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("GymCentral - Control de Pagos");
            stage.setResizable(true);
            stage.sizeToScene(); // Ajusta la ventana al tamaño real de esta vista antes de decidir si maximizar
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar el módulo de Pagos: " + e.getMessage());
        }
    }
}