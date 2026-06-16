package com.example.demo.service;

import com.example.demo.model.KnowledgeDoc;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TfIdfRetriever {

    private List<KnowledgeDoc> corpus = new ArrayList<>();
    private List<Map<String, Double>> tfidfVectors = new ArrayList<>();

    public synchronized void buildIndex(List<KnowledgeDoc> docs) {
        this.corpus = new ArrayList<>(docs);
        this.tfidfVectors = new ArrayList<>();
        if (docs.isEmpty()) return;

        List<Map<String, Integer>> termFreqs = docs.stream()
                .map(d -> termFreq(d.getTitle() + " " + d.getContent()))
                .collect(Collectors.toList());

        // IDF: log(N / df)
        Map<String, Integer> docFreq = new HashMap<>();
        for (Map<String, Integer> tf : termFreqs) {
            tf.keySet().forEach(t -> docFreq.merge(t, 1, Integer::sum));
        }
        int N = docs.size();
        Map<String, Double> idf = new HashMap<>();
        docFreq.forEach((t, df) -> idf.put(t, Math.log((double) (N + 1) / (df + 1)) + 1));

        // TF-IDF vectors
        for (Map<String, Integer> tf : termFreqs) {
            int total = tf.values().stream().mapToInt(Integer::intValue).sum();
            Map<String, Double> vec = new HashMap<>();
            tf.forEach((t, cnt) -> vec.put(t, ((double) cnt / total) * idf.getOrDefault(t, 1.0)));
            tfidfVectors.add(vec);
        }
    }

    public List<KnowledgeDoc> retrieve(String query, int topK) {
        if (corpus.isEmpty()) return Collections.emptyList();

        Map<String, Double> queryVec = new HashMap<>();
        tokenize(query).forEach(t -> queryVec.merge(t, 1.0, Double::sum));

        List<double[]> scored = new ArrayList<>(); // [index, score]
        for (int i = 0; i < corpus.size(); i++) {
            scored.add(new double[]{i, cosineSimilarity(queryVec, tfidfVectors.get(i))});
        }
        return scored.stream()
                .filter(s -> s[1] > 0)
                .sorted((a, b) -> Double.compare(b[1], a[1]))
                .limit(topK)
                .map(s -> corpus.get((int) s[0]))
                .collect(Collectors.toList());
    }

    private Map<String, Integer> termFreq(String text) {
        Map<String, Integer> tf = new HashMap<>();
        tokenize(text).forEach(t -> tf.merge(t, 1, Integer::sum));
        return tf;
    }

    // 字符 bigram，对中文无需分词器
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        String cleaned = text.replaceAll("[\\s\\p{Punct}]+", "").toLowerCase();
        for (int i = 0; i < cleaned.length() - 1; i++) {
            tokens.add(cleaned.substring(i, i + 2));
        }
        return tokens;
    }

    private double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0, normA = 0, normB = 0;
        for (Map.Entry<String, Double> e : a.entrySet()) {
            dot += e.getValue() * b.getOrDefault(e.getKey(), 0.0);
            normA += e.getValue() * e.getValue();
        }
        for (double v : b.values()) normB += v * v;
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
