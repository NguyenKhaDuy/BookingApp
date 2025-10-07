package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianRepository extends JpaRepository<TechnicianEntity, String> {
}
