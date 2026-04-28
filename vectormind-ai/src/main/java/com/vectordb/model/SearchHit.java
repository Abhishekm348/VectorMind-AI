package com.vectordb.model;

import java.util.List;

public class SearchHit {
    private int id;
    private String metadata;
    private String category;
    private List<Float> embedding;
    private float distance;

    public SearchHit(int id, String metadata, String category, List<Float> embedding, float distance) {
        this.id = id;
        this.metadata = metadata;
        this.category = category;
        this.embedding = embedding;
        this.distance = distance;
    }

    public int getId() { return id; }
    public String getMetadata() { return metadata; }
    public String getCategory() { return category; }
    public List<Float> getEmbedding() { return embedding; }
    public float getDistance() { return distance; }
}
