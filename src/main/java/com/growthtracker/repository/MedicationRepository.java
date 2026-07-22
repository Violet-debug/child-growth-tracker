package com.growthtracker.repository;

import com.growthtracker.model.health.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findBySicknessRecordIdOrderByIdAsc(Long sicknessRecordId);
}
