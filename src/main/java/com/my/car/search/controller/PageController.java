package com.my.car.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/search-result")
    public String uploadResultPage() {
        return "/index.html";
    }
}
