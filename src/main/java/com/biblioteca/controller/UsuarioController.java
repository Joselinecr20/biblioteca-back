package com.biblioteca.controller;

import com.biblioteca.dto.request.UsuarioRequest;
import com.biblioteca.dto.request.UsuarioUpdateRequest;
import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.UsuarioResponse;
import com.biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> getAll() {
        List<UsuarioResponse> usuarios = usuarioService.getAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + usuarios.size() + " usuario(s)", usuarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Usuario encontrado", usuarioService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Usuario creado exitosamente", usuarioService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(@PathVariable Integer id,
                                                                @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Usuario actualizado exitosamente", usuarioService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado exitosamente", null));
    }
}
