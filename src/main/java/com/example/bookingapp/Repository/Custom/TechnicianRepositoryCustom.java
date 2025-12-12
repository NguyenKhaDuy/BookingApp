package com.example.bookingapp.Repository.Custom;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.Request.SearchByLocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianRepositoryCustom {
    Page<TechnicianEntity> findByLocation(SearchByLocationRequest searchByLocationRequest, Pageable pageable);
    Page<TechnicianEntity> searchByName(String name_technician, Pageable pageable);
    Long countBusy(String id_tech);
}
