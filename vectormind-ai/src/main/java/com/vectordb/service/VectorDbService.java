package com.vectordb.service;

import com.vectordb.algorithm.BruteForce;
import com.vectordb.algorithm.DistanceMetrics;
import com.vectordb.algorithm.HNSW;
import com.vectordb.algorithm.KDTree;
import com.vectordb.model.SearchHit;
import com.vectordb.model.VectorItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

@Service
public class VectorDbService {

    private final Map<Integer, VectorItem> store = Collections.synchronizedMap(new LinkedHashMap<>());
    private final BruteForce bruteForce = new BruteForce();
    private final KDTree     kdTree;
    private final HNSW       hnsw      = new HNSW(16, 200);
    private final AtomicInteger nextId  = new AtomicInteger(1);

    @Value("${vectordb.demo-dims:16}")
    public int dims;

    public VectorDbService(@Value("${vectordb.demo-dims:16}") int dims) {
        this.dims   = dims;
        this.kdTree = new KDTree(dims);
    }
 
    public int insert(String metadata, String category, List<Float> embedding) {
        var distFn = DistanceMetrics.getMetric("cosine");
        int id = nextId.getAndIncrement();
        VectorItem item = new VectorItem(id, metadata, category, embedding);
        store.put(id, item);
        bruteForce.insert(item);
        kdTree.insert(item);
        hnsw.insert(item, distFn);
        return id;
    }

    public boolean remove(int id) {
        if (!store.containsKey(id)) return false;
        store.remove(id);
        bruteForce.remove(id);
        hnsw.remove(id);
        kdTree.rebuild(new ArrayList<>(store.values()));
        return true;
    }

    public List<VectorItem> getAll() {
        return new ArrayList<>(store.values());
    }

 
    public SearchResult search(List<Float> query, int k, String metric, String algo) {
        var distFn = DistanceMetrics.getMetric(metric);
        long start = System.nanoTime();

        List<float[]> raw = switch (algo.toLowerCase()) {
            case "bruteforce" -> bruteForce.knn(query, k, distFn);
            case "kdtree"     -> kdTree.knn(query, k, distFn);
            default           -> hnsw.knn(query, k, 50, distFn);
        };

        long latencyUs = (System.nanoTime() - start) / 1000;

        List<SearchHit> hits = new ArrayList<>();
        for (float[] r : raw) {
            int id = (int) r[1];
            VectorItem v = store.get(id);
            if (v != null) hits.add(new SearchHit(id, v.getMetadata(), v.getCategory(), v.getEmbedding(), r[0]));
        }
        return new SearchResult(hits, latencyUs, algo, metric);
    }

    public BenchmarkResult benchmark(List<Float> query, int k, String metric) {
        var distFn = DistanceMetrics.getMetric(metric);

        long bfUs   = time(() -> bruteForce.knn(query, k, distFn));
        long kdUs   = time(() -> kdTree.knn(query, k, distFn));
        long hnswUs = time(() -> hnsw.knn(query, k, 50, distFn));

        return new BenchmarkResult(bfUs, kdUs, hnswUs, store.size());
    }

    public HNSW.GraphInfo hnswInfo() {
        return hnsw.getInfo();
    }


    private long time(Runnable r) {
        long t = System.nanoTime();
        r.run();
        return (System.nanoTime() - t) / 1000;
    }

 
    public record SearchResult(List<SearchHit> results, long latencyUs, String algo, String metric) {}
    public record BenchmarkResult(long bruteforceUs, long kdtreeUs, long hnswUs, int itemCount) {}
}
