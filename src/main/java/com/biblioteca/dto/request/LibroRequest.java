package com.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LibroRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String editorial;

    private Integer anioPublicacion;

    private String descripcion;

    private String portadaUrl;

    private String isbn;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    private List<Integer> idAutores;
}
