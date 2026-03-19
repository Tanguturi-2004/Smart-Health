package com.healthmate.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthmate.model.DailyLog;
import com.healthmate.model.User;
import com.healthmate.repository.DailyLogRepository;
import com.healthmate.repository.UserRepository;
import com.healthmate.service.UserDetailsImpl;
import com.healthmate.dto.MessageResponse;
import com.healthmate.dto.AnalyticsDashboardDTO;
import com.healthmate.model.Workout;
import com.healthmate.model.Meal;
import com.healthmate.model.HealthPlan;
import com.healthmate.repository.WorkoutRepository;
import com.healthmate.repository.MealRepository;
import com.healthmate.repository.HealthPlanRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    DailyLogRepository dailyLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    MealRepository mealRepository;

    @Autowired
    HealthPlanRepository healthPlanRepository;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            // If it's OAuth2 but not yet converted to our UserDetailsImpl in some flows
            String email = ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getAttribute("email");
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        }

        return null;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Analytics Controller Active - Version 2.3 (Weight Preservation Fix)");
    }

    @PostMapping("/log")
    public ResponseEntity<?> logDailyStats(@RequestBody DailyLog logRequest) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: User not authenticated"));
        }
        LocalDate date = logRequest.getDate() != null ? logRequest.getDate() : LocalDate.now();

        logger.info("DEBUG: Logging stats for User: {} Date: {}", userId, date);
        logger.info("DEBUG: Incoming Water: {} Sleep: {}", logRequest.getWaterIntake(), logRequest.getSleepDuration());

        // Check if log exists for this date
        DailyLog log = dailyLogRepository.findByUserIdAndDate(userId, date)
                .orElse(new DailyLog(userId, date, logRequest.getWeight(), logRequest.getCaloriesBurned(),
                        logRequest.getWaterIntake(), logRequest.getSleepDuration(), logRequest.getNotes(), 0, 0.0,
                        0.0, 0, 0.0));

        // Update values - Preservation logic for weight
        if (logRequest.getWeight() > 0) {
            log.setWeight(logRequest.getWeight());
        }
        log.setCaloriesBurned(logRequest.getCaloriesBurned());
        log.setWaterIntake(logRequest.getWaterIntake());
        log.setSleepDuration(logRequest.getSleepDuration());
        log.setNotes(logRequest.getNotes());
        log.setSteps(logRequest.getSteps());
        log.setDistance(logRequest.getDistance());

        if (logRequest.getDailyCalorieTarget() > 0) {
            log.setDailyCalorieTarget(logRequest.getDailyCalorieTarget());
        }
        if (logRequest.getDailyWaterTarget() > 0) {
            log.setDailyWaterTarget(logRequest.getDailyWaterTarget());
        }
        if (logRequest.getDailySleepTarget() > 0) {
            log.setDailySleepTarget(logRequest.getDailySleepTarget());
        }
        if (logRequest.getDailyStepsTarget() > 0) {
            log.setDailyStepsTarget(logRequest.getDailyStepsTarget());
        }

        logger.info("DEBUG: Saving log - Goal: C={}, W={}, S={}", log.getDailyCalorieTarget(),
                log.getDailyWaterTarget(),
                log.getDailySleepTarget());
        dailyLogRepository.save(log);

        // Optional: Update user's current weight in profile too - only if weight > 0
        if (logRequest.getWeight() > 0) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setWeight(logRequest.getWeight());
                userRepository.save(user);
            });
        }

        return ResponseEntity.ok(new MessageResponse("Daily stats logged successfully!"));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyLog>> getHistory() {
        String userId = getCurrentUserId();
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByDateAsc(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/streak")
    public ResponseEntity<?> getStreak() {
        String userId = getCurrentUserId();
        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByDateAsc(userId);

        if (logs.isEmpty()) {
            return ResponseEntity.ok(0);
        }

        java.util.Set<LocalDate> loggedDates = logs.stream()
                .map(DailyLog::getDate)
                .collect(java.util.stream.Collectors.toSet());

        LocalDate today = LocalDate.now();
        LocalDate startPoint = null;

        if (loggedDates.contains(today)) {
            startPoint = today;
        } else if (loggedDates.contains(today.minusDays(1))) {
            startPoint = today.minusDays(1);
        }

        if (startPoint == null) {
            return ResponseEntity.ok(0);
        }

        int streak = 0;
        LocalDate current = startPoint;
        while (loggedDates.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }

        return ResponseEntity.ok(streak);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardDTO> getDashboardAnalytics(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String startDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String endDate) {
        String userId = getCurrentUserId();
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(6);

        List<DailyLog> logs = dailyLogRepository.findByUserIdOrderByDateAsc(userId);
        List<Workout> workouts = workoutRepository.findByUserId(userId);
        List<Meal> meals = mealRepository.findByUserId(userId);
        Optional<HealthPlan> planOpt = healthPlanRepository.findByUserId(userId);

        // 1. Today Summary (Keep as today)
        LocalDate today = LocalDate.now();
        DailyLog todayLog = logs.stream()
                .filter(l -> l.getDate().equals(today))
                .findFirst()
                .orElse(new DailyLog());

        int caloriesFromWorkoutsToday = workouts.stream()
                .filter(w -> w.getDate().equals(today))
                .mapToInt(Workout::getCaloriesBurned)
                .sum();

        AnalyticsDashboardDTO.TodaySummary todaySummary = new AnalyticsDashboardDTO.TodaySummary(
                todayLog.getSteps(),
                todayLog.getCaloriesBurned() + caloriesFromWorkoutsToday,
                todayLog.getDistance());

        // 2. Weekly Activity (Custom Range)
        List<AnalyticsDashboardDTO.WeeklyActivity> weeklyActivity = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            List<Workout> dayWorkouts = workouts.stream()
                    .filter(w -> w.getDate().equals(currentDate))
                    .collect(Collectors.toList());

            int count = dayWorkouts.size();
            int duration = dayWorkouts.stream().mapToInt(Workout::getDuration).sum();
            weeklyActivity.add(new AnalyticsDashboardDTO.WeeklyActivity(date.toString(), count, duration));
        }

        // 3. Calorie Comparison (Custom Range)
        List<AnalyticsDashboardDTO.CalorieComparison> calorieComparison = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            int consumed = meals.stream()
                    .filter(m -> m.getDate().equals(currentDate))
                    .mapToInt(Meal::getCalories)
                    .sum();

            int workoutBurned = workouts.stream()
                    .filter(w -> w.getDate().equals(currentDate))
                    .mapToInt(Workout::getCaloriesBurned)
                    .sum();

            int burned = logs.stream()
                    .filter(l -> l.getDate().equals(currentDate))
                    .mapToInt(DailyLog::getCaloriesBurned)
                    .sum() + workoutBurned;

            calorieComparison.add(new AnalyticsDashboardDTO.CalorieComparison(date.toString(), consumed, burned));
        }

        // 4. Goal Progress (Based on Latest Log)
        int weeklyWorkoutGoal = 4;
        long thisWeekWorkouts = workouts.stream()
                .filter(w -> w.getDate().isAfter(today.minusDays(7)))
                .count();
        int workoutProgress = (int) Math.min((thisWeekWorkouts * 100) / weeklyWorkoutGoal, 100);

        int stepsGoal = todayLog.getDailyStepsTarget() > 0 ? todayLog.getDailyStepsTarget() : 8000;
        int stepsProgress = (int) Math.min((todayLog.getSteps() * 100) / stepsGoal, 100);

        int weightProgress = 0;
        if (!logs.isEmpty()) {
            double initialWeight = logs.get(0).getWeight();
            double currentWeight = todayLog.getWeight() > 0 ? todayLog.getWeight()
                    : logs.get(logs.size() - 1).getWeight();
            String goal = planOpt.map(HealthPlan::getGoal).orElse("Weight Loss").toLowerCase();

            if (goal.contains("loss")) {
                double targetLoss = 5.0;
                weightProgress = (int) Math.max(0, Math.min(((initialWeight - currentWeight) * 100) / targetLoss, 100));
            } else if (goal.contains("muscle") || goal.contains("gain")) {
                double targetGain = 5.0;
                weightProgress = (int) Math.max(0, Math.min(((currentWeight - initialWeight) * 100) / targetGain, 100));
            } else {
                weightProgress = 100;
            }
        }
        AnalyticsDashboardDTO.GoalProgress goalProgress = new AnalyticsDashboardDTO.GoalProgress(weightProgress,
                stepsProgress, workoutProgress);

        // 5. Actionable Insights
        List<String> insights = new ArrayList<>();
        if (thisWeekWorkouts < 3)
            insights.add("You are behind your weekly workout goal.");
        if (todayLog.getWaterIntake() < 2.0)
            insights.add("Increase water intake by 500 ml.");
        if (todayLog.getSleepDuration() < 7.0)
            insights.add("Sleep duration is below recommended level.");
        if (todayLog.getSteps() < 5000)
            insights.add("Try to reach at least 5000 steps today.");

        return ResponseEntity
                .ok(new AnalyticsDashboardDTO(todaySummary, weeklyActivity, calorieComparison, goalProgress, insights));
    }
}
