package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.FeedbackDTO;
import com.example.bookingapp.Models.Request.ReplyFeedbackRequest;
import com.example.bookingapp.Services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class FeedbackApi {
    @Autowired
    FeedbackService feedbackService;
    @GetMapping(value = "/api/admin/feedback/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<FeedbackDTO> feedbackDTOS = feedbackService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(feedbackDTOS.getTotalPages());
        dataDTO.setData(feedbackDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/feedback/id={id_feedback}")
    public ResponseEntity<Object> detailFeedback(@PathVariable Long id_feedback){
        Object result = feedbackService.detailFeedback(id_feedback);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/feedback/reply/")
    public ResponseEntity<Object> replyFeedback(@RequestBody ReplyFeedbackRequest replyFeedbackRequest){
        Object result = feedbackService.replyFeedback(replyFeedbackRequest);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
