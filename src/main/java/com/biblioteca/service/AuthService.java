package com.biblioteca.service;

import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegistroRequest;
import com.biblioteca.dto.request.ResetPasswordRequest;
import com.biblioteca.dto.response.LoginResponse;
import com.biblioteca.dto.response.UsuarioResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Cuenta;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.CuentaRepository;
import com.biblioteca.repository.RolRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Cuenta cuenta = cuentaRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!"activo".equals(cuenta.getEstado())) {
            throw new BusinessException("La cuenta está inactiva");
        }

        if (!passwordEncoder.matches(request.getPassword(), cuenta.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        cuenta.setUltimoAcceso(LocalDateTime.now());
        cuentaRepository.save(cuenta);

        String token = jwtUtil.generateToken(
                cuenta.getUsuario(),
                cuenta.getIdCuenta(),
                cuenta.getRol().getNombre()
        );

        Usuario usuario = usuarioRepository.findByCuenta(cuenta).orElse(null);
        String nombre = usuario != null ? usuario.getNombre() : cuenta.getUsuario();
        String apellido = usuario != null ? usuario.getApellido() : "";

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .idCuenta(cuenta.getIdCuenta())
                .usuario(cuenta.getUsuario())
                .rol(cuenta.getRol().getNombre())
                .nombre(nombre)
                .apellido(apellido)
                .build();
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
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
        cuenta.setPassword(passwordEncoder.encode(request.getPassword()));
        cuenta.setRol(rol);
        cuenta.setEstado("activo");
        cuenta.setFechaCreacion(LocalDateTime.now());
        Cuenta savedCuenta = cuentaRepository.save(cuenta);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setTelefono(request.getTelefono());
        usuario.setCuenta(savedCuenta);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        Usuario saved = usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .idUsuario(saved.getIdUsuario())
                .nombre(saved.getNombre())
                .apellido(saved.getApellido())
                .correo(saved.getCorreo())
                .telefono(saved.getTelefono())
                .idCuenta(savedCuenta.getIdCuenta())
                .usuario(savedCuenta.getUsuario())
                .rol(rol.getNombre())
                .activo(saved.getActivo())
                .fechaRegistro(saved.getFechaRegistro())
                .build();
    }

    @Transactional
    public void resetPassword(Integer idCuenta, ResetPasswordRequest request) {
        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", idCuenta));
        cuenta.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        cuentaRepository.save(cuenta);
    }
}
