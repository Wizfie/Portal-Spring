package com.ms.springms.repository.penjurian.lapangan;

import com.ms.springms.entity.penjurian.lapangan.FaseLapangan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaseRepository  extends JpaRepository<FaseLapangan, Long> {


    boolean existsByNameAndType(String name , String type);

}
