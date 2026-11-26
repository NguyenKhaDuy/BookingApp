package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Repository.Custom.RequestRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequestEntity, String>, RequestRepositoryCustom {
    List<RepairRequestEntity> findByCustomerEntity(CustomerEntity customerEntity);
    Page<RepairRequestEntity> findByStatusEntityAndCustomerEntity(StatusEntity statusEntity, CustomerEntity customerEntity, Pageable pageable);
    Page<RepairRequestEntity> findByTechnicianEntity(TechnicianEntity technicianEntity, Pageable pageable);
    Page<RepairRequestEntity> findByStatusEntity(StatusEntity statusEntity, Pageable pageable);
    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE r.scheduled_date BETWEEN :dateFrom AND :dateTo 
    AND r.statusEntity.id_status = :id_status
    AND r.technicianEntity.id_user = :id_technician
    """)
    List<RepairRequestEntity> StatisticsOrderOfDay( @Param("dateFrom") LocalDate dateFrom,
                                                    @Param("dateTo") LocalDate dateTo,
                                                    @Param("id_technician") String id_technician,
                                                    @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE MONTH(r.scheduled_date) = :currentMonth AND YEAR(r.scheduled_date) = YEAR(CURRENT_DATE) 
    AND r.statusEntity.id_status = :id_status
    AND r.technicianEntity.id_user = :id_technician
    """)
    List<RepairRequestEntity> StatisticsOrderOfMonth( @Param("currentMonth") Long currentMonth,
                                                      @Param("id_technician") String id_technician,
                                                    @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE YEAR(r.scheduled_date) = :currentYear 
    AND r.technicianEntity.id_user = :id_technician
    AND r.statusEntity.id_status = :id_status
    """)
    List<RepairRequestEntity> StatisticsOrderOfYear( @Param("currentYear") Long currentYear,
                                                     @Param("id_technician") String id_technician,
                                                      @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE MONTH(r.scheduled_date) = :month
    AND YEAR(r.scheduled_date) = :year 
    AND r.technicianEntity.id_user = :id_technician
    AND r.statusEntity.id_status = :id_status
    """)
    List<RepairRequestEntity> monthlyOrderStatistics( @Param("month") Long month,
                                                    @Param("year") Long year,
                                                     @Param("id_technician") String id_technician,
                                                     @Param("id_status") String id_status);




    //admin
    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE r.scheduled_date BETWEEN :dateFrom AND :dateTo 
    AND r.statusEntity.id_status = :id_status
    """)
    List<RepairRequestEntity> StatisticsOrderOfDay( @Param("dateFrom") LocalDate dateFrom,
                                                    @Param("dateTo") LocalDate dateTo,
                                                    @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE MONTH(r.scheduled_date) = :currentMonth AND YEAR(r.scheduled_date) = YEAR(CURRENT_DATE) 
    AND r.statusEntity.id_status = :id_status
    """)
    List<RepairRequestEntity> StatisticsOrderOfMonth( @Param("currentMonth") Long currentMonth,
                                                      @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE YEAR(r.scheduled_date) = :currentYear 
    AND r.statusEntity.id_status = :id_status
    """)
    List<RepairRequestEntity> StatisticsOrderOfYear( @Param("currentYear") Long currentYear,
                                                     @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE MONTH(r.scheduled_date) = :month
    AND YEAR(r.scheduled_date) = :year 
    AND r.statusEntity.id_status = :id_status
    """)
    List<RepairRequestEntity> monthlyOrderStatistics( @Param("month") Long month,
                                                     @Param("year") Long year,
                                                     @Param("id_status") String id_status);

    @Query("""
    SELECT DISTINCT r
    FROM RepairRequestEntity r
    WHERE YEAR(r.scheduled_date) = :year 
    AND r.statusEntity.id_status = :id_status
    AND r.serviceEntity.id_service = :id_service
    """)
    List<RepairRequestEntity> StatisticsOrderOfService( @Param("year") Long year,
                                                     @Param("id_status") String id_status,
                                                        @Param("id_service") String id_service);

    @Query("""
    SELECT r FROM RepairRequestEntity r
    WHERE r.serviceEntity.id_service = :idService
    AND r.statusEntity.nameStatus = :nameStatus
    AND MONTH(r.created_at) = :month
    AND YEAR(r.created_at) = YEAR(CURRENT_DATE)
    """)
    List<RepairRequestEntity> findByServiceAndStatusInMonth(
            @Param("idService") String idService,
            @Param("nameStatus") String nameStatus,
            @Param("month") Integer month
    );
}
