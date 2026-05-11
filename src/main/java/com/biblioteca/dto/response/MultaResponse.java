package com.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultaResponse {
    private Integer idMulta;
    private Integer idPrestamo;
    private Integer idUsuario;
    private String nombreUsuario;
    private String tituloLibro;
    private BigDecimal monto;
    private Integer diasRetraso;
    private String estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaPago;
}
