package com.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BibliotecaResponse {
    private Integer idBiblioteca;
    private String nombre;
    private String ubicacion;
}
