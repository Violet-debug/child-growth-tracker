package com.growthtracker.service;

import com.growthtracker.model.child.Milestone;
import com.growthtracker.repository.MilestoneRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MilestoneService {
    private final MilestoneRepository repo;
    private final ChildService childService;

    public MilestoneService(MilestoneRepository repo, ChildService childService) {
        this.repo = repo;
        this.childService = childService;
    }

    @PostConstruct
    public void init() {
        if (repo.count() > 0) return;
        var children = childService.getAll();
        if (children.isEmpty()) return;
        Long cid = children.get(0).getId();

        List<Milestone> seeds = new ArrayList<>();
        String[][] raw = {
            {"1","MOTOR","俯卧时尝试抬头"},
            {"2","MOTOR","俯卧时抬头45°"},
            {"3","MOTOR","俯卧时抬头90°"},
            {"3","FINE_MOTOR","能抓住玩具"},
            {"4","MOTOR","会翻身（俯卧→仰卧）"},
            {"5","SOCIAL","能认出熟悉的人"},
            {"6","MOTOR","能独坐一会儿"},
            {"6","FINE_MOTOR","能伸手抓东西"},
            {"6","LANGUAGE","发出ba/ma等音节"},
            {"7","MOTOR","会翻身（仰卧→俯卧）"},
            {"8","MOTOR","会爬行"},
            {"9","FINE_MOTOR","用拇指和食指捏取"},
            {"10","MOTOR","扶着东西站起来"},
            {"11","SOCIAL","会拍手/再见"},
            {"12","MOTOR","能独立站片刻"},
            {"12","LANGUAGE","有意识叫爸爸妈妈"},
            {"13","MOTOR","能独走几步"},
            {"15","MOTOR","能独走稳"},
            {"15","FINE_MOTOR","能用勺子吃饭"},
            {"18","LANGUAGE","会说10个左右单词"},
            {"18","MOTOR","能小跑"},
            {"20","SOCIAL","模仿做家务"},
            {"24","LANGUAGE","会说2-3个词组成的短句"},
            {"24","MOTOR","能双脚跳离地面"},
            {"24","SOCIAL","能自己穿鞋袜"},
            {"30","FINE_MOTOR","会画直线和圆圈"},
            {"36","LANGUAGE","能说出自己的姓名和年龄"},
            {"36","SOCIAL","能自己穿衣"},
            {"36","MOTOR","能单脚站立片刻"},
        };
        for (String[] row : raw) {
            Milestone m = new Milestone();
            m.setChildId(cid);
            m.setExpectMonth(Integer.parseInt(row[0]));
            m.setCategory(row[1]);
            m.setDescription(row[2]);
            m.setAchieved(false);
            seeds.add(m);
        }
        repo.saveAll(seeds);
    }

    public List<Map<String, Object>> getProgress(Long childId) {
        var all = repo.findByChildIdOrderByExpectMonthAsc(childId);
        int currentAge = childService.getById(childId) != null ? childService.getById(childId).getAgeMonths() : 0;

        List<Map<String, Object>> result = new ArrayList<>();
        for (Milestone m : all) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("expectMonth", m.getExpectMonth());
            item.put("category", m.getCategory());
            item.put("description", m.getDescription());
            item.put("achieved", m.getAchieved());
            item.put("achievedDate", m.getAchievedDate());
            item.put("overdue", !Boolean.TRUE.equals(m.getAchieved()) && m.getExpectMonth() <= currentAge);
            result.add(item);
        }
        return result;
    }

    public Milestone toggle(Long milestoneId, LocalDate achievedDate) {
        Milestone m = repo.findById(milestoneId).orElseThrow();
        if (Boolean.TRUE.equals(m.getAchieved())) {
            m.setAchieved(false);
            m.setAchievedDate(null);
        } else {
            m.setAchieved(true);
            m.setAchievedDate(achievedDate != null ? achievedDate : LocalDate.now());
        }
        return repo.save(m);
    }
}
