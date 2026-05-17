package com.biblioteca.controller;

import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Categoria;
import com.biblioteca.repository.CategoriaRepository;
import com.biblioteca.repository.LibroRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository     libroRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaDto>>> getAll() {
        List<CategoriaDto> list = categoriaRepository.findAll().stream()
                .map(c -> new CategoriaDto(c.getIdCategoria(), c.getNombre()))
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Categorías encontradas", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaDto>> create(@Valid @RequestBody NameRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }
        Categoria cat = new Categoria();
        cat.setNombre(request.getNombre().trim());
        Categoria saved = categoriaRepository.save(cat);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Categoría creada", new CategoriaDto(saved.getIdCategoria(), saved.getNombre())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaDto>> update(@PathVariable Integer id,
                                                             @Valid @RequestBody NameRequest request) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));

        String nuevoNombre = request.getNombre().trim();
        if (!nuevoNombre.equalsIgnoreCase(cat.getNombre())
                && categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }
        cat.setNombre(nuevoNombre);
        Categoria saved = categoriaRepository.save(cat);
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada",
                new CategoriaDto(saved.getIdCategoria(), saved.getNombre())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));

        if (libroRepository.existsByCategoria(cat)) {
            throw new BusinessException(
                "No se puede eliminar la categoría \"" + cat.getNombre() + "\": " +
                "hay libros asignados a ella. Reasígnalos primero");
        }
        categoriaRepository.delete(cat);
        return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada"));
    }

    @Data
    public static class CategoriaDto {
        private final Integer idCategoria;
        private final String  nombre;
    }

    @Data
    public static class NameRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
    }
}
