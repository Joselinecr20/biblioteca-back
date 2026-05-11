package com.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoResponse {
    private Integer idPrestamo;
    private Integer idUsuario;
    private String nombreUsuario;
    private Integer idLibro;
    private String tituloLibro;
    private String portadaLibro;
    private Integer idBiblioteca;
    private String nombreBiblioteca;
    private Integer idReserva;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaDevolucion;
    private LocalDate fechaDevolucionReal;
    private String estado;
}
