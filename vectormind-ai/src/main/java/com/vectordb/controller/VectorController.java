package com.vectordb.controller;

import com.vectordb.algorithm.HNSW;
import com.vectordb.model.VectorItem;
import com.vectordb.service.VectorDbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class VectorController {

    private final VectorDbService db;

    public VectorController(VectorDbService db) {
        this.db = db;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String v,
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "cosine") String metric,
            @RequestParam(defaultValue = "hnsw") String algo) {

        List<Float> query = parseVec(v);
        if (query.size() != db.dims) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "need " + db.dims + "D vector"));
        }

        VectorDbService.SearchResult result = db.search(query, k, metric, algo);
        return ResponseEntity.ok(Map.of(
                "results",   result.results(),
                "latencyUs", result.latencyUs(),
                "algo",      result.algo(),
                "metric",    result.metric()
        ));
    }

  
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody InsertRequest req) {
        if (req.metadata() == null || req.embedding() == null
                || req.embedding().size() != db.dims) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid body"));
        }
        int id = db.insert(req.metadata(),
                req.category() != null ? req.category() : "custom",
                req.embedding());
        return ResponseEntity.ok(Map.of("id", id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        return ResponseEntity.ok(Map.of("ok", db.remove(id)));
    }

    @GetMapping("/items")
    public List<VectorItem> items() {
        return db.getAll();
    }

    @GetMapping("/benchmark")
    public ResponseEntity<?> benchmark(
            @RequestParam String v,
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "cosine") String metric) {

        List<Float> query = parseVec(v);
        if (query.size() != db.dims) {
            return ResponseEntity.badRequest().body(Map.of("error", "need " + db.dims + "D vector"));
        }
        return ResponseEntity.ok(db.benchmark(query, k, metric));
    }

    @GetMapping("/hnsw-info")
    public HNSW.GraphInfo hnswInfo() {
        return db.hnswInfo();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of("itemCount", db.getAll().size(), "dims", db.dims);
    }

    private List<Float> parseVec(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Float::parseFloat)
                .collect(Collectors.toList());
    }

    record InsertRequest(String metadata, String category, List<Float> embedding) {}
}
