package com.ms.springms.utils.Exceptions.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomResponse {
    private String status;
    private  String message;
    private Object data;
    private  String error;

}
