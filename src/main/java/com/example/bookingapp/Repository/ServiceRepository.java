package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByTechnicianEntities(TechnicianEntity technicianEntity);
}
