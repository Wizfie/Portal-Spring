package com.ms.springms.service.report;

import com.ms.springms.entity.*;
import com.ms.springms.repository.registration.RegistrationRepository;
import com.ms.springms.repository.event.StepRepository;
import com.ms.springms.repository.penjurian.TotalPenilaianRepository;
import com.ms.springms.repository.report.ReportLogRepository;
import com.ms.springms.repository.user.UserRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportAwardingService {

    @Autowired
    private StepRepository stepsRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private TotalPenilaianRepository totalPenilaianRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportLogRepository reportLogRepository;

    @Value("${TEMPLATE_PATH}")
    private String templatePath;


    public List<ReportLog> getAllReport () {
        return reportLogRepository.findAll();
    }

    public ByteArrayInputStream generateAwardingReport(Integer year, String noUrut, String bulan) throws IOException {
        int currentYear = year != null ? year : LocalDate.now().getYear();

        List<Steps> steps = stepsRepository.findStepsByYear(currentYear);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("No steps found for the provided year: " + currentYear);
        }

        // Cek apakah sudah ada log untuk tahun ini
        Optional<ReportLog> existingLog = reportLogRepository.findByYear(currentYear);

        String currentDateTitle;

        if (existingLog.isPresent()) {
            // Jika sudah ada, gunakan data dari log
            ReportLog log = existingLog.get();
            noUrut = log.getNoUrut();
            bulan = log.getBulan();
            LocalDateTime generatedDate = log.getGeneratedDate();

            // Use the stored date or generate new one if necessary
            DateFormat df = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            currentDateTitle = df.format(java.sql.Timestamp.valueOf(generatedDate));
        } else {
            // Jika belum ada, simpan data baru ke log
            ReportLog log = new ReportLog();
            log.setYear(currentYear);
            log.setNoUrut(noUrut);
            log.setBulan(bulan);
            log.setGeneratedDate(LocalDateTime.now());
            reportLogRepository.save(log);

            // Generate new current date title
            currentDateTitle = getCurrentFormattedDate();
        }

        // Load the Word template from the dynamic path
        File templateFile = new File(templatePath + "/Report-Awarding.docx");

        if (!templateFile.exists()) {
            throw new FileNotFoundException("Template file not found at path: " + templateFile.getPath());
        }

        InputStream inputStream = new FileInputStream(templateFile);
        XWPFDocument document = new XWPFDocument(inputStream);

        // Placeholder dan penggantinya
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("[current_year_title]", String.valueOf(currentYear));
        placeholders.put("[no_surat_title]", noUrut + "/LOG/JPL/ITG/" + bulan + "/" + currentYear);
        placeholders.put("[current_date_title]", currentDateTitle);
        placeholders.put("[penjurian_lapangan_date]", getPenjurianLapanganDate(steps));
        placeholders.put("[penjurian_presentasi_date]", getPenjurianPresentasiDate(steps));

        // Replace all placeholders per-paragraf dengan logging
        replaceTextInParagraphs(document, placeholders);

        // Isi tabel QCC dan SS
        List<Map<String, Object>> qccData = getFilteredData(currentYear, "QCC");
        fillTable(document, qccData, "Grup QCC");

        List<Map<String, Object>> ssData = getFilteredData(currentYear, "SS");
        fillTable(document, ssData, "SS / SSG");

        // Convert document to ByteArrayInputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
    private String getPenjurianLapanganDate(List<Steps> steps) {
        return getDateRangeForStep(steps, "Penjurian Lapangan");
    }

    private String getPenjurianPresentasiDate(List<Steps> steps) {
        return getDateRangeForStep(steps, "Penjurian Presentasi");
    }

    private String getDateRangeForStep(List<Steps> steps, String stepName) {
        for (Steps step : steps) {
            if (step.getStepName().equalsIgnoreCase(stepName)) {
                return formatRange(step.getStartDate(), step.getEndDate());
            }
        }
        return "";
    }

    private String formatRange(Date startDate, Date endDate) {
        DateFormat df = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        return df.format(startDate) + " – " + df.format(endDate);
    }

    private String getCurrentFormattedDate() {
        DateFormat df = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        return df.format(new Date());
    }

    private void replaceTextInParagraphs(XWPFDocument document, Map<String, String> placeholders) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            List<XWPFRun> runs = paragraph.getRuns();
            StringBuilder combinedText = new StringBuilder();

            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if (text != null) {
                    combinedText.append(text);
                }
            }

            String paragraphText = combinedText.toString();

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                if (paragraphText.contains(entry.getKey())) {
                    paragraphText = paragraphText.replace(entry.getKey(), entry.getValue());
                }
            }

            for (int i = runs.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(paragraphText);

            if (document.getParagraphs().indexOf(paragraph) <= 3) {
                newRun.setBold(true);
                newRun.setFontSize(16);
                newRun.setFontFamily("Maiandra GD");
            } else {
                newRun.setFontSize(10);
                newRun.setFontFamily("Maiandra GD");
            }
        }
    }

    private List<Map<String, Object>> getFilteredData(int year, String eventType) {
        List<Map<String, Object>> filteredData = new ArrayList<>();
        List<Registration> registrations = registrationRepository.findAll();

        for (Registration registration : registrations) {
            Event event = registration.getEvent();
            if (event.getEventYear().equals(String.valueOf(year)) && event.getEventType().equalsIgnoreCase(eventType)) {
                List<TotalPenilaian> scores = totalPenilaianRepository.findByTeamIdAndEventId(
                        registration.getTeam().getTeamId(), registration.getEvent().getEventId());

                if (!scores.isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    UserInfo user = userRepository.findById(Long.valueOf(registration.getCreatedBy())).orElse(null);

                    if (user != null) {
                        data.put("Dept.", user.getUsername());
                    }

                    if (eventType.equalsIgnoreCase("QCC")) {
                        data.put("GroupQCC", registration.getTeam().getTeamName());
                    } else if (eventType.equalsIgnoreCase("SS")) {
                        List<String> memberNames = registration.getTeam().getMembers().stream()
                                .map(TeamMember::getMemberName)
                                .collect(Collectors.toList());
                        data.put("SS / SSG", String.join(", ", memberNames));
                    }

                    data.put("Judul", registration.getJudul());
                    data.put("Nilai", calculateTotalScore(scores));

                    filteredData.add(data);
                }
            }
        }

        filteredData = sortAndRankData(filteredData);
        return filteredData;
    }

    private double calculateTotalScore(List<TotalPenilaian> scores) {
        double totalLapangan = 0;
        double totalPresentasi = 0;
        int count = 0; // Initialize count to track valid scores

        for (TotalPenilaian score : scores) {
            // Handle scoreLapangan
            String scoreLapanganStr = score.getScoreLapangan();
            if (scoreLapanganStr != null && !scoreLapanganStr.trim().isEmpty()) {
                totalLapangan += Double.parseDouble(scoreLapanganStr);
                count++; // Increment count for valid scores
            }

            // Handle scorePresentasi
            String scorePresentasiStr = score.getScorePresentasi();
            if (scorePresentasiStr != null && !scorePresentasiStr.trim().isEmpty()) {
                totalPresentasi += Double.parseDouble(scorePresentasiStr);
                count++; // Increment count for valid scores
            }
        }

        // Avoid division by zero if count is 0
        if (count == 0) {
            return 0; // or handle it accordingly
        }

        double avgLapangan = totalLapangan / count;
        double avgPresentasi = totalPresentasi / count;

        return (avgLapangan * 0.8) + (avgPresentasi * 0.2);
    }

    private List<Map<String, Object>> sortAndRankData(List<Map<String, Object>> data) {
        data.sort((a, b) -> Double.compare((double) b.get("Nilai"), (double) a.get("Nilai")));

        for (int i = 0; i < data.size(); i++) {
            if (i < 3) {
                data.get(i).put("Juara", i + 1);
            } else if (i < 6) {
                data.get(i).put("Juara", "Harapan " + (i - 2));
            } else {
                data.get(i).put("Juara", "-");
            }
        }

        return data;
    }

    private void fillTable(XWPFDocument document, List<Map<String, Object>> data, String tableName) {
        XWPFTable table = findTableByText(document, tableName);

        if (table == null) {
            throw new IllegalArgumentException("Tabel '" + tableName + "' tidak ditemukan dalam dokumen.");
        }

        for (Map<String, Object> rowData : data) {
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(String.valueOf(data.indexOf(rowData) + 1));  // No
            row.getCell(1).setText((String) rowData.get("Dept."));  // Dept
            row.getCell(2).setText((String) rowData.get(tableName.equals("Grup QCC") ? "GroupQCC" : "SS / SSG"));  // Group QCC atau SS/SSG
            row.getCell(3).setText((String) rowData.get("Judul"));  // Judul
            String formattedScore = String.format("%.2f", (double) rowData.get("Nilai"));
            row.getCell(4).setText(formattedScore);  // Nilai
            row.getCell(5).setText(String.valueOf(rowData.get("Juara")));  // Juara
        }

        applyTableBorders(table);
        setTableFont(table, "Maiandra GD", 10);
        setTableAlignment(table);
    }

    private XWPFTable findTableByText(XWPFDocument document, String searchText) {
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    if (cell.getText().contains(searchText)) {
                        return table;
                    }
                }
            }
        }
        return null;
    }

    private void applyTableBorders(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                CTTcPr tcPr = cell.getCTTc().addNewTcPr();
                CTTcBorders borders = tcPr.addNewTcBorders();
                borders.addNewBottom().setVal(STBorder.SINGLE);
                borders.addNewTop().setVal(STBorder.SINGLE);
                borders.addNewLeft().setVal(STBorder.SINGLE);
                borders.addNewRight().setVal(STBorder.SINGLE);
            }
        }
    }

    private void setTableFont(XWPFTable table, String fontFamily, int fontSize) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    for (XWPFRun run : paragraph.getRuns()) {
                        run.setFontFamily(fontFamily);
                        run.setFontSize(fontSize);
                    }
                }
            }
        }
    }

    private void setTableAlignment(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                }
            }
        }
    }
}
