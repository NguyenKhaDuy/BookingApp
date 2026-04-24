package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.OtpVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerificationEntity, Long> {
    List<OtpVerificationEntity> findByEmailOrderByIdDesc(String email);
}
