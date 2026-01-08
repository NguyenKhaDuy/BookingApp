package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.ServiceDTO;
import com.example.bookingapp.Models.Request.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ServiceService {
    Page<ServiceDTO> getAll(Integer pageNo);
    List<ServiceDTO> getAll();
    Object getById(Long id_service);
    Object createService(ServiceRequest serviceRequest);
    Object updateService(ServiceRequest serviceRequest);
    Object deleteService(Long id_service);
}
