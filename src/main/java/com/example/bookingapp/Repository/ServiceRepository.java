package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByTechnicianEntities(TechnicianEntity technicianEntity);

    @Query("""
            SELECT s
            FROM ServiceEntity s
            WHERE LOWER(s.nameService)
            LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    ServiceEntity findByKeyword(@Param("keyword") String keyword);
}
