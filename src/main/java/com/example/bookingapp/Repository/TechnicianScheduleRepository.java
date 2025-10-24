package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Entity.TechnicianScheduleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TechnicianScheduleRepository extends JpaRepository<TechnicianScheduleEntity, Long> {
    Page<TechnicianScheduleEntity> findByTechnicianEntityOrderByDateDesc(TechnicianEntity technicianEntity, Pageable pageable);
    List<TechnicianScheduleEntity> findByTechnicianEntityAndDateOrderByIdScheduleDesc(TechnicianEntity technicianEntity, LocalDate localDate);

    //lấy ra nhưng lịch hết hạn
    @Query("SELECT s FROM TechnicianScheduleEntity s WHERE s.date < :today " +
            "OR (s.date = :today AND s.time_end < :nowTime)")
    List<TechnicianScheduleEntity> findExpiredSchedules(
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime
    );
}
