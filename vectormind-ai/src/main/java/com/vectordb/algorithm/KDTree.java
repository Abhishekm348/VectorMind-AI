package com.vectordb.algorithm;

import com.vectordb.model.VectorItem;

import java.util.*;
import java.util.function.BiFunction;

public class KDTree {

    private static class KDNode {
        VectorItem item;
        KDNode left, right;
        KDNode(VectorItem item) { this.item = item; }
    }

    private KDNode root;
    private final int dims;

    public KDTree(int dims) {
        this.dims = dims;
    }

    public synchronized void insert(VectorItem item) {
        root = insert(root, item, 0);
    }

    private KDNode insert(KDNode node, VectorItem item, int depth) {
        if (node == null) return new KDNode(item);
        int axis = depth % dims;
        if (item.getEmbedding().get(axis) < node.item.getEmbedding().get(axis)) {
            node.left = insert(node.left, item, depth + 1);
        } else {
            node.right = insert(node.right, item, depth + 1);
        }
        return node;
    }

    public synchronized List<float[]> knn(List<Float> query, int k,
                                           BiFunction<List<Float>, List<Float>, Float> dist) {
        // Max-heap: {distance, id}
        PriorityQueue<float[]> heap = new PriorityQueue<>((a, b) -> Float.compare(b[0], a[0]));
        knnSearch(root, query, k, 0, dist, heap);
        List<float[]> result = new ArrayList<>(heap);
        result.sort(Comparator.comparingDouble(r -> r[0]));
        return result;
    }

    private void knnSearch(KDNode node, List<Float> query, int k, int depth,
                           BiFunction<List<Float>, List<Float>, Float> dist,
                           PriorityQueue<float[]> heap) {
        if (node == null) return;

        float d = dist.apply(query, node.item.getEmbedding());
        if (heap.size() < k || d < heap.peek()[0]) {
            heap.offer(new float[]{d, node.item.getId()});
            if (heap.size() > k) heap.poll();
        }

        int axis = depth % dims;
        float diff = query.get(axis) - node.item.getEmbedding().get(axis);
        KDNode closer  = diff < 0 ? node.left : node.right;
        KDNode farther = diff < 0 ? node.right : node.left;

        knnSearch(closer, query, k, depth + 1, dist, heap);
        if (heap.size() < k || Math.abs(diff) < heap.peek()[0]) {
            knnSearch(farther, query, k, depth + 1, dist, heap);
        }
    }

    public synchronized void rebuild(List<VectorItem> items) {
        root = null;
        for (VectorItem v : items) insert(v);
    }
}
