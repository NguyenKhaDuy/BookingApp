package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.NotificationUserEntity;
import com.example.bookingapp.Entity.NotificationsEntity;
import com.example.bookingapp.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUserEntity, Long> {
    Page<NotificationUserEntity> findByUserEntity(UserEntity userEntity, Pageable pageable);
    List<NotificationUserEntity> findByUserEntity(UserEntity userEntity);
    NotificationUserEntity findByUserEntityAndNotificationsEntity(UserEntity userEntity, NotificationsEntity notificationsEntity);
}
