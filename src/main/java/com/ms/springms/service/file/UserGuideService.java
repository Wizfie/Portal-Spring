package com.ms.springms.service.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserGuideService {

    @Value("${GUIDE_DIR}")
    private String guideDir;

    private static final Logger logger = LoggerFactory.getLogger(UserGuideService.class);


    public List<String> getUserGuideFiles() throws Exception {
        Path guidePath = Paths.get(guideDir);

        // Check if directory exists
        if (!Files.exists(guidePath)) {
            throw new Exception("Directory not found: " + guideDir);
        }

        // List all files in the directory
        return Files.list(guidePath)
                .filter(Files::isRegularFile) // Only include regular files
                .map(Path::getFileName) // Get the filename
                .map(Path::toString) // Convert to string
                .collect(Collectors.toList()); // Collect as a list
    }


    public ResponseEntity<Resource> openFile(String filename) throws Exception {
        Path filePath = Paths.get(guideDir).resolve(filename).normalize();

        // Check if the file exists
        if (!Files.exists(filePath)) {
            throw new Exception("File not found: " + filename);
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new Exception("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new Exception("Malformed URL for file: " + filename);
        }

        // Set the content type based on the file type
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        logger.info("Opening file: {} with detected content type: {}", filename, contentType);


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
