package com.ms.springms.entity.penjurian.lapangan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kriteria_lapangan")
@Getter
@Setter
public class KriteriaLapangan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Boolean active = true; // Menambahkan kolom active

    @ManyToOne
    private FaseLapangan faseLapangan;

    @OneToMany(mappedBy = "kriteriaLapangan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubKriteriaLapangan> subKriteriaLapangan = new ArrayList<>();

    // Metode untuk menonaktifkan seluruh subkriteria saat kriteria dinonaktifkan
    public void setActive(Boolean active, boolean propagate) {
        this.active = active;
        if (propagate && !active) {
            subKriteriaLapangan.forEach(subKriteria -> subKriteria.setActive(false, true));
        }
    }
}
