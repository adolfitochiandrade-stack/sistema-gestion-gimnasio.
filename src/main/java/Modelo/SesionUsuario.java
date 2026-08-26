package Modelo;

import Modelo.User;

public class SesionUsuario {
    private static SesionUsuario instancia;
    private User usuarioActivo;

    private SesionUsuario() {}

    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public void login(User usuario) {
        this.usuarioActivo = usuario;
    }

    public void logout() {
        this.usuarioActivo = null;
    }

    public User getUsuarioActivo() {
        return usuarioActivo;
    }

    public int getIdUsuarioLogueado() {
        return (usuarioActivo != null) ? usuarioActivo.getIdUsuario() : 0;
    }


    public boolean esAdministrador() {
        return usuarioActivo != null && "Administrador".equalsIgnoreCase(usuarioActivo.getRol());
    }
}
