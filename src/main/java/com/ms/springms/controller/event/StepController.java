package com.ms.springms.controller.event;

import com.ms.springms.entity.Steps;
import com.ms.springms.service.event.StepService;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/step")
public class StepController {

    @Autowired
    private StepService stepService;


    // Endpoint untuk menambahkan atau memperbarui beberapa langkah
    @PostMapping("/addOrUpdate/{eventId}")
    public ResponseEntity<?> addOrUpdateSteps(@PathVariable Long eventId, @RequestBody List<Steps> stepsList) {
        try {
            List<Steps> updatedSteps = stepService.addOrUpdateSteps(eventId, stepsList);
            return new ResponseEntity<>(updatedSteps, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            // Jika eventId tidak ditemukan
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            // Jika ada kesalahan validasi input
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            // Penanganan umum untuk error lainnya
            return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{stepId}")
    public ResponseEntity<?> deleteStep(@PathVariable Long stepId) {
        try {
            stepService.deleteStep(stepId);
            return new ResponseEntity<>("Step deleted successfully", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
