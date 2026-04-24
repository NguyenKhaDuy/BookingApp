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
    @PostMapping("/api/test/statistic/")
    public ResponseEntity<Object> testSendNotification() {

        String title = "Có đơn hàng mới";
        String body = "Vui lòng xác nhận để nhận đơn hàng";
        String type = "REQUEST_CREATED";

        MessageNotifiDTO dto = new MessageNotifiDTO();
        dto.setType(type);
        dto.setTitle(title);
        dto.setBody(body);
        dto.setDateTime(LocalDateTime.now());

        // ✅ gửi đúng user
        webSocketService.sendPrivateUser("nguyenkhaduy754@gmail.com", dto);

        return ResponseEntity.ok("Đã gửi notification");
    }
}