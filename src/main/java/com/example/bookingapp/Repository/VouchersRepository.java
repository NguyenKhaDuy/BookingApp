package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.VouchersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VouchersRepository extends JpaRepository<VouchersEntity, String> {
}
