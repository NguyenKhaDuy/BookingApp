package com.example.bookingapp.Repository;

import com.example.bookingapp.API.CustomerApi;
import com.example.bookingapp.Entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {
}
