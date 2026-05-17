package com.biblioteca.controller;

import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.BibliotecaResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Biblioteca;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.repository.LibroBibliotecaRepository;
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
@RequestMapping("/bibliotecas")
@RequiredArgsConstructor
public class BibliotecaController {

    private final BibliotecaRepository      bibliotecaRepository;
    private final LibroBibliotecaRepository libroBibliotecaRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BibliotecaResponse>>> getAll() {
        List<BibliotecaResponse> list = bibliotecaRepository.findAll().stream()
                .map(b -> new BibliotecaResponse(b.getIdBiblioteca(), b.getNombre(), b.getUbicacion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Se encontraron " + list.size() + " biblioteca(s)", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BibliotecaResponse>> create(@Valid @RequestBody BibliotecaRequest request) {
        Biblioteca b = new Biblioteca();
        b.setNombre(request.getNombre().trim());
        b.setUbicacion(request.getUbicacion() != null ? request.getUbicacion().trim() : null);
        Biblioteca saved = bibliotecaRepository.save(b);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Biblioteca creada",
                        new BibliotecaResponse(saved.getIdBiblioteca(), saved.getNombre(), saved.getUbicacion())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BibliotecaResponse>> update(@PathVariable Integer id,
                                                                   @Valid @RequestBody BibliotecaRequest request) {
        Biblioteca b = bibliotecaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca", id));
        b.setNombre(request.getNombre().trim());
        b.setUbicacion(request.getUbicacion() != null ? request.getUbicacion().trim() : null);
        Biblioteca saved = bibliotecaRepository.save(b);
        return ResponseEntity.ok(ApiResponse.ok("Biblioteca actualizada",
                new BibliotecaResponse(saved.getIdBiblioteca(), saved.getNombre(), saved.getUbicacion())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        Biblioteca b = bibliotecaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca", id));

        if (libroBibliotecaRepository.existsByBiblioteca(b)) {
            throw new BusinessException(
                "No se puede eliminar la biblioteca \"" + b.getNombre() + "\": " +
                "tiene libros asignados. Retira el stock primero");
        }
        bibliotecaRepository.delete(b);
        return ResponseEntity.ok(ApiResponse.ok("Biblioteca eliminada"));
    }

    @Data
    public static class BibliotecaRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        private String ubicacion;
    }
}
