package com.healthmate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthmate.model.HealthPlan;
import com.healthmate.model.User;
import com.healthmate.repository.HealthPlanRepository;
import com.healthmate.repository.UserRepository;

@Service
public class HealthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    HealthPlanRepository healthPlanRepository;

    public HealthPlan generateOrGetPlan(String userId) {
        Optional<HealthPlan> existingPlan = healthPlanRepository.findByUserId(userId);
        if (existingPlan.isPresent()) {
            HealthPlan plan = existingPlan.get();

            if (plan.getMealSuggestions() == null || plan.getMealSuggestions().isEmpty() ||
                    plan.getLastUpdated() == null || !plan.getLastUpdated().equals(LocalDate.now())) {
                return regeneratePlan(userId);
            }
            return plan;
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return createPlan(user);
    }

    public HealthPlan regeneratePlan(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        healthPlanRepository.findByUserId(userId).ifPresent(p -> healthPlanRepository.delete(p));
        return createPlan(user);
    }

    private HealthPlan createPlan(User user) {
        double weight = user.getWeight();
        double height = user.getHeight();
        int age = user.getAge();
        double heightM = height / 100.0;
        double bmi = weight / (heightM * heightM);
        String category = getBmiCategory(bmi);

        String healthGoal = (user.getHealthGoal() != null) ? user.getHealthGoal().toLowerCase() : "lose weight";
        String activity = (user.getActivityLevel() != null) ? user.getActivityLevel().toLowerCase() : "lightly active";

        double bmr = (10 * weight) + (6.25 * height) - (5 * age) - 80;

        double tdeeMultiplier = 1.2;
        if (activity.contains("light"))
            tdeeMultiplier = 1.375;
        else if (activity.contains("medium"))
            tdeeMultiplier = 1.55;
        else if (activity.contains("active"))
            tdeeMultiplier = 1.725;
        else if (activity.contains("very"))
            tdeeMultiplier = 1.9;

        double tdee = bmr * tdeeMultiplier;

        int dailyCals = (int) tdee;
        if (healthGoal.contains("loss")) {
            dailyCals -= 500;
        } else if (healthGoal.contains("muscle")) {
            dailyCals += 500;
        }

        if (dailyCals < 1200)
            dailyCals = 1200;

        int proteinG, fatsG, carbsG;

        if (healthGoal.contains("loss")) {
            proteinG = (int) (weight * 1.8); // 1.8g per kg for weight loss
            fatsG = (int) (weight * 0.8); // 0.8g per kg
        } else if (healthGoal.contains("muscle")) {
            proteinG = (int) (weight * 2.2); // 2.2g per kg for muscle gain
            fatsG = (int) (weight * 1.0); // 1.0g per kg
        } else {
            proteinG = (int) (weight * 1.6); // 1.6g per kg for maintenance
            fatsG = (int) (weight * 0.8); // 0.8g per kg
        }

        int remainingCals = dailyCals - (proteinG * 4) - (fatsG * 9);
        carbsG = Math.max(0, remainingCals / 4);

        List<HealthPlan.MealSuggestion> mealSuggestions = new ArrayList<>();

        // Distribution ratios: Breakfast (25%), Lunch (35%), Snack (10%), Dinner (30%)
        mealSuggestions.add(createMealSuggestion("Breakfast", 0.25, dailyCals, proteinG, carbsG, fatsG, healthGoal));
        mealSuggestions.add(createMealSuggestion("Lunch", 0.35, dailyCals, proteinG, carbsG, fatsG, healthGoal));
        mealSuggestions.add(createMealSuggestion("Snack", 0.10, dailyCals, proteinG, carbsG, fatsG, healthGoal));
        mealSuggestions.add(createMealSuggestion("Dinner", 0.30, dailyCals, proteinG, carbsG, fatsG, healthGoal));

        List<String> diet = new ArrayList<>();
        List<String> exercise = new ArrayList<>();
        String water = "2.5 Liters";
        String sleep = "7-8 Hours";

        if (healthGoal.contains("loss")) {
            diet.add("Breakfast: Oatmeal with Berries");
            diet.add("Lunch: Grilled Chicken Salad");
            diet.add("Dinner: Steamed Vegetables with Fish");
            diet.add("Snack: Green Tea + Almonds");
            diet.add("Focus: Calorie Deficit & High Fiber");
        } else if (healthGoal.contains("muscle")) {
            diet.add("Breakfast: Eggs + Whole Wheat Toast");
            diet.add("Lunch: Brown Rice + Grilled Chicken / Paneer");
            diet.add("Dinner: Quinoa + Salmon");
            diet.add("Snack: Protein Shake / Greek Yogurt");
            diet.add("Focus: Hypertrophy & Progressive Overload");
        } else {
            diet.add("Balanced Diet: Whole Foods focus");
            diet.add("Limit Sugar and Processed Foods");
        }

        if (activity.contains("low") || activity.contains("sedentary")) {
            exercise.add("Daily 30 min brisk walk");
            exercise.add("Light Yoga / Stretching");
        } else if (activity.contains("medium") || activity.contains("light")) {
            exercise.add("Cardio (Running/Cycling) 3x a week");
            exercise.add("Bodyweight Strength Training 2x a week");
        } else {
            exercise.add("HIIT Workouts 3x a week");
            exercise.add("Heavy Weight Training 4x a week");
        }

        if (category.equals("Underweight")) {
            diet.add("Priority: Increase healthy calorie-dense foods (nuts, avocados, seeds)");
            exercise.add("Focus on Strength Training to build muscle mass");
        } else if (category.equals("Overweight")) {
            diet.add("Focus: Increase fiber and lean protein for satiety");
            exercise.add("Combined Approach: Cardio + Resistance training");
        } else if (category.equals("Obese")) {
            diet.add("Priority: Controlled portion sizes and low glycemic index foods");
            exercise.clear();
            exercise.add("Safe Start: Low impact cardio (Swimming, Cycling, or Brisk Walking)");
            exercise.add("Important: Consult a professional before high-intensity training");
        } else {
            diet.add("Goal: Maintain balanced nutrition with whole foods");
            exercise.add("Sustainability: Stay consistent with regular active lifestyle");
        }

        String goalName = user.getHealthGoal();

        HealthPlan plan = new HealthPlan(user.getId(), bmi, category, water, sleep, diet, exercise, goalName,
                dailyCals, proteinG, carbsG, fatsG, mealSuggestions);
        return healthPlanRepository.save(plan);
    }

    private HealthPlan.MealSuggestion createMealSuggestion(String type, double ratio, int totalCals, int totalP,
            int totalC, int totalF, String goal) {
        int cals = (int) (totalCals * ratio);
        int p = (int) (totalP * ratio);
        int c = (int) (totalC * ratio);
        int f = (int) (totalF * ratio);

        List<String> options = new ArrayList<>();

        if (goal.contains("loss")) {
            if (type.equals("Breakfast")) {
                options.addAll(List.of(
                        "Vegetable Poha with Peanuts and Lemon",
                        "Moong Dal Chilla with Mint Chutney",
                        "Oats Upma with Finely Chopped Vegetables",
                        "Greek Yogurt with Flax Seeds and Walnuts",
                        "Sprouted Salad with Pomegranate and Lemon",
                        "Scrambled Tofu with Spinach and Toast",
                        "Masoor Dal Soup with a dash of Black Pepper",
                        "Quinoa Porridge with Apple and Cinnamon",
                        "Rawa Idli with Coconut Chutney",
                        "Sattu Drink (High Fiber) with Lemon"));
            } else if (type.equals("Lunch")) {
                options.addAll(List.of(
                        "Dal Tadka with 1 Bajra Roti and Cucumber Salad",
                        "Palak Paneer (Low Fat) with 1 Missi Roti",
                        "Brown Rice with Mixed Veg Sambar and Curd",
                        "Grilled Chicken Breast with Salad and Sauteed Veggies",
                        "Roasted Vegetables with Hummus and Whole Wheat Pita",
                        "Quinoa Pulao with Tofu and Broccoli",
                        "Chickpea Salad with Lemon Tahini Dressing",
                        "Whole Wheat Pasta with Pesto and Vegetables",
                        "Baked Sweet Potato with Black Beans and Corn",
                        "Mashed Avocado on Whole Wheat Toast with Poached Egg"));
            } else if (type.equals("Snack")) {
                options.addAll(List.of(
                        "Masala Chaas (Buttermilk) with Sprouted Moong",
                        "Roasted Makhana with Black Salt",
                        "Apple slices with a dash of Cinnamon",
                        "Handful of Almonds and Walnuts",
                        "Carrot and Cucumber sticks with Hummus",
                        "Edamame with sea salt",
                        "Rice cake with peanut butter",
                        "Cottage cheese with berries",
                        "Kale chips (Home-made/Baked)",
                        "Chia Seed Pudding (Stevia-sweetened)"));
            } else { // Dinner
                options.addAll(List.of(
                        "Lauki (Bottle Gourd) Sabzi with 1 Jowar Roti",
                        "Grilled Paneer Salad with Bell Peppers",
                        "Tinda Masala with 1 Whole Wheat Phulka",
                        "Steamed Fish with Ginger-Garlic and Sauteed Greens",
                        "Mixed Vegetable Soup with a slice of Multigrain Bread",
                        "Cauliflower Rice with Grilled Tofu and Peas",
                        "Stir-fry Vegetables with Mushroom and Tofu",
                        "Baked Salmon with Asparagus and Lemon",
                        "Zucchini Noodles with Tomato Basil Sauce",
                        "Lentil Soup (Yellow Dal) with a small portion of Sabzi"));
            }
        } else if (goal.contains("muscle")) {
            if (type.equals("Breakfast")) {
                options.addAll(List.of(
                        "Paneer Bhurji (150g) with 2 Multigrain Rotis",
                        "Sprouted Moong Salad with 2 Boiled Eggs",
                        "Masala Egg Omelet (3 eggs) with Brown Bread",
                        "Protein Pancakes (Oats + Whey + Egg Whites)",
                        "Peanut Butter and Banana on Whole Wheat Toast (2 slices)",
                        "Greek Yogurt with Granola, Almonds, and Berries",
                        "Smoked Salmon on Toasted Bagel with Cream Cheese",
                        "Chickpea flour pancakes (Besan Chilla) with extra paneer stuffing",
                        "Quinoa Egg Bhurji with vegetables",
                        "Turkey/Cottage Cheese Sandwich on Multigrain Bread"));
            } else if (type.equals("Lunch")) {
                options.addAll(List.of(
                        "Chicken Masala / Soya Chunk Curry with Brown Rice",
                        "Fish Curry with Red Rice and Sauted Greens",
                        "Mutton Curry (Lean) with 2 Bajra Rotis",
                        "Chicken & Broccoli Stir-fry with Quinoa",
                        "High Protein Pasta (Lentil Pasta) with Bolognese Sauce",
                        "Turkey Meatballs with Whole Wheat Spaghetti",
                        "Chickpea and Spinach Curry with Parboiled Rice",
                        "Grilled Tofu Bowl with Avocado, Rice, and Black Beans",
                        "Shrimp Stir-fry with Asparagus and Brown Rice",
                        "Mashed Sweet Potato with Grilled Salmon and Spinach"));
            } else if (type.equals("Snack")) {
                options.addAll(List.of(
                        "Roasted Chana with Peanut Butter / Boiled Eggs",
                        "Whey Protein with Milk and 1 Banana",
                        "Handful of Almonds and Walnuts with Greek Yogurt",
                        "Cottage Steel Bowl with Berries and Honey",
                        "Roasted Chickpeas or Chicken Strips",
                        "Hard-boiled eggs (3-4) with mustard",
                        "Protein Bar (Home-made or high quality)",
                        "Tuna Salad (In water) on Whole Wheat Crackers",
                        "Edamame beans with soy sauce",
                        "Nut Butter stuffed Celery sticks"));
            } else { // Dinner
                options.addAll(List.of(
                        "Paneer Tikka / Tandoori Fish with Dal Khichdi",
                        "Grilled Chicken Breast with Sweet Potato Mash",
                        "Keema Matar (Lean) with 1 Multigrain Roti",
                        "Baked Salmon with Broccoli and Brown Rice",
                        "Tofu and Mixed Vegetable Stir-fry with Cashews",
                        "Lentil & Vegetable Stew",
                        "Grilled Shrimp Tacos (Whole wheat tortillas)",
                        "Lentil Loaf with Roast Potatoes",
                        "Grilled Chicken with Mushroom Sauce",
                        "Quinoa and Black Bean Chili"));
            }
        } else { // Balanced
            if (type.equals("Breakfast")) {
                options.addAll(List.of(
                        "Idli Sambhar with Coconut Chutney",
                        "Vegetable Upma with Roasted Cashews",
                        "Aloo Paratha (Less Oil) with Curd",
                        "Poha with Onions and Potatoes",
                        "Wheat Dosa with Tomato Chutney",
                        "Besan Chilla with Grated Vegetables",
                        "Cornflakes with Skimmed Milk and Fruit",
                        "Sandwich with Cucumber, Tomato, and Chutney",
                        "Namkeen Sewai (Vermicelli) with Peas and Carrots",
                        "Adai (Lentil Crepe) with Avial"));
            } else if (type.equals("Lunch")) {
                options.addAll(List.of(
                        "Mixed Veg Curry with 2 Phulkas and Curd",
                        "Rajma Chawal with Kachumber Salad",
                        "Chole Kulche (Whole Wheat) with Lassi",
                        "Kadhi Pakora with Steamed Rice",
                        "Veg Pulao with Mixed Raita",
                        "Baigan Bharta with Bajra Roti and Salad",
                        "Dal Makhani (Low fat) with 1 Laccha Paratha",
                        "Spinach and Corn Rice with Dal Fry",
                        "Mushroom Masala with 2 Multi-grain Rotis",
                        "Pav Bhaji (Home-made with less butter and whole wheat buns)"));
            } else if (type.equals("Snack")) {
                options.addAll(List.of(
                        "Roasted Makhana (Fox Nuts) / Seasonal Fruit",
                        "Bhel Puri (Healthy version with sprouts)",
                        "Dhokla (2-3 pieces) with Green Chutney",
                        "Sev Murmura (Puffed rice) with dry fruits",
                        "Corn on the cob (Bhutta) with Lemon",
                        "Handful of Roasted Peanuts",
                        "Smoothies (Fruit based)",
                        "Multi-grain crackers with salsa",
                        "Sabudana Vada (Air-fried)",
                        "Baked Banana chips"));
            } else { // Dinner
                options.addAll(List.of(
                        "Paneer Matar with 1 Roti and Fresh Salad",
                        "Bhindi Masala with 1 Roti and Dal",
                        "Vegetable Biryani with Cucumber Raita",
                        "Jeera Rice with Dal Tadka and Aloo Gobhi",
                        "Thai Green Curry (Veg) with Jasmine Rice",
                        "Veg Manchurian (Home-made) with Fried Rice",
                        "Pasta Primavera with lots of veggies",
                        "Paneer Butter Masala (Mild) with 1 Nan",
                        "Mixed Vegetable Stew with Appam",
                        "Vegetable Kofta Curry with 2 Phulkas"));
            }
        }

        int dayOfMonth = LocalDate.now().getDayOfMonth();
        int primaryIndex = dayOfMonth % options.size();
        String suggestion = options.get(primaryIndex);

        // Alternatives are all options except the primary one
        List<String> alternatives = new ArrayList<>(options);
        // alternatives.remove(primaryIndex); // Keep all 10 as requested: "this plan
        // and other 10"

        return new HealthPlan.MealSuggestion(type, cals, p, c, f, suggestion, alternatives);
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5)
            return "Underweight";
        if (bmi < 24.9)
            return "Normal";
        if (bmi < 29.9)
            return "Overweight";
        return "Obese";
    }
}
