package com.ms.springms.model.penjurian.totalScore;

import com.ms.springms.model.team.TeamMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamScoreDTO {
    private String dept;
    private Long teamId;
    private String teamName;
    private Long eventId;
    private String eventName;
    private String eventyear;
    private Double totalScoreLapangan;
    private Double totalScorePresentasi;
    private Double nilaiAkhir;
    private String judul;
    private List<String> risalahFileNames;
    private List<String> risalahFilePaths;
    private List<UserScoreDTO> userScores;
    private List<TeamMemberDTO> teamMembers;

    // Constructor matching the order of the query results
    public TeamScoreDTO(String dept, Long teamId, String teamName, Long eventId, String eventName,
                        String eventyear,
                        Double totalScoreLapangan, Double totalScorePresentasi, Double nilaiAkhir) {
        this.dept = dept;
        this.teamId = teamId;
        this.teamName = teamName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventyear = eventyear;
        this.totalScoreLapangan = totalScoreLapangan;
        this.totalScorePresentasi = totalScorePresentasi;
        this.nilaiAkhir = nilaiAkhir;
        this.risalahFileNames = new ArrayList<>();
        this.risalahFilePaths = new ArrayList<>();
    }

}
