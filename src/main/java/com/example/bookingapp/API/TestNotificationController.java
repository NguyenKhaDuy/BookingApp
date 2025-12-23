package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.RevenueDTO;
import com.example.bookingapp.Models.Response.StatisticsResponse;
import com.example.bookingapp.Services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class TestNotificationController {

    @Autowired
    StatisticService statisticService;

    // API này để test việc gửi thông báo riêng tư bằng Postman
    @PostMapping(value = "/api/test/statistic/")
    public ResponseEntity<Object> testSendNotification() {
        List<RevenueDTO> revenueDTOS = statisticService.monthlyRevenue(2025L);

        return ResponseEntity.ok(revenueDTOS);
    }
}