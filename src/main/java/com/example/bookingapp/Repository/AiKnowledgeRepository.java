
package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.AiKnowledgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiKnowledgeRepository extends JpaRepository<AiKnowledgeEntity, String> {
    List<AiKnowledgeEntity> findByIntent_CodeAndEnabledTrue(String intentCode);
}

