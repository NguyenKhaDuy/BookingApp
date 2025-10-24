package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.CustomerDTO;
import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Services.CustomerService;
import com.example.bookingapp.Services.FeedbackService;
import com.example.bookingapp.Services.RatingService;
import com.example.bookingapp.Services.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerApi {
    @Autowired
    CustomerService customerService;
    @Autowired
    RatingService ratingService;
    @Autowired
    RepairRequestService repairRequestService;
    @Autowired
    FeedbackService feedbackService;
    @GetMapping(value = "/api/customer/profile/id={id_customer}")
    public ResponseEntity<Object> getProfile(@PathVariable String id_customer){
        Object result = customerService.getProfile(id_customer);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData((CustomerDTO) result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/api/customer/profile/")
    public ResponseEntity<Object> updateProfile(@RequestBody ProfileRequest profileRequest){
       Object result = customerService.updateProfile(profileRequest);
       if(result instanceof ErrorDTO){
           return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
       }
       return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/customer/profile/avatar/")
    public ResponseEntity<Object> updateAvatar(@ModelAttribute AvatarRequest avatarRequest){
        Object result = customerService.updateAvatar(avatarRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/customer/request/")
    public ResponseEntity<Object> createRepairRequest(@ModelAttribute RequestCustomerRequest requestCustomerRequest){
        Object result = repairRequestService.createRepairRequest(requestCustomerRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/customer/request/id_customer={id}")
    public ResponseEntity<Object> getRequestByCustomer(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @PathVariable String id){
        ErrorDTO errorDTO = new ErrorDTO();
        Page<RepairRequestDTO> repairRequestDTOS = repairRequestService.getAllByCustomer(pageNo, id);
        if(repairRequestDTOS == null){
            errorDTO.setMessage("Can not found customer");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(repairRequestDTOS.getTotalPages());
        dataDTO.setData(repairRequestDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/api/customer/request/cancel/id={id_request}")
    public ResponseEntity<Object> cancelRequest(@PathVariable Long id_request){
        Object result = repairRequestService.cancelRequest(id_request);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/customer/rating/")
    public ResponseEntity<Object> createRating(@RequestBody RatingRequest ratingRequest){
        Object result = ratingService.createRatingTechnician(ratingRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/customer/rating/id={id}")
    public ResponseEntity<Object> deleteRating(@PathVariable Long id){
        Object result = ratingService.deleteRating(id);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/customer/request/{id_user}/status_code={status_code}")
    public ResponseEntity<Object> getRequestByStatus(@PathVariable String status_code, @PathVariable String id_user, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        ErrorDTO errorDTO = new ErrorDTO();
        Page<RepairRequestDTO> repairRequestDTOS = repairRequestService.getByStatusAndCustomer(pageNo, id_user, status_code);
        if(repairRequestDTOS == null){
            errorDTO.setMessage("Can not found status or customer");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(repairRequestDTOS.getTotalPages());
        dataDTO.setData(repairRequestDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/api/customer/feedback/")
    public ResponseEntity<Object> createFeedback(@RequestBody FeedbackRequest feedbackRequest){
        Object result = feedbackService.createFeedback(feedbackRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
