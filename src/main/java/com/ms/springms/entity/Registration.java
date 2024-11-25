package com.ms.springms.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Entity
@Data
@Table(name = "registration")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationId;

    @Column(columnDefinition = "TEXT")
    private String judul;


    private Long createdBy;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonManagedReference
    private Team team;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonManagedReference
    private Event event;

    private String registrationStatus;
}
