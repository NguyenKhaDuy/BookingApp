
package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.AiIntentExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AiIntentExampleRepository
        extends JpaRepository<AiIntentExampleEntity, String> {

    List<AiIntentExampleEntity> findByIntentId(String intentId);

    List<AiIntentExampleEntity> findByVerifiedFalse();

    boolean existsByIntentIdAndExampleTextIgnoreCase(
            String intentId,
            String exampleText
    );
}
