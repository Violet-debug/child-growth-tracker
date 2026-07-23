package com.growthtracker.model.child;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "children")
public class Child {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gender; // MALE/FEMALE
    private LocalDate birthdate;
    private String avatar; // emoji

    public Child() {}
    public Child(String name, String gender, LocalDate birthdate) {
        this.name = name; this.gender = gender; this.birthdate = birthdate;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getGender() { return gender; }
    public void setGender(String g) { this.gender = g; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate d) { this.birthdate = d; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String a) { this.avatar = a; }

    public int getAgeMonths() {
        if (birthdate == null) return 0;
        LocalDate now = LocalDate.now();
        return (now.getYear() - birthdate.getYear()) * 12 + (now.getMonthValue() - birthdate.getMonthValue());
    }
    public int getAgeMonthsAt(LocalDate date) {
        if (birthdate == null || date == null) return 0;
        return (date.getYear() - birthdate.getYear()) * 12 + (date.getMonthValue() - birthdate.getMonthValue());
    }
}
