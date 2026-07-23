package com.growthtracker.model.child;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "milestones")
public class Milestone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long childId;
    private Integer expectMonth;    // 预期月龄
    private String category;        // MOTOR(大运动) FINE_MOTOR(精细运动) LANGUAGE(语言) SOCIAL(社交)
    private String description;
    private Boolean achieved;
    private LocalDate achievedDate;
    private String notes;

    public Milestone() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChildId() { return childId; }
    public void setChildId(Long c) { this.childId = c; }
    public Integer getExpectMonth() { return expectMonth; }
    public void setExpectMonth(Integer m) { this.expectMonth = m; }
    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public Boolean getAchieved() { return achieved; }
    public void setAchieved(Boolean a) { this.achieved = a; }
    public LocalDate getAchievedDate() { return achievedDate; }
    public void setAchievedDate(LocalDate d) { this.achievedDate = d; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
}
