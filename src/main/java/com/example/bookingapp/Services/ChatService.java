package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.ConversationDTO;
import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.Request.ChatRequest;
import com.example.bookingapp.Models.Response.ChatResponse;
import com.example.bookingapp.Models.Response.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {
    ChatResponse ask(ChatRequest chatRequest, String authorization);
    Object getConversationsByUser(String userId);
    MessageResponse deleteConversation(String conversationId);
}
