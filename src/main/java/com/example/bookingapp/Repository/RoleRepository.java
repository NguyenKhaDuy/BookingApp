package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}
