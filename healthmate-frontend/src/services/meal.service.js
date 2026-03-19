import api from "./api";

const logMeal = (mealData) => {
    return api.post("/meals", mealData);
};

const getUserMeals = () => {
    return api.get("/meals/user");
};

const deleteMeal = (id) => {
    return api.delete(`/meals/${id}`);
};

const mealService = {
    logMeal,
    getUserMeals,
    deleteMeal
};

export default mealService;
