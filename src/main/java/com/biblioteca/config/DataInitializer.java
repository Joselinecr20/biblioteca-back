package com.biblioteca.config;

import com.biblioteca.model.Cuenta;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.CuentaRepository;
import com.biblioteca.repository.RolRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DataInitializer: verificando usuarios de prueba ===");

        inicializar("admin",          "admin123", 1,
                    "admin",   "sistema",   "admin@biblioteca.edu.sv",  "0000-0000");

        inicializar("bibliotecario1", "bib123",   2,
                    "carlos",  "martinez",  "carlos@biblioteca.edu.sv", "7000-0001");

        inicializar("estudiante1",    "est123",   3,
                    "maria",   "gonzalez",  "maria@uees.edu.sv",        "7000-0002");

        log.info("=== DataInitializer: finalizado ===");
    }

    private void inicializar(String username, String password, Integer idRol,
                              String nombre, String apellido, String correo, String telefono) {

        Optional<Rol> rolOpt = rolRepository.findById(idRol);
        if (rolOpt.isEmpty()) {
            log.warn("Rol con id {} no encontrado, omitiendo usuario '{}'", idRol, username);
            return;
        }
        Rol rol = rolOpt.get();

        Optional<Cuenta> cuentaOpt = cuentaRepository.findByUsuario(username);

        Cuenta cuenta;
        if (cuentaOpt.isPresent()) {
            cuenta = cuentaOpt.get();
            // Si la contraseña no tiene formato BCrypt, actualizarla
            if (!cuenta.getPassword().startsWith("$2")) {
                cuenta.setPassword(passwordEncoder.encode(password));
                cuentaRepository.save(cuenta);
                log.info("Contraseña de '{}' actualizada a BCrypt", username);
            } else {
                log.info("Usuario '{}' ya existe con BCrypt, omitiendo", username);
            }
        } else {
            cuenta = new Cuenta();
            cuenta.setUsuario(username);
            cuenta.setPassword(passwordEncoder.encode(password));
            cuenta.setRol(rol);
            cuenta.setEstado("activo");
            cuenta.setFechaCreacion(LocalDateTime.now());
            cuenta = cuentaRepository.save(cuenta);
            log.info("Cuenta '{}' creada correctamente", username);
        }

        // Crear registro en usuario si no existe para esta cuenta
        if (usuarioRepository.findByCuenta(cuenta).isEmpty()) {
            // Verificar también por correo para evitar duplicados
            if (correo != null && usuarioRepository.existsByCorreo(correo)) {
                log.info("Correo '{}' ya existe, omitiendo creación de usuario para '{}'", correo, username);
                return;
            }
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setCuenta(cuenta);
            usuario.setActivo(true);
            usuario.setFechaRegistro(LocalDateTime.now());
            usuarioRepository.save(usuario);
            log.info("Usuario '{}' creado en tabla usuario", username);
        }
    }
}
