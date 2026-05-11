package com.biblioteca.controller;

import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.dto.response.DashboardAdminResponse;
import com.biblioteca.dto.response.DashboardBibliotecarioResponse;
import com.biblioteca.dto.response.DashboardEstudianteResponse;
import com.biblioteca.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<DashboardAdminResponse>> getAdmin(Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.ok("Dashboard de administrador", dashboardService.getDashboardAdmin(auth.getName())));
    }

    @GetMapping("/bibliotecario")
    public ResponseEntity<ApiResponse<DashboardBibliotecarioResponse>> getBibliotecario(Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.ok("Dashboard de bibliotecario", dashboardService.getDashboardBibliotecario(auth.getName())));
    }

    @GetMapping("/estudiante")
    public ResponseEntity<ApiResponse<DashboardEstudianteResponse>> getEstudiante(Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.ok("Dashboard del estudiante", dashboardService.getDashboardEstudiante(auth.getName())));
    }
}
