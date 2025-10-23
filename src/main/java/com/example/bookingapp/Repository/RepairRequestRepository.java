package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RepairRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequestEntity, Long> {
    Page<RepairRequestEntity> findByCustomerEntity(CustomerEntity customerEntity, Pageable pageable);
}
