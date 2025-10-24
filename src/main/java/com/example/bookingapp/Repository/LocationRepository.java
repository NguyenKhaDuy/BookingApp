package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.LocationEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    Page<LocationEntity> findByTechnicianEntities(TechnicianEntity technicianEntity, Pageable pageable);
}
