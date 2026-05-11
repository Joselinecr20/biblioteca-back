package com.biblioteca.controller;

import com.biblioteca.dto.request.ReservaRequest;
import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.ReservaResponse;
import com.biblioteca.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> getAll() {
        List<ReservaResponse> reservas = reservaService.getAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + reservas.size() + " reserva(s)", reservas));
    }

    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> getMis(Authentication auth) {
        List<ReservaResponse> reservas = reservaService.getMis(auth.getName());
        return ResponseEntity.ok(
                ApiResponse.ok("Se encontraron " + reservas.size() + " reserva(s) tuyas", reservas));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponse>> crear(@Valid @RequestBody ReservaRequest request,
                                                               Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Reserva creada exitosamente. Espera la aprobación del bibliotecario",
                        reservaService.crear(auth.getName(), request)));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<Void>> aprobar(@PathVariable Integer id, Authentication auth) {
        reservaService.aprobar(id, auth.getName());
        return ResponseEntity.ok(
                ApiResponse.ok("Reserva #" + id + " aprobada. El préstamo fue registrado automáticamente"));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<Void>> rechazar(@PathVariable Integer id,
                                                       @RequestBody(required = false) Map<String, String> body,
                                                       Authentication auth) {
        String motivo = body != null ? body.getOrDefault("observaciones", "") : "";
        reservaService.rechazar(id, motivo, auth.getName());
        return ResponseEntity.ok(
                ApiResponse.ok("Reserva #" + id + " rechazada correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable Integer id, Authentication auth) {
        reservaService.cancelar(id, auth.getName());
        return ResponseEntity.ok(
                ApiResponse.ok("Reserva #" + id + " cancelada correctamente"));
    }
}
