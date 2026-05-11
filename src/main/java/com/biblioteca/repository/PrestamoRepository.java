package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {
    List<Prestamo> findByUsuario(Usuario usuario);
    List<Prestamo> findByUsuarioOrderByFechaPrestamoDesc(Usuario usuario);
    List<Prestamo> findAllByOrderByFechaPrestamoDesc();
    long countByEstado(String estado);
    long countByUsuarioAndEstado(Usuario usuario, String estado);
    boolean existsByLibro(Libro libro);
    boolean existsByLibroAndEstado(Libro libro, String estado);
}
