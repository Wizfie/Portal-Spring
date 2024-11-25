package com.ms.springms.service.report;

import com.ms.springms.entity.Team;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.entity.penjurian.lapangan.DetailEvaluasiLapangan;
import com.ms.springms.entity.penjurian.lapangan.PertanyaanLapangan;
import com.ms.springms.entity.penjurian.presentasi.DetailEvaluasiPresentasi;
import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import com.ms.springms.entity.penjurian.yelyel.DetailEvaluasiYelyel;
import com.ms.springms.entity.penjurian.yelyel.PointPenilaianYelyel;
import com.ms.springms.model.penjurian.yelyel.DetailEvaluasiYelyelDTO;
import com.ms.springms.model.penjurian.yelyel.HasilEvaluasiYelyelDTO;
import com.ms.springms.repository.penjurian.lapangan.DetailEvaluasiLapanganRepository;
import com.ms.springms.repository.penjurian.lapangan.PertanyaanLapanganRepository;
import com.ms.springms.repository.penjurian.presentasi.DetailEvaluasiPresentasiRepository;
import com.ms.springms.repository.penjurian.presentasi.PertanyaanPresentasiRepository;
import com.ms.springms.repository.penjurian.yelyel.DetailEvaluasiYelyelRepository;
import com.ms.springms.repository.penjurian.yelyel.PointPenilaianYelyelRepository;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.repository.user.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportEvaluasiService {

    private static final Logger logger = LoggerFactory.getLogger(ReportEvaluasiService.class);

    @Autowired
    private DetailEvaluasiLapanganRepository detailEvaluasiLapanganRepository;
    @Autowired
    private PertanyaanLapanganRepository pertanyaanLapanganRepository;
    @Autowired
    private DetailEvaluasiPresentasiRepository detailEvaluasiPresentasiRepository;
    @Autowired
    private PertanyaanPresentasiRepository pertanyaanPresentasiRepository;

    @Autowired
    private PointPenilaianYelyelRepository pointPenilaianYelyelRepository;

    @Autowired
    private DetailEvaluasiYelyelRepository detailEvaluasiYelyelRepository;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRepository userRepository;

    public byte[] reportEvaluasiLapangan(Long userId, Long eventId, Long year) throws IOException {
        logger.info("Generating lapangan evaluation report for userId: {}, eventId: {}, year: {}", userId, eventId, year);

        // Ambil data user berdasarkan userId
        UserInfo user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("User fetched: {}", user);

        // Ambil semua evaluasi berdasarkan userId, eventId, dan tahun
        List<DetailEvaluasiLapangan> evaluations = detailEvaluasiLapanganRepository.findByUserIdAndEventIdAndYear(userId, eventId, year);
        logger.info("Evaluations fetched for eventId {}, year {}: {}", eventId, year, evaluations.size());

        // Ambil semua pertanyaanId unik dari evaluations
        List<Long> pertanyaanIds = evaluations.stream()
                .map(DetailEvaluasiLapangan::getPertanyaanId)
                .distinct()
                .collect(Collectors.toList());

        // Ambil semua pertanyaan yang sesuai dengan pertanyaanId yang ada di evaluations dan active = true
        List<PertanyaanLapangan> questions = pertanyaanLapanganRepository.findByIdIn(pertanyaanIds);
        logger.info("Questions fetched for provided pertanyaanIds: {}", questions.size());

        // Ambil teamId unik dari evaluations
        List<Long> teamIds = evaluations.stream()
                .map(DetailEvaluasiLapangan::getTeamId)
                .distinct()
                .collect(Collectors.toList());

        // Ambil data tim berdasarkan teamId yang unik
        List<Team> teams = teamRepository.findAllById(teamIds);
        logger.info("Teams fetched: {}", teams.size());

        // Map untuk menyimpan skor per teamId dan questionId
        Map<Long, Map<Long, String>> teamScores = evaluations.stream()
                .collect(Collectors.groupingBy(
                        DetailEvaluasiLapangan::getTeamId,
                        Collectors.toMap(
                                DetailEvaluasiLapangan::getPertanyaanId,
                                DetailEvaluasiLapangan::getScore
                        )
                ));

        // Load template Excel
        Workbook workbook;
        try (InputStream templateStream = new ClassPathResource("templates/evaluation_report.xlsx").getInputStream()) {
            workbook = new XSSFWorkbook(templateStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Mengakses sheet dan memodifikasi template
        Sheet sheet = workbook.getSheet("Evaluation Report");
        if (sheet == null) {
            throw new RuntimeException("Sheet 'Evaluation Report' not found in template.");
        }

        generateLapanganReport(sheet, workbook, user, teams, questions, teamScores);

        // Tulis data ke byte array
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        } finally {
            workbook.close();
        }
    }

    public byte[] reportEvaluasiPresentasi(Long userId, Long eventId, Long year) throws IOException {
        logger.info("Generating presentation evaluation report for userId: {}, eventId: {}, year: {}", userId, eventId, year);

        // Ambil data user berdasarkan userId
        UserInfo user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("User fetched: {}", user);

        // Ambil semua evaluasi berdasarkan userId, eventId, dan tahun
        List<DetailEvaluasiPresentasi> evaluations = detailEvaluasiPresentasiRepository.findByUserIdAndEventIdAndYear(userId, eventId, year);
        logger.info("Evaluations fetched for eventId {}, year {}: {}", eventId, year, evaluations.size());

        List<Long> pertanyaanIds = evaluations.stream()
                .map(DetailEvaluasiPresentasi::getPertanyaanId)
                .distinct()
                .collect(Collectors.toList());

        List<PertanyaanPresentasi> questions = pertanyaanPresentasiRepository.findByIdIn(pertanyaanIds);
        logger.info("Questions fetched for provided pertanyaanIds: {}", questions.size());

        // Ambil teamId unik dari evaluations
        List<Long> teamIds = evaluations.stream()
                .map(DetailEvaluasiPresentasi::getTeamId)
                .distinct()
                .collect(Collectors.toList());

        // Ambil data tim berdasarkan teamId yang unik
        List<Team> teams = teamRepository.findAllById(teamIds);
        logger.info("Teams fetched: {}", teams.size());

        // Map untuk menyimpan skor per teamId dan questionId
        Map<Long, Map<Long, String>> teamScores = evaluations.stream()
                .collect(Collectors.groupingBy(
                        DetailEvaluasiPresentasi::getTeamId,
                        Collectors.toMap(
                                DetailEvaluasiPresentasi::getPertanyaanId,
                                DetailEvaluasiPresentasi::getScore
                        )
                ));

        // Load template Excel
        Workbook workbook;
        try (InputStream templateStream = new ClassPathResource("templates/evaluation_report.xlsx").getInputStream()) {
            workbook = new XSSFWorkbook(templateStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Mengakses sheet dan memodifikasi template
        Sheet sheet = workbook.getSheet("Evaluation Report");
        if (sheet == null) {
            throw new RuntimeException("Sheet 'Presentation Evaluation Report' not found in template.");
        }

        generatePresentasiReport(sheet, workbook, user, teams, questions, teamScores);

        // Tulis data ke byte array
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        } finally {
            workbook.close();
        }
    }


    // New method for Yelyel Report
    public byte[] reportEvaluasiYelyel(Long deptId, Long juriId, int year) throws IOException {
        logger.info("Generating Yelyel evaluation report for deptId: {}, juriId: {}, year: {}", deptId, juriId, year);

        // Fetch evaluations based on department, jury, and year
        List<HasilEvaluasiYelyelDTO> evaluations = detailEvaluasiYelyelRepository.findByDeptIdAndJuriIdAndYear(deptId, juriId, year);

        // Load template Excel
        Workbook workbook;
        try (InputStream templateStream = new ClassPathResource("templates/evaluation_report.xlsx").getInputStream()) {
            workbook = new XSSFWorkbook(templateStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Access sheet and modify template
        Sheet sheet = workbook.getSheet("Evaluation Report");
        if (sheet == null) {
            throw new RuntimeException("Sheet 'Evaluation Report' not found in template.");
        }

        // Create styles for the header, content, and total rows with proper alignment
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle contentStyle = createContentStyle(workbook);

        // Style for left-aligned and bordered "Total Score"
        CellStyle totalStyle = createTotalStyle(workbook);
        totalStyle.setBorderTop(BorderStyle.THIN);
        totalStyle.setBorderBottom(BorderStyle.THIN);
        totalStyle.setBorderLeft(BorderStyle.THIN);
        totalStyle.setBorderRight(BorderStyle.THIN);

        // Create a style with borders for other cells
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Centered style for "No." and "Score" with borders
        CellStyle centerBorderStyle = workbook.createCellStyle();
        centerBorderStyle.setBorderTop(BorderStyle.THIN);
        centerBorderStyle.setBorderBottom(BorderStyle.THIN);
        centerBorderStyle.setBorderLeft(BorderStyle.THIN);
        centerBorderStyle.setBorderRight(BorderStyle.THIN);
        centerBorderStyle.setAlignment(HorizontalAlignment.CENTER);
        centerBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Add "Penilai" field in row 6
        Row penilaiRow = sheet.createRow(5);
        Cell penilaiCell = penilaiRow.createCell(1);
        penilaiCell.setCellValue("Penilai: " + evaluations.get(0).getJuriName());
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 2));

        // Create the header row (starting from row 7 to match the layout)
        Row headerRow = sheet.createRow(6);
        Cell headerCell = headerRow.createCell(1);
        headerCell.setCellValue("No.");
        headerCell.setCellStyle(headerStyle);

        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Pertanyaan");
        headerCell.setCellStyle(headerStyle);

        headerCell = headerRow.createCell(3);
        headerCell.setCellValue(evaluations.get(0).getDeptName());
        headerCell.setCellStyle(headerStyle);

        // Populate the rows with evaluation data (starting from row 8)
        int rowNum = 7;
        int index = 1;
        Map<String, Double> deptTotalScores = new HashMap<>();

        for (HasilEvaluasiYelyelDTO eval : evaluations) {
            Row row = sheet.createRow(rowNum++);
            Cell noCell = row.createCell(1);
            noCell.setCellValue(index++);
            noCell.setCellStyle(centerBorderStyle);  // Center and add border for "No."

            Cell pertanyaanCell = row.createCell(2);
            pertanyaanCell.setCellValue(eval.getPertanyaan());
            pertanyaanCell.setCellStyle(borderStyle);  // Add border for "Pertanyaan" without centering

            Cell scoreCell = row.createCell(3);
            scoreCell.setCellValue(Double.parseDouble(eval.getScore()));
            scoreCell.setCellStyle(centerBorderStyle);  // Center and add border for "Score"

            // Calculate total score for each department
            deptTotalScores.merge(eval.getDeptName(), Double.parseDouble(eval.getScore()), Double::sum);
        }

        // Add total score row at the end, ensuring it is in D20, left-aligned but with a border
        Row totalRow = sheet.createRow(19);  // Row index for D20 is 19 (zero-based index)
        Cell totalLabelCell = totalRow.createCell(1);
        totalLabelCell.setCellValue("Total Score");
        totalLabelCell.setCellStyle(totalStyle);  // Left-aligned and bordered
        sheet.addMergedRegion(new CellRangeAddress(19, 19, 1, 2));

        // Shift the total score to D20
        Cell totalScoreCell = totalRow.createCell(3);
        totalScoreCell.setCellValue(deptTotalScores.values().stream().mapToDouble(Double::doubleValue).sum());
        totalScoreCell.setCellStyle(totalStyle);  // Left-aligned and bordered

        // Remove the BPW in cell B21
        Row rowB21 = sheet.getRow(20);  // Row 20 for B21 (zero-based index)
        if (rowB21 != null) {
            Cell b21Cell = rowB21.getCell(1);
            if (b21Cell != null && "BPW".equals(b21Cell.getStringCellValue())) {
                b21Cell.setCellValue("");
            }
        }

        // Adjust column widths to fit content
        sheet.autoSizeColumn(1); // No (centered)
        sheet.autoSizeColumn(2); // Pertanyaan (fit content)
        sheet.autoSizeColumn(3); // Score (centered)

        // Write the workbook to a byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }





    private void generateLapanganReport(Sheet sheet, Workbook workbook, UserInfo user, List<Team> teams, List<PertanyaanLapangan> questions, Map<Long, Map<Long, String>> teamScores) {
        // Style untuk header
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Style untuk konten
        CellStyle contentNumberStyle = createContentNumberStyle(workbook);
        CellStyle contentQuestionStyle = createContentQuestionStyle(workbook);
        CellStyle contentScoreStyle = createContentScoreStyle(workbook);

        // Style untuk total skor
        CellStyle totalStyle = createTotalStyle(workbook);

        // Set Penilai di B6-C6
        Row penilaiRow = sheet.createRow(5);
        Cell penilaiCell = penilaiRow.createCell(1);
        penilaiCell.setCellValue("Penilai: " + user.getUsername());
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 2));
        CellStyle penilaiStyle = workbook.createCellStyle();
        penilaiStyle.setAlignment(HorizontalAlignment.LEFT);
        penilaiStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        penilaiStyle.setFont(createFont(workbook, true, IndexedColors.BLACK.getIndex(), 14));
        penilaiCell.setCellStyle(penilaiStyle);

        // Membuat header
        Row headerRow1 = sheet.createRow(6);
        Row headerRow2 = sheet.createRow(7);

        Cell noHeaderCell = headerRow1.createCell(1);
        noHeaderCell.setCellValue("No.");
        sheet.addMergedRegion(new CellRangeAddress(6, 7, 1, 1));
        noHeaderCell.setCellStyle(headerStyle);

        Cell pertanyaanHeaderCell = headerRow1.createCell(2);
        pertanyaanHeaderCell.setCellValue("Pertanyaan");
        sheet.addMergedRegion(new CellRangeAddress(6, 7, 2, 2));
        pertanyaanHeaderCell.setCellStyle(headerStyle);

        for (int i = 0; i < teams.size(); i++) {
            Cell teamHeaderCell = headerRow1.createCell(3 + i);
            teamHeaderCell.setCellValue(teams.get(i).getTeamName());
            sheet.addMergedRegion(new CellRangeAddress(6, 7, 3 + i, 3 + i));
            teamHeaderCell.setCellStyle(headerStyle);
        }

        Map<Long, Double> totalScores = new HashMap<>();

        // Isi data pertanyaan dan skor
        for (int i = 0; i < questions.size(); i++) {
            Row row = sheet.createRow(i + 8); // Dimulai dari baris ke-8
            PertanyaanLapangan question = questions.get(i);

            Cell noCell = row.createCell(1);
            noCell.setCellValue(i + 1);
            noCell.setCellStyle(contentNumberStyle); // Gunakan contentNumberStyle untuk nomor

            Cell questionCell = row.createCell(2);
            questionCell.setCellValue(question.getPertanyaan());
            questionCell.setCellStyle(contentQuestionStyle); // Gunakan contentQuestionStyle untuk pertanyaan
            sheet.setColumnWidth(2, 100 * 256); // Set column width
            row.setHeight((short) -1); // AutoFit row height

            for (int j = 0; j < teams.size(); j++) {
                Team team = teams.get(j);
                String scoreStr = teamScores.getOrDefault(team.getTeamId(), Map.of())
                        .getOrDefault(question.getId(), "N/A");
                Double score = "N/A".equals(scoreStr) ? 0 : Double.parseDouble(scoreStr);

                Cell scoreCell = row.createCell(3 + j);
                scoreCell.setCellValue(score);
                scoreCell.setCellStyle(contentScoreStyle); // Gunakan contentScoreStyle untuk skor

                // Hitung total skor per tim
                totalScores.merge(team.getTeamId(), score, Double::sum);
            }
        }

        // Tambahkan baris total skor
        Row totalRow = sheet.createRow(questions.size() + 8);
        Cell totalLabelCell = totalRow.createCell(1);
        totalLabelCell.setCellValue("Total");
        totalLabelCell.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(questions.size() + 8, questions.size() + 8, 1, 2));

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            Cell totalScoreCell = totalRow.createCell(3 + i);
            totalScoreCell.setCellValue(totalScores.getOrDefault(team.getTeamId(), 0.0));
            totalScoreCell.setCellStyle(totalStyle);
        }
    }

    private void generatePresentasiReport(Sheet sheet, Workbook workbook, UserInfo user, List<Team> teams, List<PertanyaanPresentasi> questions, Map<Long, Map<Long, String>> teamScores) {
        // Style untuk header
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Style untuk konten
        CellStyle contentNumberStyle = createContentNumberStyle(workbook);
        CellStyle contentQuestionStyle = createContentQuestionStyle(workbook);
        CellStyle contentScoreStyle = createContentScoreStyle(workbook);

        // Style untuk total skor
        CellStyle totalStyle = createTotalStyle(workbook);

        // Set Penilai di B6-C6
        Row penilaiRow = sheet.createRow(5);
        Cell penilaiCell = penilaiRow.createCell(1);
        penilaiCell.setCellValue("Penilai: " + user.getUsername());
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 2));
        CellStyle penilaiStyle = workbook.createCellStyle();
        penilaiStyle.setAlignment(HorizontalAlignment.LEFT);
        penilaiStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        penilaiStyle.setFont(createFont(workbook, true, IndexedColors.BLACK.getIndex(), 14));
        penilaiCell.setCellStyle(penilaiStyle);

        // Membuat header
        Row headerRow1 = sheet.createRow(6);
        Row headerRow2 = sheet.createRow(7);

        Cell noHeaderCell = headerRow1.createCell(1);
        noHeaderCell.setCellValue("No.");
        sheet.addMergedRegion(new CellRangeAddress(6, 7, 1, 1));
        noHeaderCell.setCellStyle(headerStyle);

        Cell pertanyaanHeaderCell = headerRow1.createCell(2);
        pertanyaanHeaderCell.setCellValue("Pertanyaan");
        sheet.addMergedRegion(new CellRangeAddress(6, 7, 2, 2));
        pertanyaanHeaderCell.setCellStyle(headerStyle);

        for (int i = 0; i < teams.size(); i++) {
            Cell teamHeaderCell = headerRow1.createCell(3 + i);
            teamHeaderCell.setCellValue(teams.get(i).getTeamName());
            sheet.addMergedRegion(new CellRangeAddress(6, 7, 3 + i, 3 + i));
            teamHeaderCell.setCellStyle(headerStyle);
        }

        Map<Long, Double> totalScores = new HashMap<>();

        // Isi data pertanyaan dan skor
        for (int i = 0; i < questions.size(); i++) {
            Row row = sheet.createRow(i + 8); // Dimulai dari baris ke-8
            PertanyaanPresentasi question = questions.get(i);

            Cell noCell = row.createCell(1);
            noCell.setCellValue(i + 1);
            noCell.setCellStyle(contentNumberStyle); // Gunakan contentNumberStyle untuk nomor

            Cell questionCell = row.createCell(2);
            questionCell.setCellValue(question.getPertanyaan());
            questionCell.setCellStyle(contentQuestionStyle); // Gunakan contentQuestionStyle untuk pertanyaan
            sheet.setColumnWidth(2, 100 * 256); // Set column width
            row.setHeight((short) -1); // AutoFit row height

            for (int j = 0; j < teams.size(); j++) {
                Team team = teams.get(j);
                String scoreStr = teamScores.getOrDefault(team.getTeamId(), Map.of())
                        .getOrDefault(question.getId(), "N/A");
                Double score = "N/A".equals(scoreStr) ? 0 : Double.parseDouble(scoreStr);

                Cell scoreCell = row.createCell(3 + j);
                scoreCell.setCellValue(score);
                scoreCell.setCellStyle(contentScoreStyle); // Gunakan contentScoreStyle untuk skor

                // Hitung total skor per tim
                totalScores.merge(team.getTeamId(), score, Double::sum);
            }
        }

        // Tambahkan baris total skor
        Row totalRow = sheet.createRow(questions.size() + 8);
        Cell totalLabelCell = totalRow.createCell(1);
        totalLabelCell.setCellValue("Total");
        totalLabelCell.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(questions.size() + 8, questions.size() + 8, 1, 2));

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            Cell totalScoreCell = totalRow.createCell(3 + i);
            totalScoreCell.setCellValue(totalScores.getOrDefault(team.getTeamId(), 0.0));
            totalScoreCell.setCellStyle(totalStyle);
        }
    }

    // Fungsi reusable untuk menambahkan border ke CellStyle
    private void setCellBorder(CellStyle style, BorderStyle borderStyle, short color) {
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);

        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFont(createFont(workbook, true, IndexedColors.BLACK.getIndex(), 16));
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[] {(byte) 180, (byte) 198, (byte) 231}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setCellBorder(headerStyle, BorderStyle.THIN, IndexedColors.BLACK.getIndex());
        return headerStyle;
    }

    private CellStyle createContentNumberStyle(Workbook workbook) {
        CellStyle contentNumberStyle = workbook.createCellStyle();
        contentNumberStyle.setAlignment(HorizontalAlignment.CENTER);
        contentNumberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentNumberStyle.setFont(createFont(workbook, false, IndexedColors.BLACK.getIndex(), 14));
        setCellBorder(contentNumberStyle, BorderStyle.THIN, IndexedColors.BLACK.getIndex());
        return contentNumberStyle;
    }

    private CellStyle createContentQuestionStyle(Workbook workbook) {
        CellStyle contentQuestionStyle = workbook.createCellStyle();
        contentQuestionStyle.setAlignment(HorizontalAlignment.LEFT); // Left alignment for questions
        contentQuestionStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentQuestionStyle.setWrapText(true); // Enable text wrapping
        contentQuestionStyle.setFont(createFont(workbook, false, IndexedColors.BLACK.getIndex(), 14));
        setCellBorder(contentQuestionStyle, BorderStyle.THIN, IndexedColors.BLACK.getIndex());
        return contentQuestionStyle;
    }

    private CellStyle createContentScoreStyle(Workbook workbook) {
        CellStyle contentScoreStyle = workbook.createCellStyle();
        contentScoreStyle.setAlignment(HorizontalAlignment.CENTER);
        contentScoreStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentScoreStyle.setFont(createFont(workbook, false, IndexedColors.BLACK.getIndex(), 14));
        setCellBorder(contentScoreStyle, BorderStyle.THIN, IndexedColors.BLACK.getIndex());
        return contentScoreStyle;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.setFont(createFont(workbook, true, IndexedColors.WHITE.getIndex(), 14));
        totalStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        totalStyle.setAlignment(HorizontalAlignment.CENTER);
        totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setCellBorder(totalStyle, BorderStyle.THIN, IndexedColors.BLACK.getIndex());
        return totalStyle;
    }

    // Helper Fungsi untuk membuat font
    private Font createFont(Workbook workbook, boolean isBold, short color, int fontSize) {
        Font font = workbook.createFont();
        font.setBold(isBold);
        font.setColor(color);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) fontSize);
        return font;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createContentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font contentFont = workbook.createFont();
        contentFont.setFontHeightInPoints((short) 10);
        style.setFont(contentFont);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }
}
