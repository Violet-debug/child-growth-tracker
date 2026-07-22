package com.growthtracker.controller;

import com.growthtracker.service.GrowthStandardService;
import com.growthtracker.repository.GrowthRecordRepository;
import com.growthtracker.model.GrowthRecord;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/standards")
public class StandardController {

    private final GrowthStandardService service;
    private final GrowthRecordRepository repo;

    public StandardController(GrowthStandardService service, GrowthRecordRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    /** 列出可用标准 */
    @GetMapping("/list")
    public List<Map<String, String>> listStandards() {
        return service.getAvailableStandards();
    }

    /** 获取标准曲线数据 */
    @GetMapping
    public Map<String, Object> getStandards(
            @RequestParam(defaultValue = "WHO_2006") String standard,
            @RequestParam(defaultValue = "MALE") String gender) {
        return service.getStandards(standard, gender);
    }

    /** 计算百分位 */
    @GetMapping("/percentile")
    public Map<String, Object> getPercentile(
            @RequestParam(defaultValue = "WHO_2006") String standard,
            @RequestParam(defaultValue = "MALE") String gender,
            @RequestParam String indicator,
            @RequestParam double value,
            @RequestParam int ageMonths) {
        return service.calculatePercentile(standard, gender, indicator, value, ageMonths);
    }

    /** 预测生长趋势 */
    @GetMapping("/prediction")
    public Map<String, Object> getPrediction(@RequestParam(defaultValue = "12") int monthsAhead) {
        List<GrowthRecord> records = repo.findAllByOrderByRecordDateAsc();
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (GrowthRecord r : records) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("recordDate", r.getRecordDate() != null ? r.getRecordDate().toString() : "");
            m.put("height", r.getHeight());
            m.put("weight", r.getWeight());
            m.put("headCircumference", r.getHeadCircumference());
            mapped.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("height", service.predict(mapped, "recordDate", "height", monthsAhead));
        result.put("weight", service.predict(mapped, "recordDate", "weight", monthsAhead));
        result.put("headCircumference", service.predict(mapped, "recordDate", "headCircumference", monthsAhead));
        return result;
    }
}
