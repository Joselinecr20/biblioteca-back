package com.biblioteca.controller;

import com.biblioteca.dto.response.ApiResponse;
import com.biblioteca.exception.BusinessException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.UsuarioRepository;
import com.biblioteca.security.CuentaUserDetails;
import com.biblioteca.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    @PostMapping("/usuario/{idUsuario}")
    public ResponseEntity<ApiResponse<String>> subirFotoUsuario(
            @PathVariable Integer idUsuario,
            @RequestParam("imagen") MultipartFile imagen,
            @AuthenticationPrincipal CuentaUserDetails userDetails) {

        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));

        if (!esAdmin) {
            Usuario autenticado = usuarioRepository.findByCuenta(userDetails.getCuenta())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado", 0));
            if (!autenticado.getIdUsuario().equals(idUsuario)) {
                throw new BusinessException("Solo puedes modificar tu propia foto de perfil");
            }
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario));

        fileStorageService.eliminarImagen(usuario.getFotoUrl());

        String url = fileStorageService.guardarImagen(imagen, "usuarios");
        usuario.setFotoUrl(url);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(ApiResponse.ok("Foto de perfil actualizada exitosamente", url));
    }

    @PostMapping("/libro/{idLibro}")
    public ResponseEntity<ApiResponse<String>> subirPortadaLibro(
            @PathVariable Integer idLibro,
            @RequestParam("imagen") MultipartFile imagen) {

        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", idLibro));

        fileStorageService.eliminarImagen(libro.getPortadaUrl());

        String url = fileStorageService.guardarImagen(imagen, "libros");
        libro.setPortadaUrl(url);
        libroRepository.save(libro);

        return ResponseEntity.ok(ApiResponse.ok("Portada del libro actualizada exitosamente", url));
    }

    @GetMapping("/usuario/{idUsuario}/imagen")
    public ResponseEntity<Resource> obtenerImagenUsuario(@PathVariable Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario));

        Path ruta = fileStorageService.resolverRuta(usuario.getFotoUrl());
        return servirImagen(ruta);
    }

    @GetMapping("/libro/{idLibro}/imagen")
    public ResponseEntity<Resource> obtenerImagenLibro(@PathVariable Integer idLibro) {
        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", idLibro));

        Path ruta = fileStorageService.resolverRuta(libro.getPortadaUrl());
        return servirImagen(ruta);
    }

    private ResponseEntity<Resource> servirImagen(Path ruta) {
        try {
            if (ruta == null || !Files.exists(ruta)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(ruta.toUri());
            String contentType = Files.probeContentType(ruta);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
