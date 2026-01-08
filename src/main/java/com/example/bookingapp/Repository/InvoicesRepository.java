package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.InvoicesEntity;
import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Models.DTO.RevenueByServiceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoicesRepository extends JpaRepository<InvoicesEntity, String> {
    Page<InvoicesEntity> findByCustomerEntity(CustomerEntity customerEntity, Pageable pageable);
    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    JOIN i.repairRequestEntity r
    WHERE i.paidAt BETWEEN :dateFrom AND :dateTo 
    AND i.statusEntity.nameStatus = 'PAID'
    AND r.technicianEntity.id_user = :id_technician
    """)
    List<InvoicesEntity> findByInvoiceOfDay(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("id_technician") String id_technician
    );

    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.repairRequestEntity r
    JOIN i.detailInvoicesEntities d
    WHERE MONTH(i.paidAt) = :currentMonth 
    AND YEAR(i.paidAt) = YEAR(CURRENT_DATE) 
    AND i.statusEntity.nameStatus = 'PAID'
    AND r.technicianEntity.id_user = :id_technician
    """)
    List<InvoicesEntity> findByInvoiceOfMonth(
            @Param("currentMonth") Long currentMonth,
            @Param("id_technician") String id_technician
    );

    @Query("""
    SELECT DISTINCT i
    FROM InvoicesEntity i
    JOIN i.repairRequestEntity r
    JOIN i.detailInvoicesEntities d
    WHERE YEAR(i.paidAt) = :currentYear 
    AND i.statusEntity.nameStatus = 'PAID'
    AND r.technicianEntity.id_user = :id_technician
    """)
    List<InvoicesEntity> findByInvoiceOfYear(
            @Param("currentYear") Long currentYear,
            @Param("id_technician") String id_technician
    );

    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    WHERE i.paidAt BETWEEN :dateFrom AND :dateTo 
    AND i.statusEntity.nameStatus = 'PAID'
    """)
    List<InvoicesEntity> findByInvoiceOfDay(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    WHERE MONTH(i.paidAt) = :currentMonth 
    AND YEAR(i.paidAt) = YEAR(CURRENT_DATE) 
    AND i.statusEntity.nameStatus = 'PAID'
    """)
    List<InvoicesEntity> findByInvoiceOfMonth(
            @Param("currentMonth") Long currentMonth
    );

    @Query("""
    SELECT DISTINCT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    WHERE YEAR(i.paidAt) = :currentYear 
    AND i.statusEntity.nameStatus = 'PAID'
    """)
    List<InvoicesEntity> findByInvoiceOfYear(
            @Param("currentYear") Long currentYear
    );

    //dùng để vẽ biểu ồ cột doanh thu theo tháng
    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    WHERE MONTH(i.paidAt) = :month 
    AND YEAR(i.paidAt) = :year
    AND i.statusEntity.nameStatus = 'PAID'
    """)
    List<InvoicesEntity> monthlyRevenueThroughoutYear(
            @Param("month") Long month,
            @Param("year") Long year
    );

    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    JOIN i.repairRequestEntity r
    WHERE MONTH(i.paidAt) = :month 
    AND YEAR(i.paidAt) = :year
    AND i.statusEntity.nameStatus = 'PAID'
    AND r.technicianEntity.id_user = :id_technician
    """)
    List<InvoicesEntity> monthlyRevenueThroughoutYearOfTechnician(
            @Param("month") Long month,
            @Param("year") Long year,
            @Param("id_technician") String id_technician
    );

    //dùng để vẽ biểu ồ cột doanh thu theo tháng của từng dịch vụ của admin
    @Query("""
    SELECT i
    FROM InvoicesEntity i
    JOIN i.detailInvoicesEntities d
    JOIN i.repairRequestEntity r
    WHERE YEAR(i.paidAt) = :year
    AND i.statusEntity.nameStatus = 'PAID'
    AND r.serviceEntity.id_service = :id_service
    """)
    List<InvoicesEntity> monthlyRevenueThroughoutYearOfService(
            @Param("year") Long year,
            @Param("id_service") Long id_service
    );
}
