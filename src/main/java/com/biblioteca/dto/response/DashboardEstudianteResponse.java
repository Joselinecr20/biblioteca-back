package com.biblioteca.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardEstudianteResponse {

    private String usuario;
    private String fotoUrl;
    private String nombreCompleto;

    private long prestamosActivos;
    private long totalPrestamos;

    private long reservasPendientes;
    private long totalReservas;

    private long multasPendientes;
    private BigDecimal montoMultasPendientes;
}
