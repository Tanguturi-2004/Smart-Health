package com.healthmate.dto;

import java.util.List;

public class AnalyticsDashboardDTO {
    private TodaySummary todaySummary;
    private List<WeeklyActivity> weeklyActivity;
    private List<CalorieComparison> calorieComparison;
    private GoalProgress goalProgress;
    private List<String> insights;

    public AnalyticsDashboardDTO() {
    }

    public AnalyticsDashboardDTO(TodaySummary todaySummary, List<WeeklyActivity> weeklyActivity,
            List<CalorieComparison> calorieComparison, GoalProgress goalProgress,
            List<String> insights) {
        this.todaySummary = todaySummary;
        this.weeklyActivity = weeklyActivity;
        this.calorieComparison = calorieComparison;
        this.goalProgress = goalProgress;
        this.insights = insights;
    }

    public TodaySummary getTodaySummary() {
        return todaySummary;
    }

    public void setTodaySummary(TodaySummary todaySummary) {
        this.todaySummary = todaySummary;
    }

    public List<WeeklyActivity> getWeeklyActivity() {
        return weeklyActivity;
    }

    public void setWeeklyActivity(List<WeeklyActivity> weeklyActivity) {
        this.weeklyActivity = weeklyActivity;
    }

    public List<CalorieComparison> getCalorieComparison() {
        return calorieComparison;
    }

    public void setCalorieComparison(List<CalorieComparison> calorieComparison) {
        this.calorieComparison = calorieComparison;
    }

    public GoalProgress getGoalProgress() {
        return goalProgress;
    }

    public void setGoalProgress(GoalProgress goalProgress) {
        this.goalProgress = goalProgress;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public static class TodaySummary {
        private int steps;
        private int caloriesBurned;
        private double distance;

        public TodaySummary(int steps, int caloriesBurned, double distance) {
            this.steps = steps;
            this.caloriesBurned = caloriesBurned;
            this.distance = distance;
        }

        public int getSteps() {
            return steps;
        }

        public int getCaloriesBurned() {
            return caloriesBurned;
        }

        public double getDistance() {
            return distance;
        }
    }

    public static class WeeklyActivity {
        private String date;
        private int workoutsCount;
        private int totalDuration;

        public WeeklyActivity(String date, int workoutsCount, int totalDuration) {
            this.date = date;
            this.workoutsCount = workoutsCount;
            this.totalDuration = totalDuration;
        }

        public String getDate() {
            return date;
        }

        public int getWorkoutsCount() {
            return workoutsCount;
        }

        public int getTotalDuration() {
            return totalDuration;
        }
    }

    public static class CalorieComparison {
        private String date;
        private int consumed;
        private int burned;

        public CalorieComparison(String date, int consumed, int burned) {
            this.date = date;
            this.consumed = consumed;
            this.burned = burned;
        }

        public String getDate() {
            return date;
        }

        public int getConsumed() {
            return consumed;
        }

        public int getBurned() {
            return burned;
        }
    }

    public static class GoalProgress {
        private int weightLossProgress;
        private int stepsProgress;
        private int workoutProgress;

        public GoalProgress(int weightLossProgress, int stepsProgress, int workoutProgress) {
            this.weightLossProgress = weightLossProgress;
            this.stepsProgress = stepsProgress;
            this.workoutProgress = workoutProgress;
        }

        public int getWeightLossProgress() {
            return weightLossProgress;
        }

        public int getStepsProgress() {
            return stepsProgress;
        }

        public int getWorkoutProgress() {
            return workoutProgress;
        }
    }
}
