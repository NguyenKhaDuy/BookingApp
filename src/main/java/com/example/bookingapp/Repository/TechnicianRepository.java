package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Repository.Custom.TechnicianRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianRepository extends JpaRepository<TechnicianEntity, String>, TechnicianRepositoryCustom {

}
