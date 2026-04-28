package com.vectordb.config;

import com.vectordb.service.VectorDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final VectorDbService db;

    public DemoDataLoader(VectorDbService db) {
        this.db = db;
    }

    @Override
    public void run(String... args) {
        log.info("Loading 20 demo vectors...");

        insert("Linked List: nodes connected by pointers", "cs",
                0.90f,0.85f,0.72f,0.68f, 0.12f,0.08f,0.15f,0.10f, 0.05f,0.08f,0.06f,0.09f, 0.07f,0.11f,0.08f,0.06f);
        insert("Binary Search Tree: O(log n) search and insert", "cs",
                0.88f,0.82f,0.78f,0.74f, 0.15f,0.10f,0.08f,0.12f, 0.06f,0.07f,0.08f,0.05f, 0.09f,0.06f,0.07f,0.10f);
        insert("Dynamic Programming: memoization overlapping subproblems", "cs",
                0.82f,0.76f,0.88f,0.80f, 0.20f,0.18f,0.12f,0.09f, 0.07f,0.06f,0.08f,0.07f, 0.08f,0.09f,0.06f,0.07f);
        insert("Graph BFS and DFS: breadth and depth first traversal", "cs",
                0.85f,0.80f,0.75f,0.82f, 0.18f,0.14f,0.10f,0.08f, 0.06f,0.09f,0.07f,0.06f, 0.10f,0.08f,0.09f,0.07f);
        insert("Hash Table: O(1) lookup with collision chaining", "cs",
                0.87f,0.78f,0.70f,0.76f, 0.13f,0.11f,0.09f,0.14f, 0.08f,0.07f,0.06f,0.08f, 0.07f,0.10f,0.08f,0.09f);

        insert("Calculus: derivatives integrals and limits", "math",
                0.12f,0.15f,0.18f,0.10f, 0.91f,0.86f,0.78f,0.72f, 0.08f,0.06f,0.07f,0.09f, 0.07f,0.08f,0.06f,0.10f);
        insert("Linear Algebra: matrices eigenvalues eigenvectors", "math",
                0.20f,0.18f,0.15f,0.12f, 0.88f,0.90f,0.82f,0.76f, 0.09f,0.07f,0.08f,0.06f, 0.10f,0.07f,0.08f,0.09f);
        insert("Probability: distributions random variables Bayes theorem", "math",
                0.15f,0.12f,0.20f,0.18f, 0.84f,0.80f,0.88f,0.82f, 0.07f,0.08f,0.06f,0.10f, 0.09f,0.06f,0.09f,0.08f);
        insert("Number Theory: primes modular arithmetic RSA cryptography", "math",
                0.22f,0.16f,0.14f,0.20f, 0.80f,0.85f,0.76f,0.90f, 0.08f,0.09f,0.07f,0.06f, 0.08f,0.10f,0.07f,0.06f);
        insert("Combinatorics: permutations combinations generating functions", "math",
                0.18f,0.20f,0.16f,0.14f, 0.86f,0.78f,0.84f,0.80f, 0.06f,0.07f,0.09f,0.08f, 0.06f,0.09f,0.10f,0.07f);

        insert("Neapolitan Pizza: wood-fired dough San Marzano tomatoes", "food",
                0.08f,0.06f,0.09f,0.07f, 0.07f,0.08f,0.06f,0.09f, 0.90f,0.86f,0.78f,0.72f, 0.08f,0.06f,0.09f,0.07f);
        insert("Sushi: vinegared rice raw fish and nori rolls", "food",
                0.06f,0.08f,0.07f,0.09f, 0.09f,0.06f,0.08f,0.07f, 0.86f,0.90f,0.82f,0.76f, 0.07f,0.09f,0.06f,0.08f);
        insert("Ramen: noodle soup with chashu pork and soft-boiled eggs", "food",
                0.09f,0.07f,0.06f,0.08f, 0.08f,0.09f,0.07f,0.06f, 0.82f,0.78f,0.90f,0.84f, 0.09f,0.07f,0.08f,0.06f);
        insert("Tacos: corn tortillas with carnitas salsa and cilantro", "food",
                0.07f,0.09f,0.08f,0.06f, 0.06f,0.07f,0.09f,0.08f, 0.78f,0.82f,0.86f,0.90f, 0.06f,0.08f,0.07f,0.09f);
        insert("Croissant: laminated pastry with buttery flaky layers", "food",
                0.06f,0.07f,0.10f,0.09f, 0.10f,0.06f,0.07f,0.10f, 0.85f,0.80f,0.76f,0.82f, 0.09f,0.07f,0.10f,0.06f);

        insert("Basketball: fast-paced shooting dribbling slam dunks", "sports",
                0.09f,0.07f,0.08f,0.10f, 0.08f,0.09f,0.07f,0.06f, 0.08f,0.07f,0.09f,0.06f, 0.91f,0.85f,0.78f,0.72f);
        insert("Football: tackles touchdowns field goals and strategy", "sports",
                0.07f,0.09f,0.06f,0.08f, 0.09f,0.07f,0.10f,0.08f, 0.07f,0.09f,0.08f,0.07f, 0.87f,0.89f,0.82f,0.76f);
        insert("Tennis: racket volleys groundstrokes and Wimbledon serves", "sports",
                0.08f,0.06f,0.09f,0.07f, 0.07f,0.08f,0.06f,0.09f, 0.09f,0.06f,0.07f,0.08f, 0.83f,0.80f,0.88f,0.82f);
        insert("Chess: openings endgames tactics strategic board game", "sports",
                0.25f,0.20f,0.22f,0.18f, 0.22f,0.18f,0.20f,0.15f, 0.06f,0.08f,0.07f,0.09f, 0.80f,0.84f,0.78f,0.90f);
        insert("Swimming: butterfly freestyle backstroke Olympic competition", "sports",
                0.06f,0.08f,0.07f,0.09f, 0.08f,0.06f,0.09f,0.07f, 0.10f,0.08f,0.06f,0.07f, 0.85f,0.82f,0.86f,0.80f);

        log.info("Demo data loaded: {} vectors", db.getAll().size());
    }

    private void insert(String metadata, String category, float... values) {
        List<Float> emb = new java.util.ArrayList<>(values.length);
        for (float v : values) emb.add(v);
        db.insert(metadata, category, emb);
    }
}
