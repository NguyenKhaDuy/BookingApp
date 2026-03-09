package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.SkillEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<SkillEntity, Long> {
    List<SkillEntity> findByTechnicianEntities(TechnicianEntity technicianEntity);
}
