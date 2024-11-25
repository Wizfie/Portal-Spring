package com.ms.springms.model.event;// EventRequest.java

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Steps;
import lombok.Data;

import java.util.List;
@Data
public class EventRequest {
    private Event event;
    private List<Steps> steps;

    // Getters and setters
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Steps> getSteps() {
        return steps;
    }

    public void setSteps(List<Steps> steps) {
        this.steps = steps;
    }
}
