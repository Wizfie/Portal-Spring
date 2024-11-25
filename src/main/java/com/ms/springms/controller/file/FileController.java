package com.ms.springms.controller.file;


import com.ms.springms.entity.Steps;
import com.ms.springms.entity.Registration;
import com.ms.springms.service.file.UploadFilesService;
import com.ms.springms.service.file.UserGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private UploadFilesService uploadFilesService;

    @Autowired
    private UserGuideService userGuideService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                              @RequestParam("eventName") String eventName,
                                              @RequestParam("teamName") String teamName,
                                              @RequestParam("steps") Steps steps,
                                              @RequestParam("registration") Registration registration) {
        try {
            // Coba lakukan upload file
            uploadFilesService.uploadFiles(files, eventName, teamName, steps, registration);
            return ResponseEntity.status(HttpStatus.OK).body("Files uploaded successfully");

        } catch (IllegalArgumentException e) {
            // Handle jika ada masalah validasi (misalnya ukuran file te    rlalu besar, tipe file tidak diizinkan, dll.)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());

        } catch (IOException e) {
            // Handle error yang berhubungan dengan IO (misalnya masalah penyimpanan file)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files due to server error: " + e.getMessage());

        } catch (Exception e) {
            // Handle error umum lainnya
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @PutMapping("/update/{fileId}")
    public ResponseEntity<String> updateFile(@PathVariable Long fileId,
                                             @RequestParam("file") MultipartFile file)
                                              {
        try {
            uploadFilesService.updateFileById(fileId, file);
            return ResponseEntity.status(HttpStatus.OK).body("File berhasil diperbarui");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gagal memperbarui file: " + e.getMessage());
        }
    }

    @PutMapping("/approve/{fileId}")
    public ResponseEntity<String> approveFile(@PathVariable Long fileId, @RequestParam String approvalStatus) {
        try {
            uploadFilesService.approveFile(fileId, approvalStatus);
            return ResponseEntity.ok("File approval status updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/reject")
    public ResponseEntity<String> rejectFile(@RequestParam Long fileId,
                                             @RequestParam Long groupId,
                                             @RequestParam("description") String description,
                                             @RequestParam("file") MultipartFile file
                                             ) {
        try {
            uploadFilesService.rejectFile(fileId ,groupId, description , file);
            return ResponseEntity.ok("Reject File Success");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }



    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws MalformedURLException {
        Resource resource = uploadFilesService.downloadFile(fileName);

        String contentType = "application/octet-stream";
        try {
            contentType = resource.getURL().openConnection().getContentType();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/userguide")
    public ResponseEntity<List<String>> getUserGuideFiles() {
        try {
            List<String> files = userGuideService.getUserGuideFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/userguide/open/{filename:.+}")
    public ResponseEntity<Resource> openFile(@PathVariable String filename) {
        try {
            return userGuideService.openFile(filename);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

}
