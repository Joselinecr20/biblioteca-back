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
public class ReservaResponse {
    private Integer idReserva;
    private Integer idUsuario;
    private String nombreUsuario;
    private Integer idLibro;
    private String tituloLibro;
    private String portadaLibro;
    private Integer idBiblioteca;
    private String nombreBiblioteca;
    private LocalDateTime fechaReserva;
    private LocalDate fechaExpiracion;
    private String estado;
    private String observaciones;
    private Integer diasPrestamo;
}
