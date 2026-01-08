package com.example.bookingapp.Repository;


import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Entity.TechnicianRefusedRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRefusedRquestRepository extends JpaRepository<TechnicianRefusedRequestEntity, Long> {
    TechnicianRefusedRequestEntity findByTechnicianEntityAndRepairRequestEntity(TechnicianEntity technicianEntity, RepairRequestEntity repairRequestEntity);
}
