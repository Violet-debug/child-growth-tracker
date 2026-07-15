package com.growthtracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "growth_records")
public class GrowthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double weight;

    private Double headCircumference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public GrowthRecord() {}

    public GrowthRecord(LocalDate recordDate, Double height, Double weight) {
        this.recordDate = recordDate;
        this.height = height;
        this.weight = weight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeadCircumference() { return headCircumference; }
    public void setHeadCircumference(Double headCircumference) { this.headCircumference = headCircumference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
