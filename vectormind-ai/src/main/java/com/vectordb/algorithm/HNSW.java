package com.vectordb.algorithm;

import com.vectordb.model.VectorItem;

import java.util.*;
import java.util.function.BiFunction;

/**
 * HNSW — Hierarchical Navigable Small World Graph
 * Same algorithm used by Pinecone, Weaviate, Chroma, Milvus.
 * O(log N) approximate nearest-neighbor search.
 */
public class HNSW {

    private static class Node {
        VectorItem item;
        int maxLayer;
        List<List<Integer>> neighbors; // neighbors[layer] = list of node IDs

        Node(VectorItem item, int maxLayer) {
            this.item = item;
            this.maxLayer = maxLayer;
            this.neighbors = new ArrayList<>();
            for (int i = 0; i <= maxLayer; i++) neighbors.add(new ArrayList<>());
        }
    }

    private final Map<Integer, Node> graph = new HashMap<>();
    private final int M;          // max connections per layer (except layer 0)
    private final int M0;         // max connections at layer 0
    private final int efBuild;    // ef_construction
    private final double mL;      // level multiplier
    private final Random rng = new Random(42);

    private int entryPoint = -1;
    private int topLayer   = -1;

    public HNSW() {
        this(16, 200);
    }

    public HNSW(int m, int efBuild) {
        this.M       = m;
        this.M0      = 2 * m;
        this.efBuild = efBuild;
        this.mL      = 1.0 / Math.log(m);
    }

    private int randomLevel() {
        return (int) Math.floor(-Math.log(rng.nextDouble()) * mL);
    }

    public synchronized void insert(VectorItem item,
                                    BiFunction<List<Float>, List<Float>, Float> dist) {
        int id    = item.getId();
        int level = randomLevel();
        Node node = new Node(item, level);
        graph.put(id, node);

        if (entryPoint == -1) {
            entryPoint = id;
            topLayer   = level;
            return;
        }

        int ep = entryPoint;

        // Greedy descent through layers above our new node's level
        for (int lc = topLayer; lc > level; lc--) {
            List<float[]> W = searchLayer(item.getEmbedding(), ep, 1, lc, dist);
            if (!W.isEmpty()) ep = (int) W.get(0)[1];
        }

        // Insert at each layer from min(topLayer,level) down to 0
        for (int lc = Math.min(topLayer, level); lc >= 0; lc--) {
            List<float[]> W = searchLayer(item.getEmbedding(), ep, efBuild, lc, dist);
            int maxM = (lc == 0) ? M0 : M;
            List<Integer> selected = selectNeighbors(W, maxM);

            // Ensure we have a neighbors list for this layer
            while (node.neighbors.size() <= lc) node.neighbors.add(new ArrayList<>());
            node.neighbors.set(lc, selected);

            // Bidirectional connections
            for (int nid : selected) {
                Node neighbor = graph.get(nid);
                if (neighbor == null) continue;
                while (neighbor.neighbors.size() <= lc) neighbor.neighbors.add(new ArrayList<>());
                neighbor.neighbors.get(lc).add(id);

                // Prune if over-connected
                if (neighbor.neighbors.get(lc).size() > maxM) {
                    List<float[]> ds = new ArrayList<>();
                    for (int cid : neighbor.neighbors.get(lc)) {
                        Node cn = graph.get(cid);
                        if (cn != null)
                            ds.add(new float[]{dist.apply(neighbor.item.getEmbedding(), cn.item.getEmbedding()), cid});
                    }
                    ds.sort(Comparator.comparingDouble(r -> r[0]));
                    List<Integer> pruned = new ArrayList<>();
                    for (int i = 0; i < maxM && i < ds.size(); i++) pruned.add((int) ds.get(i)[1]);
                    neighbor.neighbors.set(lc, pruned);
                }
            }

            if (!W.isEmpty()) ep = (int) W.get(0)[1];
        }

        if (level > topLayer) {
            topLayer   = level;
            entryPoint = id;
        }
    }

    /**
     * Search a single layer — returns sorted list of {distance, id} pairs.
     */
    private List<float[]> searchLayer(List<Float> query, int ep, int ef, int layer,
                                      BiFunction<List<Float>, List<Float>, Float> dist) {
        Set<Integer> visited = new HashSet<>();
        // Min-heap: {dist, id}
        PriorityQueue<float[]> candidates = new PriorityQueue<>(Comparator.comparingDouble(r -> r[0]));
        // Max-heap: {dist, id}  — keep top ef results
        PriorityQueue<float[]> found      = new PriorityQueue<>((a, b) -> Float.compare(b[0], a[0]));

        Node epNode = graph.get(ep);
        if (epNode == null) return Collections.emptyList();

        float d0 = dist.apply(query, epNode.item.getEmbedding());
        visited.add(ep);
        candidates.offer(new float[]{d0, ep});
        found.offer(new float[]{d0, ep});

        while (!candidates.isEmpty()) {
            float[] current = candidates.poll();
            float   cd      = current[0];
            int     cid     = (int) current[1];

            if (!found.isEmpty() && found.size() >= ef && cd > found.peek()[0]) break;

            Node cNode = graph.get(cid);
            if (cNode == null || layer >= cNode.neighbors.size()) continue;

            for (int nid : cNode.neighbors.get(layer)) {
                if (visited.contains(nid)) continue;
                visited.add(nid);

                Node nNode = graph.get(nid);
                if (nNode == null) continue;

                float nd = dist.apply(query, nNode.item.getEmbedding());
                if (found.size() < ef || nd < found.peek()[0]) {
                    candidates.offer(new float[]{nd, nid});
                    found.offer(new float[]{nd, nid});
                    if (found.size() > ef) found.poll();
                }
            }
        }

        List<float[]> result = new ArrayList<>(found);
        result.sort(Comparator.comparingDouble(r -> r[0]));
        return result;
    }

    private List<Integer> selectNeighbors(List<float[]> candidates, int maxM) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < Math.min(candidates.size(), maxM); i++) {
            result.add((int) candidates.get(i)[1]);
        }
        return result;
    }

    public synchronized List<float[]> knn(List<Float> query, int k, int ef,
                                           BiFunction<List<Float>, List<Float>, Float> dist) {
        if (entryPoint == -1) return Collections.emptyList();

        int ep = entryPoint;

        // Greedy descent to layer 1
        for (int lc = topLayer; lc > 0; lc--) {
            List<float[]> W = searchLayer(query, ep, 1, lc, dist);
            if (!W.isEmpty()) ep = (int) W.get(0)[1];
        }

        // Full search at layer 0
        List<float[]> W = searchLayer(query, ep, Math.max(ef, k), 0, dist);
        if (W.size() > k) W = W.subList(0, k);
        return W;
    }

    public synchronized void remove(int id) {
        if (!graph.containsKey(id)) return;

        // Remove all references to this node from neighbors
        for (Node node : graph.values()) {
            for (List<Integer> layer : node.neighbors) {
                layer.removeIf(nid -> nid == id);
            }
        }

        if (entryPoint == id) {
            entryPoint = graph.keySet().stream().filter(k -> k != id).findFirst().orElse(-1);
        }
        graph.remove(id);
    }

    public synchronized GraphInfo getInfo() {
        GraphInfo info = new GraphInfo();
        info.topLayer  = topLayer;
        info.nodeCount = graph.size();

        int maxL = Math.max(topLayer + 1, 1);
        info.nodesPerLayer = new int[maxL];
        info.edgesPerLayer = new int[maxL];
        info.nodes = new ArrayList<>();
        info.edges = new ArrayList<>();

        for (Map.Entry<Integer, Node> entry : graph.entrySet()) {
            int  id   = entry.getKey();
            Node node = entry.getValue();
            info.nodes.add(new NodeView(id, node.item.getMetadata(), node.item.getCategory(), node.maxLayer));

            for (int lc = 0; lc <= node.maxLayer && lc < maxL; lc++) {
                info.nodesPerLayer[lc]++;
                if (lc < node.neighbors.size()) {
                    for (int nid : node.neighbors.get(lc)) {
                        if (id < nid) {
                            info.edgesPerLayer[lc]++;
                            info.edges.add(new EdgeView(id, nid, lc));
                        }
                    }
                }
            }
        }
        return info;
    }

    public synchronized int size() { return graph.size(); }

    // ── Inner DTOs for graph info ──────────────────────────────────────

    public static class GraphInfo {
        public int topLayer, nodeCount;
        public int[] nodesPerLayer, edgesPerLayer;
        public List<NodeView> nodes;
        public List<EdgeView> edges;
    }

    public record NodeView(int id, String metadata, String category, int maxLyr) {}
    public record EdgeView(int src, int dst, int lyr) {}
}
