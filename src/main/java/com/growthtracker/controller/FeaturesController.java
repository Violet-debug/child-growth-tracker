package com.growthtracker.controller;

import com.growthtracker.model.GrowthRecord;
import com.growthtracker.model.child.Child;
import com.growthtracker.model.health.SicknessRecord;
import com.growthtracker.model.health.SupplementRecord;
import com.growthtracker.repository.*;
import com.growthtracker.service.ChildService;
import com.growthtracker.service.MilestoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/features")
public class FeaturesController {

    private final ChildService childService;
    private final MilestoneService milestoneService;
    private final GrowthRecordRepository growthRepo;
    private final SicknessRecordRepository sickRepo;
    private final SupplementRecordRepository supRepo;

    public FeaturesController(ChildService cs, MilestoneService ms,
                              GrowthRecordRepository gr, SicknessRecordRepository sr,
                              SupplementRecordRepository spr) {
        this.childService = cs; this.milestoneService = ms;
        this.growthRepo = gr; this.sickRepo = sr; this.supRepo = spr;
    }

    // === 孩子管理 ===
    @GetMapping("/children")
    public List<Child> getChildren() { return childService.getAll(); }

    @PostMapping("/children")
    public Child addChild(@RequestBody Child c) { return childService.save(c); }

    @GetMapping("/children/{id}")
    public Child getChild(@PathVariable Long id) { return childService.getById(id); }

    // === 发育里程碑 ===
    @GetMapping("/milestones/{childId}")
    public List<Map<String, Object>> getMilestones(@PathVariable Long childId) {
        return milestoneService.getProgress(childId);
    }

    @PutMapping("/milestones/{id}/toggle")
    public MilestoneResponse toggleMilestone(@PathVariable Long id, @RequestParam(required = false) String achievedDate) {
        LocalDate d = achievedDate != null ? LocalDate.parse(achievedDate) : null;
        var m = milestoneService.toggle(id, d);
        return new MilestoneResponse(m.getId(), m.getDescription(), m.getAchieved(), m.getAchievedDate());
    }

    record MilestoneResponse(Long id, String description, Boolean achieved, LocalDate achievedDate) {}

    // === 日历视图 ===
    @GetMapping("/calendar/{childId}")
    public Map<String, Object> getCalendar(@PathVariable Long childId,
                                            @RequestParam int year, @RequestParam int month) {
        List<Map<String, Object>> events = new ArrayList<>();

        // 成长记录
        for (GrowthRecord r : growthRepo.findAllByOrderByRecordDateAsc()) {
            if (r.getRecordDate().getYear() == year && r.getRecordDate().getMonthValue() == month) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("date", r.getRecordDate().toString());
                e.put("type", "growth");
                e.put("title", "📏 身高" + (r.getHeight() != null ? r.getHeight() : "?") + "cm 体重" + (r.getWeight() != null ? r.getWeight() : "?") + "kg");
                events.add(e);
            }
        }

        // 生病记录
        for (SicknessRecord s : sickRepo.findAllByOrderByStartDateDesc()) {
            LocalDate d = s.getStartDate();
            if (d.getYear() == year && d.getMonthValue() == month) {
                events.add(Map.of("date", d.toString(), "type", "sickness", "title", "🤒 " + s.getSymptoms()));
            }
        }

        // 补充剂记录
        for (SupplementRecord s : supRepo.findAllByOrderByRecordDateDesc()) {
            if (s.getRecordDate() != null && s.getRecordDate().getYear() == year && s.getRecordDate().getMonthValue() == month) {
                events.add(Map.of("date", s.getRecordDate().toString(), "type", "supplement", "title", "💊 " + (s.getSupplementName() != null ? s.getSupplementName() : "补充剂")));
            }
        }

        return Map.of("events", events);
    }

    // === 提醒功能 ===
    @GetMapping("/reminders/{childId}")
    public List<Map<String, Object>> getReminders(@PathVariable Long childId) {
        List<Map<String, Object>> reminders = new ArrayList<>();
        Child child = childService.getById(childId);
        if (child == null) return reminders;

        int age = child.getAgeMonths();
        LocalDate now = LocalDate.now();

        // 距上次测量超过30天提醒
        var records = growthRepo.findAllByOrderByRecordDateAsc();
        if (!records.isEmpty()) {
            var last = records.get(records.size() - 1);
            long daysSince = java.time.temporal.ChronoUnit.DAYS.between(last.getRecordDate(), now);
            if (daysSince > 30) {
                reminders.add(Map.of("type", "measure", "priority", "high",
                    "message", "⚠️ 已经" + daysSince + "天没有测量了！建议每月测量一次。"));
            }
        } else {
            reminders.add(Map.of("type", "measure", "priority", "high",
                "message", "📏 还没有记录过身高体重，快来添加第一条吧！"));
        }

        // 里程碑提醒
        var milestones = milestoneService.getProgress(childId);
        for (var m : milestones) {
            if (Boolean.TRUE.equals(m.get("overdue")) && !Boolean.TRUE.equals(m.get("achieved"))) {
                reminders.add(Map.of("type", "milestone", "priority", "medium",
                    "message", "🎯 " + m.get("description") + "（该月龄应已掌握，点击里程碑标记完成）"));
                break; // 只提醒一条里程碑
            }
        }

        // 补充剂建议（如果今天还没吃）
        long todaySup = supRepo.findAllByOrderByRecordDateDesc().stream()
            .filter(s -> s.getRecordDate() != null && s.getRecordDate().equals(now)).count();
        if (todaySup == 0) {
            reminders.add(Map.of("type", "supplement", "priority", "low",
                "message", "💊 今天还没记录补充剂哦"));
        }

        return reminders;
    }

    // === 趋势洞察 ===
    @GetMapping("/trends/{childId}")
    public List<Map<String, Object>> getTrends(@PathVariable Long childId) {
        List<Map<String, Object>> trends = new ArrayList<>();
        var records = growthRepo.findAllByOrderByRecordDateAsc();
        if (records.size() < 3) return trends;

        // 检查身高趋势
        checkTrend(records, "height", trends);
        checkTrend(records, "weight", trends);
        return trends;
    }

    private void checkTrend(List<GrowthRecord> records, String field, List<Map<String, Object>> trends) {
        int n = records.size();
        var early = records.get(0);
        var mid = records.get(n / 2);
        var late = records.get(n - 1);

        double v1, v2, v3;
        String fieldName, unit;
        if (field.equals("height")) {
            v1 = early.getHeight(); v2 = mid.getHeight(); v3 = late.getHeight();
            fieldName = "身高"; unit = "cm";
        } else {
            v1 = early.getWeight(); v2 = mid.getWeight(); v3 = late.getWeight();
            fieldName = "体重"; unit = "kg";
        }
        if (v1 == 0 || v2 == 0 || v3 == 0) return;

        double d1 = v2 - v1, d2 = v3 - v2;
        if (d2 < d1 * 0.6 && d2 > 0) {
            trends.add(Map.of("field", field, "level", "warn",
                "message", fieldName + "增长速度放缓（前段+" + Math.round(d1*10)/10.0 + unit + " → 后段+" + Math.round(d2*10)/10.0 + unit + "），建议关注",
                "change", Math.round((d2/d1 - 1) * 100) + "%"));
        } else if (d2 > d1 * 1.5) {
            trends.add(Map.of("field", field, "level", "info",
                "message", fieldName + "增长速度加快（前段+" + Math.round(d1*10)/10.0 + unit + " → 后段+" + Math.round(d2*10)/10.0 + unit + "）",
                "change", "+" + Math.round((d2/d1 - 1) * 100) + "%"));
        }
    }

    // === 数据导出 ===
    @GetMapping("/export/{childId}")
    public Map<String, Object> exportData(@PathVariable Long childId) {
        Child child = childService.getById(childId);
        if (child == null) return Map.of("error", "孩子不存在");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("child", Map.of("name", child.getName(), "gender", child.getGender(),
            "birthdate", child.getBirthdate().toString(), "ageMonths", child.getAgeMonths()));
        data.put("growthRecords", growthRepo.findAllByOrderByRecordDateAsc());
        data.put("sicknessRecords", sickRepo.findAllByOrderByStartDateDesc());
        data.put("supplementRecords", supRepo.findAllByOrderByRecordDateDesc());
        data.put("milestones", milestoneService.getProgress(childId));
        data.put("exportDate", LocalDate.now().toString());
        return data;
    }
}
