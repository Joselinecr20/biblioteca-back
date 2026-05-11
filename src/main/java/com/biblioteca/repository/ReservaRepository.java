package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByUsuario(Usuario usuario);
    List<Reserva> findByUsuarioOrderByFechaReservaDesc(Usuario usuario);
    List<Reserva> findAllByOrderByFechaReservaDesc();
    boolean existsByLibro(Libro libro);
    boolean existsByLibroAndEstadoIn(Libro libro, List<String> estados);
    long countByEstado(String estado);
    long countByUsuarioAndEstado(Usuario usuario, String estado);
}
