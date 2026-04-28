package com.vectordb.algorithm;

import com.vectordb.model.VectorItem;

import java.util.*;
import java.util.function.BiFunction;

public class BruteForce {

    private final List<VectorItem> items = new ArrayList<>();

    public synchronized void insert(VectorItem item) {
        items.add(item);
    }

    public synchronized void remove(int id) {
        items.removeIf(v -> v.getId() == id);
    }

    public synchronized List<float[]> knn(List<Float> query, int k,
                                          BiFunction<List<Float>, List<Float>, Float> dist) {
        // float[] = {distance, id}
        List<float[]> results = new ArrayList<>();
        for (VectorItem v : items) {
            results.add(new float[]{dist.apply(query, v.getEmbedding()), v.getId()});
        }
        results.sort(Comparator.comparingDouble(r -> r[0]));
        return results.subList(0, Math.min(k, results.size()));
    }

    public synchronized List<VectorItem> getAll() {
        return new ArrayList<>(items);
    }
}
