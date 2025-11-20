package com.example.bookingapp.Repository.Custom;

import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Models.Request.FillterRequest;
import com.example.bookingapp.Models.Request.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepositoryCustom {
    Page<RepairRequestEntity> searchRequest(SearchRequest searchRequest, Pageable pageable);
    Page<RepairRequestEntity> fillterRequest(FillterRequest fillterRequest, Pageable pageable);
}
