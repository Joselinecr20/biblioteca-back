package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "libro")
@Getter
@Setter
@NoArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Integer idLibro;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "editorial", length = 255)
    private String editorial;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "portada_url", length = 255)
    private String portadaUrl;

    @Column(name = "isbn", unique = true, length = 50)
    private String isbn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ToString.Exclude
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LibroAutor> libroAutores = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LibroImagen> imagenes = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "libro")
    private List<LibroBiblioteca> librosBibliotecas = new ArrayList<>();
}
