package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.Request.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RepairRequestService {
    Object createRepairRequest(RequestCustomerRequest requestCustomerRequest);
    Page<RepairRequestDTO> getAll(Integer pageNo);
    List<RepairRequestDTO> getAllByCustomer(String id_user);
    Object getById(Long id_request);
    Object cancelRequest(Long id_request);
    Page<RepairRequestDTO> getByStatusAndCustomer(Integer pageNo, String id_user, String status_code);
    Page<RepairRequestDTO> getByStatus(Integer pageNo, String status_code);
    MessageResponse deleteRequest(DeleteRequest deleteRequest);
    Page<RepairRequestDTO> getByTechnician(Integer pageNo, String id_user);
    Object acceptRequest(AcceptRequest acceptRequest);
    Object refuseRequest(String id_tech, Long id_request);
    Page<RepairRequestDTO> searchRequest(SearchRequest searchRequest, Integer pageNo);
    Page<RepairRequestDTO> fillterRequest(FillterRequest fillterRequest, Integer pageNo);
    Object updateStatusRequest(UpdateStatusRquest updateStatusRquest);
}
