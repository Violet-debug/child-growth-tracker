package com.growthtracker.repository;

import com.growthtracker.model.health.SicknessRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SicknessRecordRepository extends JpaRepository<SicknessRecord, Long> {
    List<SicknessRecord> findAllByOrderByStartDateDesc();
}
