# VectorDB - High Performance Vector Search Engine (Spring Boot, Java)

A Spring Boot based vector database system that implements similarity search algorithms and integrates them with real embeddings and LLM-powered Retrieval-Augmented Generation (RAG) using Ollama.

--------------------------------------------------

## OVERVIEW

This project demonstrates how modern AI systems perform semantic search, vector indexing, and context-aware response generation.

Core Idea:
Convert data into vectors -> Store and index -> Retrieve similar results -> Generate intelligent responses

--------------------------------------------------

## FEATURES

Vector Search Algorithms:
- HNSW (approximate nearest neighbor search)
- KD-Tree (space partitioned search)
- Brute Force (exact search)

Distance Metrics:
- Cosine Similarity
- Euclidean Distance
- Manhattan Distance

Embeddings:
- Uses Ollama (nomic-embed-text, 768 dimensions)
- Enables semantic similarity search

RAG Pipeline:
- Retrieves top-K chunks using HNSW
- Generates answers using Llama3.2

REST API:
- Supports CRUD, search, and benchmarking

--------------------------------------------------

## TECH STACK

- Backend: Spring Boot (Java)
- Algorithms: HNSW, KD-Tree, Brute Force
- LLM and Embeddings: Ollama
- Build Tool: Maven

--------------------------------------------------

## HOW IT WORKS

1. Input text is converted into vector embeddings
2. Vectors are stored using indexing algorithms
3. Similarity search is performed using distance metrics
4. (Optional) RAG pipeline generates responses using LLM

--------------------------------------------------

## RUN

mvn spring-boot:run

--------------------------------------------------

## ACCESS APPLICATION

Open in browser:
http://localhost:8080

--------------------------------------------------

## REST API

Vector Operations:
- GET /search -> Perform KNN search
- POST /insert -> Insert vector
- DELETE /delete/{id} -> Delete vector
- GET /items -> List vectors
- GET /benchmark -> Compare algorithms
- GET /hnsw-info -> Graph stats

Document and RAG:
- POST /doc/insert -> Store document
- POST /doc/ask -> Ask question
- GET /doc/list -> List documents
- DELETE /doc/delete/{id} -> Delete document

--------------------------------------------------

## PERFORMANCE HIGHLIGHTS

- Fast approximate search using HNSW
- Efficient similarity comparison
- Real-time semantic search
- Built-in benchmarking

--------------------------------------------------

## PROJECT STRUCTURE

src/main/java/com/vectordb/
  algorithm/
  model/
  service/
  controller/
  config/

src/main/resources/
  static/

--------------------------------------------------

## PURPOSE

- Learn vector database internals
- Implement search algorithms
- Explore RAG systems
- Work with embeddings and LLMs

--------------------------------------------------

## LIMITATIONS

- In-memory storage
- Not production optimized
- Requires local Ollama

--------------------------------------------------

Feel free to explore and improve.
