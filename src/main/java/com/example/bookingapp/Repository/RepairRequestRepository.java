package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Repository.Custom.RequestRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequestEntity, Long>, RequestRepositoryCustom {
    Page<RepairRequestEntity> findByCustomerEntity(CustomerEntity customerEntity, Pageable pageable);
    Page<RepairRequestEntity> findByStatusEntityAndCustomerEntity(StatusEntity statusEntity, CustomerEntity customerEntity, Pageable pageable);
    Page<RepairRequestEntity> findByStatusEntityAndTechnicianEntity(StatusEntity statusEntity, TechnicianEntity technicianEntity, Pageable pageable);
    Page<RepairRequestEntity> findByStatusEntity(StatusEntity statusEntity, Pageable pageable);
}
