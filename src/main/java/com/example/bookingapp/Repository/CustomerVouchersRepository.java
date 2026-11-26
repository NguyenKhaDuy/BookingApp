package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.CustomerVouchersEntity;
import com.example.bookingapp.Entity.VouchersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerVouchersRepository extends JpaRepository<CustomerVouchersEntity, String> {
    List<CustomerVouchersEntity> findByCustomerEntity(CustomerEntity customerEntity);
}
