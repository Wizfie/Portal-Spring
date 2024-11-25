package com.ms.springms.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventWithStages {
    private Long eventId;
    private String eventName;
    private String eventType;
    private List<EventStepsDTO> stages;


    }
