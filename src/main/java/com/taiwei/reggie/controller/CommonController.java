package com.taiwei.reggie.controller;

import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.config.ReggieProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Image upload (admin) and download (admin + customers). Files live in {@code reggie.image-path}; the images of
 * the bundled demo data are served from the classpath ({@code seed-images/}) when no uploaded file matches.
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Tag(name = "Images")
public class CommonController {

    private static final Map<String, MediaType> IMAGE_TYPES = Map.of(
            "jpg", MediaType.IMAGE_JPEG,
            "jpeg", MediaType.IMAGE_JPEG,
            "png", MediaType.IMAGE_PNG,
            "gif", MediaType.IMAGE_GIF,
            "webp", MediaType.parseMediaType("image/webp"));

    private final Path baseDir;

    public CommonController(ReggieProperties properties) {
        this.baseDir = Paths.get(properties.getImagePath()).toAbsolutePath().normalize();
    }

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new CustomException("no file uploaded");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!IMAGE_TYPES.containsKey(extension)) {
            throw new CustomException("only jpg, png, gif and webp images can be uploaded");
        }
        Files.createDirectories(baseDir);
        String fileName = UUID.randomUUID() + "." + extension;
        file.transferTo(baseDir.resolve(fileName));
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        String extension = extensionOf(name);
        MediaType mediaType = IMAGE_TYPES.get(extension);
        // a bare file name only: no directories, no ".." - nothing outside the image directory can be read
        if (mediaType == null || name.contains("/") || name.contains("\\") || name.contains("..")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path file = baseDir.resolve(name).normalize();
        InputStream in = null;
        if (file.startsWith(baseDir) && Files.isRegularFile(file)) {
            in = Files.newInputStream(file);
        } else {
            ClassPathResource seed = new ClassPathResource("seed-images/" + name);
            if (seed.exists()) {
                in = seed.getInputStream();
            }
        }
        if (in == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(mediaType.toString());
        response.setHeader("Cache-Control", "public, max-age=3600");
        try (InputStream source = in; OutputStream out = response.getOutputStream()) {
            source.transferTo(out);
        }
    }

    private static String extensionOf(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') < 0) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
