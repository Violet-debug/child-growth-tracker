package com.growthtracker.controller;

import com.growthtracker.model.GrowthRecord;
import com.growthtracker.repository.GrowthRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/records")
public class GrowthRecordController {

    private final GrowthRecordRepository repository;

    public GrowthRecordController(GrowthRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<GrowthRecord> getAllRecords() {
        return repository.findAllByOrderByRecordDateAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrowthRecord> getRecord(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public GrowthRecord createRecord(@RequestBody GrowthRecord record) {
        return repository.save(record);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrowthRecord> updateRecord(@PathVariable Long id, @RequestBody GrowthRecord record) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setRecordDate(record.getRecordDate());
                    existing.setHeight(record.getHeight());
                    existing.setWeight(record.getWeight());
                    existing.setHeadCircumference(record.getHeadCircumference());
                    existing.setNotes(record.getNotes());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

   @DeleteMapping("/{id}")
   public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
       if (repository.existsById(id)) {
           repository.deleteById(id);
           return ResponseEntity.noContent().build();
       }
       return ResponseEntity.notFound().build();
   }

    @PostMapping("/batch")
    public List<GrowthRecord> batchImport(@RequestBody List<GrowthRecord> records) {
        return repository.saveAll(records);
    }
}
