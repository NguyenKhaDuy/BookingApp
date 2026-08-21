package com.example.bookingapp.API;

import com.example.bookingapp.Models.Request.ChatRequest;
import com.example.bookingapp.Models.Response.ChatResponse;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {
    @Autowired
    ChatService chatService;

    @PostMapping("/api/chat")
    public ResponseEntity<Object> chat(
            @RequestBody ChatRequest chatRequest,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        ChatResponse chatResponse = chatService.ask(chatRequest, authorization);
        return new ResponseEntity<>(chatResponse, HttpStatus.OK);
    }

    @GetMapping("/api/customer/chat/history/idUser={id}")
    public ResponseEntity<Object> getChatHistory(@PathVariable String id) {
        Object result = chatService.getConversationsByUser(id);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/api/customer/chat/idChat={id}")
    public ResponseEntity<Object> deleteChat(@PathVariable String id) {
        MessageResponse messageResponse = chatService.deleteConversation(id);
        return new ResponseEntity<>(messageResponse, messageResponse.getHttpStatus());
    }
}
