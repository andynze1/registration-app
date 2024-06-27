package com.webapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyService {

    @GetMapping("/")
    public String home() {
        return "Hello, World!";
    }
}

