package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.LevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<LevelEntity, Long> {
    LevelEntity findByLevel(String level);
}
