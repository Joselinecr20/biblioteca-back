package com.biblioteca.service;

import com.biblioteca.dto.response.DashboardAdminResponse;
import com.biblioteca.dto.response.DashboardBibliotecarioResponse;
import com.biblioteca.dto.response.DashboardEstudianteResponse;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.LibroBibliotecaRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.MultaRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LibroRepository libroRepository;
    private final LibroBibliotecaRepository libroBibliotecaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaRepository reservaRepository;
    private final MultaRepository multaRepository;

    @Transactional(readOnly = true)
    public DashboardAdminResponse getDashboardAdmin(String username) {
        String fotoUrl = usuarioRepository.findByCuentaUsuario(username)
                .map(Usuario::getFotoUrl)
                .orElse(null);

        return DashboardAdminResponse.builder()
                .fotoUrl(fotoUrl)
                .totalLibros(libroRepository.count())
                .totalEjemplares(libroBibliotecaRepository.sumCantidadTotal())
                .ejemplaresDisponibles(libroBibliotecaRepository.sumCantidadDisponible())
                .totalUsuarios(usuarioRepository.count())
                .totalEstudiantes(usuarioRepository.countByCuentaRolNombre("estudiante"))
                .totalBibliotecarios(usuarioRepository.countByCuentaRolNombre("bibliotecario"))
                .prestamosActivos(prestamoRepository.countByEstado("activo"))
                .totalPrestamos(prestamoRepository.count())
                .reservasPendientes(reservaRepository.countByEstado("pendiente"))
                .totalReservas(reservaRepository.count())
                .multasPendientes(multaRepository.countByEstado("pendiente"))
                .montoMultasPendientes(multaRepository.sumMontoByEstado("pendiente"))
                .montoMultasPagadas(multaRepository.sumMontoByEstado("pagado"))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardBibliotecarioResponse getDashboardBibliotecario(String username) {
        String fotoUrl = usuarioRepository.findByCuentaUsuario(username)
                .map(Usuario::getFotoUrl)
                .orElse(null);

        List<DashboardBibliotecarioResponse.LibroBajoStockResponse> bajoStock =
                libroBibliotecaRepository.findByCantidadDisponibleLessThanEqual(2).stream()
                        .map(lb -> DashboardBibliotecarioResponse.LibroBajoStockResponse.builder()
                                .idLibro(lb.getLibro().getIdLibro())
                                .titulo(lb.getLibro().getTitulo())
                                .biblioteca(lb.getBiblioteca().getNombre())
                                .cantidadDisponible(lb.getCantidadDisponible())
                                .cantidadTotal(lb.getCantidadTotal())
                                .build())
                        .toList();

        return DashboardBibliotecarioResponse.builder()
                .fotoUrl(fotoUrl)
                .totalLibros(libroRepository.count())
                .totalEjemplares(libroBibliotecaRepository.sumCantidadTotal())
                .ejemplaresDisponibles(libroBibliotecaRepository.sumCantidadDisponible())
                .librosBajoStock(bajoStock)
                .reservasPendientes(reservaRepository.countByEstado("pendiente"))
                .prestamosActivos(prestamoRepository.countByEstado("activo"))
                .multasPendientes(multaRepository.countByEstado("pendiente"))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardEstudianteResponse getDashboardEstudiante(String username) {
        Usuario usuario = usuarioRepository.findByCuentaUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        BigDecimal montoMultasPendientes = multaRepository
                .sumMontoByPrestamoUsuarioAndEstado(usuario, "pendiente");

        return DashboardEstudianteResponse.builder()
                .usuario(username)
                .fotoUrl(usuario.getFotoUrl())
                .nombreCompleto(usuario.getNombre() + " " + usuario.getApellido())
                .prestamosActivos(prestamoRepository.countByUsuarioAndEstado(usuario, "activo"))
                .totalPrestamos(prestamoRepository.findByUsuario(usuario).size())
                .reservasPendientes(reservaRepository.countByUsuarioAndEstado(usuario, "pendiente"))
                .totalReservas(reservaRepository.findByUsuario(usuario).size())
                .multasPendientes(multaRepository.countByPrestamoUsuarioAndEstado(usuario, "pendiente"))
                .montoMultasPendientes(montoMultasPendientes != null ? montoMultasPendientes : BigDecimal.ZERO)
                .build();
    }
}
