package com.biblioteca.service;

import com.biblioteca.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    @Value("${file.upload.dir}")
    private String uploadDir;

    public String guardarImagen(MultipartFile file, String carpeta) {
        if (!esImagenValida(file)) {
            throw new BusinessException(
                "Archivo no válido. Solo se permiten imágenes JPG, JPEG, PNG o WEBP de hasta 5MB");
        }

        String extension = obtenerExtension(file.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + "." + extension;
        Path destino = Paths.get(uploadDir, carpeta, nombreArchivo);

        try {
            Path destinoAbsoluto = destino.toAbsolutePath();
            Files.createDirectories(destinoAbsoluto.getParent());
            file.transferTo(destinoAbsoluto);
        } catch (IOException e) {
            throw new BusinessException("Error al guardar la imagen: " + e.getMessage());
        }

        return "http://localhost:8080/uploads/" + carpeta + "/" + nombreArchivo;
    }

    public void eliminarImagen(String url) {
        if (url == null || url.isBlank()) return;
        if (!url.startsWith("http://localhost:8080/uploads/")) return;

        String rutaRelativa = url.replace("http://localhost:8080/uploads/", "");
        Path archivo = Paths.get(uploadDir, rutaRelativa);
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
        }
    }

    public Path resolverRuta(String url) {
        if (url == null || url.isBlank()) return null;
        if (!url.startsWith("http://localhost:8080/uploads/")) return null;
        String rutaRelativa = url.replace("http://localhost:8080/uploads/", "");
        return Paths.get(uploadDir, rutaRelativa).toAbsolutePath();
    }

    public boolean esImagenValida(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        if (file.getSize() > MAX_SIZE_BYTES) return false;
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) return false;
        String extension = obtenerExtension(originalFilename);
        return TIPOS_PERMITIDOS.contains(extension);
    }

    private String obtenerExtension(String nombreArchivo) {
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
    }
}
