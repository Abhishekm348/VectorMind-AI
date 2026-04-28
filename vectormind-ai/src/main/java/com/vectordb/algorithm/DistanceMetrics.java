package com.vectordb.algorithm;

import java.util.List;
import java.util.function.BiFunction;

public class DistanceMetrics {

    public static float euclidean(List<Float> a, List<Float> b) {
        float sum = 0;
        for (int i = 0; i < a.size(); i++) {
            float d = a.get(i) - b.get(i);
            sum += d * d;
        }
        return (float) Math.sqrt(sum);
    }

    public static float cosine(List<Float> a, List<Float> b) {
        float dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            na  += a.get(i) * a.get(i);
            nb  += b.get(i) * b.get(i);
        }
        if (na < 1e-9f || nb < 1e-9f) return 1.0f;
        return 1.0f - dot / ((float) Math.sqrt(na) * (float) Math.sqrt(nb));
    }

    public static float manhattan(List<Float> a, List<Float> b) {
        float sum = 0;
        for (int i = 0; i < a.size(); i++) {
            sum += Math.abs(a.get(i) - b.get(i));
        }
        return sum;
    }

    public static BiFunction<List<Float>, List<Float>, Float> getMetric(String name) {
        return switch (name.toLowerCase()) {
            case "cosine"    -> DistanceMetrics::cosine;
            case "manhattan" -> DistanceMetrics::manhattan;
            default          -> DistanceMetrics::euclidean;
        };
    }
}
