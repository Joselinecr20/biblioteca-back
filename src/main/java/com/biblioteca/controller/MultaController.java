package com.biblioteca.controller;

import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.MultaResponse;
import com.biblioteca.service.MultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/multas")
@RequiredArgsConstructor
public class MultaController {

    private final MultaService multaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MultaResponse>>> getAll() {
        List<MultaResponse> multas = multaService.getAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + multas.size() + " multa(s)", multas));
    }

    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<List<MultaResponse>>> getMis(Authentication auth) {
        List<MultaResponse> multas = multaService.getMis(auth.getName());
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + multas.size() + " multa(s) tuya(s)", multas));
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<MultaResponse>> pagar(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Multa #" + id + " registrada como pagada exitosamente",
                        multaService.pagar(id)));
    }
}
