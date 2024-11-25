package com.ms.springms.service.event;

import com.ms.springms.entity.Steps;
import com.ms.springms.model.event.EventDashboardDTO;
import com.ms.springms.model.event.StepsDTO;
import com.ms.springms.utils.Exceptions.DuplicateEntryException;
import com.ms.springms.entity.Event;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.event.StepRepository;
import com.ms.springms.service.team.TeamMemberService;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TeamMemberService teamMemberService;

    @Autowired
    private StepService stepService;

    @Autowired
    private StepRepository stepRepository;

    @Transactional
    public Event createEventWithSteps(Event event, List<Steps> stepsList) {
        String eventName = event.getEventName();
        String eventYear = event.getEventYear();
        String eventType = event.getEventType();

        if (eventName == null || eventName.isBlank()) {
            throw new IllegalArgumentException("Event name Harus di isi");
        }

        // Check for duplicate event name
        if (eventRepository.existsByEventName(eventName)) {
            throw new DuplicateEntryException("Nama event sudah digunakan");
        }

        try {
            // Save the event first
            Event newEvent = new Event();
            newEvent.setEventName(eventName);
            newEvent.setEventYear(eventYear);
            newEvent.setEventType(eventType);
            Event savedEvent = eventRepository.save(newEvent);

            // Try to add steps to the event
            stepService.addOrUpdateSteps(savedEvent.getEventId(), stepsList);

            // Return the event if all steps were successfully created
            return savedEvent;

        } catch (Exception ex) {
            throw new RuntimeException( ex.getMessage());
        }
    }

    public List<EventDashboardDTO> getAllEvent() {
        List<Event> events = eventRepository.findAll();

        return events.stream().map(event -> {
            List<Steps> steps = stepRepository.findByEvent(event);

            List<StepsDTO> stepsDTOList = steps.stream().map(step -> new StepsDTO(
                    step.getStepId(),
                    step.getStepName(),
                    (Date) step.getStartDate(),
                    (Date) step.getEndDate(),
                    step.getDescription(),
                    step.getBerkas()
            )).collect(Collectors.toList());

            return new EventDashboardDTO(event.getEventId(), event.getEventName() ,  event.getEventYear(),event.getEventType(), stepsDTOList);
        }).collect(Collectors.toList());
    }



    public void deleteEvent(Long eventId) throws ResourceNotFoundException {
        // Periksa apakah event dengan eventId yang diberikan ada
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Dapatkan semua steps terkait eventId tersebut
        List<Steps> relatedSteps = stepRepository.findByEvent(event);

        stepRepository.deleteAll(relatedSteps);


        // Setelah semua steps dihapus, hapus event tersebut
        eventRepository.delete(event);
    }

    @Transactional
    public Event updateEvent(Long eventId, Event updatedEvent) throws ResourceNotFoundException {
        // Periksa apakah event dengan eventId yang diberikan ada
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Perbarui detail event
        existingEvent.setEventName(updatedEvent.getEventName());
        existingEvent.setEventYear(updatedEvent.getEventYear());
        existingEvent.setEventType(updatedEvent.getEventType());

        // Simpan perubahan pada event
        return eventRepository.save(existingEvent);
    }


}


