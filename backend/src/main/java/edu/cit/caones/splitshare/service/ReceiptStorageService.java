package edu.cit.caones.splitshare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class ReceiptStorageService {

    private final Path storageDir;

    public ReceiptStorageService(@Value("${application.upload.dir:uploads}") String uploadDir) {
        this.storageDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        Files.createDirectories(storageDir);

        String originalName = file.getOriginalFilename() == null ? "receipt" : file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = storageDir.resolve(fileName);
        Files.copy(file.getInputStream(), target);

        return "/uploads/" + fileName;
    }
}