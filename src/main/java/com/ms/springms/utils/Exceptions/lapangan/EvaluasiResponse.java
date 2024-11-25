package com.ms.springms.utils.Exceptions.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluasiResponse {

    private Long id;
    private String jawaban;
    private String username;
    private LocalDateTime createdAt;
    private String teamName;
    private String eventName;
    private String pertanyaanName;

}

