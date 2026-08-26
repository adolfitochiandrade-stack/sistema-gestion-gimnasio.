package Modelo;

public class User {
    private int idUsuario;
    private String username;
    private String contrasenia;
    private String rol;
    private String correo;

    public User() {
        // Constructor vacío
    }

    public User(int idUsuario, String username, String contrasenia, String rol, String correo) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.correo = correo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getContrasenia() {
        return contrasenia;
    }
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }

}

