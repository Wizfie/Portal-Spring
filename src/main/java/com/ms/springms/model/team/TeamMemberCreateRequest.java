package com.ms.springms.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberCreateRequest {

    private Long teamId;  // ID Tim yang terkait
    private Long eventId; // ID Event yang terkait
    private String memberName; // Nama anggota
    private String memberPosition; // P
    private String nip;
}
