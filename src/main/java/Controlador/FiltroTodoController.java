package Controlador;

import Modelo.FiltroFecha;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class FiltroTodoController {

    @FXML private DatePicker dpClientesDesde;
    @FXML private DatePicker dpClientesHasta;
    @FXML private DatePicker dpPagosDesde;
    @FXML private DatePicker dpPagosHasta;
    @FXML private DatePicker dpProductosDesde;
    @FXML private DatePicker dpProductosHasta;
    @FXML private Label lblErrorClientesDesde;
    @FXML private Label lblErrorClientesHasta;
    @FXML private Label lblErrorPagosDesde;
    @FXML private Label lblErrorPagosHasta;
    @FXML private Label lblErrorProductosDesde;
    @FXML private Label lblErrorProductosHasta;
    @FXML private Button btnGuardar;

    private boolean guardado = false;

    // Formato de fecha usado únicamente en el módulo de exportaciones: DD/MM/AAAA (ej. 03/04/2026)
    private static final DateTimeFormatter FORMATO_FECHA_EXPORT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        configurarDatePicker(dpClientesDesde, lblErrorClientesDesde);
        configurarDatePicker(dpClientesHasta, lblErrorClientesHasta);
        configurarDatePicker(dpPagosDesde, lblErrorPagosDesde);
        configurarDatePicker(dpPagosHasta, lblErrorPagosHasta);
        configurarDatePicker(dpProductosDesde, lblErrorProductosDesde);
        configurarDatePicker(dpProductosHasta, lblErrorProductosHasta);
    }

    // Precarga el modal con los filtros actuales (para que al reabrirlo se recuerde la selección)
    public void setFiltrosActuales(FiltroFecha filtroClientes, FiltroFecha filtroPagos, FiltroFecha filtroProductos) {
        if (filtroClientes != null) {
            dpClientesDesde.setValue(filtroClientes.getDesde());
            dpClientesHasta.setValue(filtroClientes.getHasta());
        }
        if (filtroPagos != null) {
            dpPagosDesde.setValue(filtroPagos.getDesde());
            dpPagosHasta.setValue(filtroPagos.getHasta());
        }
        if (filtroProductos != null) {
            dpProductosDesde.setValue(filtroProductos.getDesde());
            dpProductosHasta.setValue(filtroProductos.getHasta());
        }
    }

    @FXML
    void guardar(ActionEvent event) {
        if (algunaFechaConFormatoInvalido()) {
            return;
        }
        guardado = true;
        cerrar();
    }

    @FXML
    void limpiarFiltros(ActionEvent event) {
        dpClientesDesde.setValue(null);
        dpClientesHasta.setValue(null);
        dpPagosDesde.setValue(null);
        dpPagosHasta.setValue(null);
        dpProductosDesde.setValue(null);
        dpProductosHasta.setValue(null);

        ocultarErrorCampo(dpClientesDesde.getEditor(), lblErrorClientesDesde);
        ocultarErrorCampo(dpClientesHasta.getEditor(), lblErrorClientesHasta);
        ocultarErrorCampo(dpPagosDesde.getEditor(), lblErrorPagosDesde);
        ocultarErrorCampo(dpPagosHasta.getEditor(), lblErrorPagosHasta);
        ocultarErrorCampo(dpProductosDesde.getEditor(), lblErrorProductosDesde);
        ocultarErrorCampo(dpProductosHasta.getEditor(), lblErrorProductosHasta);
    }

    @FXML
    void cancelar(ActionEvent event) {
        guardado = false;
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    public boolean isGuardado() {
        return guardado;
    }

    public FiltroFecha getFiltroClientes() {
        return new FiltroFecha(dpClientesDesde.getValue(), dpClientesHasta.getValue());
    }

    public FiltroFecha getFiltroPagos() {
        return new FiltroFecha(dpPagosDesde.getValue(), dpPagosHasta.getValue());
    }

    public FiltroFecha getFiltroProductos() {
        return new FiltroFecha(dpProductosDesde.getValue(), dpProductosHasta.getValue());
    }

    // Detecta si alguna de las 6 fechas quedó marcada en rojo por tener un formato inválido
    private boolean algunaFechaConFormatoInvalido() {
        return dpClientesDesde.getEditor().getStyleClass().contains("input-field-active-error") ||
                dpClientesHasta.getEditor().getStyleClass().contains("input-field-active-error") ||
                dpPagosDesde.getEditor().getStyleClass().contains("input-field-active-error") ||
                dpPagosHasta.getEditor().getStyleClass().contains("input-field-active-error") ||
                dpProductosDesde.getEditor().getStyleClass().contains("input-field-active-error") ||
                dpProductosHasta.getEditor().getStyleClass().contains("input-field-active-error");
    }

    /** Hace que el DatePicker muestre y acepte el formato DD/MM/AAAA (ej. 03/04/2026). */
    private void configurarDatePicker(DatePicker datePicker, Label lblError) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : FORMATO_FECHA_EXPORT.format(fecha);
            }
            @Override
            public LocalDate fromString(String texto) {
                return parsearFechaEstricta(texto);
            }
        });
        datePicker.setPromptText("DD/MM/AAAA");

        // Paso 1: mientras se escribe, solo se permiten números y "/", máximo 10 caracteres (DD/MM/AAAA)
        limitarCampo(datePicker.getEditor(), lblError, 10, "[0-9/]*", "Máximo 10 caracteres.", "Solo se permiten números y \"/\".");

        // Paso 2: al salir del campo, se valida que la fecha esté completa y sea real
        datePicker.getEditor().focusedProperty().addListener((obs, estabaEnfocado, estaEnfocado) -> {
            if (!estaEnfocado) {
                String texto = datePicker.getEditor().getText();
                if (texto != null && !texto.trim().isEmpty()) {
                    LocalDate fecha = parsearFechaEstricta(texto.trim());
                    if (fecha == null) {
                        mostrarErrorCampo(datePicker.getEditor(), lblError, "Formato inválido. Usa DD/MM/AAAA (ej. 12/05/2026).");
                        datePicker.setValue(null);
                    } else {
                        datePicker.setValue(fecha);
                        ocultarErrorCampo(datePicker.getEditor(), lblError);
                    }
                } else {
                    ocultarErrorCampo(datePicker.getEditor(), lblError);
                }
            }
        });
    }

    // Valida que el texto tenga EXACTAMENTE el formato DD/MM/AAAA y sea una fecha real
    private LocalDate parsearFechaEstricta(String texto) {
        if (texto == null || !texto.matches("\\d{2}/\\d{2}/\\d{4}")) return null;
        try {
            return LocalDate.parse(texto, FORMATO_FECHA_EXPORT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Filtra en tiempo real: revierte el cambio si excede el máximo o si el carácter no es válido
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