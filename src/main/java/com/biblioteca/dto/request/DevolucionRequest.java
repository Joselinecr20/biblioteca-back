package com.biblioteca.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DevolucionRequest {

    @NotNull(message = "La fecha de devolución real es obligatoria")
    private LocalDate fechaDevolucionReal;
}
