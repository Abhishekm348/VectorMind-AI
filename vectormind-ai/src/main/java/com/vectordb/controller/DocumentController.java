package com.vectordb.controller;

import com.vectordb.model.DocItem;
import com.vectordb.service.DocumentDbService;
import com.vectordb.service.OllamaService;
import com.vectordb.service.TextChunker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class DocumentController {

    private final DocumentDbService docDb;
    private final OllamaService     ollama;
    private final TextChunker       chunker;

    public DocumentController(DocumentDbService docDb, OllamaService ollama, TextChunker chunker) {
        this.docDb  = docDb;
        this.ollama = ollama;
        this.chunker = chunker;
    }

    @PostMapping("/doc/insert")
    public ResponseEntity<?> insertDoc(@RequestBody DocInsertRequest req) {
        if (req.title() == null || req.text() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "need title and text"));
        }

        List<String> chunks = chunker.chunk(req.text());
        List<Integer> ids = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            List<Float> emb = ollama.embed(chunks.get(i));
            if (emb.isEmpty()) {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "Ollama unavailable. Install from https://ollama.com then run: " +
                                 "ollama pull nomic-embed-text && ollama pull llama3.2"));
            }
            String chunkTitle = chunks.size() > 1
                    ? req.title() + " [" + (i + 1) + "/" + chunks.size() + "]"
                    : req.title();
            ids.add(docDb.insert(chunkTitle, chunks.get(i), emb));
        }

        return ResponseEntity.ok(Map.of(
                "ids",    ids,
                "chunks", chunks.size(),
                "dims",   docDb.getDims()
        ));
    }

    @GetMapping("/doc/list")
    public List<Map<String, Object>> listDocs() {
        return docDb.getAll().stream().map(doc -> {
            String preview = doc.getText().length() > 120
                    ? doc.getText().substring(0, 120) + "…"
                    : doc.getText();
            int words = doc.getText().split("\\s+").length;
            return Map.<String, Object>of(
                    "id",      doc.getId(),
                    "title",   doc.getTitle(),
                    "preview", preview,
                    "words",   words
            );
        }).collect(Collectors.toList());
    }

    /** DELETE /doc/delete/{id} */
    @DeleteMapping("/doc/delete/{id}")
    public Map<String, Object> deleteDoc(@PathVariable int id) {
        return Map.of("ok", docDb.remove(id));
    }

    /**
     * POST /doc/search
     * Body: {"question":"...","k":3}
     * Embeds the question and retrieves nearest chunks (no LLM call).
     */
    @PostMapping("/doc/search")
    public ResponseEntity<?> docSearch(@RequestBody AskRequest req) {
        if (req.question() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "need question"));
        }

        List<Float> qEmb = ollama.embed(req.question());
        if (qEmb.isEmpty()) {
            return ResponseEntity.status(503).body(Map.of("error", "Ollama unavailable"));
        }

        int k = req.k() != null ? req.k() : 3;
        List<DocumentDbService.DocHit> hits = docDb.search(qEmb, k);

        List<Map<String, Object>> contexts = hits.stream().map(h -> Map.<String, Object>of(
                "id",       h.doc().getId(),
                "title",    h.doc().getTitle(),
                "distance", h.distance()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("contexts", contexts));
    }

    /**
     * POST /doc/ask
     * Body: {"question":"...","k":3}
     * Full RAG: embed → retrieve → build prompt → generate answer.
     */
    @PostMapping("/doc/ask")
    public ResponseEntity<?> ask(@RequestBody AskRequest req) {
        if (req.question() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "need question"));
        }
        if (docDb.size() == 0) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "No documents yet. Use /doc/insert to add documents first."));
        }

        List<Float> qEmb = ollama.embed(req.question());
        if (qEmb.isEmpty()) {
            return ResponseEntity.status(503).body(Map.of("error", "Ollama embed unavailable"));
        }

        int k = req.k() != null ? req.k() : 3;
        List<DocumentDbService.DocHit> hits = docDb.search(qEmb, k);

        if (hits.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "answer",   "No relevant documents found for your question.",
                    "contexts", List.of()
            ));
        }

        // Build RAG prompt
        StringBuilder context = new StringBuilder();
        List<Map<String, Object>> contextMeta = new ArrayList<>();
        for (DocumentDbService.DocHit hit : hits) {
            context.append("[").append(hit.doc().getTitle()).append("]\n");
            context.append(hit.doc().getText()).append("\n\n");
            contextMeta.add(Map.of(
                    "id",       hit.doc().getId(),
                    "title",    hit.doc().getTitle(),
                    "distance", hit.distance()
            ));
        }

        String prompt = """
                You are a helpful assistant. Answer the question below using ONLY the provided context.
                If the answer is not in the context, say "I don't have enough information to answer that."
                
                Context:
                %s
                Question: %s
                
                Answer:""".formatted(context, req.question());

        String answer = ollama.generate(prompt);

        return ResponseEntity.ok(Map.of(
                "answer",   answer,
                "contexts", contextMeta
        ));
    }

    /** GET /status — Ollama health check */
    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean up = ollama.isAvailable();
        return Map.of(
                "ollama",     up ? "ONLINE" : "OFFLINE",
                "embedModel", ollama.getEmbedModel(),
                "genModel",   ollama.getGenModel(),
                "docCount",   docDb.size()
        );
    }

    // ── Request records ───────────────────────────────────────────────
    record DocInsertRequest(String title, String text) {}
    record AskRequest(String question, Integer k) {}
}
