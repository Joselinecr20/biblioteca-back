package com.biblioteca.dto.request;

import lombok.Data;

@Data
public class UsuarioUpdateRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String fotoUrl;
    private String usuario;
    private String password;
    private Integer idRol;
}
