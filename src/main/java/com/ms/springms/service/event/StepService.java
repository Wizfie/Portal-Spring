package com.ms.springms.service.event;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Steps;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.event.StepRepository;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StepService {

    @Autowired
    private StepRepository stepRepository;

    @Autowired
    private EventRepository eventRepository;

    // Fungsi untuk menambahkan atau memperbarui langkah-langkah
    public List<Steps> addOrUpdateSteps(Long eventId, List<Steps> stepsList) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        for (Steps steps : stepsList) {
            if (steps.getStepName() == null || steps.getStartDate() == null || steps.getEndDate() == null) {
                throw new IllegalArgumentException("Semua Input harus di isi");
            }

            if (steps.getStepId() != null && !stepRepository.existsById(steps.getStepId())) {
                throw new IllegalArgumentException("Step with ID " + steps.getStepId() + " does not exist");
            }

            if (steps.getBerkas() == null || steps.getBerkas().isEmpty()) {
                steps.setBerkas("false");
            }

            steps.setEvent(event);

            try {
                stepRepository.save(steps);
            } catch (Exception e) {
                throw new RuntimeException( e.getMessage());
            }
        }

        return stepsList;
    }

    // Method untuk menghapus langkah berdasarkan stepId
    public void deleteStep(Long stepId) throws ResourceNotFoundException {
        Steps step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found with id: " + stepId));
        stepRepository.delete(step);
    }
}
