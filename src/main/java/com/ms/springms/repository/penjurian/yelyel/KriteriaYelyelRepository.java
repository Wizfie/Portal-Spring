package com.ms.springms.repository.penjurian.yelyel;

import com.ms.springms.entity.penjurian.yelyel.KriteriaYelyel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KriteriaYelyelRepository extends JpaRepository<KriteriaYelyel, Long> {


    List<KriteriaYelyel> findByActiveTrue();
}
