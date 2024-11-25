package com.ms.springms.model.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import lombok.Data;

@Data
public class UpdateMemberRequest {

    private String memberName;
    private String memberPosition;

}
