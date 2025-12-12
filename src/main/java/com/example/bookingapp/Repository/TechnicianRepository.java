package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Repository.Custom.TechnicianRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRepository extends JpaRepository<TechnicianEntity, String>, TechnicianRepositoryCustom {
    Page<TechnicianEntity> findByServiceEntities(ServiceEntity serviceEntity, Pageable pageable);
    List<TechnicianEntity> findByServiceEntities(ServiceEntity serviceEntity);
}
