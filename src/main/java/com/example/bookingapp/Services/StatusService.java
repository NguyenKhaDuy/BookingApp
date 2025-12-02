package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.StatusDTO;
import com.example.bookingapp.Models.Request.StatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface StatusService {
    Page<StatusDTO> getAll(Integer pageNo);
    Object detailStatus(Long id_status);
    Object createStatus(StatusRequest statusRequest);
    Object updateStatus(StatusRequest statusRequest);
    Object deleteStatus(Long id_status);
}
