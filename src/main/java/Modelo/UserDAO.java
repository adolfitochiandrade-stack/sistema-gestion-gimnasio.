package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User login(String identificador, String password) {
        // Buscamos coincidencia en ambas columnas usando OR
        String sql = "SELECT idUsuario, username, correo, rol FROM USUARIO WHERE (username = ? OR correo = ?) AND contrasenia = ?";

        try (Connection con = Conexion_DB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, identificador);
            ps.setString(2, identificador);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setIdUsuario(rs.getInt("idUsuario"));
                    user.setUsername(rs.getString("username"));
                    user.setCorreo(rs.getString("correo"));
                    user.setRol(rs.getString("rol")); // Rescatamos el rol (Admin/Encargado)
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en la autenticación del DAO: " + e.getMessage());
        }
        return null; // Retorna null si las credenciales fallan
    }

    //nuevo


}


