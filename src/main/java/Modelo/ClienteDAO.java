package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // Obtiene la lista completa de clientes desde la base de datos
    public List<Cliente> obtenerTodosLosClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS usuario_nombre, " +
                "(SELECT p.fechaVencimiento FROM pago p WHERE p.idCliente = c.idCliente " +
                " ORDER BY p.fechaPago DESC, p.idPago DESC LIMIT 1) AS ultimo_vencimiento " +
                "FROM cliente c " +
                "INNER JOIN usuario u ON c.idUsuario = u.idUsuario";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = extractClienteFromResultSet(rs);
                lista.add(cliente);
            }

        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener clientes: " + e.getMessage());
        }
        return lista;
    }

    // Obtiene los clientes registrados dentro de un rango de fechas (para el reporte filtrado).
    // Si desde o hasta vienen nulos, ese extremo del rango queda abierto.
    public List<Cliente> obtenerClientesPorFecha(LocalDate desde, LocalDate hasta) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS usuario_nombre, " +
                "(SELECT p.fechaVencimiento FROM pago p WHERE p.idCliente = c.idCliente " +
                " ORDER BY p.fechaPago DESC, p.idPago DESC LIMIT 1) AS ultimo_vencimiento " +
                "FROM cliente c " +
                "INNER JOIN usuario u ON c.idUsuario = u.idUsuario " +
                "WHERE (? IS NULL OR DATE(c.fecha_registro) >= ?) " +
                "AND (? IS NULL OR DATE(c.fecha_registro) <= ?)";

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
                    lista.add(extractClienteFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener clientes por fecha: " + e.getMessage());
        }
        return lista;
    }

    // Cuenta el total de clientes registrados (para el indicador del menú)
    public int contarClientes() {
        String sql = "SELECT COUNT(*) AS total FROM cliente";
        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al contar clientes: " + e.getMessage());
        }
        return 0;
    }

    // Inserta un nuevo cliente en la tabla
    public boolean registrarCliente(Cliente cliente) {
        String sql = "INSERT INTO cliente (nombre, telefono, correo, idUsuario) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getCorreo());
            ps.setInt(4, cliente.getIdUsuario());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    // Actualiza los datos de un cliente existente basado en su ID
    public boolean modificarCliente(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre = ?, telefono = ?, correo = ?, idUsuario = ? WHERE idCliente = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getCorreo());
            ps.setInt(4, cliente.getIdUsuario());
            ps.setInt(5, cliente.getIdCliente());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            return false;
        }
    }

    // Verifica si un cliente tiene pagos registrados asociados
    public boolean tienePagosRegistrados(int idCliente) {
        String sql = "SELECT COUNT(*) FROM pago WHERE idCliente = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al verificar pagos del cliente: " + e.getMessage());
        }
        return false;
    }

    // Borra un cliente de la base de datos usando su ID
    public boolean eliminarCliente(int id) {
        String sql = "DELETE FROM cliente WHERE idCliente = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error en DAO al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    // Realiza una búsqueda de clientes por nombre o ID
    public List<Cliente> buscarClientes(String criterio) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.username AS usuario_nombre, " +
                "(SELECT p.fechaVencimiento FROM pago p WHERE p.idCliente = c.idCliente " +
                " ORDER BY p.fechaPago DESC, p.idPago DESC LIMIT 1) AS ultimo_vencimiento " +
                "FROM cliente c " +
                "INNER JOIN usuario u ON c.idUsuario = u.idUsuario " +
                "WHERE c.idCliente LIKE ? OR c.nombre LIKE ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String searchPattern = "%" + criterio + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = extractClienteFromResultSet(rs);
                    lista.add(cliente);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al buscar cliente: " + e.getMessage());
        }
        return lista;
    }

    // Convierte una fila de la base de datos a un objeto Cliente
    private Cliente extractClienteFromResultSet(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("idCliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setCorreo(rs.getString("correo"));
        cliente.setIdUsuario(rs.getInt("idUsuario"));

        try {
            String userNombre = rs.getString("usuario_nombre");
            cliente.setRegistradoPor(userNombre != null ? userNombre.trim() : "");
        } catch (SQLException e) {
            cliente.setRegistradoPor("");
        }

        // Fecha de vencimiento del último pago realizado por el cliente (si nunca ha pagado, "Sin pagos")
        try {
            String ultimoVencimiento = rs.getString("ultimo_vencimiento");
            cliente.setFechaVencimiento(ultimoVencimiento != null ? ultimoVencimiento : "Sin pagos");
        } catch (SQLException e) {
            cliente.setFechaVencimiento("Sin pagos");
        }

        // Fecha de registro del cliente (para el filtro de fechas del reporte)
        try {
            java.sql.Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
            cliente.setFechaRegistro(fechaRegistro != null ? fechaRegistro.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        } catch (SQLException e) {
            cliente.setFechaRegistro("");
        }
        return cliente;
    }
}