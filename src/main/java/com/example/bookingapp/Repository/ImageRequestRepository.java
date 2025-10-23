package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.ImageRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRequestRepository extends JpaRepository<ImageRequestEntity, Long> {
}
