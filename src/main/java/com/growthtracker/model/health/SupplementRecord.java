package com.growthtracker.model.health;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "supplement_records")
public class SupplementRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private String supplementName;

    private String brand;
    private String dosage;
    private String notes;

    public SupplementRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate d) { this.recordDate = d; }
    public String getSupplementName() { return supplementName; }
    public void setSupplementName(String s) { this.supplementName = s; }
    public String getBrand() { return brand; }
    public void setBrand(String b) { this.brand = b; }
    public String getDosage() { return dosage; }
    public void setDosage(String d) { this.dosage = d; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
}
