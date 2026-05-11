package com.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaPassword;
}
