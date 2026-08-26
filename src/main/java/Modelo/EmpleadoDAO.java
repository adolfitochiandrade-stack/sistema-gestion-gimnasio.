package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public List<Empleado> obtenerTodosLosEmpleados() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, username, correo, contrasenia, rol FROM usuario";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(extractEmpleadoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al obtener empleados: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarEmpleado(Empleado empleado) {
        String sql = "INSERT INTO usuario (username, correo, contrasenia, rol) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, empleado.getUsername());
            ps.setString(2, empleado.getCorreo());
            ps.setString(3, empleado.getContrasenia());
            ps.setString(4, empleado.getRol());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar empleado: " + e.getMessage());
            return false;
        }
    }

    public boolean modificarEmpleado(Empleado empleado) {
        String sql = "UPDATE usuario SET username = ?, correo = ?, contrasenia = ?, rol = ? WHERE idUsuario = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, empleado.getUsername());
            ps.setString(2, empleado.getCorreo());
            ps.setString(3, empleado.getContrasenia());
            ps.setString(4, empleado.getRol());
            ps.setInt(5, empleado.getIdUsuario());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            return false;
        }
    }

    // Se usa cuando el campo de contraseña se deja vacío al editar
    public boolean modificarEmpleadoSinContrasenia(Empleado empleado) {
        String sql = "UPDATE usuario SET username = ?, correo = ?, rol = ? WHERE idUsuario = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, empleado.getUsername());
            ps.setString(2, empleado.getCorreo());
            ps.setString(3, empleado.getRol());
            ps.setInt(4, empleado.getIdUsuario());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            return false;
        }
    }

    // Revisa si el usuario tiene clientes, pagos o productos registrados a su nombre
    public boolean tieneMovimientosRegistrados(int idUsuario) {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM cliente  WHERE idUsuario = ?) + " +
                "(SELECT COUNT(*) FROM pago     WHERE idUsuario = ?) + " +
                "(SELECT COUNT(*) FROM producto WHERE idUsuario = ?) AS total";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al verificar movimientos del empleado: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminarEmpleado(int id) {
        String sql = "DELETE FROM usuario WHERE idUsuario = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en DAO al eliminar empleado: " + e.getMessage());
            return false;
        }
    }

    public List<Empleado> buscarEmpleados(String criterio) {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, username, correo, contrasenia, rol FROM usuario " +
                "WHERE idUsuario LIKE ? OR username LIKE ? OR correo LIKE ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String patron = "%" + criterio + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(extractEmpleadoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en DAO al buscar empleado: " + e.getMessage());
        }
        return lista;
    }
    private Empleado extractEmpleadoFromResultSet(ResultSet rs) throws SQLException {
        Empleado empleado = new Empleado();
        empleado.setIdUsuario(rs.getInt("idUsuario"));
        empleado.setUsername(rs.getString("username"));
        empleado.setCorreo(rs.getString("correo"));
        empleado.setContrasenia(rs.getString("contrasenia"));
        empleado.setRol(rs.getString("rol"));
        return empleado;
    }
}