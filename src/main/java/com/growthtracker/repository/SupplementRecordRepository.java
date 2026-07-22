package com.growthtracker.repository;

import com.growthtracker.model.health.SupplementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplementRecordRepository extends JpaRepository<SupplementRecord, Long> {
    List<SupplementRecord> findAllByOrderByRecordDateDesc();
    List<SupplementRecord> findBySupplementNameOrderByRecordDateDesc(String name);
}
