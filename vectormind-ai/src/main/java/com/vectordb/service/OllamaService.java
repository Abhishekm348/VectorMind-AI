package com.vectordb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    @Value("${ollama.host:http://localhost:11434}")
    private String ollamaHost;

    @Value("${ollama.embed-model:nomic-embed-text}")
    private String embedModel;

    @Value("${ollama.gen-model:llama3.2}")
    private String genModel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public boolean isAvailable() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaHost + "/api/tags"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Embed text using nomic-embed-text.
     * Returns empty list if Ollama is unavailable.
     */
    public List<Float> embed(String text) {
        try {
            String body = mapper.writeValueAsString(new EmbedRequest(embedModel, text));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaHost + "/api/embeddings"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                log.error("Ollama embed error: {}", res.body());
                return List.of();
            }

            JsonNode root = mapper.readTree(res.body());
            JsonNode embArr = root.get("embedding");
            if (embArr == null || !embArr.isArray()) return List.of();

            List<Float> result = new ArrayList<>();
            embArr.forEach(n -> result.add(n.floatValue()));
            return result;

        } catch (Exception e) {
            log.error("Ollama embed failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Generate a response using llama3.2.
     */
    public String generate(String prompt) {
        try {
            String body = mapper.writeValueAsString(new GenerateRequest(genModel, prompt, false));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaHost + "/api/generate"))
                    .timeout(Duration.ofMinutes(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return "ERROR: Ollama returned " + res.statusCode();

            JsonNode root = mapper.readTree(res.body());
            JsonNode resp = root.get("response");
            return resp != null ? resp.asText() : "ERROR: empty response";

        } catch (Exception e) {
            log.error("Ollama generate failed: {}", e.getMessage());
            return "ERROR: Ollama unavailable. Run: ollama serve";
        }
    }

    public String getEmbedModel() { return embedModel; }
    public String getGenModel()   { return genModel;   }

    // ── Jackson-friendly request records ──────────────────────────────

    record EmbedRequest(String model, String prompt) {}
    record GenerateRequest(String model, String prompt, boolean stream) {}
}
