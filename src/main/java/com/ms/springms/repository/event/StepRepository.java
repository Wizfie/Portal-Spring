package com.ms.springms.repository.event;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Steps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StepRepository extends JpaRepository<Steps, Long> {

    List<Steps> findByEvent(Event event);

    @Query("SELECT s FROM Steps s WHERE YEAR(s.startDate) = :year OR YEAR(s.endDate) = :year")
    List<Steps> findStepsByYear(@Param("year") int year);

    @Query("SELECT s FROM Steps s WHERE s.startDate = :notificationDate")
    List<Steps> findStepsStartingOn(LocalDate notificationDate);


}
