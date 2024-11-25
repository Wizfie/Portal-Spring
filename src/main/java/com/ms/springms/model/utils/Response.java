package com.ms.springms.model.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response   {
    private String message;
    private Object data;

    public Response(String message) {
        this.message = message;
    }


}
