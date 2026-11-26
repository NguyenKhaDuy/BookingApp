
package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.AiConversationContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiConversationContextRepository
        extends JpaRepository<AiConversationContextEntity, Long> {


    Optional<AiConversationContextEntity>
    findByConversationEntity_IdConversation(
            String conversationId
    );

    void deleteByConversationEntity_IdConversation(
            String conversationId
    );
}
