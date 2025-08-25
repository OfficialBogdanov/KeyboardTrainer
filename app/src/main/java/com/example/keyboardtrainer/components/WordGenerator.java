package com.example.keyboardtrainer.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WordGenerator {
    // Constants
    public static final int DIFFICULTY_EASY = 0;
    /** @noinspection unused*/
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_HARD = 2;

    // Word
    private final List<String> easyNouns = Arrays.asList(
            "cat", "dog", "house", "car", "book", "sun", "water",
            "tree", "computer", "phone", "music", "friend", "city"
    );

    private final List<String> normalNouns = Arrays.asList(
            "algorithm", "database", "interface", "variable", "function",
            "repository", "framework", "compiler", "debugger", "syntax",
            "iterator", "pointer", "recursion", "abstraction", "inheritance"
    );

    private final List<String> hardNouns = Arrays.asList(
            "polymorphism", "encapsulation", "asynchrony", "concurrency",
            "serialization", "deserialization", "persistence", "reflection",
            "decomposition", "optimization", "synchronization"
    );

    private final List<String> easyAdjectives = Arrays.asList(
            "big", "small", "happy", "beautiful", "fast", "smart",
            "funny", "kind", "strong", "bright", "quiet", "modern"
    );

    private final List<String> normalAdjectives = Arrays.asList(
            "asynchronous", "recursive", "polymorphic", "deterministic",
            "idempotent", "immutable", "concurrent", "distributed",
            "volatile", "transient", "synchronized"
    );

    private final List<String> hardAdjectives = Arrays.asList(
            "thread-safe", "type-safe", "memory-efficient", "cache-friendly",
            "lock-free", "wait-free", "exception-safe", "null-safe",
            "atomic", "reentrant", "side-effect-free"
    );

    private final List<String> verbs = Arrays.asList(
            "runs", "jumps", "reads", "writes", "sings", "plays",
            "learns", "thinks", "works", "swims", "drives", "builds",
            "compiles", "executes", "debugs", "optimizes", "analyzes"
    );

    private final List<String> adverbs = Arrays.asList(
            "quickly", "slowly", "happily", "easily", "quietly",
            "carefully", "suddenly", "usually", "brightly", "loudly",
            "efficiently", "concurrently", "asynchronously", "recursively"
    );

    private final List<String> pronouns = Arrays.asList(
            "I", "You", "He", "She", "We", "They", "It"
    );

    private final List<String> symbols = Arrays.asList(
            "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "+", "=",
            "{", "}", "[", "]", "|", ":", ";", "'", "<", ">",
            ",", ".", "?", "/", "~", "`"
    );

    private final List<String> operators = Arrays.asList(
            "+", "-", "*", "/", "%", "=", "==", "!=", "<", ">", "<=", ">=",
            "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", ">>>="
    );

    // Sentence
    private final List<String[]> easyTemplates = Arrays.asList(
            new String[]{"adjective", "noun", "verb", "adverb"},
            new String[]{"pronoun", "verb", "adjective", "noun"},
            new String[]{"adjective", "noun", "verb"},
            new String[]{"pronoun", "adverb", "verb"},
            new String[]{"noun", "verb", "adverb"}
    );

    private final List<String[]> normalTemplates = Arrays.asList(
            new String[]{"adjective", "noun", "verb", "adverb", "and", "adjective", "noun", "verb"},
            new String[]{"pronoun", "verb", "adjective", "noun", "with", "adjective", "noun"},
            new String[]{"adjective", "noun", "verb", "adverb", "but", "pronoun", "verb", "adverb"},
            new String[]{"noun", "and", "noun", "verb", "adverb"},
            new String[]{"pronoun", "verb", "adjective", "noun", "while", "verb", "adjective", "noun"}
    );

    private final List<String[]> hardTemplates = Arrays.asList(
            new String[]{"noun", "operator", "value", "operator", "value", ";"},
            new String[]{"noun", "(", "params", ")", "{", "statement", "}"},
            new String[]{"if", "(", "condition", ")", "{", "statement", "}"},
            new String[]{"for", "(", "init", ";", "condition", ";", "step", ")", "{", "statement", "}"},
            new String[]{"while", "(", "condition", ")", "{", "statement", "}"},
            new String[]{"noun", "operator", "function", "(", "params", ")", ";"}
    );

    private final Random random = new Random();

    // Generator
    public GeneratedWords generateWords(int sentenceCount, int difficulty) {
        StringBuilder textBuilder = new StringBuilder();
        List<Integer> wordStarts = new ArrayList<>();
        List<Integer> wordEndings = new ArrayList<>();

        for (int i = 0; i < sentenceCount; i++) {
            String sentence = generateSentence(difficulty);
            String[] words = sentence.split(" ");
            int startPos = textBuilder.length();

            for (String word : words) {
                wordStarts.add(startPos);
                startPos += word.length() + 1;
                wordEndings.add(startPos - 2);
            }

            textBuilder.append(sentence).append(" ");
        }

        return new GeneratedWords(
                textBuilder.toString().trim(),
                wordStarts,
                wordEndings
        );
    }

    private String generateSentence(int difficulty) {
        String[] template = getRandomTemplate(difficulty);
        StringBuilder sentence = new StringBuilder();

        for (int i = 0; i < template.length; i++) {
            String word = getRandomWord(template[i], difficulty);

            if (i == 0) {
                word = capitalizeFirstLetter(word);
            }

            if (sentence.length() > 0) {
                sentence.append(" ");
            }
            sentence.append(word);
        }

        return sentence.append(".").toString();
    }

    // Selection
    private String[] getRandomTemplate(int difficulty) {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return easyTemplates.get(random.nextInt(easyTemplates.size()));
            case DIFFICULTY_HARD:
                return hardTemplates.get(random.nextInt(hardTemplates.size()));
            default:
                return normalTemplates.get(random.nextInt(normalTemplates.size()));
        }
    }

    private String getRandomWord(String partOfSpeech, int difficulty) {
        switch (partOfSpeech) {
            case "adjective":
                return getRandomAdjective(difficulty);
            case "noun":
                return getRandomNoun(difficulty);
            case "verb":
                return verbs.get(random.nextInt(verbs.size()));
            case "adverb":
                return adverbs.get(random.nextInt(adverbs.size()));
            case "pronoun":
                return pronouns.get(random.nextInt(pronouns.size()));
            case "operator":
                return operators.get(random.nextInt(operators.size()));
            case "value":
                return String.valueOf(random.nextInt(1000));
            case "params":
                return getRandomNoun(difficulty) + ", " + getRandomNoun(difficulty);
            case "condition":
                return getRandomNoun(difficulty) + " " + operators.get(random.nextInt(5)) + " " + random.nextInt(100);
            case "statement":
                return getRandomNoun(difficulty) + " = " + random.nextInt(100) + ";";
            case "init":
                return getRandomNoun(difficulty) + " = " + random.nextInt(10);
            case "step":
                return getRandomNoun(difficulty) + "++";
            case "function":
                return getRandomNoun(difficulty) + "Function";
            case "and":
                return "and";
            case "with":
                return "with";
            case "while":
                return "while";
            case "if":
                return "if";
            case "for":
                return "for";
            case ";":
                return ";";
            default:
                return symbols.get(random.nextInt(symbols.size()));
        }
    }

    private String getRandomNoun(int difficulty) {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return easyNouns.get(random.nextInt(easyNouns.size()));
            case DIFFICULTY_HARD:
                return hardNouns.get(random.nextInt(hardNouns.size()));
            default:
                return normalNouns.get(random.nextInt(normalNouns.size()));
        }
    }

    private String getRandomAdjective(int difficulty) {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return easyAdjectives.get(random.nextInt(easyAdjectives.size()));
            case DIFFICULTY_HARD:
                return hardAdjectives.get(random.nextInt(hardAdjectives.size()));
            default:
                return normalAdjectives.get(random.nextInt(normalAdjectives.size()));
        }
    }

    // Utility
    private String capitalizeFirstLetter(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    // Data
    public static class GeneratedWords {
        private final String text;
        private final List<Integer> wordStarts;
        private final List<Integer> wordEndings;

        public GeneratedWords(String text, List<Integer> wordStarts, List<Integer> wordEndings) {
            this.text = text;
            this.wordStarts = wordStarts;
            this.wordEndings = wordEndings;
        }

        public String getText() { return text; }
        public List<Integer> getWordStarts() { return wordStarts; }
        public List<Integer> getWordEndings() { return wordEndings; }
    }
}