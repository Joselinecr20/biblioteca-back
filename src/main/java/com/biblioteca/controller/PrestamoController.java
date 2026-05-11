package com.biblioteca.controller;

import com.biblioteca.dto.request.DevolucionRequest;
import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.PrestamoResponse;
import com.biblioteca.service.PrestamoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrestamoResponse>>> getAll() {
        List<PrestamoResponse> prestamos = prestamoService.getAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + prestamos.size() + " préstamo(s)", prestamos));
    }

    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<List<PrestamoResponse>>> getMis(Authentication auth) {
        List<PrestamoResponse> prestamos = prestamoService.getMis(auth.getName());
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + prestamos.size() + " préstamo(s) tuyo(s)", prestamos));
    }

    @PostMapping("/{id}/devolver")
    public ResponseEntity<ApiResponse<PrestamoResponse>> devolver(@PathVariable Integer id,
                                                                   @Valid @RequestBody DevolucionRequest request,
                                                                   Authentication auth) {
        PrestamoResponse resultado = prestamoService.devolver(id, request, auth.getName());
        String mensaje = "retrasado".equals(resultado.getEstado())
                ? "Devolución registrada. Se generó una multa por " + resultado.getEstado()
                : "Devolución registrada exitosamente. Libro devuelto a tiempo";
        return ResponseEntity.ok(ApiResponse.ok(mensaje, resultado));
    }
}
