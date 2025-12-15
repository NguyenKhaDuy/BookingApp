package com.example.bookingapp.API;

import com.example.bookingapp.Services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestNotificationController {

    @Autowired
    private WebSocketService webSocketService;

    // API này để test việc gửi thông báo riêng tư bằng Postman
    @PostMapping(value = "/api/test/send-notify/")
    public ResponseEntity<String> testSendNotification(
            @RequestParam(required = false) String email,
            @RequestParam String title,
            @RequestParam String body) {

//        if(email != null){
//            webSocketService.sendPrivateUser(email,title, body);
//        }else{
//            webSocketService.sendAllUser(title, body);
//        }


        return ResponseEntity.ok("Success");
    }
}