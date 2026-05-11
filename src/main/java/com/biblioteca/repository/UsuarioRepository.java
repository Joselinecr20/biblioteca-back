package com.biblioteca.repository;

import com.biblioteca.model.Cuenta;
import com.biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCuenta(Cuenta cuenta);
    Optional<Usuario> findByCuentaUsuario(String usuario);
    boolean existsByCorreo(String correo);
    long countByCuentaRolNombre(String rolNombre);
}
