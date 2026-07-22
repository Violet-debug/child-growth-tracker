package com.growthtracker.controller;

import com.growthtracker.model.health.*;
import com.growthtracker.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthRestController {

    private final SupplementRecordRepository supRepo;
    private final SicknessRecordRepository sickRepo;
    private final MedicationRepository medRepo;

    public HealthRestController(SupplementRecordRepository supRepo, SicknessRecordRepository sickRepo, MedicationRepository medRepo) {
        this.supRepo = supRepo;
        this.sickRepo = sickRepo;
        this.medRepo = medRepo;
    }

    // === 补给 ===
    @GetMapping("/supplements")
    public List<SupplementRecord> getSupplements() { return supRepo.findAllByOrderByRecordDateDesc(); }

    @PostMapping("/supplements")
    public SupplementRecord addSupplement(@RequestBody SupplementRecord r) { return supRepo.save(r); }

    @DeleteMapping("/supplements/{id}")
    public ResponseEntity<Void> deleteSupplement(@PathVariable Long id) {
        if (supRepo.existsById(id)) { supRepo.deleteById(id); return ResponseEntity.noContent().build(); }
        return ResponseEntity.notFound().build();
    }

    // === 生病记录 ===
    @GetMapping("/sickness")
    public List<SicknessRecord> getSickness() { return sickRepo.findAllByOrderByStartDateDesc(); }

    @GetMapping("/sickness/{id}")
    public ResponseEntity<SicknessRecord> getSicknessDetail(@PathVariable Long id) {
        return sickRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sickness")
    public SicknessRecord addSickness(@RequestBody SicknessRecord r) { return sickRepo.save(r); }

    @PutMapping("/sickness/{id}")
    public ResponseEntity<SicknessRecord> updateSickness(@PathVariable Long id, @RequestBody SicknessRecord r) {
        return sickRepo.findById(id).map(existing -> {
            existing.setStartDate(r.getStartDate());
            existing.setEndDate(r.getEndDate());
            existing.setSymptoms(r.getSymptoms());
            existing.setDiagnosis(r.getDiagnosis());
            existing.setHospital(r.getHospital());
            existing.setNotes(r.getNotes());
            return ResponseEntity.ok(sickRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sickness/{id}")
    public ResponseEntity<Void> deleteSickness(@PathVariable Long id) {
        if (sickRepo.existsById(id)) { sickRepo.deleteById(id); return ResponseEntity.noContent().build(); }
        return ResponseEntity.notFound().build();
    }

    // === 药品 ===
    @GetMapping("/sickness/{sickId}/medications")
    public List<Medication> getMedications(@PathVariable Long sickId) {
        return medRepo.findBySicknessRecordIdOrderByIdAsc(sickId);
    }

    @PostMapping("/sickness/{sickId}/medications")
    public ResponseEntity<Medication> addMedication(@PathVariable Long sickId, @RequestBody Medication m) {
        return sickRepo.findById(sickId).map(sick -> {
            m.setSicknessRecord(sick);
            return ResponseEntity.ok(medRepo.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/medications/{id}")
    public ResponseEntity<Medication> updateMedication(@PathVariable Long id, @RequestBody Medication m) {
        return medRepo.findById(id).map(existing -> {
            existing.setMedicationName(m.getMedicationName());
            existing.setSpecification(m.getSpecification());
            existing.setDosage(m.getDosage());
            existing.setUsageMethod(m.getUsageMethod());
            existing.setFrequency(m.getFrequency());
            existing.setStartDate(m.getStartDate());
            existing.setEndDate(m.getEndDate());
            existing.setSkinTest(m.getSkinTest());
            existing.setInstructions(m.getInstructions());
            existing.setNotes(m.getNotes());
            return ResponseEntity.ok(medRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/medications/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        if (medRepo.existsById(id)) { medRepo.deleteById(id); return ResponseEntity.noContent().build(); }
        return ResponseEntity.notFound().build();
    }
}
