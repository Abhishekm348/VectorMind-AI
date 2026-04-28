package com.vectordb.service;

import com.vectordb.algorithm.BruteForce;
import com.vectordb.algorithm.HNSW;
import com.vectordb.algorithm.DistanceMetrics;
import com.vectordb.model.DocItem;
import com.vectordb.model.VectorItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DocumentDbService {

    private final Map<Integer, DocItem> store = Collections.synchronizedMap(new LinkedHashMap<>());
    private final HNSW       hnsw       = new HNSW(16, 200);
    private final BruteForce bruteForce = new BruteForce(); // fallback for tiny sets
    private final AtomicInteger nextId  = new AtomicInteger(1);
    private volatile int dims = 0;

    public int insert(String title, String text, List<Float> embedding) {
        if (dims == 0) dims = embedding.size();
        int id = nextId.getAndIncrement();
        DocItem item = new DocItem(id, title, text, embedding);
        store.put(id, item);

        VectorItem vi = new VectorItem(id, title, "doc", embedding);
        hnsw.insert(vi, DistanceMetrics::cosine);
        bruteForce.insert(vi);
        return id;
    }

    /**
     * Semantic search — returns top-k chunks with distance ≤ max_dist.
     */
    public List<DocHit> search(List<Float> query, int k, float maxDist) {
        if (store.isEmpty()) return List.of();

        List<float[]> raw = (store.size() < 10)
                ? bruteForce.knn(query, k, DistanceMetrics::cosine)
                : hnsw.knn(query, k, 50, DistanceMetrics::cosine);

        List<DocHit> results = new ArrayList<>();
        for (float[] r : raw) {
            int id = (int) r[1];
            DocItem doc = store.get(id);
            if (doc != null && r[0] <= maxDist) results.add(new DocHit(r[0], doc));
        }
        return results;
    }

    public List<DocHit> search(List<Float> query, int k) {
        return search(query, k, 0.9f);
    }

    public boolean remove(int id) {
        if (!store.containsKey(id)) return false;
        store.remove(id);
        hnsw.remove(id);
        bruteForce.remove(id);
        return true;
    }

    public List<DocItem> getAll() {
        return new ArrayList<>(store.values());
    }

    public int size()  { return store.size(); }
    public int getDims() { return dims; }

    public record DocHit(float distance, DocItem doc) {}
}
