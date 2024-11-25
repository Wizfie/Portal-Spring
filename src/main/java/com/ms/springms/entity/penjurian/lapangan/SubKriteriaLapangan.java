package com.ms.springms.entity.penjurian.lapangan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "sub_kriteria_lapangan")
@Getter
@Setter
public class SubKriteriaLapangan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Boolean active = true; // Menambahkan kolom active

    @ManyToOne
    private KriteriaLapangan kriteriaLapangan;

    @OneToMany(mappedBy = "subKriteriaLapangan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PertanyaanLapangan> pertanyaanLapanganList;

    // Metode untuk menonaktifkan seluruh pertanyaan saat subkriteria dinonaktifkan
    public void setActive(Boolean active, boolean propagate) {
        this.active = active;
        if (propagate && !active) {
            pertanyaanLapanganList.forEach(pertanyaan -> pertanyaan.setActive(false, true));
        }
    }
}
