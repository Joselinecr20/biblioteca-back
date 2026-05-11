package com.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo;
    private Integer idCuenta;
    private String usuario;
    private String rol;
    private String nombre;
    private String apellido;
}
