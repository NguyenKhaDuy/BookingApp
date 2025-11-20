package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Models.Request.NotificationRequest;
import com.example.bookingapp.Services.NotificationService;
import com.example.bookingapp.Services.NotificationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationApi {
    @Autowired
    NotificationUserService notificationUserService;
    @Autowired
    NotificationService notificationService;
    @GetMapping(value = "/api/notification/id_user={id_user}")
    public ResponseEntity<Object> getAllByUser(@PathVariable String id_user,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<NotificationDTO> notificationDTOS = notificationUserService.getAllByUser(id_user, pageNo);
        if(notificationDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(notificationDTOS.getTotalPages());
        dataDTO.setData(notificationDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/notification/")
    public ResponseEntity<Object> detailNotification(@RequestParam(name = "id_user") String id_user, @RequestParam(name = "id_notify") Long id_notify ){
        Object result = notificationUserService.getById(id_user, id_notify);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/notification/id_user={id}")
    public ResponseEntity<Object> deleteNotification(@PathVariable String id, @RequestBody DeleteRequest deleteRequest){
        Object result = notificationUserService.deleteNotification(id, deleteRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/notifications/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<NotificationDTO> notificationDTOS = notificationService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(notificationDTOS.getTotalPages());
        dataDTO.setData(notificationDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/notifications/id={id_notify}")
    public ResponseEntity<Object> detailNotification(@PathVariable Long id_notify){
        Object result = notificationService.detailNotification(id_notify);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/notifications/id={id_notify}")
    public ResponseEntity<Object> deleteNotification(@PathVariable Long id_notify){
        Object result = notificationService.detailNotification(id_notify);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/notification/")
    public ResponseEntity<Object> createNotification(@RequestBody NotificationRequest notificationRequest){
        Object result = notificationService.createNotification(notificationRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/notification/")
    public ResponseEntity<Object> updateNotification(@RequestBody NotificationRequest notificationRequest){
        Object result = notificationService.updateNotification(notificationRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
