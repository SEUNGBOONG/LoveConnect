//package com.example.demo.chatgpt.controller;
//
//import com.example.demo.chatgpt.service.GPTService;
//import com.example.demo.common.exception.ApiResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/gpt")
//@RequiredArgsConstructor
//public class GPTController {
//
//    private final GPTService gptService;
//
//    @PostMapping("/analyze")
//    public ResponseEntity<ApiResponse<String>> analyze(@RequestBody String text) throws Exception {
//        String result = gptService.ask(text);
//        return ResponseEntity.ok(ApiResponse.success(result));
//    }
//}
