package com.biblioteca.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardAdminResponse {

    private String fotoUrl;

    private long totalLibros;
    private long totalEjemplares;
    private long ejemplaresDisponibles;

    private long totalUsuarios;
    private long totalEstudiantes;
    private long totalBibliotecarios;

    private long prestamosActivos;
    private long totalPrestamos;

    private long reservasPendientes;
    private long totalReservas;

    private long multasPendientes;
    private BigDecimal montoMultasPendientes;
    private BigDecimal montoMultasPagadas;
}
