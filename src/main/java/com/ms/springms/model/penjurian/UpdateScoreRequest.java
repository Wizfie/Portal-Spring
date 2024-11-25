package com.ms.springms.model.penjurian;

import lombok.Data;

@Data
public class UpdateScoreRequest {

    private String scoreType;
    private String scoreValue;

}
