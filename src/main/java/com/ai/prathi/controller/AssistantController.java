package com.ai.prathi.controller;

import com.ai.prathi.agent.Assistant;
import com.ai.prathi.agent.OrderAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final Assistant assistant;
    private final OrderAgent orderAgent;

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return assistant.chat(message);
    }

    @GetMapping("/order")
    public String order(@RequestParam("message") String message) {
        return orderAgent.chat(message);
    }

}