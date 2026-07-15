package com.growthtracker.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GrowthStandardService {

    private static final List<StandardPoint> HEIGHT = List.of(
        new StandardPoint(0, 46.5, 49.9, 53.0), new StandardPoint(1, 50.0, 53.5, 57.0),
        new StandardPoint(2, 52.5, 56.0, 60.0), new StandardPoint(3, 56.0, 60.5, 64.5),
        new StandardPoint(4, 58.5, 63.0, 67.0), new StandardPoint(5, 60.5, 65.5, 69.5),
        new StandardPoint(6, 62.5, 67.5, 72.5), new StandardPoint(8, 65.5, 70.5, 75.5),
        new StandardPoint(10, 68.5, 73.5, 78.5), new StandardPoint(12, 70.0, 75.5, 81.0),
        new StandardPoint(15, 72.5, 78.5, 84.5), new StandardPoint(18, 75.0, 81.5, 88.0),
        new StandardPoint(21, 77.5, 84.0, 91.0), new StandardPoint(24, 80.0, 86.5, 93.5),
        new StandardPoint(27, 82.0, 88.5, 96.0), new StandardPoint(30, 84.0, 91.0, 98.5),
        new StandardPoint(33, 86.0, 93.0, 101.0), new StandardPoint(36, 87.5, 95.0, 103.0),
        new StandardPoint(42, 91.0, 98.5, 106.5), new StandardPoint(48, 94.0, 102.0, 110.5),
        new StandardPoint(54, 97.0, 105.5, 114.0), new StandardPoint(60, 99.5, 108.5, 118.0),
        new StandardPoint(66, 102.0, 111.5, 121.5), new StandardPoint(72, 104.5, 114.5, 125.0)
    );

    private static final List<StandardPoint> WEIGHT = List.of(
        new StandardPoint(0, 2.5, 3.3, 4.3), new StandardPoint(1, 3.4, 4.5, 5.8),
        new StandardPoint(2, 4.0, 5.3, 6.8), new StandardPoint(3, 4.7, 6.3, 7.9),
        new StandardPoint(4, 5.3, 7.0, 8.8), new StandardPoint(5, 5.8, 7.5, 9.4),
        new StandardPoint(6, 6.0, 7.9, 10.0), new StandardPoint(8, 6.7, 8.7, 11.0),
        new StandardPoint(10, 7.2, 9.3, 11.7), new StandardPoint(12, 7.7, 9.8, 12.3),
        new StandardPoint(15, 8.3, 10.5, 13.3), new StandardPoint(18, 9.0, 11.3, 14.2),
        new StandardPoint(21, 9.4, 11.8, 14.9), new StandardPoint(24, 9.8, 12.2, 15.5),
        new StandardPoint(27, 10.2, 12.6, 16.1), new StandardPoint(30, 10.5, 13.1, 16.7),
        new StandardPoint(33, 10.8, 13.5, 17.3), new StandardPoint(36, 11.2, 14.0, 18.1),
        new StandardPoint(42, 11.9, 14.9, 19.4), new StandardPoint(48, 12.5, 15.8, 20.7),
        new StandardPoint(54, 13.1, 16.7, 22.0), new StandardPoint(60, 13.8, 17.7, 23.4),
        new StandardPoint(66, 14.4, 18.5, 24.8), new StandardPoint(72, 15.0, 19.3, 26.0)
    );

    private static final List<StandardPoint> HEAD = List.of(
        new StandardPoint(0, 32.0, 34.5, 37.0), new StandardPoint(1, 34.5, 37.0, 39.5),
        new StandardPoint(3, 38.0, 40.5, 43.0), new StandardPoint(6, 40.5, 43.0, 45.5),
        new StandardPoint(8, 41.5, 44.0, 46.5), new StandardPoint(12, 43.0, 45.5, 48.0),
        new StandardPoint(18, 44.0, 46.5, 49.0), new StandardPoint(24, 45.0, 47.5, 50.0),
        new StandardPoint(30, 45.5, 48.0, 50.5), new StandardPoint(36, 46.0, 48.5, 51.0),
        new StandardPoint(48, 46.5, 49.0, 51.5), new StandardPoint(60, 46.5, 49.5, 52.0),
        new StandardPoint(72, 46.5, 49.5, 52.0)
    );

    public Map<String, Object> getStandards() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("height", format(HEIGHT));
        result.put("weight", format(WEIGHT));
        result.put("headCircumference", format(HEAD));
        result.put("note", "数据来源：WHO儿童生长标准（男孩P3/P50/P97百分位）");
        return result;
    }

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

        // 只取最近 3 个数据点做回归，反映的是当前的生长速度
        int start = Math.max(0, pts.size() - 3);
        List<double[]> recent = pts.subList(start, pts.size());

        double sx = 0, sy = 0, sxy = 0, sx2 = 0;
        for (double[] p : recent) {
            sx += p[0]; sy += p[1]; sxy += p[0] * p[1]; sx2 += p[0] * p[0];
        }
        int n = recent.size();
        double slope, inter;
        if (n == 2) {
            // 只有2个点：直接算
            slope = (recent.get(1)[1] - recent.get(0)[1]) / (recent.get(1)[0] - recent.get(0)[0]);
            inter = recent.get(0)[1] - slope * recent.get(0)[0];
        } else {
            slope = (n * sxy - sx * sy) / (n * sx2 - sx * sx);
            inter = (sy - slope * sx) / n;
        }

        // 限制斜率：身高不超过 1.2cm/月（正常范围 0.5-1.0），体重不超过 0.3kg/月
        double maxSlope = valueField.equals("weight") ? 0.3 : 1.2;
        double minSlope = valueField.equals("weight") ? 0.0 : 0.2;
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

    private List<Map<String, Object>> format(List<StandardPoint> s) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (StandardPoint p : s) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("monthAge", p.monthAge); m.put("p3", p.p3); m.put("p50", p.p50); m.put("p97", p.p97);
            r.add(m);
        }
        return r;
    }

    public static class StandardPoint {
        public final int monthAge; public final double p3, p50, p97;
        public StandardPoint(int m, double a, double b, double c) { monthAge = m; p3 = a; p50 = b; p97 = c; }
    }
}
