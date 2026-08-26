package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Obtiene la lista completa de productos desde la base de datos
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.username AS usuario_nombre FROM producto p " +
                "INNER JOIN usuario u ON p.idUsuario = u.idUsuario";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto producto = extractProductoFromResultSet(rs);
                lista.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener productos: " + e.getMessage());
        }
        return lista;
    }

    // Obtiene los productos registrados dentro de un rango de fechas (para el reporte filtrado).
    // Si desde o hasta vienen nulos, ese extremo del rango queda abierto.
    // Requiere la columna producto.fecha_registro (ver script agregar_fecha_registro_producto.sql).
    public List<Producto> obtenerProductosPorFecha(LocalDate desde, LocalDate hasta) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.username AS usuario_nombre FROM producto p " +
                "INNER JOIN usuario u ON p.idUsuario = u.idUsuario " +
                "WHERE (? IS NULL OR DATE(p.fecha_registro) >= ?) " +
                "AND (? IS NULL OR DATE(p.fecha_registro) <= ?)";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String desdeStr = desde != null ? desde.toString() : null;
            String hastaStr = hasta != null ? hasta.toString() : null;
            ps.setString(1, desdeStr);
            ps.setString(2, desdeStr);
            ps.setString(3, hastaStr);
            ps.setString(4, hastaStr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extractProductoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener productos por fecha (¿ejecutaste agregar_fecha_registro_producto.sql?): " + e.getMessage());
        }
        return lista;
    }

    // Cuenta el total de productos registrados (para el indicador del menú)
    public int contarProductos() {
        String sql = "SELECT COUNT(*) AS total FROM producto";
        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al contar productos: " + e.getMessage());
        }
        return 0;
    }


    // Inserta un nuevo producto en la tabla
    public boolean registrarProducto(Producto producto) {
        String sql = "INSERT INTO producto (nombreProducto, descripcion, stock, idUsuario, precio_compra, precio_venta) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombreProducto());
            ps.setString(2, producto.getDescripcion());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getIdUsuario());
            ps.setDouble(5, producto.getPrecioCompra());
            ps.setDouble(6, producto.getPrecioVenta());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar producto: " + e.getMessage());
            return false;
        }
    }

    // Actualiza los datos de un producto basado en su ID
    public boolean modificarProducto(Producto producto) {
        String sql = "UPDATE producto SET nombreProducto = ?, descripcion = ?, stock = ?, idUsuario = ?, precio_compra = ?, precio_venta = ? WHERE idProducto = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombreProducto());
            ps.setString(2, producto.getDescripcion());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getIdUsuario());
            ps.setDouble(5, producto.getPrecioCompra());
            ps.setDouble(6, producto.getPrecioVenta());
            ps.setInt(7, producto.getIdProducto());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error SQL en modificación de producto: " + e.getMessage());
            return false;
        }
    }

    // Borra un producto de la base de datos usando su ID
    public boolean eliminarProducto(int id) {
        String sql = "DELETE FROM producto WHERE idProducto = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error en DAO al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // Realiza una búsqueda de productos por ID o por nombre
    public List<Producto> buscarProductos(String criterio) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.username AS usuario_nombre FROM producto p " +
                "INNER JOIN usuario u ON p.idUsuario = u.idUsuario " +
                "WHERE p.idProducto LIKE ? OR p.nombreProducto LIKE ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String searchPattern = "%" + criterio + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = extractProductoFromResultSet(rs);
                    lista.add(producto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al buscar producto: " + e.getMessage());
        }
        return lista;
    }

    // Convierte una fila de la base de datos a un objeto Producto
    private Producto extractProductoFromResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("idProducto"));
        producto.setNombreProducto(rs.getString("nombreProducto"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecioCompra(rs.getDouble("precio_compra"));
        producto.setPrecioVenta(rs.getDouble("precio_venta"));
        producto.setStock(rs.getInt("stock"));
        producto.setIdUsuario(rs.getInt("idUsuario"));

        try {
            String userNombre = rs.getString("usuario_nombre");
            producto.setRegistradoPor(userNombre != null ? userNombre.trim() : "");
        } catch (SQLException e) {
            producto.setRegistradoPor("");
        }

        // Fecha de registro del producto (para el filtro de fechas del reporte)
        try {
            java.sql.Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
            producto.setFechaRegistro(fechaRegistro != null ? fechaRegistro.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        } catch (SQLException e) {
            // La columna fecha_registro todavía no existe en la BD (falta correr el script)
            producto.setFechaRegistro("");
        }
        return producto;
    }
}