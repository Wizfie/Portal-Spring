package com.ms.springms.controller.event;

import com.ms.springms.entity.Steps;
import com.ms.springms.model.event.EventDashboardDTO;
import com.ms.springms.model.event.EventRequest;
import com.ms.springms.utils.Exceptions.DuplicateEntryException;
import com.ms.springms.entity.Event;
import com.ms.springms.service.event.EventService;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class
EventController {


    @Autowired
    private EventService eventService;





    @GetMapping("/all")
    public List<EventDashboardDTO> getAllEvent() {
        return eventService.getAllEvent();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEventWithSteps(@RequestBody EventRequest eventRequest) {
        try {
            Event createdEvent = eventService.createEventWithSteps(eventRequest.getEvent(), eventRequest.getSteps());
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (DuplicateEntryException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>( e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<?> deleteStep(@PathVariable Long eventId) {
        try {
            eventService.deleteEvent(eventId);
            return new ResponseEntity<>("Event deleted successfully", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long eventId,
            @RequestBody Event updatedEvent
    ) {
        try {
            Event event = eventService.updateEvent(eventId, updatedEvent);
            return ResponseEntity.ok(event);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }





}
