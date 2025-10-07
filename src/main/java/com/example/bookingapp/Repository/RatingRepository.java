package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.RatingEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, Long> {
    Page<RatingEntity> findByTechnicianEntity(TechnicianEntity technicianEntity , Pageable pageable);
}
