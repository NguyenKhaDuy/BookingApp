package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.NotificationTypeDTO;
import com.example.bookingapp.Models.Request.NotificationTypeRequest;
import com.example.bookingapp.Services.NotificationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NotificationTypeApi {
    @Autowired
    NotificationTypeService notificationTypeService;

    @GetMapping(value = "/api/admin/notification-type/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<NotificationTypeDTO> notificationTypeDTOS =notificationTypeService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(notificationTypeDTOS.getTotalPages());
        dataDTO.setData(notificationTypeDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/notification-type/id-type={id_type}")
    public ResponseEntity<Object> detailNotificationType(@PathVariable Long id_type){
        DataDTO dataDTO = new DataDTO();
        Object result = notificationTypeService.detailNotifyType(id_type);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/notification-type/")
    public ResponseEntity<Object> createNotificationType(@RequestBody NotificationTypeRequest notificationTypeRequest){
        Object result = notificationTypeService.createNotifyType(notificationTypeRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping(value = "/api/admin/notification-type/")
    public ResponseEntity<Object> updateNotificationType(@RequestBody NotificationTypeRequest notificationTypeRequest){
        Object result = notificationTypeService.updateNotifyType(notificationTypeRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/notification-type/id-type={id_type}")
    public ResponseEntity<Object> deleteNotificationType(@PathVariable Long id_type){
        Object result = notificationTypeService.deleteNotifyType(id_type);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
