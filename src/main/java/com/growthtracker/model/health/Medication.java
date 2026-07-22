package com.growthtracker.model.health;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "medications")
public class Medication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sickness_record_id", nullable = false)
    @JsonIgnore
    private SicknessRecord sicknessRecord;

    @Column(nullable = false)
    private String medicationName;

    private String specification;   // 规格 (如 0.2g/片、100ml/瓶)
    private String dosage;          // 用量 (如 5ml、1粒)
    private String usageMethod;     // 用法 (口服/外用/雾化/注射/滴眼/滴鼻)
    private String frequency;       // 频次 (如 3次/天、每8小时)
    private LocalDate startDate;    // 开始日期
    private LocalDate endDate;      // 结束日期
    private Boolean skinTest;       // 是否需要皮试
    private String instructions;    // 医嘱说明
    private String notes;

    public Medication() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SicknessRecord getSicknessRecord() { return sicknessRecord; }
    public void setSicknessRecord(SicknessRecord s) { this.sicknessRecord = s; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String n) { this.medicationName = n; }
    public String getSpecification() { return specification; }
    public void setSpecification(String s) { this.specification = s; }
    public String getDosage() { return dosage; }
    public void setDosage(String d) { this.dosage = d; }
    public String getUsageMethod() { return usageMethod; }
    public void setUsageMethod(String u) { this.usageMethod = u; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String f) { this.frequency = f; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate d) { this.endDate = d; }
    public Boolean getSkinTest() { return skinTest; }
    public void setSkinTest(Boolean s) { this.skinTest = s; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String i) { this.instructions = i; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
}
