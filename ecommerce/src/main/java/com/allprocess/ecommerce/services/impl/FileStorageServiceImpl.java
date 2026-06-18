package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.services.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path storageLocation;

    @PostConstruct
    public void init() {
        storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento: " + uploadDir, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Formato no permitido. Use JPG, PNG o WEBP.");
        }
        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID() + ext;
        try {
            Path target = storageLocation.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path target = storageLocation.resolve(filename).normalize();
            if (!target.startsWith(storageLocation)) return;
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo {}: {}", filename, e.getMessage());
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = storageLocation.resolve(filename).normalize();
            if (!file.startsWith(storageLocation)) {
                throw new RuntimeException("Ruta no permitida");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Archivo no encontrado: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el archivo: " + filename, e);
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
