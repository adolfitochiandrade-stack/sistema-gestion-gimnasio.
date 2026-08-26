package Modelo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class ExcelExportador {

    // Colores por módulo (mismos que usa la interfaz)
    private static final byte[] AZUL = {(byte) 0x18, (byte) 0x90, (byte) 0xFF};
    private static final byte[] AZUL_CLARO = {(byte) 0xE6, (byte) 0xF7, (byte) 0xFF};
    private static final byte[] AMARILLO = {(byte) 0xFA, (byte) 0xAD, (byte) 0x14};
    private static final byte[] AMARILLO_CLARO = {(byte) 0xFF, (byte) 0xFB, (byte) 0xE6};
    private static final byte[] VERDE = {(byte) 0x52, (byte) 0xC4, (byte) 0x1A};
    private static final byte[] VERDE_CLARO = {(byte) 0xF6, (byte) 0xFF, (byte) 0xED};
    private static final byte[] BORDE_CLARO = {(byte) 0xE8, (byte) 0xE8, (byte) 0xE8};

    // Genera un solo libro de Excel con las 3 hojas (Clientes, Productos, Pagos)
    public void exportarTodo(List<Cliente> clientes, List<Producto> productos, List<Pago> pagos, File archivo) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            crearHojaClientes(libro, clientes);
            crearHojaProductos(libro, productos);
            crearHojaPagos(libro, pagos);
            guardar(libro, archivo);
        }
    }

    // Exporta un libro de Excel únicamente con la hoja de Clientes (para el botón individual del módulo)
    public void exportarClientes(List<Cliente> clientes, File archivo) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            crearHojaClientes(libro, clientes);
            guardar(libro, archivo);
        }
    }

    // Exporta un libro de Excel únicamente con la hoja de Productos (para el botón individual del módulo)
    public void exportarProductos(List<Producto> productos, File archivo) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            crearHojaProductos(libro, productos);
            guardar(libro, archivo);
        }
    }

    // Exporta un libro de Excel únicamente con la hoja de Pagos (para el botón individual del módulo)
    public void exportarPagos(List<Pago> pagos, File archivo) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            crearHojaPagos(libro, pagos);
            guardar(libro, archivo);
        }
    }

    private void crearHojaClientes(XSSFWorkbook libro, List<Cliente> clientes) {
        XSSFSheet hoja = libro.createSheet("Clientes");
        hoja.setTabColor(new XSSFColor(AZUL, null));

        String[] encabezados = {"ID", "Nombre", "Teléfono", "Correo", "Fecha Vencimiento", "Registrado por"};
        escribirEncabezados(hoja, encabezados, estiloEncabezado(libro, AZUL));

        XSSFCellStyle estiloNormal = estiloFila(libro, null, false);
        XSSFCellStyle estiloBanda = estiloFila(libro, AZUL_CLARO, false);

        int fila = 1;
        for (Cliente c : clientes) {
            XSSFCellStyle estilo = (fila % 2 == 0) ? estiloBanda : estiloNormal;
            Row r = hoja.createRow(fila++);
            celdaTexto(r, 0, String.valueOf(c.getIdCliente()), estilo);
            celdaTexto(r, 1, c.getNombre(), estilo);
            celdaTexto(r, 2, c.getTelefono(), estilo);
            celdaTexto(r, 3, c.getCorreo(), estilo);
            celdaTexto(r, 4, c.getFechaVencimiento(), estilo);
            celdaTexto(r, 5, c.getRegistradoPor(), estilo);
        }
        hoja.createFreezePane(0, 1);
        autoAjustarColumnas(hoja, encabezados.length);
    }

    private void crearHojaProductos(XSSFWorkbook libro, List<Producto> productos) {
        XSSFSheet hoja = libro.createSheet("Productos");
        hoja.setTabColor(new XSSFColor(AMARILLO, null));

        String[] encabezados = {"ID", "Nombre", "Descripción", "Precio Compra", "Precio Venta", "Stock", "Registrado por"};
        escribirEncabezados(hoja, encabezados, estiloEncabezado(libro, AMARILLO));

        XSSFCellStyle estiloNormal = estiloFila(libro, null, false);
        XSSFCellStyle estiloBanda = estiloFila(libro, AMARILLO_CLARO, false);
        XSSFCellStyle estiloMonedaNormal = estiloFila(libro, null, true);
        XSSFCellStyle estiloMonedaBanda = estiloFila(libro, AMARILLO_CLARO, true);

        int fila = 1;
        for (Producto p : productos) {
            boolean banda = fila % 2 == 0;
            XSSFCellStyle estilo = banda ? estiloBanda : estiloNormal;
            XSSFCellStyle estiloMoneda = banda ? estiloMonedaBanda : estiloMonedaNormal;

            Row r = hoja.createRow(fila++);
            celdaTexto(r, 0, String.valueOf(p.getIdProducto()), estilo);
            celdaTexto(r, 1, p.getNombreProducto(), estilo);
            celdaTexto(r, 2, p.getDescripcion(), estilo);
            celdaNumero(r, 3, p.getPrecioCompra(), estiloMoneda);
            celdaNumero(r, 4, p.getPrecioVenta(), estiloMoneda);
            celdaTexto(r, 5, String.valueOf(p.getStock()), estilo);
            celdaTexto(r, 6, p.getRegistradoPor(), estilo);
        }
        hoja.createFreezePane(0, 1);
        autoAjustarColumnas(hoja, encabezados.length);
    }

    private void crearHojaPagos(XSSFWorkbook libro, List<Pago> pagos) {
        XSSFSheet hoja = libro.createSheet("Pagos");
        hoja.setTabColor(new XSSFColor(VERDE, null));

        String[] encabezados = {"ID", "Monto", "Fecha Pago", "Fecha Vencimiento", "ID Cliente", "Registrado por"};
        escribirEncabezados(hoja, encabezados, estiloEncabezado(libro, VERDE));

        XSSFCellStyle estiloNormal = estiloFila(libro, null, false);
        XSSFCellStyle estiloBanda = estiloFila(libro, VERDE_CLARO, false);
        XSSFCellStyle estiloMonedaNormal = estiloFila(libro, null, true);
        XSSFCellStyle estiloMonedaBanda = estiloFila(libro, VERDE_CLARO, true);

        int fila = 1;
        for (Pago p : pagos) {
            boolean banda = fila % 2 == 0;
            XSSFCellStyle estilo = banda ? estiloBanda : estiloNormal;
            XSSFCellStyle estiloMoneda = banda ? estiloMonedaBanda : estiloMonedaNormal;

            Row r = hoja.createRow(fila++);
            celdaTexto(r, 0, String.valueOf(p.getIdPago()), estilo);
            celdaNumero(r, 1, p.getMonto(), estiloMoneda);
            celdaTexto(r, 2, p.getFechaPago(), estilo);
            celdaTexto(r, 3, p.getFechaVencimiento(), estilo);
            celdaTexto(r, 4, String.valueOf(p.getIdCliente()), estilo);
            celdaTexto(r, 5, p.getRegistradoPor(), estilo);
        }
        hoja.createFreezePane(0, 1);
        autoAjustarColumnas(hoja, encabezados.length);
    }

    // Encabezado en negritas, blanco sobre el color del módulo
    private XSSFCellStyle estiloEncabezado(XSSFWorkbook libro, byte[] colorFondo) {
        XSSFCellStyle estilo = libro.createCellStyle();
        Font fuenteBlanca = libro.createFont();
        fuenteBlanca.setBold(true);
        fuenteBlanca.setFontHeightInPoints((short) 11);
        fuenteBlanca.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuenteBlanca);
        estilo.setFillForegroundColor(new XSSFColor(colorFondo, null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    // Estilo de fila de datos: color de banda opcional (alternado) + borde inferior tenue + formato moneda opcional
    private XSSFCellStyle estiloFila(XSSFWorkbook libro, byte[] colorBanda, boolean moneda) {
        XSSFCellStyle estilo = libro.createCellStyle();
        if (colorBanda != null) {
            estilo.setFillForegroundColor(new XSSFColor(colorBanda, null));
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBottomBorderColor(new XSSFColor(BORDE_CLARO, null));
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        if (moneda) {
            estilo.setDataFormat(libro.createDataFormat().getFormat("$#,##0.00"));
        }
        return estilo;
    }

    private void escribirEncabezados(Sheet hoja, String[] encabezados, CellStyle estilo) {
        Row filaEncabezado = hoja.createRow(0);
        filaEncabezado.setHeightInPoints(22);
        for (int i = 0; i < encabezados.length; i++) {
            Cell celda = filaEncabezado.createCell(i);
            celda.setCellValue(encabezados[i]);
            celda.setCellStyle(estilo);
        }
    }

    private void celdaTexto(Row fila, int col, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(col);
        celda.setCellValue(valor != null ? valor : "");
        celda.setCellStyle(estilo);
    }

    private void celdaNumero(Row fila, int col, double valor, CellStyle estilo) {
        Cell celda = fila.createCell(col);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private void autoAjustarColumnas(Sheet hoja, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private void guardar(XSSFWorkbook libro, File archivo) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            libro.write(fos);
        }
    }
}