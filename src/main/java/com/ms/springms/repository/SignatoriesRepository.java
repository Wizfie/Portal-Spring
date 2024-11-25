package com.ms.springms.repository;

import com.ms.springms.entity.Signatories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatoriesRepository extends JpaRepository<Signatories, Long> {

}
