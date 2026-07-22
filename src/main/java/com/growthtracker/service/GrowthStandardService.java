package com.growthtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class GrowthStandardService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 标准来源列表 */
    public enum StandardSource {
        WHO_2006("WHO 2006 (国际标准)"),
        CN_2022("卫健委 WS/T 423-2022");

        public final String label;
        StandardSource(String label) { this.label = label; }
    }

    public enum Gender { MALE, FEMALE }

    /** CN_2022 原始 LMS 数据 */
    private Map<String, Map<String, List<LmsPoint>>> cnData = new HashMap<>();

    /** WHO 2006 标准曲线 (男孩) */
    private static final List<CurvePoint> WHO_HEIGHT = List.of(
        new CurvePoint(0, 46.5, 49.9, 53.0), new CurvePoint(1, 50.0, 53.5, 57.0),
        new CurvePoint(2, 52.5, 56.0, 60.0), new CurvePoint(3, 56.0, 60.5, 64.5),
        new CurvePoint(4, 58.5, 63.0, 67.0), new CurvePoint(5, 60.5, 65.5, 69.5),
        new CurvePoint(6, 62.5, 67.5, 72.5), new CurvePoint(8, 65.5, 70.5, 75.5),
        new CurvePoint(10, 68.5, 73.5, 78.5), new CurvePoint(12, 70.0, 75.5, 81.0),
        new CurvePoint(15, 72.5, 78.5, 84.5), new CurvePoint(18, 75.0, 81.5, 88.0),
        new CurvePoint(21, 77.5, 84.0, 91.0), new CurvePoint(24, 80.0, 86.5, 93.5),
        new CurvePoint(27, 82.0, 88.5, 96.0), new CurvePoint(30, 84.0, 91.0, 98.5),
        new CurvePoint(33, 86.0, 93.0, 101.0), new CurvePoint(36, 87.5, 95.0, 103.0),
        new CurvePoint(42, 91.0, 98.5, 106.5), new CurvePoint(48, 94.0, 102.0, 110.5),
        new CurvePoint(54, 97.0, 105.5, 114.0), new CurvePoint(60, 99.5, 108.5, 118.0),
        new CurvePoint(66, 102.0, 111.5, 121.5), new CurvePoint(72, 104.5, 114.5, 125.0)
    );

    private static final List<CurvePoint> WHO_WEIGHT = List.of(
        new CurvePoint(0, 2.5, 3.3, 4.3), new CurvePoint(1, 3.4, 4.5, 5.8),
        new CurvePoint(2, 4.0, 5.3, 6.8), new CurvePoint(3, 4.7, 6.3, 7.9),
        new CurvePoint(4, 5.3, 7.0, 8.8), new CurvePoint(5, 5.8, 7.5, 9.4),
        new CurvePoint(6, 6.0, 7.9, 10.0), new CurvePoint(8, 6.7, 8.7, 11.0),
        new CurvePoint(10, 7.2, 9.3, 11.7), new CurvePoint(12, 7.7, 9.8, 12.3),
        new CurvePoint(15, 8.3, 10.5, 13.3), new CurvePoint(18, 9.0, 11.3, 14.2),
        new CurvePoint(21, 9.4, 11.8, 14.9), new CurvePoint(24, 9.8, 12.2, 15.5),
        new CurvePoint(27, 10.2, 12.6, 16.1), new CurvePoint(30, 10.5, 13.1, 16.7),
        new CurvePoint(33, 10.8, 13.5, 17.3), new CurvePoint(36, 11.2, 14.0, 18.1),
        new CurvePoint(42, 11.9, 14.9, 19.4), new CurvePoint(48, 12.5, 15.8, 20.7),
        new CurvePoint(54, 13.1, 16.7, 22.0), new CurvePoint(60, 13.8, 17.7, 23.4),
        new CurvePoint(66, 14.4, 18.5, 24.8), new CurvePoint(72, 15.0, 19.3, 26.0)
    );

    private static final List<CurvePoint> WHO_HEAD = List.of(
        new CurvePoint(0, 32.0, 34.5, 37.0), new CurvePoint(1, 34.5, 37.0, 39.5),
        new CurvePoint(3, 38.0, 40.5, 43.0), new CurvePoint(6, 40.5, 43.0, 45.5),
        new CurvePoint(8, 41.5, 44.0, 46.5), new CurvePoint(12, 43.0, 45.5, 48.0),
        new CurvePoint(18, 44.0, 46.5, 49.0), new CurvePoint(24, 45.0, 47.5, 50.0),
        new CurvePoint(30, 45.5, 48.0, 50.5), new CurvePoint(36, 46.0, 48.5, 51.0),
        new CurvePoint(48, 46.5, 49.0, 51.5), new CurvePoint(60, 46.5, 49.5, 52.0),
        new CurvePoint(72, 46.5, 49.5, 52.0)
    );

    @PostConstruct
    public void init() {
        try {
            InputStream is = new ClassPathResource("data/cn-2022-standards.json").getInputStream();
            Map<String, Object> raw = objectMapper.readValue(is, Map.class);
            cnData.clear();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                String indicator = entry.getKey();
                Map<String, Object> inner = (Map<String, Object>) entry.getValue();
                Map<String, List<LmsPoint>> genderMap = new HashMap<>();
                for (Map.Entry<String, Object> ie : inner.entrySet()) {
                    String key = ie.getKey();
                    if (key.equals("description")) continue;
                    List<Map<String, Object>> pts = (List<Map<String, Object>>) ie.getValue();
                    List<LmsPoint> list = new ArrayList<>();
                    for (Map<String, Object> pt : pts) {
                        LmsPoint lp = new LmsPoint();
                        lp.ageMonth = (Integer) pt.get("ageMonth");
                        lp.l = ((Number) pt.get("l")).doubleValue();
                        lp.m = ((Number) pt.get("m")).doubleValue();
                        lp.s = ((Number) pt.get("s")).doubleValue();
                        list.add(lp);
                    }
                    genderMap.put(key, list);
                }
                cnData.put(indicator, genderMap);
            }
            System.out.println("CN_2022 标准加载完成: " + cnData.size() + " 个指标");
        } catch (Exception e) {
            System.err.println("CN_2022 标准数据加载失败: " + e.getMessage());
        }
    }

    /** 获取可用标准来源列表 */
    public List<Map<String, String>> getAvailableStandards() {
        return List.of(
            Map.of("id", "WHO_2006", "label", "WHO 2006 (国际标准)"),
            Map.of("id", "CN_2022", "label", "卫健委 WS/T 423-2022")
        );
    }

    /** 获取标准曲线数据 */
    public Map<String, Object> getStandards(String standard, String gender) {
        StandardSource src = StandardSource.valueOf(standard);
        Gender g;
        try { g = Gender.valueOf(gender.toUpperCase()); } catch (Exception e) { g = Gender.MALE; }

        Map<String, Object> result = new LinkedHashMap<>();

        if (src == StandardSource.WHO_2006) {
            result.put("height", formatCurve(WHO_HEIGHT));
            result.put("weight", formatCurve(WHO_WEIGHT));
            result.put("headCircumference", formatCurve(WHO_HEAD));
            result.put("note", "WHO 2006 儿童生长标准（男孩P3/P50/P97）");
        } else {
            String key = g == Gender.MALE ? "MALE" : "FEMALE";
            result.put("height", computeFromLms(cnData.get("LFA"), key));
            result.put("weight", computeFromLms(cnData.get("WFA"), key));
            result.put("headCircumference", computeFromLms(cnData.get("HCA"), key));
            result.put("note", "卫健委 WS/T 423-2022（" + (g == Gender.MALE ? "男" : "女") + "童P3/P50/P97）");
        }
        return result;
    }

    /** 根据标准计算 Z-Score 和百分位 */
    public Map<String, Object> calculatePercentile(String standard, String gender,
                                                     String indicator, double value, int ageMonths) {
        StandardSource src = StandardSource.valueOf(standard);
        Gender g;
        try { g = Gender.valueOf(gender.toUpperCase()); } catch (Exception e) { g = Gender.MALE; }

        if (src == StandardSource.WHO_2006) {
            return calculateWhoPercentile(gender, indicator, value, ageMonths);
        }

        // CN_2022 - 使用 LMS 方法
        String key = g == Gender.MALE ? "MALE" : "FEMALE";
        Map<String, List<LmsPoint>> indicatorData = cnData.get(indicator);
        if (indicatorData == null) return Map.of("error", "指标不存在");

        List<LmsPoint> points = indicatorData.get(key);
        if (points == null || points.isEmpty()) return Map.of("error", "无数据");

        // 查找对应月龄
        LmsPoint best = null;
        for (LmsPoint p : points) {
            if (p.ageMonth == ageMonths) { best = p; break; }
        }
        if (best == null) {
            // 线性插值
            best = interpolateLms(points, ageMonths);
        }
        if (best == null) return Map.of("error", "月龄超出范围");

        // LMS Z-Score 计算
        double l = best.l, m = best.m, s = best.s;
        double zScore;
        if (Math.abs(l) < 0.001) {
            zScore = Math.log(value / m) / s;
        } else {
            zScore = (Math.pow(value / m, l) - 1) / (l * s);
        }

        double percentile = normalCdf(zScore) * 100;

        String interpretation;
        if (percentile < 3) interpretation = "偏低（<P3）";
        else if (percentile < 10) interpretation = "中下（P3-P10）";
        else if (percentile < 25) interpretation = "中等偏下（P10-P25）";
        else if (percentile < 50) interpretation = "中等（P25-P50）";
        else if (percentile < 75) interpretation = "中等偏上（P50-P75）";
        else if (percentile < 90) interpretation = "中上（P75-P90）";
        else if (percentile < 97) interpretation = "偏高（P90-P97）";
        else interpretation = "偏高（>P97）";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("indicator", indicator);
        result.put("value", value);
        result.put("ageMonths", ageMonths);
        result.put("zScore", Math.round(zScore * 100.0) / 100.0);
        result.put("percentile", Math.round(percentile * 10.0) / 10.0);
        result.put("interpretation", interpretation);
        result.put("standard", standard);
        result.put("standardLabel", src.label);
        return result;
    }

    /** 预测生长趋势 */
    public Map<String, Object> predict(List<Map<String, Object>> records, String dateField, String valueField, int monthsAhead) {
        List<double[]> pts = new ArrayList<>();
        for (Map<String, Object> r : records) {
            Object v = r.get(valueField);
            if (!(v instanceof Number)) continue;
            double val = ((Number) v).doubleValue();
            String ds = (String) r.get(dateField);
            int ma = 0;
            try {
                String[] p = ds.split("-");
                ma = (Integer.parseInt(p[0]) - 2023) * 12 + (Integer.parseInt(p[1]) - 12);
            } catch (Exception e) { continue; }
            pts.add(new double[]{ma, val});
        }
        if (pts.size() < 2) return Map.of("predicted", List.of());
        pts.sort(Comparator.comparingDouble(a -> a[0]));

        int start = Math.max(0, pts.size() - 3);
        List<double[]> recent = pts.subList(start, pts.size());

        double sx = 0, sy = 0, sxy = 0, sx2 = 0;
        for (double[] p : recent) { sx += p[0]; sy += p[1]; sxy += p[0] * p[1]; sx2 += p[0] * p[0]; }
        int n = recent.size();
        double slope, inter;
        if (n == 2) {
            slope = (recent.get(1)[1] - recent.get(0)[1]) / (recent.get(1)[0] - recent.get(0)[0]);
            inter = recent.get(0)[1] - slope * recent.get(0)[0];
        } else {
            slope = (n * sxy - sx * sy) / (n * sx2 - sx * sx);
            inter = (sy - slope * sx) / n;
        }

        double maxSlope = valueField.contains("weight") ? 0.3 : 1.2;
        double minSlope = valueField.contains("weight") ? 0.0 : 0.2;
        slope = Math.max(minSlope, Math.min(maxSlope, slope));

        int lastAge = (int) Math.round(pts.get(pts.size() - 1)[0]);
        List<Map<String, Object>> pred = new ArrayList<>();
        for (int m = 1; m <= monthsAhead; m++) {
            int a = lastAge + m;
            double val = slope * a + inter;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("monthAge", a); p.put("value", Math.round(val * 10.0) / 10.0);
            pred.add(p);
        }
        return Map.of("predicted", pred, "slope", Math.round(slope * 100.0) / 100.0);
    }

    // ========== 内部方法 ==========

    private Map<String, Object> calculateWhoPercentile(String gender, String indicator, double value, int ageMonths) {
        List<CurvePoint> data;
        String fieldKey = switch (indicator) {
            case "height" -> "height"; case "weight" -> "weight"; default -> "headCircumference";
        };
        data = fieldKey.equals("height") ? WHO_HEIGHT : fieldKey.equals("weight") ? WHO_WEIGHT : WHO_HEAD;

        CurvePoint bp = binarySearchCurve(data, ageMonths);
        if (bp == null) return Map.of("error", "月龄超出范围");

        double p50 = bp.p50, p3 = bp.p3, p97 = bp.p97;
        double approxPct;
        if (value >= p97) approxPct = 97.5;
        else if (value <= p3) approxPct = 1.5;
        else approxPct = (value - p3) / (p97 - p3) * 94 + 3;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("indicator", indicator);
        result.put("value", value);
        result.put("ageMonths", ageMonths);
        result.put("p3", p3); result.put("p50", p50); result.put("p97", p97);
        result.put("approxPercentile", Math.round(approxPct * 10.0) / 10.0);
        result.put("standard", "WHO_2006");
        result.put("standardLabel", "WHO 2006 (国际标准)");
        return result;
    }

    private List<Map<String, Object>> computeFromLms(Map<String, List<LmsPoint>> data, String genderKey) {
        if (data == null) return List.of();
        List<LmsPoint> points = data.get(genderKey);
        if (points == null) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        // Z-Scores for P3, P50, P97
        double z3 = -1.881, z50 = 0, z97 = 1.881;
        for (LmsPoint p : points) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("monthAge", p.ageMonth);
            double l = p.l, mVal = p.m, s = p.s;
            m.put("p3", lmsToValue(l, mVal, s, z3));
            m.put("p50", lmsToValue(l, mVal, s, z50));
            m.put("p97", lmsToValue(l, mVal, s, z97));
            result.add(m);
        }
        return result;
    }

    private double lmsToValue(double l, double m, double s, double z) {
        if (Math.abs(l) < 0.001) return m * Math.exp(s * z);
        return m * Math.pow(1 + l * s * z, 1 / l);
    }

    private LmsPoint interpolateLms(List<LmsPoint> points, int targetAge) {
        if (points.isEmpty()) return null;
        if (targetAge <= points.get(0).ageMonth) return points.get(0);
        if (targetAge >= points.get(points.size() - 1).ageMonth) return points.get(points.size() - 1);

        for (int i = 0; i < points.size() - 1; i++) {
            LmsPoint low = points.get(i), high = points.get(i + 1);
            if (targetAge >= low.ageMonth && targetAge <= high.ageMonth) {
                if (targetAge == low.ageMonth) return low;
                if (targetAge == high.ageMonth) return high;
                double ratio = (double) (targetAge - low.ageMonth) / (high.ageMonth - low.ageMonth);
                return new LmsPoint(targetAge,
                    low.l + ratio * (high.l - low.l),
                    low.m + ratio * (high.m - low.m),
                    low.s + ratio * (high.s - low.s));
            }
        }
        return points.get(points.size() - 1);
    }

    private CurvePoint binarySearchCurve(List<CurvePoint> data, int ageMonths) {
        CurvePoint best = null;
        for (CurvePoint p : data) {
            if (p.monthAge <= ageMonths) best = p;
            else break;
        }
        return best;
    }

    private double normalCdf(double x) {
        // 近似计算标准正态分布CDF
        final double a1 = 0.254829592, a2 = -0.284496736, a3 = 1.421413741;
        final double a4 = -1.453152027, a5 = 1.061405429, p = 0.3275911;
        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x / 2);
        return 0.5 * (1.0 + sign * (y - 0.5) * 2);
    }

    private List<Map<String, Object>> formatCurve(List<CurvePoint> points) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CurvePoint p : points) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("monthAge", p.monthAge); m.put("p3", p.p3); m.put("p50", p.p50); m.put("p97", p.p97);
            result.add(m);
        }
        return result;
    }

    // 数据结构
    public static class CurvePoint {
        public final int monthAge; public final double p3, p50, p97;
        public CurvePoint(int m, double a, double b, double c) { monthAge = m; p3 = a; p50 = b; p97 = c; }
    }

    private static class LmsPoint {
        public int ageMonth; public double l, m, s;
        public LmsPoint() {}
        public LmsPoint(int a, double l, double m, double s) { this.ageMonth = a; this.l = l; this.m = m; this.s = s; }
    }
}
