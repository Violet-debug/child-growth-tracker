package com.growthtracker.controller;

import com.growthtracker.model.GrowthRecord;
import com.growthtracker.repository.GrowthRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class WebController {

    private final GrowthRecordRepository repository;

    public WebController(GrowthRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("records", repository.findAllByOrderByRecordDateAsc());
        model.addAttribute("record", new GrowthRecord());
        return "index";
    }

    @PostMapping("/add")
    public String addRecord(@ModelAttribute GrowthRecord record) {
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        repository.save(record);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }
}
