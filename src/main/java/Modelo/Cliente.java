package Modelo;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String telefono;
    private String correo;
    private int idUsuario;
    private String registradoPor;
    private String fechaVencimiento;
    private String fechaRegistro;

    // Constructor vacío
    public Cliente() {}

    // Constructor completo actualizado
    public Cliente(int idCliente, String nombre, String telefono, String correo, int idUsuario, String registradoPor) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.idUsuario = idUsuario;
        this.registradoPor = registradoPor;
    }

    // --- Getters y Setters Actuales ---
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    // --- NUEVOS GETTERS Y SETTERS ---
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    // Fecha en que el cliente fue registrado (columna fecha_registro, ya existente en la BD)
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

}