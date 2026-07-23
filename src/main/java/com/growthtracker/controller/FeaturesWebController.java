package com.growthtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/features")
public class FeaturesWebController {

    @GetMapping("/milestones")
    public String milestones() { return "features/milestones"; }

    @GetMapping("/calendar")
    public String calendar() { return "features/calendar"; }
}
