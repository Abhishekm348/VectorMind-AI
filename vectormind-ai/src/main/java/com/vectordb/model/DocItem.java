package com.vectordb.model;

import java.util.List;

public class DocItem {
    private int id;
    private String title;
    private String text;
    private List<Float> embedding;

    public DocItem() {}

    public DocItem(int id, String title, String text, List<Float> embedding) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.embedding = embedding;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
}
