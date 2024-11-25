package com.ms.springms.entity.penjurian.lapangan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fase_lapangan")
@Getter
@Setter
public class FaseLapangan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private Boolean active = true; // Menambahkan kolom active

    @OneToMany(mappedBy = "faseLapangan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KriteriaLapangan> kriteriaLapangan = new ArrayList<>();

    // Metode untuk menonaktifkan seluruh kriteria saat fase dinonaktifkan
    public void setActive(Boolean active, boolean propagate) {
        this.active = active;
        if (propagate && !active) {
            kriteriaLapangan.forEach(kriteria -> kriteria.setActive(false, true));
        }
    }
}
