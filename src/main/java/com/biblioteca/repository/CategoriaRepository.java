package com.biblioteca.repository;

import com.biblioteca.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
