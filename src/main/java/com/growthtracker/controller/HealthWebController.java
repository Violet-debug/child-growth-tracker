package com.growthtracker.controller;

import com.growthtracker.model.health.*;
import com.growthtracker.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/health")
public class HealthWebController {

    private final SupplementRecordRepository supRepo;
    private final SicknessRecordRepository sickRepo;
    private final MedicationRepository medRepo;

    public HealthWebController(SupplementRecordRepository supRepo, SicknessRecordRepository sickRepo, MedicationRepository medRepo) {
        this.supRepo = supRepo;
        this.sickRepo = sickRepo;
        this.medRepo = medRepo;
    }

    @GetMapping
    public String index() { return "health/index"; }

    // === 补给 ===
    @GetMapping("/supplements")
    public String supplements(Model model) {
        model.addAttribute("supplements", supRepo.findAllByOrderByRecordDateDesc());
        model.addAttribute("newSupplement", new SupplementRecord());
        return "health/supplements";
    }

    @PostMapping("/supplements/add")
    public String addSupplement(@ModelAttribute SupplementRecord r) {
        if (r.getRecordDate() == null) r.setRecordDate(LocalDate.now());
        supRepo.save(r);
        return "redirect:/health/supplements";
    }

    @PostMapping("/supplements/delete/{id}")
    public String deleteSupplement(@PathVariable Long id) {
        supRepo.deleteById(id);
        return "redirect:/health/supplements";
    }

    // === 就医 ===
    @GetMapping("/sickness")
    public String sickness(Model model) {
        model.addAttribute("sicknessList", sickRepo.findAllByOrderByStartDateDesc());
        model.addAttribute("newSickness", new SicknessRecord());
        return "health/sickness";
    }

    @GetMapping("/sickness/{id}")
    public String sicknessDetail(@PathVariable Long id, Model model) {
        var sick = sickRepo.findById(id).orElseThrow();
        model.addAttribute("sick", sick);
        return "health/sickness-detail";
    }

    @PostMapping("/sickness/add")
    public String addSickness(@ModelAttribute SicknessRecord r) {
        if (r.getStartDate() == null) r.setStartDate(LocalDate.now());
        sickRepo.save(r);
        return "redirect:/health/sickness";
    }

    @PostMapping("/sickness/{id}/update")
    public String updateSickness(@PathVariable Long id, @ModelAttribute SicknessRecord r) {
        sickRepo.findById(id).ifPresent(existing -> {
            existing.setEndDate(r.getEndDate());
            existing.setDiagnosis(r.getDiagnosis());
            existing.setHospital(r.getHospital());
            existing.setNotes(r.getNotes());
            if (r.getSymptoms() != null && !r.getSymptoms().isBlank()) existing.setSymptoms(r.getSymptoms());
            sickRepo.save(existing);
        });
        return "redirect:/health/sickness/" + id;
    }

    @PostMapping("/sickness/{id}/medication/add")
    public String addMedication(@PathVariable Long id, @ModelAttribute Medication m) {
        sickRepo.findById(id).ifPresent(sick -> {
            m.setSicknessRecord(sick);
            medRepo.save(m);
        });
        return "redirect:/health/sickness/" + id;
    }

    @PostMapping("/medication/{id}/delete")
    public String deleteMedication(@PathVariable Long id) {
        var m = medRepo.findById(id);
        if (m.isPresent()) {
            Long sickId = m.get().getSicknessRecord().getId();
            medRepo.deleteById(id);
            return "redirect:/health/sickness/" + sickId;
        }
        return "redirect:/health/sickness";
    }

    @PostMapping("/sickness/{id}/delete")
    public String deleteSickness(@PathVariable Long id) {
        sickRepo.deleteById(id);
        return "redirect:/health/sickness";
    }
}
