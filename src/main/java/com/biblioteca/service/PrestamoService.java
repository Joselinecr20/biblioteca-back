package com.biblioteca.service;

import com.biblioteca.dto.request.DevolucionRequest;
import com.biblioteca.dto.response.PrestamoResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.*;
import com.biblioteca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroBibliotecaRepository libroBibliotecaRepository;
    private final MultaRepository multaRepository;
    private final UsuarioRepository usuarioRepository;

    // Días de préstamo según rol
    private static final int DIAS_PRESTAMO_ESTUDIANTE = 15;
    private static final int DIAS_PRESTAMO_BIBLIOTECARIO = 30;
    private static final int DIAS_PRESTAMO_ADMIN = 60;

    @Transactional(readOnly = true)
    public List<PrestamoResponse> getAll() {
        return prestamoRepository.findAllByOrderByFechaPrestamoDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> getMis(String username) {
        Usuario usuario = usuarioRepository.findByCuentaUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return prestamoRepository.findByUsuarioOrderByFechaPrestamoDesc(usuario).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrestamoResponse devolver(Integer idPrestamo, DevolucionRequest request, String username) {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo", idPrestamo));

        if (!"activo".equals(prestamo.getEstado())) {
            throw new BusinessException("Solo se pueden devolver préstamos en estado activo");
        }

        prestamo.setFechaDevolucionReal(request.getFechaDevolucionReal());

        // Sumar disponibilidad en libro_biblioteca
        libroBibliotecaRepository
                .findByLibroAndBiblioteca(prestamo.getLibro(), prestamo.getBiblioteca())
                .ifPresent(lb -> {
                    lb.setCantidadDisponible(lb.getCantidadDisponible() + 1);
                    libroBibliotecaRepository.save(lb);
                });

        // Calcular retraso y generar multa si aplica
        if (request.getFechaDevolucionReal().isAfter(prestamo.getFechaDevolucion())) {
            long diasRetraso = ChronoUnit.DAYS.between(
                    prestamo.getFechaDevolucion(),
                    request.getFechaDevolucionReal());

            BigDecimal monto = BigDecimal.valueOf(diasRetraso).multiply(BigDecimal.valueOf(0.25));

            Multa multa = new Multa();
            multa.setPrestamo(prestamo);
            multa.setDiasRetraso((int) diasRetraso);
            multa.setMonto(monto);
            multa.setEstado("pendiente");
            multa.setFechaGeneracion(LocalDateTime.now());
            multaRepository.save(multa);

            prestamo.setEstado("retrasado");
        } else {
            prestamo.setEstado("devuelto");
        }

        return toResponse(prestamoRepository.save(prestamo));
    }

    // Llamado desde ReservaService al aprobar una reserva
    @Transactional
    public void crearDesdereserva(Reserva reserva, Cuenta aprobadoPor) {
        int diasPrestamo = switch (aprobadoPor.getRol().getNombre()) {
            case "admin" -> DIAS_PRESTAMO_ADMIN;
            case "bibliotecario" -> DIAS_PRESTAMO_BIBLIOTECARIO;
            default -> DIAS_PRESTAMO_ESTUDIANTE;
        };

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(reserva.getUsuario());
        prestamo.setLibro(reserva.getLibro());
        prestamo.setBiblioteca(reserva.getBiblioteca());
        prestamo.setReserva(reserva);
        prestamo.setAprobadoPor(aprobadoPor);
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setFechaDevolucion(LocalDateTime.now().toLocalDate().plusDays(diasPrestamo));
        prestamo.setEstado("activo");

        prestamoRepository.save(prestamo);
    }

    private PrestamoResponse toResponse(Prestamo p) {
        return PrestamoResponse.builder()
                .idPrestamo(p.getIdPrestamo())
                .idUsuario(p.getUsuario().getIdUsuario())
                .nombreUsuario(p.getUsuario().getNombre() + " " + p.getUsuario().getApellido())
                .idLibro(p.getLibro().getIdLibro())
                .tituloLibro(p.getLibro().getTitulo())
                .portadaLibro(p.getLibro().getPortadaUrl())
                .idBiblioteca(p.getBiblioteca().getIdBiblioteca())
                .nombreBiblioteca(p.getBiblioteca().getNombre())
                .idReserva(p.getReserva() != null ? p.getReserva().getIdReserva() : null)
                .fechaPrestamo(p.getFechaPrestamo())
                .fechaDevolucion(p.getFechaDevolucion())
                .fechaDevolucionReal(p.getFechaDevolucionReal())
                .estado(p.getEstado())
                .build();
    }
}
