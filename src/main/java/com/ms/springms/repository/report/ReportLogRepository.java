package com.ms.springms.repository.report;

import com.ms.springms.entity.ReportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    Optional<ReportLog> findByYear(int currentYear);
}
