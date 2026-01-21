package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.LocationEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findByTechnicianEntities(TechnicianEntity technicianEntity);
}
