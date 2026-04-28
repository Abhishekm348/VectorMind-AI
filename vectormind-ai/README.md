# VectorDB — Spring Boot Edition

Same project as [perryvegehan/Your-OWN-AI](https://github.com/perryvegehan/Your-OWN-AI), converted from C++ to **Spring Boot (Java 17)**.

---

## What This Does

| Feature | Description |
|---|---|
| **3 Search Algorithms** | HNSW, KD-Tree, Brute Force — same logic as C++ version |
| **3 Distance Metrics** | Cosine, Euclidean, Manhattan |
| **16D Demo Vectors** | 20 pre-loaded vectors across 4 categories (CS, Math, Food, Sports) |
| **PCA Scatter Plot** | Live 2D visualization in the browser |
| **Real Embeddings** | Paste text → Ollama embeds it with `nomic-embed-text` (768D) |
| **RAG Pipeline** | Ask questions → HNSW retrieves chunks → llama3.2 answers |
| **REST API** | Same endpoints as the C++ version |

---

## Prerequisites

1. **Java 17+** — `java -version`
2. **Maven 3.6+** — `mvn -version`
3. **Ollama** — [Download from ollama.com](https://ollama.com)

After installing Ollama, pull the required models:
```bash
ollama pull nomic-embed-text   # ~274MB — embedding model
ollama pull llama3.2           # ~2GB   — language model
```

---

## Run

```bash
# Clone / unzip the project
cd vectordb-spring

# Run with Maven (downloads dependencies, compiles, starts server)
mvn spring-boot:run

# OR build a JAR and run
mvn clean package
java -jar target/vectordb-spring-1.0.0.jar
```

Open your browser at: **http://localhost:8080**

---

## Project Structure

```
src/main/java/com/vectordb/
├── VectorDbApplication.java          ← Spring Boot entry point
├── algorithm/
│   ├── BruteForce.java               ← O(N·d) exact search
│   ├── KDTree.java                   ← O(log N) axis-partitioned search
│   ├── HNSW.java                     ← O(log N) approximate graph search
│   └── DistanceMetrics.java          ← cosine / euclidean / manhattan
├── model/
│   ├── VectorItem.java
│   ├── DocItem.java
│   └── SearchHit.java
├── service/
│   ├── VectorDbService.java          ← unified 16D demo index
│   ├── DocumentDbService.java        ← HNSW over Ollama embeddings
│   ├── OllamaService.java            ← HTTP client for Ollama
│   └── TextChunker.java              ← 250-word overlapping chunks
├── controller/
│   ├── VectorController.java         ← /search /insert /delete /items /benchmark /hnsw-info
│   └── DocumentController.java       ← /doc/insert /doc/list /doc/ask /status
└── config/
    └── DemoDataLoader.java           ← loads 20 demo vectors on startup

src/main/resources/
├── application.properties
└── static/index.html                 ← same frontend UI
```

---

## REST API (identical to C++ version)

### Demo Vectors
| Method | Endpoint | Description |
|---|---|---|
| GET | `/search?v=f1,f2,...&k=5&metric=cosine&algo=hnsw` | KNN search |
| POST | `/insert` | Insert vector `{"metadata":"...","category":"...","embedding":[...]}` |
| DELETE | `/delete/{id}` | Delete by ID |
| GET | `/items` | List all demo vectors |
| GET | `/benchmark?v=...&k=5&metric=cosine` | Compare all 3 algorithms |
| GET | `/hnsw-info` | HNSW graph stats |
| GET | `/stats` | DB statistics |

### Documents & RAG
| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/doc/insert` | `{"title":"...","text":"..."}` | Embed & store |
| GET | `/doc/list` | — | List stored chunks |
| DELETE | `/doc/delete/{id}` | — | Delete chunk |
| POST | `/doc/ask` | `{"question":"...","k":3}` | RAG: retrieve + generate |
| GET | `/status` | — | Ollama health check |

---

## Configuration (`application.properties`)

```properties
server.port=8080
ollama.host=http://localhost:11434
ollama.embed-model=nomic-embed-text
ollama.gen-model=llama3.2           # change to llama3.2:1b for faster (but weaker) answers
vectordb.demo-dims=16
vectordb.chunk-words=250
vectordb.chunk-overlap=30
```

---

## C++ vs Spring Boot Mapping

| C++ | Spring Boot |
|---|---|
| `httplib.h` HTTP server | Spring MVC (`@RestController`) |
| `HNSW` struct + graph | `HNSW.java` class with `HashMap<Integer, Node>` |
| `KDTree` recursive | `KDTree.java` with same recursive insert/search |
| `OllamaClient` | `OllamaService.java` using `java.net.http.HttpClient` |
| `VectorDB` unified index | `VectorDbService.java` Spring `@Service` |
| `DocumentDB` | `DocumentDbService.java` Spring `@Service` |
| `chunkText()` | `TextChunker.java` Spring `@Component` |
| Demo data in `main()` | `DemoDataLoader.java` implements `CommandLineRunner` |
| `index.html` as embedded string | `src/main/resources/static/index.html` |
