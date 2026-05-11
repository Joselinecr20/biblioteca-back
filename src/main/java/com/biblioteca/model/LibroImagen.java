package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "libro_imagen")
@Getter
@Setter
@NoArgsConstructor
public class LibroImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "es_portada")
    private Boolean esPortada;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "descripcion", length = 100)
    private String descripcion;
}
