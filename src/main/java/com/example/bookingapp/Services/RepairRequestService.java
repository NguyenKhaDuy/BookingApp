package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.Request.RequestCustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface RepairRequestService {
    Object createRepairRequest(RequestCustomerRequest requestCustomerRequest);
    Page<RepairRequestDTO> getAll(Integer pageNo);
    Page<RepairRequestDTO> getAllByUser(Integer pageNo, String id_user);
    Object getById(Long id_request);
    Object cancelRequest(Long id_request);
}
