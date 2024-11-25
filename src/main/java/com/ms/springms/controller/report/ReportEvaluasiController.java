package com.ms.springms.controller.report;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.service.report.ReportEvaluasiService;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/report")
public class ReportEvaluasiController {

    @Autowired
    private ReportEvaluasiService reportEvaluasiService;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/evaluasi-lapangan")
    public ResponseEntity<byte[]> generateEvaluasiLapanganReport(
            @RequestParam("userId") Long userId,
            @RequestParam("eventId") Long eventId,
            @RequestParam("year") Long year) {
        try {
            String[] userDetails = getUserAndEventDetails(userId, eventId);
            String username = userDetails[0];
            String eventType = userDetails[1];

            byte[] reportData = reportEvaluasiService.reportEvaluasiLapangan(userId, eventId, year);

            HttpHeaders headers = new HttpHeaders();
            String fileName = String.format("evaluasi-lapangan-report_%s_%s_%d.xlsx", username, eventType, year);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(reportData);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/evaluasi-presentasi")
    public ResponseEntity<byte[]> generateEvaluasiPresentasiReport(
            @RequestParam("userId") Long userId,
            @RequestParam("eventId") Long eventId,
            @RequestParam("year") Long year) {
        try {
            String[] userDetails = getUserAndEventDetails(userId, eventId);
            String username = userDetails[0];
            String eventType = userDetails[1];

            byte[] reportData = reportEvaluasiService.reportEvaluasiPresentasi(userId, eventId, year);

            HttpHeaders headers = new HttpHeaders();
            String fileName = String.format("evaluasi-presentasi-report_%s_%s_%d.xlsx", username, eventType, year);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(reportData);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/evaluasi-yelyel")
    public ResponseEntity<byte[]> getYelyelReport(
            @RequestParam Long deptId,
            @RequestParam Long juriId,
            @RequestParam int year) {
        try {
            byte[] reportData = reportEvaluasiService.reportEvaluasiYelyel(deptId, juriId, year);

            // Jika data valid, kembalikan laporan
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=yelyel_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
        }
        catch (IndexOutOfBoundsException e) {
            System.err.println("No data available: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // Kode 404 untuk tidak ada data
                    .body("No data available for the specified parameters.".getBytes());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.".getBytes());
        }
    }





    private String[] getUserAndEventDetails(Long userId, Long eventId) {
        UserInfo user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        return new String[] { user.getUsername(), event.getEventType() };
    }
}
