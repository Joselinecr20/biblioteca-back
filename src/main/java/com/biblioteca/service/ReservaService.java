package com.biblioteca.service;

import com.biblioteca.dto.request.ReservaRequest;
import com.biblioteca.dto.response.ReservaResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.*;
import com.biblioteca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final LibroBibliotecaRepository libroBibliotecaRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final CuentaRepository cuentaRepository;
    private final PrestamoService prestamoService;

    @Transactional(readOnly = true)
    public List<ReservaResponse> getAll() {
        return reservaRepository.findAllByOrderByFechaReservaDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> getMis(String username) {
        Usuario usuario = getUsuarioByUsername(username);
        return reservaRepository.findByUsuarioOrderByFechaReservaDesc(usuario).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservaResponse crear(String username, ReservaRequest request) {
        Usuario usuario = getUsuarioByUsername(username);

        // Validar multas pendientes
        if (multaRepository.existsByPrestamoUsuarioAndEstado(usuario, "pendiente")) {
            throw new BusinessException("No puedes hacer reservas con multas pendientes de pago");
        }

        // Validar límite de préstamos activos
        int maxPrestamos = usuario.getCuenta().getRol().getMaxPrestamos();
        long prestamosActivos = prestamoRepository.countByUsuarioAndEstado(usuario, "activo");
        if (prestamosActivos >= maxPrestamos) {
            throw new BusinessException("Has alcanzado el límite de " + maxPrestamos + " préstamos activos");
        }

        Libro libro = libroRepository.findById(request.getIdLibro())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", request.getIdLibro()));

        Biblioteca biblioteca = bibliotecaRepository.findById(request.getIdBiblioteca())
                .orElseThrow(() -> new ResourceNotFoundException("Biblioteca", request.getIdBiblioteca()));

        // Validar disponibilidad
        LibroBiblioteca lb = libroBibliotecaRepository.findByLibroAndBiblioteca(libro, biblioteca)
                .orElseThrow(() -> new BusinessException("El libro no está disponible en esa biblioteca"));

        if (lb.getCantidadDisponible() == null || lb.getCantidadDisponible() <= 0) {
            throw new BusinessException("No hay ejemplares disponibles de ese libro en la biblioteca seleccionada");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setBiblioteca(biblioteca);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setFechaExpiracion(LocalDate.now().plusDays(3));
        reserva.setEstado("pendiente");
        reserva.setDiasPrestamo(request.getDiasPrestamo() != null ? request.getDiasPrestamo() : 7);

        return toResponse(reservaRepository.save(reserva));
    }

    @Transactional
    public void aprobar(Integer idReserva, String username) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", idReserva));

        if (!"pendiente".equals(reserva.getEstado())) {
            throw new BusinessException("Solo se pueden aprobar reservas en estado pendiente");
        }

        Cuenta bibliotecario = cuentaRepository.findByUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        LibroBiblioteca lb = libroBibliotecaRepository
                .findByLibroAndBiblioteca(reserva.getLibro(), reserva.getBiblioteca())
                .orElseThrow(() -> new BusinessException("Inventario no encontrado"));

        if (lb.getCantidadDisponible() == null || lb.getCantidadDisponible() <= 0) {
            throw new BusinessException("No hay ejemplares disponibles para aprobar la reserva");
        }

        // Descontar disponibilidad
        lb.setCantidadDisponible(lb.getCantidadDisponible() - 1);
        libroBibliotecaRepository.save(lb);

        // Crear préstamo
        prestamoService.crearDesdereserva(reserva, bibliotecario);

        // Actualizar reserva
        reserva.setEstado("aprobada");
        reserva.setAtendidaPor(bibliotecario);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void rechazar(Integer idReserva, String motivo, String username) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", idReserva));

        if (!"pendiente".equals(reserva.getEstado())) {
            throw new BusinessException("Solo se pueden rechazar reservas en estado pendiente");
        }

        Cuenta bibliotecario = cuentaRepository.findByUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        reserva.setEstado("rechazada");
        reserva.setAtendidaPor(bibliotecario);
        reserva.setObservaciones(motivo);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelar(Integer idReserva, String username) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", idReserva));

        Usuario usuario = getUsuarioByUsername(username);

        if (!reserva.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new BusinessException("No puedes cancelar una reserva que no es tuya");
        }

        if (!"pendiente".equals(reserva.getEstado())) {
            throw new BusinessException("Solo se pueden cancelar reservas en estado pendiente");
        }

        reserva.setEstado("cancelada");
        reservaRepository.save(reserva);
    }

    private Usuario getUsuarioByUsername(String username) {
        return usuarioRepository.findByCuentaUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para la cuenta: " + username));
    }

    private ReservaResponse toResponse(Reserva r) {
        return ReservaResponse.builder()
                .idReserva(r.getIdReserva())
                .idUsuario(r.getUsuario().getIdUsuario())
                .nombreUsuario(r.getUsuario().getNombre() + " " + r.getUsuario().getApellido())
                .idLibro(r.getLibro().getIdLibro())
                .tituloLibro(r.getLibro().getTitulo())
                .portadaLibro(r.getLibro().getPortadaUrl())
                .idBiblioteca(r.getBiblioteca().getIdBiblioteca())
                .nombreBiblioteca(r.getBiblioteca().getNombre())
                .fechaReserva(r.getFechaReserva())
                .fechaExpiracion(r.getFechaExpiracion())
                .estado(r.getEstado())
                .observaciones(r.getObservaciones())
                .diasPrestamo(r.getDiasPrestamo())
                .build();
    }
}
