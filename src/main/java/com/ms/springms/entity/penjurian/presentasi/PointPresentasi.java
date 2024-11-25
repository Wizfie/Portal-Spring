package com.ms.springms.entity.penjurian.presentasi;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "point_presentasi")
public class PointPresentasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String pointPenilaian;

    private String scoreMaksimal;

    @ManyToOne
    @JoinColumn(name = "pertanyaan_id")
    private PertanyaanPresentasi pertanyaanPresentasi;

    private Boolean active;  // Menambahkan status aktif/nonaktif
}
