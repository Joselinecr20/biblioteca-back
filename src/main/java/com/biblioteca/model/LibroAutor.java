package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "libro_autor")
@Getter
@Setter
@NoArgsConstructor
public class LibroAutor {

    @EmbeddedId
    private LibroAutorId id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idLibro")
    @JoinColumn(name = "id_libro")
    private Libro libro;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idAutor")
    @JoinColumn(name = "id_autor")
    private Autor autor;

    public LibroAutor(Libro libro, Autor autor) {
        this.id = new LibroAutorId(libro.getIdLibro(), autor.getIdAutor());
        this.libro = libro;
        this.autor = autor;
    }
}
