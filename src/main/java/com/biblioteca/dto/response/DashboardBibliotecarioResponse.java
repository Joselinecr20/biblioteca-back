package com.biblioteca.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardBibliotecarioResponse {

    private String fotoUrl;

    private long totalLibros;
    private long totalEjemplares;
    private long ejemplaresDisponibles;
    private List<LibroBajoStockResponse> librosBajoStock;

    private long reservasPendientes;
    private long prestamosActivos;
    private long multasPendientes;

    @Data
    @Builder
    public static class LibroBajoStockResponse {
        private Integer idLibro;
        private String titulo;
        private String biblioteca;
        private int cantidadDisponible;
        private int cantidadTotal;
    }
}
