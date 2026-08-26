package Modelo;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/*
 * Utilidad para exportar el reporte general (Clientes, Productos y Pagos)
 * a un solo archivo PDF usando OpenPDF (com.github.librepdf:openpdf).
 */
public class PdfExportador {

    private static final Color AZUL = new Color(0x18, 0x90, 0xFF);
    private static final Color AZUL_CLARO = new Color(0xE6, 0xF7, 0xFF);
    private static final Color AMARILLO = new Color(0xFA, 0xAD, 0x14);
    private static final Color AMARILLO_CLARO = new Color(0xFF, 0xFB, 0xE6);
    private static final Color VERDE = new Color(0x52, 0xC4, 0x1A);
    private static final Color VERDE_CLARO = new Color(0xF6, 0xFF, 0xED);
    private static final Color BLANCO = Color.WHITE;

    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font FUENTE_ENCABEZADO = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_TEXTO = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font FUENTE_VACIO = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);

    public void exportarTodo(List<Cliente> clientes, List<Producto> productos, List<Pago> pagos, File archivo)
            throws DocumentException, java.io.IOException {

        Document documento = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarSeccion(documento, "Reporte de Clientes", AZUL, AZUL_CLARO,
                    new String[]{"ID", "Nombre", "Teléfono", "Correo", "Vence", "Registró"},
                    new float[]{1f, 2.5f, 2f, 3f, 1.6f, 2f},
                    filasClientes(clientes), false);

            agregarSeccion(documento, "Reporte de Productos", AMARILLO, AMARILLO_CLARO,
                    new String[]{"ID", "Nombre", "Descripción", "P. Compra", "P. Venta", "Stock", "Registró"},
                    new float[]{1f, 2f, 3f, 1.6f, 1.6f, 1.2f, 2f},
                    filasProductos(productos), true);

            agregarSeccion(documento, "Reporte de Pagos", VERDE, VERDE_CLARO,
                    new String[]{"ID", "Monto", "Fecha Pago", "Vence", "ID Cliente", "Registró"},
                    new float[]{1f, 1.6f, 2f, 2f, 1.6f, 3f},
                    filasPagos(pagos), true);

            documento.close();
        }
    }

    // Exporta un PDF únicamente con el reporte de Clientes (para el botón individual del módulo)
    public void exportarClientes(List<Cliente> clientes, File archivo) throws DocumentException, java.io.IOException {
        Document documento = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            agregarSeccion(documento, "Reporte de Clientes", AZUL, AZUL_CLARO,
                    new String[]{"ID", "Nombre", "Teléfono", "Correo", "Vence", "Registró"},
                    new float[]{1f, 2.5f, 2f, 3f, 1.6f, 2f},
                    filasClientes(clientes), false);
            documento.close();
        }
    }

    // Exporta un PDF únicamente con el reporte de Productos (para el botón individual del módulo)
    public void exportarProductos(List<Producto> productos, File archivo) throws DocumentException, java.io.IOException {
        Document documento = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            agregarSeccion(documento, "Reporte de Productos", AMARILLO, AMARILLO_CLARO,
                    new String[]{"ID", "Nombre", "Descripción", "P. Compra", "P. Venta", "Stock", "Registró"},
                    new float[]{1f, 2f, 3f, 1.6f, 1.6f, 1.2f, 2f},
                    filasProductos(productos), false);
            documento.close();
        }
    }

    // Exporta un PDF únicamente con el reporte de Pagos (para el botón individual del módulo)
    public void exportarPagos(List<Pago> pagos, File archivo) throws DocumentException, java.io.IOException {
        Document documento = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            agregarSeccion(documento, "Reporte de Pagos", VERDE, VERDE_CLARO,
                    new String[]{"ID", "Monto", "Fecha Pago", "Vence", "ID Cliente", "Registró"},
                    new float[]{1f, 1.6f, 2f, 2f, 1.6f, 3f},
                    filasPagos(pagos), false);
            documento.close();
        }
    }

    private List<String[]> filasClientes(List<Cliente> clientes) {
        List<String[]> filas = new java.util.ArrayList<>();
        if (clientes != null) {
            for (Cliente c : clientes) {
                filas.add(new String[]{
                        String.valueOf(c.getIdCliente()),
                        c.getNombre() != null ? c.getNombre() : "",
                        c.getTelefono() != null ? c.getTelefono() : "",
                        c.getCorreo() != null ? c.getCorreo() : "",
                        c.getFechaVencimiento() != null ? c.getFechaVencimiento() : "",
                        c.getRegistradoPor() != null ? c.getRegistradoPor() : ""
                });
            }
        }
        return filas;
    }

    private List<String[]> filasProductos(List<Producto> productos) {
        List<String[]> filas = new java.util.ArrayList<>();
        if (productos != null) {
            for (Producto p : productos) {
                filas.add(new String[]{
                        String.valueOf(p.getIdProducto()),
                        p.getNombreProducto() != null ? p.getNombreProducto() : "",
                        p.getDescripcion() != null ? p.getDescripcion() : "",
                        String.format("$%.2f", p.getPrecioCompra()),
                        String.format("$%.2f", p.getPrecioVenta()),
                        String.valueOf(p.getStock()),
                        p.getRegistradoPor() != null ? p.getRegistradoPor() : ""
                });
            }
        }
        return filas;
    }

    private List<String[]> filasPagos(List<Pago> pagos) {
        List<String[]> filas = new java.util.ArrayList<>();
        if (pagos != null) {
            for (Pago p : pagos) {
                filas.add(new String[]{
                        String.valueOf(p.getIdPago()),
                        String.format("$%.2f", p.getMonto()),
                        p.getFechaPago() != null ? p.getFechaPago() : "",
                        p.getFechaVencimiento() != null ? p.getFechaVencimiento() : "",
                        String.valueOf(p.getIdCliente()),
                        p.getRegistradoPor() != null ? p.getRegistradoPor() : ""
                });
            }
        }
        return filas;
    }

    private void agregarSeccion(Document documento, String titulo, Color colorEncabezado, Color colorBanda,
                                String[] encabezados, float[] anchosRelativos, List<String[]> filas,
                                boolean nuevaPagina) throws DocumentException {

        if (nuevaPagina) {
            documento.newPage();
        }

        Paragraph tituloParrafo = new Paragraph(titulo, FUENTE_TITULO);
        tituloParrafo.setSpacingAfter(12);
        documento.add(tituloParrafo);

        PdfPTable tabla = new PdfPTable(anchosRelativos);
        tabla.setWidthPercentage(100);

        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_ENCABEZADO));
            celda.setBackgroundColor(colorEncabezado);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celda.setPadding(5);
            tabla.addCell(celda);
        }
        tabla.setHeaderRows(1);

        if (filas.isEmpty()) {
            PdfPCell celdaVacia = new PdfPCell(new Phrase("Sin registros.", FUENTE_VACIO));
            celdaVacia.setColspan(encabezados.length);
            celdaVacia.setPadding(8);
            celdaVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaVacia);
        } else {
            boolean banda = false;
            for (String[] fila : filas) {
                Color fondoFila = banda ? colorBanda : BLANCO;
                for (String valor : fila) {
                    PdfPCell celda = new PdfPCell(new Phrase(valor, FUENTE_TEXTO));
                    celda.setBackgroundColor(fondoFila);
                    celda.setPadding(5);
                    celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    tabla.addCell(celda);
                }
                banda = !banda;
            }
        }

        documento.add(tabla);
    }
}