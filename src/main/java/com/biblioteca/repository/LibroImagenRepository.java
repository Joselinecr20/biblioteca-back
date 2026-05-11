package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroImagenRepository extends JpaRepository<LibroImagen, Integer> {
    List<LibroImagen> findByLibro(Libro libro);
}
