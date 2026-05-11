package com.biblioteca.repository;

import com.biblioteca.model.Biblioteca;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroBibliotecaRepository extends JpaRepository<LibroBiblioteca, Integer> {
    Optional<LibroBiblioteca> findByLibroAndBiblioteca(Libro libro, Biblioteca biblioteca);
    void deleteAllByLibro(Libro libro);
    List<LibroBiblioteca> findByCantidadDisponibleLessThanEqual(Integer cantidad);

    @Query("SELECT COALESCE(SUM(lb.cantidadTotal), 0) FROM LibroBiblioteca lb")
    Long sumCantidadTotal();

    @Query("SELECT COALESCE(SUM(lb.cantidadDisponible), 0) FROM LibroBiblioteca lb")
    Long sumCantidadDisponible();
}
