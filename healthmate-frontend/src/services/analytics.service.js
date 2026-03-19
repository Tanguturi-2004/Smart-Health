import api from "./api";

const getDashboardData = (startDate, endDate) => {
    let url = "/analytics/dashboard";
    if (startDate && endDate) {
        url += `?startDate=${startDate}&endDate=${endDate}`;
    }
    return api.get(url);
};

const getHistory = () => api.get("/analytics/history");
const getStreak = () => api.get("/analytics/streak");

const analyticsService = {
    getDashboardData,
    getHistory,
    getStreak
};

export default analyticsService;
