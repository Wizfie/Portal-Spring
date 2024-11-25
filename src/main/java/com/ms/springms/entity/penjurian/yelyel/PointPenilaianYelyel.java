package com.ms.springms.entity.penjurian.yelyel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_yelyel")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointPenilaianYelyel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private String pertanyaan;
    private double scoreMaksimal;
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "kriteria_id")
    private KriteriaYelyel kriteriaYelyel;
}
