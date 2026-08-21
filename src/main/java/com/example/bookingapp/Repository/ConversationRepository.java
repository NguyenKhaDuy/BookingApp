package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.ConversationEntity;
import com.example.bookingapp.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {
    List<ConversationEntity> findByUserEntity(UserEntity userEntity);
}
