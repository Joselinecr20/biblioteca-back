package com.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String fotoUrl;
    private Integer idCuenta;
    private String usuario;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
