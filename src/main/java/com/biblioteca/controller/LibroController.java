package com.biblioteca.controller;

import com.biblioteca.dto.request.LibroRequest;
import com.biblioteca.dto.request.StockRequest;
import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.LibroResponse;
import com.biblioteca.service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LibroResponse>>> getAll() {
        List<LibroResponse> libros = libroService.getAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + libros.size() + " libro(s)", libros));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibroResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Libro encontrado", libroService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LibroResponse>> create(@Valid @RequestBody LibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Libro creado exitosamente", libroService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LibroResponse>> update(@PathVariable Integer id,
                                                              @Valid @RequestBody LibroRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Libro actualizado exitosamente", libroService.update(id, request)));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<LibroResponse>> setStock(@PathVariable Integer id,
                                                               @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Stock actualizado exitosamente", libroService.setStock(id, request.getItems())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        libroService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Libro eliminado exitosamente"));
    }
}
