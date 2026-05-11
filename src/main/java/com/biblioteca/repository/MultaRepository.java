package com.biblioteca.repository;

import com.biblioteca.model.Multa;
import com.biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Integer> {
    List<Multa> findByPrestamoUsuario(Usuario usuario);
    boolean existsByPrestamoUsuarioAndEstado(Usuario usuario, String estado);
    Optional<Multa> findByPrestamo_IdPrestamo(Integer idPrestamo);

    long countByEstado(String estado);
    long countByPrestamoUsuarioAndEstado(Usuario usuario, String estado);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Multa m WHERE m.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") String estado);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Multa m WHERE m.prestamo.usuario = :usuario AND m.estado = :estado")
    BigDecimal sumMontoByPrestamoUsuarioAndEstado(@Param("usuario") Usuario usuario, @Param("estado") String estado);
}
