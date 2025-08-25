package com.example.keyboardtrainer.components;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keyboardtrainer.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;
import java.util.Locale;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {
    private List<Achievement> achievements;

    // Constructor
    public AchievementsAdapter(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    /** @noinspection ClassEscapesDefinedScope*/ // Recycle
    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.achievement_item, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAchievements(List<Achievement> newAchievements) {
        this.achievements = newAchievements;
        notifyDataSetChanged();
    }

    // Icon
    private void setupAchievementIcon(AchievementViewHolder holder, Achievement achievement) {
        switch (achievement.getId()) {
            case "beginner": holder.icon.setImageResource(R.drawable.home); break;
            case "pro": holder.icon.setImageResource(R.drawable.shield); break;
            case "speedster": holder.icon.setImageResource(R.drawable.clock); break;
            case "accuracy": holder.icon.setImageResource(R.drawable.check_circle); break;
            case "marathon": holder.icon.setImageResource(R.drawable.chevrons_right); break;
            case "master": holder.icon.setImageResource(R.drawable.keyboard); break;
            case "lightning": holder.icon.setImageResource(R.drawable.zap); break;
            case "sniper": holder.icon.setImageResource(R.drawable.target); break;
            case "legend": holder.icon.setImageResource(R.drawable.star); break;
            case "flawless": holder.icon.setImageResource(R.drawable.award); break;
        }
    }

    // Text
    private void setupTextViews(AchievementViewHolder holder, Achievement achievement, int progressPercent) {
        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());
        holder.progressText.setText(String.format(Locale.getDefault(), "%d%%", progressPercent));
    }

    // Progress
    private void setupProgressIndicator(AchievementViewHolder holder, int progressPercent) {
        int indicatorColor = getProgressColor(holder, progressPercent);

        holder.progressCircle.setIndicatorColor(indicatorColor);
        holder.progressText.setTextColor(indicatorColor);

        animateProgress(holder.progressCircle, progressPercent);
    }

    private int getProgressColor(AchievementViewHolder holder, int progressPercent) {
        if (progressPercent >= 100) {
            return ContextCompat.getColor(holder.itemView.getContext(), R.color.green_success);
        } else if (progressPercent >= 50) {
            return ContextCompat.getColor(holder.itemView.getContext(), R.color.light_primary);
        } else if (progressPercent >= 10) {
            return ContextCompat.getColor(holder.itemView.getContext(), R.color.yellow_warning);
        } else {
            return ContextCompat.getColor(holder.itemView.getContext(), R.color.red_error);
        }
    }

    // Animations
    private void animateProgress(CircularProgressIndicator progressIndicator, int targetProgress) {
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, targetProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressIndicator.setProgress(progress);
        });
        progressAnimator.start();
    }

    private void animateProgressBackground(AchievementViewHolder holder, int progressPercent) {
        holder.itemView.post(() -> {
            ViewGroup.LayoutParams params = holder.progressBackground.getLayoutParams();
            int targetWidth = (int) (holder.itemView.getWidth() * progressPercent / 100f);

            ValueAnimator animator = ValueAnimator.ofInt(0, targetWidth);
            animator.setDuration(800);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                params.width = (int) animation.getAnimatedValue();
                holder.progressBackground.setLayoutParams(params);
            });
            animator.start();
        });
    }

    // View
    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView description;
        TextView progressText;
        CircularProgressIndicator progressCircle;
        View progressBackground;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.achievement_icon);
            title = itemView.findViewById(R.id.achievement_title);
            description = itemView.findViewById(R.id.achievement_description);
            progressText = itemView.findViewById(R.id.achievement_progress_text);
            progressCircle = itemView.findViewById(R.id.progress_circle);
            progressBackground = itemView.findViewById(R.id.progress_background);
        }
    }

    /** @noinspection ClassEscapesDefinedScope*/
    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        int progressPercent = achievement.getProgressPercent();
        if (progressPercent > 100) progressPercent = 100;

        setupAchievementIcon(holder, achievement);
        setupTextViews(holder, achievement, progressPercent);
        setupProgressIndicator(holder, progressPercent);
        animateProgressBackground(holder, progressPercent);
    }
}