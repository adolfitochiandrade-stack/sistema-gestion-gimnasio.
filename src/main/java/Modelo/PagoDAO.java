package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    // Obtiene la lista completa de pagos desde la base de datos
    public List<Pago> obtenerTodosLosPagos() {
        List<Pago> lista = new ArrayList<>();

        String sql = "SELECT p.*, u.username AS usuario_nombre FROM pago p "
                + "INNER JOIN usuario u ON p.idUsuario = u.idUsuario";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pago pago = extractPagoFromResultSet(rs);
                lista.add(pago);
            }

        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener pagos: " + e.getMessage());
        }

        return lista;
    }

    // Obtiene los pagos dentro de un rango de fechas, usando la fecha de pago
    // (que es el mismo día en que se realizó el pago). Si desde o hasta vienen
    // nulos, ese extremo del rango queda abierto.
    public List<Pago> obtenerPagosPorFecha(LocalDate desde, LocalDate hasta) {
        List<Pago> lista = new ArrayList<>();

        String sql = "SELECT p.*, u.username AS usuario_nombre FROM pago p "
                + "INNER JOIN usuario u ON p.idUsuario = u.idUsuario "
                + "WHERE (? IS NULL OR p.fechaPago >= ?) "
                + "AND (? IS NULL OR p.fechaPago <= ?)";

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
                    lista.add(extractPagoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener pagos por fecha: " + e.getMessage());
        }

        return lista;
    }

    // Cuenta el total de pagos registrados (para el indicador del menú)
    public int contarPagos() {
        String sql = "SELECT COUNT(*) AS total FROM pago";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error en DAO al contar pagos: " + e.getMessage());
        }

        return 0;
    }

    // Inserta un nuevo registro de pago en la tabla
    public boolean registrarPago(Pago pago) {
        String sql = "INSERT INTO pago (monto, fechaPago, fechaVencimiento, idCliente, idUsuario) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, pago.getMonto());
            ps.setString(2, pago.getFechaPago());
            ps.setString(3, pago.getFechaVencimiento());
            ps.setInt(4, pago.getIdCliente());
            ps.setInt(5, pago.getIdUsuario());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar pago: " + e.getMessage());
            return false;
        }
    }

    // Actualiza los datos de un pago basado en su ID
    public boolean modificarPago(Pago pago) {
        String sql = "UPDATE pago SET monto = ?, fechaPago = ?, fechaVencimiento = ?, idCliente = ?, idUsuario = ? WHERE idPago = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, pago.getMonto());
            ps.setString(2, pago.getFechaPago());
            ps.setString(3, pago.getFechaVencimiento());
            ps.setInt(4, pago.getIdCliente());
            ps.setInt(5, pago.getIdUsuario());
            ps.setInt(6, pago.getIdPago());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error SQL en modificación de pago: " + e.getMessage());
            return false;
        }
    }

    // Borra un pago de la base de datos usando su ID
    public boolean eliminarPago(int id) {
        String sql = "DELETE FROM pago WHERE idPago = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error en DAO al eliminar pago: " + e.getMessage());
            return false;
        }
    }

    // Realiza una búsqueda de pagos únicamente por ID de cliente
    public List<Pago> buscarPagos(String criterio) {
        List<Pago> lista = new ArrayList<>();

        String sql = "SELECT p.*, u.username AS usuario_nombre FROM pago p "
                + "INNER JOIN usuario u ON p.idUsuario = u.idUsuario "
                + "WHERE p.idCliente LIKE ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Si el usuario busca con formato "C-008", nos quedamos solo con los
            // dígitos (sin ceros a la izquierda) para que coincida con el valor
            // numérico real guardado en idCliente.
            String soloDigitos = criterio == null ? "" : criterio.replaceAll("[^0-9]", "");
            soloDigitos = soloDigitos.replaceFirst("^0+(?!$)", "");

            String criterioBusqueda = soloDigitos.isEmpty() ? criterio : soloDigitos;
            String searchPattern = "%" + criterioBusqueda + "%";

            ps.setString(1, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago pago = extractPagoFromResultSet(rs);
                    lista.add(pago);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en DAO al buscar pago: " + e.getMessage());
        }

        return lista;
    }

    // Convierte una fila de la base de datos a un objeto Pago
    private Pago extractPagoFromResultSet(ResultSet rs) throws SQLException {
        Pago pago = new Pago();

        pago.setIdPago(rs.getInt("idPago"));
        pago.setMonto(rs.getDouble("monto"));
        pago.setFechaPago(rs.getString("fechaPago"));
        pago.setFechaVencimiento(rs.getString("fechaVencimiento"));
        pago.setIdCliente(rs.getInt("idCliente"));
        pago.setIdUsuario(rs.getInt("idUsuario"));

        try {
            String userNombre = rs.getString("usuario_nombre");
            pago.setRegistradoPor(userNombre != null ? userNombre.trim() : "");
        } catch (SQLException e) {
            pago.setRegistradoPor("");
        }

        return pago;
    }
}