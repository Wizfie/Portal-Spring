package com.ms.springms.entity.penjurian.lapangan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "pertanyaan_lapangan")
@Getter
@Setter
public class PertanyaanLapangan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String pertanyaan;

    private Boolean active = true; // Menambahkan kolom active

    @ManyToOne
    private SubKriteriaLapangan subKriteriaLapangan;

    @OneToMany(mappedBy = "pertanyaanLapangan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointLapangan> points;

    // Metode untuk menonaktifkan seluruh poin saat pertanyaan dinonaktifkan
    public void setActive(Boolean active, boolean propagate) {
        this.active = active;
        if (propagate && !active) {
            points.forEach(point -> point.setActive(false));
        }
    }
}
