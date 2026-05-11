package com.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LibroAutorId implements Serializable {

    @Column(name = "id_libro")
    private Integer idLibro;

    @Column(name = "id_autor")
    private Integer idAutor;
}
