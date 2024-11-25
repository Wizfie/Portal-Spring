package com.ms.springms.service.file;

import com.ms.springms.entity.Steps;
import com.ms.springms.entity.Registration;
import com.ms.springms.entity.UploadFileGroup;
import com.ms.springms.entity.UploadFiles;
import com.ms.springms.repository.files.UploadFileGroupRepository;
import com.ms.springms.repository.files.UploadFileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UploadFilesService {

    @Autowired
    private UploadFileRepository uploadFileRepository;

    @Autowired
    private UploadFileGroupRepository uploadFileGroupRepository;

    private Path fileStorageLocation;

    @Value("${file.upload-dir}")
    private String uploadDir;


//    public UploadFilesService() {
//    }
    @PostConstruct // Metode ini akan dipanggil setelah semua dependency di-inject
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create directory for uploaded files", ex);
        }
    }




    //  ukuran maksimal file (10 MB) dan tipe file yang diizinkan
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    //  tipe file Excel, Word, dan PowerPoint ke dalam daftar ALLOWED_FILE_TYPES
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/png", "image/jpeg", "application/pdf",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // Excel
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // Word
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation" // PowerPoint
    );

    //  ekstensi file Excel, Word, dan PowerPoint ke dalam daftar ALLOWED_EXTENSIONS
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "png", "jpg", "jpeg", "pdf",
            "xls", "xlsx", // Excel
            "doc", "docx", // Word
            "ppt", "pptx" // PowerPoint
    );

    public void uploadFiles(List<MultipartFile> files, String eventName, String teamName, Steps steps, Registration registration) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("The list of files is empty");
        }

        // Create a new UploadFileGroup instance
        UploadFileGroup uploadFileGroup = new UploadFileGroup();
        uploadFileGroup.setApprovalStatus("PENDING");

        List<UploadFiles> uploadFilesList = new ArrayList<>();

        for (MultipartFile file : files) {
            // Validasi ukuran file
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File size exceeds the allowed limit of 10 MB");
            }

            // Validasi tipe file
            String fileType = file.getContentType();
            if (!ALLOWED_FILE_TYPES.contains(fileType)) {
                throw new IllegalArgumentException("File type " + fileType + " is not allowed");
            }

            // Sanitasi nama file
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            // Validasi path traversal
            if (fileName.contains("..")) {
                throw new IllegalArgumentException("Invalid file path");
            }

            // Validasi ekstensi file
            String fileExtension = getFileExtension(fileName);
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                throw new IllegalArgumentException("File extension " + fileExtension + " is not allowed");
            }

            try {
                String uniqueFileName = generateUniqueFileName(fileName, eventName, teamName);
                Path uploadPath = Paths.get(uploadDir);

                // Buat folder jika belum ada
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(file.getInputStream(), filePath);

                // Simpan atribut file
                UploadFiles uploadFile = new UploadFiles();
                uploadFile.setFileName(uniqueFileName);
                uploadFile.setFilePath(filePath.toString());
                uploadFile.setUploadedBy(teamName);
                uploadFile.setUploadedAt(LocalDateTime.now());
                uploadFile.setSteps(steps);
                uploadFile.setRegistration(registration);
                uploadFile.setUploadFileGroup(uploadFileGroup);

                if (steps.getStepName().toLowerCase().matches(".*final$")) {
                    uploadFile.setRisalah(true);
                } else {
                    uploadFile.setRisalah(false);
                }

                uploadFilesList.add(uploadFile);
            } catch (IOException ex) {
                // Tangani kesalahan untuk file ini dan lanjutkan ke file berikutnya
                throw new IOException("Failed to upload file: " + file.getOriginalFilename() + " due to " + ex);
            }
        }

        // Set daftar file yang telah diupload ke UploadFileGroup
        uploadFileGroup.setFiles(uploadFilesList);

        // Simpan UploadFileGroup yang akan cascade dan menyimpan instansi UploadFiles
        uploadFileGroupRepository.save(uploadFileGroup);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    public void updateFileById(Long fileId, MultipartFile file) throws IOException {
        // check file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Retrieve file by ID
            Optional<UploadFiles> optionalUploadFile = uploadFileRepository.findById(fileId);
            if (optionalUploadFile.isPresent()) {
                UploadFiles uploadFile = optionalUploadFile.get();

                // Generate new unique file name
                String fileName = file.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(fileName);
                Path uploadPath = Paths.get(uploadDir);

                // Create folder if not exists
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFileName);

                Files.copy(file.getInputStream(), filePath);

                // Update file attributes
                uploadFile.setFileName(uniqueFileName);
                uploadFile.setFilePath(filePath.toString());
                uploadFile.setUploadedAt(LocalDateTime.now());
                uploadFile.setApprovalStatus("PENDING");


                uploadFileRepository.save(uploadFile);
            } else {
                throw new IllegalArgumentException("File not found with ID: " + fileId);
            }
        } catch (IOException ex) {
            throw new IOException("Failed to upload file: " + ex);
        }
    }


    public void approveFile(Long groupId, String approvalStatus) {
        Optional<UploadFileGroup> optionalFile = uploadFileGroupRepository.findById(groupId);
        if (optionalFile.isPresent()) {
            UploadFileGroup uploadFile = optionalFile.get();

            uploadFile.setApprovalStatus(approvalStatus);


            List<UploadFiles> uploadFilesList = uploadFile.getFiles();
            for (UploadFiles uploadFiles : uploadFilesList) {
                uploadFiles.setApprovalStatus(approvalStatus);
            }
            uploadFileGroupRepository.save(uploadFile);
        } else {
            throw new RuntimeException("File not found with id: " + groupId);
        }
    }

    public void rejectFile(Long fileId, Long groupId, String description, MultipartFile newFile) throws IOException {
        Optional<UploadFiles> optionalFile = uploadFileRepository.findById(fileId);
        Optional<UploadFileGroup> status = uploadFileGroupRepository.findById(groupId);


        if (optionalFile.isPresent() && status.isPresent()) {
            UploadFiles uploadFile = optionalFile.get();
            UploadFileGroup fileStatus = status.get();

            fileStatus.setApprovalStatus("REJECT");

            if (newFile != null) {
                String fileName = newFile.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(fileName );
                Path uploadPath = Paths.get(uploadDir);

                // Buat folder jika belum ada
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFileName);

                Files.copy(newFile.getInputStream(), filePath);



                // Perbarui atribut file
                uploadFile.setResponseFileName(uniqueFileName);
                uploadFile.setResponseFilePath(filePath.toString());
                uploadFile.setResponseUploadedAt(LocalDateTime.now());
                uploadFile.setResponseDescription(description);
            }

            uploadFileRepository.save(uploadFile);
        } else {
            throw new RuntimeException("File not found with id: " + fileId);
        }
    }



    public Resource downloadFile(String fileName) throws MalformedURLException {
        Path  filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()){
            return resource;
        } else {
                throw new RuntimeException("File no found " + fileName);
        }
    }




    private String generateUniqueFileName(String originalFileName, String... additionalParams) {
        String fileNameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        StringBuilder uniqueFileNameBuilder = new StringBuilder(fileNameWithoutExtension);

        // Menambahkan parameter tambahan jika ada
        for (String param : additionalParams) {
            uniqueFileNameBuilder.append("_").append(param);
        }

        uniqueFileNameBuilder.append(fileExtension);
        String uniqueFileName = uniqueFileNameBuilder.toString();

        // Memastikan nama file unik dengan mengganti karakter yang tidak valid
        uniqueFileName = uniqueFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        return uniqueFileName;
    }
    }
