package com.vectordb.model;

import java.util.List;

public class VectorItem {
    private int id;
    private String metadata;
    private String category;
    private List<Float> embedding;

    public VectorItem() {}

    public VectorItem(int id, String metadata, String category, List<Float> embedding) {
        this.id = id;
        this.metadata = metadata;
        this.category = category;
        this.embedding = embedding;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
}
