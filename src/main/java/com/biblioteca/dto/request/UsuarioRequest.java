package com.biblioteca.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Email(message = "Formato de correo inválido")
    private String correo;

    private String telefono;

    private String fotoUrl;

    @NotBlank(message = "El usuario de la cuenta es obligatorio")
    private String usuario;

    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Integer idRol;
}
