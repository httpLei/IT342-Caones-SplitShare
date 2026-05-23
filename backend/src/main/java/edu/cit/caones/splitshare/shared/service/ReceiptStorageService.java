package edu.cit.caones.splitshare.shared.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Service
public class ReceiptStorageService {

    private final Path storageDir;
    private final HttpClient httpClient;
    private final String supabaseUrl;
    private final String supabaseServiceRoleKey;
    private final String supabaseBucket;

    public ReceiptStorageService(
            @Value("${application.upload.dir:uploads}") String uploadDir,
            @Value("${supabase.storage.url:}") String supabaseUrl,
            @Value("${supabase.storage.service-role-key:}") String supabaseServiceRoleKey,
            @Value("${supabase.storage.bucket:receipts}") String supabaseBucket
    ) {
        this.storageDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.httpClient = HttpClient.newHttpClient();
        this.supabaseUrl = trimTrailingSlash(supabaseUrl);
        this.supabaseServiceRoleKey = supabaseServiceRoleKey;
        this.supabaseBucket = supabaseBucket;
    }

    public String store(MultipartFile file) throws IOException {
        String fileName = buildFileName(file);
        if (isSupabaseStorageConfigured()) {
            return storeInSupabase(file, fileName);
        }

        return storeLocally(file, fileName);
    }

    private String storeLocally(MultipartFile file, String fileName) throws IOException {
        Files.createDirectories(storageDir);

        Path target = storageDir.resolve(fileName);
        Files.copy(file.getInputStream(), target);

        return "/uploads/" + fileName;
    }

    private String storeInSupabase(MultipartFile file, String fileName) throws IOException {
        String objectPath = "receipts/" + fileName;
        String encodedObjectPath = encodePath(objectPath);
        String bucket = encodePath(supabaseBucket);
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodedObjectPath;
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + encodedObjectPath;

        HttpRequest request = HttpRequest.newBuilder(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + supabaseServiceRoleKey)
                .header("apikey", supabaseServiceRoleKey)
                .header("Content-Type", contentType(file))
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Supabase Storage upload failed with status "
                        + response.statusCode() + ": " + response.body());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Supabase Storage upload was interrupted", ex);
        }

        return publicUrl;
    }

    private String buildFileName(MultipartFile file) {
        String originalName = file.getOriginalFilename() == null ? "receipt" : file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        return UUID.randomUUID() + extension;
    }

    private boolean isSupabaseStorageConfigured() {
        return !supabaseUrl.isBlank() && !supabaseServiceRoleKey.isBlank() && !supabaseBucket.isBlank();
    }

    private String contentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    private String encodePath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }
}
