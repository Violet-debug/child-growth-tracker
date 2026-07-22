package com.growthtracker.model.health;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sickness_records")
public class SicknessRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    private String hospital;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "sicknessRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Medication> medications = new ArrayList<>();

    public SicknessRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate d) { this.endDate = d; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String s) { this.symptoms = s; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String d) { this.diagnosis = d; }
    public String getHospital() { return hospital; }
    public void setHospital(String h) { this.hospital = h; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> m) { this.medications = m; }
}
