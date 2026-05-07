package com.shootersplatform.backend.training.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record HitScore(ScoringType type, Map<String, Integer> hits) {

    private static final List<String> IDPA_KEYS = List.of("alpha", "charlie", "delta", "miss");
    private static final List<String> TARGET_KEYS = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

    public static HitScore empty(ScoringType type) {
        return new HitScore(type, Map.of());
    }

    public HitScore {
        hits = normalize(type, hits);
    }

    public HitScore withHit(String key, int count) {
        Map<String, Integer> updated = new LinkedHashMap<>(hits);
        if (!updated.containsKey(key)) {
            throw new TrainingValidationException("Unsupported hit score key: " + key);
        }
        if (count < 0) {
            throw new TrainingValidationException("Hit counters cannot be negative");
        }
        updated.put(key, count);
        return new HitScore(type, updated);
    }

    public int count(String key) {
        Integer count = hits.get(key);
        if (count == null) {
            throw new TrainingValidationException("Unsupported hit score key: " + key);
        }
        return count;
    }

    public boolean hasAnyHit() {
        return hits.values().stream().anyMatch(count -> count > 0);
    }

    public static List<String> keysFor(ScoringType type) {
        return switch (type) {
            case IDPA -> IDPA_KEYS;
            case TARGET -> TARGET_KEYS;
        };
    }

    private static Map<String, Integer> normalize(ScoringType type, Map<String, Integer> source) {
        List<String> allowedKeys = keysFor(type);
        LinkedHashMap<String, Integer> normalized = new LinkedHashMap<>();
        allowedKeys.forEach(key -> normalized.put(key, 0));

        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            if (!normalized.containsKey(entry.getKey())) {
                throw new TrainingValidationException("Unsupported hit score key: " + entry.getKey());
            }
            if (entry.getValue() < 0) {
                throw new TrainingValidationException("Hit counters cannot be negative");
            }
            normalized.put(entry.getKey(), entry.getValue());
        }

        return Collections.unmodifiableMap(normalized);
    }
}
