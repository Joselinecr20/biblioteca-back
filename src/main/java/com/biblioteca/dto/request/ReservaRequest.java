package com.biblioteca.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaRequest {

    @NotNull(message = "El libro es obligatorio")
    private Integer idLibro;

    @NotNull(message = "La biblioteca es obligatoria")
    private Integer idBiblioteca;

    private Integer diasPrestamo;
}
