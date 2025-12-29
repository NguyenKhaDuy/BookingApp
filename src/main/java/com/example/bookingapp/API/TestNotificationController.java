package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.MessageNotifiDTO;
import com.example.bookingapp.Models.DTO.RevenueDTO;
import com.example.bookingapp.Models.Response.StatisticsResponse;
import com.example.bookingapp.Services.StatisticService;
import com.example.bookingapp.Services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TestNotificationController {

    @Autowired
    StatisticService statisticService;
    @Autowired
    WebSocketService webSocketService;

    // API này để test việc gửi thông báo riêng tư bằng Postman
    @PostMapping(value = "/api/test/statistic/")
    public ResponseEntity<Object> testSendNotification() {
        List<RevenueDTO> revenueDTOS = statisticService.monthlyRevenue(2025L);
        String title = "Có đơn hàng mới";
        String body = "Vui lòng xác nhận để nhận đơn hàng";
        String type = "REQUEST_CREATED";
        MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
        messageNotifiDTO.setType(type);
        messageNotifiDTO.setTitle(title);
        messageNotifiDTO.setBody(body);
        messageNotifiDTO.setDateTime(LocalDateTime.now());
        webSocketService.sendPrivateUser("duynguyen@gmail.com", messageNotifiDTO);
        return ResponseEntity.ok(revenueDTOS);
    }
}