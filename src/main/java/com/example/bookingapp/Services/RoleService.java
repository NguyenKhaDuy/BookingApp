package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.RoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
    Page<RoleDTO> getAll(Integer pageNo);
    Object detailRole(Long id_role);
    Object createRole(RoleRequest roleRequest);
    Object updateRole(RoleRequest roleRequest);
    Object deleteRole(Long id_role);
}
