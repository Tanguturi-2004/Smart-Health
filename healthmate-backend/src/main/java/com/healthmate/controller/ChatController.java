package com.healthmate.controller;

import com.healthmate.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<Map<String, String>> getChatResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String botReply = chatService.getResponse(userMessage);

        Map<String, String> response = new HashMap<>();
        response.put("reply", botReply);

        return ResponseEntity.ok(response);
    }
}
