package com.growthtracker;

import com.growthtracker.model.GrowthRecord;
import com.growthtracker.repository.GrowthRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GrowthTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrowthTrackerApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(GrowthRecordRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            GrowthRecord r1 = new GrowthRecord(java.time.LocalDate.of(2023, 12, 15), 50.0, 2.65);
            r1.setNotes("新生儿");
            repository.save(r1);

            GrowthRecord r2 = new GrowthRecord(java.time.LocalDate.of(2024, 1, 15), 52.5, 3.60);
            r2.setHeadCircumference(36.0);
            r2.setNotes("1个月");
            repository.save(r2);

            GrowthRecord r3 = new GrowthRecord(java.time.LocalDate.of(2024, 3, 15), 58.0, 5.00);
            r3.setHeadCircumference(38.5);
            r3.setNotes("3个月");
            repository.save(r3);

            GrowthRecord r4 = new GrowthRecord(java.time.LocalDate.of(2024, 6, 17), 66.0, 7.60);
            r4.setHeadCircumference(42.0);
            r4.setNotes("6个月");
            repository.save(r4);

            GrowthRecord r5 = new GrowthRecord(java.time.LocalDate.of(2024, 8, 15), 69.5, 8.10);
            r5.setHeadCircumference(44.0);
            r5.setNotes("8个月");
            repository.save(r5);

            GrowthRecord r6 = new GrowthRecord(java.time.LocalDate.of(2024, 12, 16), 74.5, 10.60);
            r6.setHeadCircumference(45.0);
            r6.setNotes("1岁");
            repository.save(r6);

            GrowthRecord r7 = new GrowthRecord(java.time.LocalDate.of(2025, 6, 15), 83.0, 11.10);
            r7.setHeadCircumference(46.0);
            r7.setNotes("1岁6个月");
            repository.save(r7);

            GrowthRecord r8 = new GrowthRecord(java.time.LocalDate.of(2025, 12, 17), 89.0, 14.50);
            r8.setHeadCircumference(47.5);
            r8.setNotes("2岁");
            repository.save(r8);

            GrowthRecord r9 = new GrowthRecord(java.time.LocalDate.of(2026, 6, 13), 93.0, 14.00);
            r9.setHeadCircumference(48.0);
            r9.setNotes("2岁5个月");
            repository.save(r9);
        };
    }
}
