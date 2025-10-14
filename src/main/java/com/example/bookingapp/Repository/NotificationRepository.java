package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.NotificationsEntity;
import com.example.bookingapp.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificationRepository extends JpaRepository<NotificationsEntity, Long> {

}
