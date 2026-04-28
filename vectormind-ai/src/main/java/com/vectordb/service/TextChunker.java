package com.vectordb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TextChunker {

    @Value("${vectordb.chunk-words:250}")
    private int chunkWords;

    @Value("${vectordb.chunk-overlap:30}")
    private int overlapWords;

    public List<String> chunk(String text) {
        String[] words = text.trim().split("\\s+");
        if (words.length == 0) return List.of();
        if (words.length <= chunkWords) return List.of(text);

        List<String> chunks = new ArrayList<>();
        int step = chunkWords - overlapWords;

        for (int i = 0; i < words.length; i += step) {
            int end = Math.min(i + chunkWords, words.length);
            chunks.add(String.join(" ", Arrays.copyOfRange(words, i, end)));
            if (end == words.length) break;
        }
        return chunks;
    }
}
