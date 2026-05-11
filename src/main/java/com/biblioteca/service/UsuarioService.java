package com.biblioteca.service;

import com.biblioteca.dto.request.UsuarioRequest;
import com.biblioteca.dto.request.UsuarioUpdateRequest;
import com.biblioteca.dto.response.UsuarioResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Cuenta;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.CuentaRepository;
import com.biblioteca.repository.RolRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> getAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        if (cuentaRepository.existsByUsuario(request.getUsuario())) {
            throw new BusinessException("El nombre de usuario ya está en uso: " + request.getUsuario());
        }
        if (request.getCorreo() != null && usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("El correo ya está registrado: " + request.getCorreo());
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol", request.getIdRol()));

        Cuenta cuenta = new Cuenta();
        cuenta.setUsuario(request.getUsuario());
        cuenta.setPassword(passwordEncoder.encode(
                request.getPassword() != null ? request.getPassword() : "temporal123"));
        cuenta.setRol(rol);
        cuenta.setEstado("activo");
        cuenta.setFechaCreacion(LocalDateTime.now());
        Cuenta savedCuenta = cuentaRepository.save(cuenta);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setTelefono(request.getTelefono());
        usuario.setFotoUrl(request.getFotoUrl());
        usuario.setCuenta(savedCuenta);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse update(Integer id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // ── Datos personales: solo actualiza el campo si viene en el request ──
        if (request.getNombre() != null) {
            if (request.getNombre().isBlank())
                throw new BusinessException("El nombre no puede estar vacío");
            usuario.setNombre(request.getNombre());
        }

        if (request.getApellido() != null) {
            if (request.getApellido().isBlank())
                throw new BusinessException("El apellido no puede estar vacío");
            usuario.setApellido(request.getApellido());
        }

        if (request.getCorreo() != null) {
            if (!request.getCorreo().equals(usuario.getCorreo())
                    && usuarioRepository.existsByCorreo(request.getCorreo())) {
                throw new BusinessException("El correo ya está registrado: " + request.getCorreo());
            }
            usuario.setCorreo(request.getCorreo());
        }

        if (request.getTelefono() != null) usuario.setTelefono(request.getTelefono());
        if (request.getFotoUrl()  != null) usuario.setFotoUrl(request.getFotoUrl());

        usuarioRepository.save(usuario);

        // ── Datos de cuenta: solo actualiza si viene en el request ────────────
        Cuenta cuenta = usuario.getCuenta();
        boolean cuentaModificada = false;

        if (request.getUsuario() != null && !request.getUsuario().isBlank()) {
            if (!cuenta.getUsuario().equals(request.getUsuario())
                    && cuentaRepository.existsByUsuario(request.getUsuario())) {
                throw new BusinessException("El nombre de usuario ya está en uso: " + request.getUsuario());
            }
            cuenta.setUsuario(request.getUsuario());
            cuentaModificada = true;
        }

        if (request.getIdRol() != null) {
            Rol rol = rolRepository.findById(request.getIdRol())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", request.getIdRol()));
            cuenta.setRol(rol);
            cuentaModificada = true;
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            cuenta.setPassword(passwordEncoder.encode(request.getPassword()));
            cuentaModificada = true;
        }

        if (cuentaModificada) cuentaRepository.save(cuenta);

        return toResponse(usuarioRepository.findById(id).get());
    }

    @Transactional
    public void delete(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        fileStorageService.eliminarImagen(usuario.getFotoUrl());

        Cuenta cuenta = usuario.getCuenta();
        usuarioRepository.delete(usuario);
        if (cuenta != null) {
            cuentaRepository.delete(cuenta);
        }
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .correo(u.getCorreo())
                .telefono(u.getTelefono())
                .fotoUrl(u.getFotoUrl())
                .idCuenta(u.getCuenta().getIdCuenta())
                .usuario(u.getCuenta().getUsuario())
                .rol(u.getCuenta().getRol().getNombre())
                .activo(u.getActivo())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }
}
