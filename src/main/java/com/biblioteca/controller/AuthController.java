package com.biblioteca.controller;

import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegistroRequest;
import com.biblioteca.dto.request.ResetPasswordRequest;
import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.LoginResponse;
import com.biblioteca.dto.response.UsuarioResponse;
import com.biblioteca.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Inicio de sesión exitoso", authService.login(request)));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioResponse>> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Usuario registrado exitosamente", authService.registrar(request)));
    }

    @PutMapping("/reset-password/{idCuenta}")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Integer idCuenta,
                                                            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(idCuenta, request);
        return ResponseEntity.ok(
                ApiResponse.ok("Contraseña actualizada correctamente"));
    }
}
