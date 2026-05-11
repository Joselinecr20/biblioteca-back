package com.biblioteca.service;

import com.biblioteca.dto.response.MultaResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Multa;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.MultaRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultaService {

    private final MultaRepository multaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MultaResponse> getAll() {
        return multaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MultaResponse> getMis(String username) {
        Usuario usuario = usuarioRepository.findByCuentaUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return multaRepository.findByPrestamoUsuario(usuario).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MultaResponse pagar(Integer idMulta) {
        Multa multa = multaRepository.findById(idMulta)
                .orElseThrow(() -> new ResourceNotFoundException("Multa", idMulta));

        if ("pagada".equals(multa.getEstado())) {
            throw new BusinessException("La multa ya ha sido pagada");
        }

        multa.setEstado("pagada");
        multa.setFechaPago(LocalDateTime.now());

        return toResponse(multaRepository.save(multa));
    }

    private MultaResponse toResponse(Multa m) {
        Usuario usuario = m.getPrestamo().getUsuario();
        return MultaResponse.builder()
                .idMulta(m.getIdMulta())
                .idPrestamo(m.getPrestamo().getIdPrestamo())
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombre() + " " + usuario.getApellido())
                .tituloLibro(m.getPrestamo().getLibro().getTitulo())
                .monto(m.getMonto())
                .diasRetraso(m.getDiasRetraso())
                .estado(m.getEstado())
                .fechaGeneracion(m.getFechaGeneracion())
                .fechaPago(m.getFechaPago())
                .build();
    }
}
