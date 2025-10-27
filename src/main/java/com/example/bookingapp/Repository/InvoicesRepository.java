package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.InvoicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoicesRepository extends JpaRepository<InvoicesEntity, String> {
}
