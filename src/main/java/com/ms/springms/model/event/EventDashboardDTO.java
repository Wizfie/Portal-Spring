package com.ms.springms.model.event;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Steps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDashboardDTO {
    private Long eventId;
    private String eventName;
    private String eventYear;
    private String eventType;
    List<StepsDTO> Steps;
}
