import React, { useState, useEffect } from 'react';
import { Chart as ChartJS, registerables } from 'chart.js';
import { Chart, Bar } from 'react-chartjs-2';

ChartJS.register(...registerables);

const useTheme = () => {
    const [isDarkMode, setIsDarkMode] = useState(!document.body.classList.contains('light-mode'));

    useEffect(() => {
        const observer = new MutationObserver(() => {
            setIsDarkMode(!document.body.classList.contains('light-mode'));
        });
        observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });
        return () => observer.disconnect();
    }, []);

    return {
        isDarkMode,
        colors: {
            grid: isDarkMode ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)',
            textMain: isDarkMode ? '#f8fafc' : '#0f172a',
            textMuted: isDarkMode ? '#94a3b8' : '#475569',
            primary: '#6366f1',
            secondary: '#10b981',
            danger: '#ef4444',
            warning: '#f59e0b',
            glassBorder: 'var(--glass-border)'
        }
    };
};

const DateRangeHeader = ({ title, startDate, endDate, onRangeChange }) => {
    const { colors } = useTheme();
    return (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '10px' }}>
            <h4 style={{ margin: 0, color: colors.textMain, fontSize: '1.2rem', fontWeight: '700' }}>{title}</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', background: 'rgba(255,255,255,0.03)', padding: '6px 12px', borderRadius: '12px', border: `1px solid ${colors.glassBorder}` }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '0.7rem', color: colors.textMuted, fontWeight: '700', textTransform: 'uppercase' }}>From</span>
                    <input
                        type="date"
                        value={startDate}
                        onChange={(e) => onRangeChange(e.target.value, endDate)}
                        style={{ background: 'transparent', border: 'none', color: colors.primary, fontSize: '0.8rem', fontWeight: '700', outline: 'none', cursor: 'pointer' }}
                    />
                </div>
                <div style={{ width: '1px', height: '12px', background: colors.glassBorder }}></div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '0.7rem', color: colors.textMuted, fontWeight: '700', textTransform: 'uppercase' }}>To</span>
                    <input
                        type="date"
                        value={endDate}
                        onChange={(e) => onRangeChange(startDate, e.target.value)}
                        style={{ background: 'transparent', border: 'none', color: colors.primary, fontSize: '0.8rem', fontWeight: '700', outline: 'none', cursor: 'pointer' }}
                    />
                </div>
            </div>
        </div>
    );
};

export const WeeklyWorkoutChart = ({ data, startDate, endDate, onRangeChange }) => {
    const { colors } = useTheme();

    const chartData = {
        labels: data.map(d => new Date(d.date).toLocaleDateString('en-US', { weekday: 'short', day: 'numeric' })),
        datasets: [
            {
                label: 'Workouts',
                data: data.map(d => d.workoutsCount),
                backgroundColor: 'rgba(99, 102, 241, 0.8)',
                borderColor: colors.primary,
                borderWidth: 2,
                borderRadius: 4,
                yAxisID: 'y',
                order: 2,
            },
            {
                label: 'Duration (min)',
                data: data.map(d => d.totalDuration),
                backgroundColor: 'rgba(16, 185, 129, 0.8)',
                borderColor: colors.secondary,
                borderWidth: 2,
                borderRadius: 4,
                yAxisID: 'y1',
                order: 1,
            }
        ]
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                align: 'end',
                labels: {
                    color: colors.textMain,
                    font: { family: "'Outfit', sans-serif", size: 11, weight: '500' },
                    usePointStyle: true,
                    boxWidth: 8
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { color: colors.grid, drawBorder: false }
            },
            y1: {
                position: 'right',
                beginAtZero: true,
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { drawOnChartArea: false }
            },
            x: {
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { display: false }
            }
        }
    };

    return (
        <div style={{ height: '350px' }}>
            <DateRangeHeader title="Activity Logs" startDate={startDate} endDate={endDate} onRangeChange={onRangeChange} />
            <div style={{ flex: 1, height: '280px' }}>
                <Bar data={chartData} options={options} />
            </div>
        </div>
    );
};

export const CalorieComparisonChart = ({ data, startDate, endDate, onRangeChange }) => {
    const { colors } = useTheme();

    const chartData = {
        labels: data.map(d => new Date(d.date).toLocaleDateString('en-US', { weekday: 'short', day: 'numeric' })),
        datasets: [
            {
                label: 'Consumed',
                data: data.map(d => d.consumed),
                backgroundColor: 'rgba(239, 68, 68, 0.8)',
                borderColor: colors.danger,
                borderWidth: 2,
                borderRadius: 4,
            },
            {
                label: 'Burned',
                data: data.map(d => d.burned),
                backgroundColor: 'rgba(16, 185, 129, 0.8)',
                borderColor: colors.secondary,
                borderWidth: 2,
                borderRadius: 4,
            }
        ]
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                align: 'end',
                labels: {
                    color: colors.textMain,
                    font: { family: "'Outfit', sans-serif", size: 11, weight: '500' },
                    usePointStyle: true,
                    boxWidth: 8
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { color: colors.grid, drawBorder: false }
            },
            x: {
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { display: false }
            }
        }
    };

    return (
        <div style={{ height: '350px' }}>
            <DateRangeHeader title="Calorie Distribution" startDate={startDate} endDate={endDate} onRangeChange={onRangeChange} />
            <div style={{ flex: 1, height: '280px' }}>
                <Bar data={chartData} options={options} />
            </div>
        </div>
    );
};

export const WellnessTrendChart = ({ history = [], startDate, endDate, onRangeChange }) => {
    const { colors } = useTheme();

    const filteredData = (history || []).filter(d => {
        if (!d || !d.date) return false;
        const date = typeof d.date === 'string' ? d.date.split('T')[0] : '';
        return date >= startDate && date <= endDate;
    }).sort((a, b) => new Date(a.date) - new Date(b.date));

    const chartData = {
        labels: filteredData.map(d => new Date(d.date).toLocaleDateString('en-US', { weekday: 'short', day: 'numeric' })),
        datasets: [
            {
                label: 'Water (L)',
                data: filteredData.map(d => d.waterIntake),
                backgroundColor: 'rgba(14, 165, 233, 0.8)',
                borderColor: '#0ea5e9',
                borderWidth: 2,
                borderRadius: 4,
                yAxisID: 'y',
                order: 2
            },
            {
                label: 'Sleep (h)',
                data: filteredData.map(d => d.sleepDuration),
                backgroundColor: 'rgba(139, 92, 246, 0.8)',
                borderColor: '#8b5cf6',
                borderWidth: 2,
                borderRadius: 4,
                yAxisID: 'y1',
                order: 1
            }
        ]
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                align: 'end',
                labels: {
                    color: colors.textMain,
                    font: { family: "'Outfit', sans-serif", size: 11, weight: '500' },
                    usePointStyle: true,
                    boxWidth: 8
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                suggestedMax: 4,
                title: { display: true, text: 'Water (L)', color: colors.textMuted, font: { size: 9, weight: '600' } },
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { color: colors.grid, drawBorder: false }
            },
            y1: {
                position: 'right',
                beginAtZero: true,
                suggestedMax: 12,
                title: { display: true, text: 'Sleep (h)', color: colors.textMuted, font: { size: 9, weight: '600' } },
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { drawOnChartArea: false }
            },
            x: {
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { display: false }
            }
        }
    };

    return (
        <div style={{ height: '350px' }}>
            <DateRangeHeader title="Wellness Patterns" startDate={startDate} endDate={endDate} onRangeChange={onRangeChange} />
            <div style={{ flex: 1, height: '280px' }}>
                <Bar data={chartData} options={options} />
            </div>
        </div>
    );
};

export const WeightProgressChart = ({ history = [], startDate, endDate, onRangeChange }) => {
    const { colors } = useTheme();

    const filteredData = (history || []).filter(d => {
        if (!d || !d.date) return false;
        const date = typeof d.date === 'string' ? d.date.split('T')[0] : '';
        return date >= startDate && date <= endDate;
    }).sort((a, b) => new Date(a.date) - new Date(b.date));

    const chartData = {
        labels: filteredData.map(d => new Date(d.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })),
        datasets: [
            {
                label: 'Weight (kg)',
                data: filteredData.map(d => d.weight),
                backgroundColor: 'rgba(99, 102, 241, 0.8)',
                borderColor: colors.primary,
                borderWidth: 2,
                borderRadius: 4,
            }
        ]
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                align: 'end',
                labels: {
                    color: colors.textMain,
                    font: { family: "'Outfit', sans-serif", size: 11, weight: '500' },
                    usePointStyle: true,
                    boxWidth: 8
                }
            }
        },
        scales: {
            y: {
                beginAtZero: false,
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { color: colors.grid, drawBorder: false }
            },
            x: {
                ticks: { color: colors.textMuted, font: { size: 10 } },
                grid: { display: false }
            }
        }
    };

    return (
        <div style={{ height: '350px' }}>
            <DateRangeHeader title="Weight Progress" startDate={startDate} endDate={endDate} onRangeChange={onRangeChange} />
            <div style={{ flex: 1, height: '280px' }}>
                {filteredData.length > 0 ? (
                    <Bar data={chartData} options={options} />
                ) : (
                    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                        <div style={{ fontSize: '2.5rem', marginBottom: '10px', opacity: 0.5 }}>📊</div>
                        <p style={{ margin: 0 }}>No data for this range.</p>
                    </div>
                )}
            </div>
        </div>
    );
};
