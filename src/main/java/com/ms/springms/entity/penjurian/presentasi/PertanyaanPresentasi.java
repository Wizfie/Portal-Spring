package com.ms.springms.entity.penjurian.presentasi;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pertanyaan_presentasi")
public class PertanyaanPresentasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String pertanyaan;

    private String type;

    private Boolean active;  // Menambahkan status aktif/nonaktif
}
