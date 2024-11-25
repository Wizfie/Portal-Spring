package com.ms.springms.entity.penjurian.lapangan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "point_lapangan")
public class PointLapangan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jawaban;
    private Boolean active = true; // Menambahkan kolom active

    @ManyToOne
    @JoinColumn(name = "pertanyaan_lapangan_id")
    private PertanyaanLapangan pertanyaanLapangan;

    public void setActive(Boolean active) {
        this.active = active;
    }
}
