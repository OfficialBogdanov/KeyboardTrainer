package com.example.keyboardtrainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordGenerator {
    private final String[] words = {"apple", "banana", "cherry", "dog", "elephant", "flower", "guitar", "house", "ice", "jungle"};

    public GeneratedWords generateWords(int wordCount) {
        StringBuilder randomWords = new StringBuilder();
        List<Integer> wordEndings = new ArrayList<>();
        List<Integer> wordStarts = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < wordCount; i++) {
            String word = words[random.nextInt(words.length)];
            wordStarts.add(randomWords.length());
            randomWords.append(word).append(" ");
            wordEndings.add(randomWords.length() - 2);
        }

        return new GeneratedWords(randomWords.toString().trim(), wordStarts, wordEndings);
    }

    public static class GeneratedWords {
        private final String text;
        private final List<Integer> wordStarts;
        private final List<Integer> wordEndings;

        public GeneratedWords(String text, List<Integer> wordStarts, List<Integer> wordEndings) {
            this.text = text;
            this.wordStarts = wordStarts;
            this.wordEndings = wordEndings;
        }

        public String getText() {
            return text;
        }

        public List<Integer> getWordStarts() {
            return wordStarts;
        }

        public List<Integer> getWordEndings() {
            return wordEndings;
        }
    }
}

