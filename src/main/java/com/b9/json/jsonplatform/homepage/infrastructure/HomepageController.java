package com.b9.json.jsonplatform.homepage.infrastructure;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/homepage")
public class HomepageController {

    @GetMapping("")
    public String index(Model model) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        model.addAttribute("title", "Project JSON - Deployment Success");
        model.addAttribute("time", now);
        model.addAttribute("host", "AWS Learner Lab EC2");

        return "homepage";
    }
}