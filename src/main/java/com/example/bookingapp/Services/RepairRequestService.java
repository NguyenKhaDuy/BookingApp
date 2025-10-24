package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.Request.AcceptRequest;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Models.Request.RequestCustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RepairRequestService {
    Object createRepairRequest(RequestCustomerRequest requestCustomerRequest);
    Page<RepairRequestDTO> getAll(Integer pageNo);
    Page<RepairRequestDTO> getAllByCustomer(Integer pageNo, String id_user);
    Object getById(Long id_request);
    Object cancelRequest(Long id_request);
    Page<RepairRequestDTO> getByStatusAndCustomer(Integer pageNo, String id_user, String status_code);
    Page<RepairRequestDTO> getByStatus(Integer pageNo, String status_code);
    MessageDTO deleteRequest(DeleteRequest deleteRequest);
    Page<RepairRequestDTO> getByStatusAndTechnician(Integer pageNo, String id_user, String status_code);
    Object acceptRequest(AcceptRequest acceptRequest);
}
