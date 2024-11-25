package com.ms.springms.model.penjurian.lapangan;

import java.time.LocalDateTime;

public interface EvaluasiLapanganDTO {
    Long getUserId();
    String getDeptName();
    Long getTeamId();
    String getTeamName();
    Long getEventId();
    String getEventName();
    Long getJuriId();
    String getJuriName();
    Double getScoreLapangan();
    LocalDateTime getCreatedAt();
}
