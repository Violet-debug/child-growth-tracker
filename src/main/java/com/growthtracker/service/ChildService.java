package com.growthtracker.service;

import com.growthtracker.model.child.Child;
import com.growthtracker.repository.ChildRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChildService {
    private final ChildRepository repo;

    public ChildService(ChildRepository repo) { this.repo = repo; }

    @PostConstruct
    public void init() {
        if (repo.count() == 0) {
            // Seed default child from the first growth record's date
            Child c = new Child("宝宝", "FEMALE", LocalDate.of(2023, 12, 15));
            repo.save(c);
        }
    }

    public List<Child> getAll() { return repo.findAll(); }
    public Child getCurrent() { return repo.findAll().stream().findFirst().orElse(null); }
    public Child getById(Long id) { return repo.findById(id).orElse(null); }
    public Child save(Child c) { return repo.save(c); }
    public void delete(Long id) { repo.deleteById(id); }
}
