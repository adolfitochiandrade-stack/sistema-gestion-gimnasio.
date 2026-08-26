package Modelo;

public class Empleado {
    private int idUsuario;
    private String username;
    private String correo;
    private String contrasenia;
    private String rol;

    public Empleado() {}

    public Empleado(int idUsuario, String username, String correo, String contrasenia, String rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
