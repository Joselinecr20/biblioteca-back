package com.biblioteca.service;

import com.biblioteca.dto.request.LibroRequest;
import com.biblioteca.dto.request.StockRequest;
import com.biblioteca.dto.response.LibroResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.*;
import com.biblioteca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final LibroBibliotecaRepository libroBibliotecaRepository;
    private final ReservaRepository reservaRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional(readOnly = true)
    public List<LibroResponse> getAll() {
        return libroRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LibroResponse getById(Integer id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
        return toResponse(libro);
    }

    @Transactional
    public LibroResponse create(LibroRequest request) {
        if (request.getIsbn() != null && libroRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.getIdCategoria()));

        Libro libro = new Libro();
        libro.setTitulo(request.getTitulo());
        libro.setEditorial(request.getEditorial());
        libro.setAnioPublicacion(request.getAnioPublicacion());
        libro.setDescripcion(request.getDescripcion());
        libro.setPortadaUrl(request.getPortadaUrl());
        libro.setIsbn(request.getIsbn());
        libro.setCategoria(categoria);

        Libro saved = libroRepository.save(libro);

        if (request.getIdAutores() != null && !request.getIdAutores().isEmpty()) {
            for (Integer idAutor : request.getIdAutores()) {
                Autor autor = autorRepository.findById(idAutor)
                        .orElseThrow(() -> new ResourceNotFoundException("Autor", idAutor));
                saved.getLibroAutores().add(new LibroAutor(saved, autor));
            }
            libroRepository.save(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public LibroResponse update(Integer id, LibroRequest request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));

        if (request.getIsbn() != null && !request.getIsbn().equals(libro.getIsbn())
                && libroRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.getIdCategoria()));

        libro.setTitulo(request.getTitulo());
        libro.setEditorial(request.getEditorial());
        libro.setAnioPublicacion(request.getAnioPublicacion());
        libro.setDescripcion(request.getDescripcion());
        libro.setPortadaUrl(request.getPortadaUrl());
        libro.setIsbn(request.getIsbn());
        libro.setCategoria(categoria);

        if (request.getIdAutores() != null) {
            libro.getLibroAutores().clear();
            for (Integer idAutor : request.getIdAutores()) {
                Autor autor = autorRepository.findById(idAutor)
                        .orElseThrow(() -> new ResourceNotFoundException("Autor", idAutor));
                libro.getLibroAutores().add(new LibroAutor(libro, autor));
            }
        }

        return toResponse(libroRepository.save(libro));
    }

    @Transactional
    public void delete(Integer id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));

        // 1. Bloquear si hay préstamo activo (el libro está en manos de alguien)
        if (prestamoRepository.existsByLibroAndEstado(libro, "activo")) {
            throw new BusinessException(
                "No se puede eliminar '" + libro.getTitulo() + "': " +
                "tiene préstamos activos en curso. Espera a que sean devueltos");
        }

        // 2. Bloquear si hay reservas pendientes o aprobadas sin atender
        if (reservaRepository.existsByLibroAndEstadoIn(
                libro, List.of("pendiente", "aprobada"))) {
            throw new BusinessException(
                "No se puede eliminar '" + libro.getTitulo() + "': " +
                "tiene reservas pendientes o aprobadas. Resuélvelas primero");
        }

        // 3. Bloquear si existe historial (préstamos o reservas pasadas)
        //    MySQL no permitirá eliminar el libro mientras exista cualquier
        //    reserva o préstamo que lo referencie, sin importar su estado
        if (prestamoRepository.existsByLibro(libro)) {
            throw new BusinessException(
                "No se puede eliminar '" + libro.getTitulo() + "': " +
                "tiene historial de préstamos registrado. " +
                "Los libros con historial se conservan para auditoría");
        }
        if (reservaRepository.existsByLibro(libro)) {
            throw new BusinessException(
                "No se puede eliminar '" + libro.getTitulo() + "': " +
                "tiene historial de reservas registrado. " +
                "Los libros con historial se conservan para auditoría");
        }

        // 4. Eliminar stock en bibliotecas
        //    (la relación librosBibliotecas no tiene cascade en la entidad,
        //    hay que hacerlo manualmente antes de borrar el libro)
        libroBibliotecaRepository.deleteAllByLibro(libro);

        // 5. Eliminar libro
        //    Las cascades de la entidad se encargan de libro_autor y libro_imagen
        libroRepository.delete(libro);
    }

    @Transactional
    public LibroResponse setStock(Integer idLibro, List<StockRequest.StockItem> items) {
        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", idLibro));

        for (StockRequest.StockItem item : items) {
            Biblioteca biblioteca = bibliotecaRepository.findById(item.getIdBiblioteca())
                    .orElseThrow(() -> new ResourceNotFoundException("Biblioteca", item.getIdBiblioteca()));

            LibroBiblioteca lb = libroBibliotecaRepository
                    .findByLibroAndBiblioteca(libro, biblioteca)
                    .orElseGet(() -> {
                        LibroBiblioteca n = new LibroBiblioteca();
                        n.setLibro(libro);
                        n.setBiblioteca(biblioteca);
                        n.setCantidadDisponible(item.getCantidadTotal());
                        return n;
                    });

            if (lb.getIdLibroBiblioteca() != null) {
                int totalOld   = lb.getCantidadTotal()      != null ? lb.getCantidadTotal()      : 0;
                int dispOld    = lb.getCantidadDisponible() != null ? lb.getCantidadDisponible() : 0;
                int prestados  = totalOld - dispOld;
                lb.setCantidadDisponible(Math.max(0, item.getCantidadTotal() - prestados));
            }
            lb.setCantidadTotal(item.getCantidadTotal());
            libroBibliotecaRepository.save(lb);
        }

        return toResponse(libroRepository.findById(idLibro).get());
    }

    public LibroResponse toResponse(Libro libro) {
        List<String> autores = libro.getLibroAutores().stream()
                .map(la -> la.getAutor().getNombre() + " " + la.getAutor().getApellido())
                .collect(Collectors.toList());

        List<LibroResponse.ImagenDto> imagenes = libro.getImagenes().stream()
                .map(img -> LibroResponse.ImagenDto.builder()
                        .idImagen(img.getIdImagen())
                        .url(img.getUrl())
                        .esPortada(img.getEsPortada())
                        .orden(img.getOrden())
                        .descripcion(img.getDescripcion())
                        .build())
                .collect(Collectors.toList());

        Integer disponible = libro.getLibrosBibliotecas().stream()
                .mapToInt(lb -> lb.getCantidadDisponible() != null ? lb.getCantidadDisponible() : 0)
                .sum();

        List<LibroResponse.BibliotecaDto> bibliotecas = libro.getLibrosBibliotecas().stream()
                .filter(lb -> lb.getCantidadDisponible() != null && lb.getCantidadDisponible() > 0)
                .map(lb -> LibroResponse.BibliotecaDto.builder()
                        .idBiblioteca(lb.getBiblioteca().getIdBiblioteca())
                        .nombre(lb.getBiblioteca().getNombre())
                        .cantidadDisponible(lb.getCantidadDisponible())
                        .cantidadTotal(lb.getCantidadTotal())
                        .build())
                .collect(Collectors.toList());

        return LibroResponse.builder()
                .idLibro(libro.getIdLibro())
                .titulo(libro.getTitulo())
                .editorial(libro.getEditorial())
                .anioPublicacion(libro.getAnioPublicacion())
                .descripcion(libro.getDescripcion())
                .portadaUrl(libro.getPortadaUrl())
                .isbn(libro.getIsbn())
                .idCategoria(libro.getCategoria() != null ? libro.getCategoria().getIdCategoria() : null)
                .categoria(libro.getCategoria() != null ? libro.getCategoria().getNombre() : null)
                .autores(autores)
                .imagenes(imagenes)
                .cantidadDisponible(disponible)
                .bibliotecas(bibliotecas)
                .build();
    }
}
