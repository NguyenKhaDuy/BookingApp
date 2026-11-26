
package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.AiIntentEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AiIntentRepository
        extends JpaRepository<AiIntentEntity, String> {

    Optional<AiIntentEntity> findByCode(String code);

    @EntityGraph(attributePaths = "examples")
    List<AiIntentEntity> findByEnabledTrue();
}
