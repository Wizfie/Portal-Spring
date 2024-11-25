package com.ms.springms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "upload_files")
public class  UploadFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long filesId;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private Steps steps;

    @ManyToOne
    @JoinColumn(name = "registration_id")
    private Registration registration;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private UploadFileGroup uploadFileGroup;

    private String fileName;

    private String filePath;

    private String uploadedBy;

    private LocalDateTime uploadedAt;

    private String approvalStatus;

    private String responseFileName;

    private String responseFilePath;

    private LocalDateTime responseUploadedAt;

    @Column(columnDefinition = "TEXT")
    private String responseDescription;

    private boolean isRisalah;

}

