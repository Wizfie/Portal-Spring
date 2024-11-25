package com.ms.springms.model.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailRequest {
    private String[] to;
    private String[] cc;
    private String[] bcc;
    private String subject;
    private String text;
    private String name;
    private String from;

}
