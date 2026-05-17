package com.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibroResponse {
    private Integer idLibro;
    private String titulo;
    private String editorial;
    private Integer anioPublicacion;
    private String descripcion;
    private String portadaUrl;
    private String isbn;
    private Integer idCategoria;
    private String categoria;
    private List<String> autores;
    private List<ImagenDto> imagenes;
    private Integer cantidadDisponible;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImagenDto {
        private Integer idImagen;
        private String url;
        private Boolean esPortada;
        private Integer orden;
        private String descripcion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BibliotecaDto {
        private Integer idBiblioteca;
        private String nombre;
        private Integer cantidadDisponible;
        private Integer cantidadTotal;
    }

    private List<BibliotecaDto> bibliotecas;
}
