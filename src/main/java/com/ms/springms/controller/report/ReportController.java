package com.ms.springms.controller.report;

import com.ms.springms.entity.ReportLog;
import com.ms.springms.service.report.ReportAwardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportAwardingService reportAwardingService;

    @GetMapping("/generate-awarding")
    public ResponseEntity<InputStreamResource> generateAwardingReport(
            @RequestParam(required = true) Integer year,
            @RequestParam(required = false) String noUrut,
            @RequestParam(required = false) String bulan)
    {
        year = year != null ? year : LocalDate.now().getYear();
        try {
            ByteArrayInputStream bis = reportAwardingService.generateAwardingReport(year, noUrut , bulan);

            if (bis == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=Surat_Keputusan_Juara_" + year + "_.docx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(new InputStreamResource(bis));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null    );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/log")
    public ResponseEntity<List<ReportLog>> reportList(){
        try {
        List<ReportLog> logList = reportAwardingService.getAllReport();
        return ResponseEntity.status(HttpStatus.OK).body(logList);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
