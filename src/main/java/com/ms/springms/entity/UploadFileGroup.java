package com.ms.springms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "upload_file_groups")
public class UploadFileGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "uploadFileGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UploadFiles> files;

    private String approvalStatus;
}