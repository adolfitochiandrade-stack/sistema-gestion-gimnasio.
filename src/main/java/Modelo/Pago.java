package Modelo;

public class Pago {
    private int idPago;
    private double monto;
    private String fechaPago;
    private String fechaVencimiento;
    private int idCliente;
    private int idUsuario;
    private String registradoPor;


    public Pago() {
    }

    public Pago(int idPago, double monto, String fechaPago, String fechaVencimiento, int idCliente, int idUsuario, String registradoPor, String fechaRegistro) {
        this.idPago = idPago;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.fechaVencimiento = fechaVencimiento;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.registradoPor = registradoPor;

    }

    // Getters and Setters
    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }


}