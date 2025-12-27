package com.anonchat.backend.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class FilterService {

    private static final List<String> BAD_WORDS = Arrays.asList(
            "badword", "vulgar", "stupid", "idiot", "ass"
    );

    public String sanitize(String content) {
        if (content == null) return "";

        String sanitized = content;
        for (String word : BAD_WORDS) {
            // (?i) enables case-insensitivity (matches 'Stupid', 'STUPID', 'stupid')
            // \\b ensures we match whole words (so 'assassins' doesn't become '****assins')
            String pattern = "(?i)\\b" + Pattern.quote(word) + "\\b";
            sanitized = sanitized.replaceAll(pattern, "*".repeat(word.length()));
        }

        return sanitized;
    }
}