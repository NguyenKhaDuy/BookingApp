package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.ServiceDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ServiceService {
    Page<ServiceDTO> getAll(Integer pageNo);
    Object getById(Long id_service);
}
