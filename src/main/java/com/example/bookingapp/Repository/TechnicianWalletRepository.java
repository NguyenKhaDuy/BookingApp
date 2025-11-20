package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Entity.TechnicianWalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianWalletRepository extends JpaRepository<TechnicianWalletEntity, String> {
    TechnicianWalletEntity findByTechnicianEntity(TechnicianEntity technicianEntity);
}
