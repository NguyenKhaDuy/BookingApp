package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Repository.Custom.TechnicianRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRepository extends JpaRepository<TechnicianEntity, String>, TechnicianRepositoryCustom {
    Page<TechnicianEntity> findByServiceEntities(ServiceEntity serviceEntity, Pageable pageable);

    List<TechnicianEntity> findByServiceEntities(ServiceEntity serviceEntity);

    @Query("""
            SELECT DISTINCT t
            FROM TechnicianEntity t
            JOIN t.serviceEntities s
            JOIN t.locationEntities l
            WHERE s = :serviceEntity
            AND (
                :district IS NULL 
                OR LOWER(l.district) LIKE LOWER(CONCAT('%',:district,'%'))
            )
            AND (
                :ward IS NULL 
                OR LOWER(l.ward) LIKE LOWER(CONCAT('%',:ward,'%'))
            )
            AND (
                :conscious IS NULL 
                OR LOWER(l.conscious) LIKE LOWER(CONCAT('%',:conscious,'%'))
            )
            """)
    List<TechnicianEntity> searchTechnicianByLocation(
            @Param("serviceEntity") ServiceEntity serviceEntity,
            @Param("district") String district,
            @Param("ward") String ward,
            @Param("conscious") String conscious
    );
}
