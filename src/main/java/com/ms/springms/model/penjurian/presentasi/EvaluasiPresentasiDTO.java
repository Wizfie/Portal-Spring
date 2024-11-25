package com.ms.springms.model.penjurian.presentasi;


import java.time.LocalDateTime;

public interface EvaluasiPresentasiDTO {
    Long getTeamId();
    String getTeamName();
    String getDeptName();
    Long getEventId();
    String getEventName();
    Long getJuriId();
    String getJuriName();
    Double getScore();
    LocalDateTime getCreatedAt();

}
