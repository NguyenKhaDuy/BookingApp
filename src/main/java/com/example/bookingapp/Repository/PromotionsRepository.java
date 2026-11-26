package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.PromotionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionsRepository extends JpaRepository<PromotionsEntity, String> {
}
