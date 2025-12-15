package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.NotificationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationTypeEntity, Long> {
    NotificationTypeEntity findByType(String type);
}
