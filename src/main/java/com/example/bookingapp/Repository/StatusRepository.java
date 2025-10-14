package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<StatusEntity, Long> {
    StatusEntity findByNameStatus(String name_status);
}
