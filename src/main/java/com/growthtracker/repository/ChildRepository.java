package com.growthtracker.repository;

import com.growthtracker.model.child.Child;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<Child, Long> {}
