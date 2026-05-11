package com.biblioteca.repository;

import com.biblioteca.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {
    Optional<Cuenta> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
}
