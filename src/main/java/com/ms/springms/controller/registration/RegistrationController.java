package com.ms.springms.controller.registration;

import com.ms.springms.model.registration.RegistrationDTO;
import com.ms.springms.model.registration.RegistrationForYelyel;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import com.ms.springms.model.registration.RegistrationRequest;
import com.ms.springms.model.registration.RegistrationResponseDTO;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.service.registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/get-all")
    public ResponseEntity<PageResponse<List<RegistrationResponseDTO>>> getAllRegistrations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long createdBy) {
        Page<RegistrationResponseDTO> registrationPage;
        Pageable pageable = PageRequest.of(
                page - 1, // Mengurangi nomor halaman dengan 1
                pageSize,
                Sort.by("createdAt").descending()
        );
        if (createdBy != null) {
            registrationPage = registrationService.getRegistrationsByCreatedBy(createdBy, pageable);
        } else {
            registrationPage = registrationService.getAllRegistrations(pageable);
        }
        PageResponse<List<RegistrationResponseDTO>> response = new PageResponse<>(
                registrationPage.getContent(),
                registrationPage.getNumber() + 1,
                registrationPage.getTotalPages(),
                registrationPage.getSize(),
                registrationPage.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest registrationRequest) {
        try {
            registrationService.registration(registrationRequest);
            return ResponseEntity.ok("Registration successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<RegistrationResponseDTO> getRegistrationById(@PathVariable Long registrationId) {
        try {
            RegistrationResponseDTO registrationResponseDTO = registrationService.getRegistrationById(registrationId);
            return ResponseEntity.ok(registrationResponseDTO);
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan saat mengambil registrasi dengan ID " + registrationId, ex);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDTO> updateRegistrationTitle(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newTitle = requestBody.get("judul");

        if (newTitle == null || newTitle.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            RegistrationResponseDTO responseDTO = registrationService.updateRegistrationTitle(id, newTitle);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponse<List<RegistrationDTO>>> getAllRegistrationsWithUsernames(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        // Panggil service untuk mendapatkan data dengan pagination dan filter pencarian
        Page<RegistrationDTO> registrationsPage = registrationService.getAllRegistrationsWithDetailsAndFilesJPQL(search, page, size);

        // Buat PageResponse berdasarkan hasil pencarian dan pagination
        PageResponse<List<RegistrationDTO>> pageResponse = new PageResponse<>(
                registrationsPage.getContent(),
                page,
                registrationsPage.getTotalPages(),
                size,
                registrationsPage.getTotalElements()
        );

        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/yelyel-user")
    public ResponseEntity<?> getRegistrationsWithScores(@RequestParam(required = false) Long juriId) {
        try {
            List<RegistrationForYelyel> registrations;

            // Cek apakah juriId ada atau tidak
            if (juriId != null) {
                // Jika juriId ada, panggil metode untuk mendapatkan data dengan juriId
                registrations = registrationService.getRegistrationsWithScores(juriId);
            } else {
                // Jika juriId tidak ada, panggil metode untuk mendapatkan semua data tanpa juriId
                registrations = registrationService.getAllRegistrationsWithoutJuri();
            }

            return new ResponseEntity<>(registrations, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Mengembalikan pesan error dengan status 500
            return new ResponseEntity<>("Error occurred while fetching registrations: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}