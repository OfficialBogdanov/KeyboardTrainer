package com.example.keyboardtrainer.components;

public class Achievement {
    private final String id;
    private final String title;
    private final String description;
    private int progress;
    private final int target;
    private boolean achieved;

    // Constructor
    public Achievement(String id, String title, String description, int progress, int target, boolean achieved) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.target = target;
        this.achieved = achieved;
    }

    // Getter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    /** @noinspection unused*/
    public int getProgress() { return progress; }
    public int getTarget() { return target; }
    /** @noinspection unused*/
    public boolean isAchieved() { return achieved; }

    // Setter
    public void setProgress(int progress) { this.progress = progress; }
    public void setAchieved(boolean achieved) { this.achieved = achieved; }

    // Utility
    public int getProgressPercent() {
        if (target == 0) return 0;
        return (int) ((float) progress / target * 100);
    }
}