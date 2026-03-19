import React, { useMemo } from 'react';

const ActivityHeatmap = ({ history, showHeader = true, showStats = false }) => {
    const dayLabels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    // Generate dates for the last 365 days, aligned by week
    const heatmapData = useMemo(() => {
        const today = new Date();
        const yearAgo = new Date();
        yearAgo.setDate(today.getDate() - 364);

        // Align yearAgo to the start of its week (Sunday) to keep grid consistent
        const startOfGrid = new Date(yearAgo);
        startOfGrid.setDate(yearAgo.getDate() - yearAgo.getDay());

        const dates = [];
        const logMap = new Map();

        history.forEach(log => {
            if (log.date) {
                const dateStr = typeof log.date === 'string' ? log.date.split('T')[0] : '';
                const steps = parseInt(log.steps) || 0;
                logMap.set(dateStr, steps);
            }
        });

        // Fill from startOfGrid to today
        let currentDate = new Date(startOfGrid);
        while (currentDate <= today) {
            const dateStr = currentDate.toISOString().split('T')[0];
            dates.push({
                date: dateStr,
                steps: logMap.get(dateStr) || 0,
                hasLog: logMap.has(dateStr),
                month: currentDate.toLocaleString('default', { month: 'short' }),
                dayOfWeek: currentDate.getDay()
            });
            currentDate.setDate(currentDate.getDate() + 1);
        }

        // Group by weeks (7 days each, starting Sunday)
        const columns = [];
        for (let i = 0; i < dates.length; i += 7) {
            columns.push(dates.slice(i, i + 7));
        }

        // Calculate month label positions
        const monthPositions = [];
        let lastMonth = '';
        columns.forEach((week, i) => {
            const month = week[0].month;
            if (month !== lastMonth) {
                monthPositions.push({ month, index: i });
                lastMonth = month;
            }
        });

        return { columns, totalSubmissions: history.length, monthPositions };
    }, [history]);

    // Calculate Streak
    const stats = useMemo(() => {
        let maxStreak = 0;
        let currentStreak = 0;
        let totalActiveDays = 0;

        const sortedLogs = [...(history || [])]
            .filter(l => l && l.date)
            .map(l => typeof l.date === 'string' ? l.date.split('T')[0] : '')
            .filter(d => d)
            .sort();

        const uniqueDates = [...new Set(sortedLogs)];
        totalActiveDays = uniqueDates.length;

        if (uniqueDates.length > 0) {
            for (let i = 0; i < uniqueDates.length; i++) {
                if (i > 0) {
                    const prev = new Date(uniqueDates[i - 1]);
                    const curr = new Date(uniqueDates[i]);
                    const diffDays = Math.ceil(Math.abs(curr - prev) / (1000 * 60 * 60 * 24));

                    if (diffDays === 1) {
                        currentStreak++;
                    } else {
                        currentStreak = 1;
                    }
                } else {
                    currentStreak = 1;
                }
                maxStreak = Math.max(maxStreak, currentStreak);
            }
        }

        return { maxStreak, totalActiveDays };
    }, [history]);

    const getColor = (steps, hasLog) => {
        const isLight = document.body.classList.contains('light-mode');
        if (!hasLog) return isLight ? 'rgba(0, 0, 0, 0.05)' : 'rgba(255, 255, 255, 0.05)';
        if (steps >= 10000) return '#22c55e';
        if (steps >= 5000) return '#16a34a';
        if (steps > 0) return '#15803d';
        return isLight ? '#bbf7d0' : '#166534';
    };

    return (
        <div style={{ width: '100%', overflowX: 'auto' }} className="custom-scrollbar">
            {(showHeader || showStats) && (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <div style={{ fontSize: '1.2rem', fontWeight: '600' }}>
                        <span style={{ fontSize: '1.8rem', fontWeight: '900', color: 'var(--primary)', marginRight: '8px' }}>{heatmapData.totalSubmissions}</span>
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '1px' }}>Submissions past year</span>
                    </div>
                    <div style={{ display: 'flex', gap: '25px', textAlign: 'right' }}>
                        <div>
                            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: '800', textTransform: 'uppercase' }}>Active Days</div>
                            <div style={{ fontSize: '1.2rem', fontWeight: '800', color: 'var(--text-main)' }}>{stats.totalActiveDays}</div>
                        </div>
                        <div>
                            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: '800', textTransform: 'uppercase' }}>Max Streak</div>
                            <div style={{ fontSize: '1.2rem', fontWeight: '800', color: '#f59e0b' }}>{stats.maxStreak} 🔥</div>
                        </div>
                    </div>
                </div>
            )}

            <div style={{ display: 'flex', gap: '8px' }}>
                {/* Day Labels */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', paddingTop: '2px' }}>
                    {dayLabels.map((day, i) => (
                        <div key={day} style={{
                            height: '12px',
                            fontSize: '0.62rem',
                            color: 'var(--text-muted)',
                            fontWeight: '700',
                            display: 'flex',
                            alignItems: 'center',
                            textTransform: 'uppercase'
                        }}>
                            {day.charAt(0)}
                        </div>
                    ))}
                </div>

                {/* Heatmap Grid */}
                <div>
                    <div style={{ display: 'flex', gap: '4px' }}>
                        {heatmapData.columns.map((week, weekIndex) => (
                            <div key={weekIndex} style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                {week.map((day, dayIndex) => (
                                    <div
                                        key={day.date}
                                        title={`${new Date(day.date).toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' })}: ${day.steps} steps`}
                                        style={{
                                            width: '12px',
                                            height: '12px',
                                            borderRadius: '2px',
                                            backgroundColor: getColor(day.steps, day.hasLog),
                                            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                                            cursor: 'pointer'
                                        }}
                                        onMouseEnter={(e) => {
                                            e.target.style.transform = 'scale(1.4)';
                                            e.target.style.zIndex = '10';
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.transform = 'scale(1)';
                                            e.target.style.zIndex = '1';
                                        }}
                                    />
                                ))}
                            </div>
                        ))}
                    </div>

                    {/* Month Labels */}
                    <div style={{ display: 'flex', marginTop: '6px', color: 'var(--text-muted)', fontSize: '0.65rem', fontWeight: '700', position: 'relative', height: '15px' }}>
                        {heatmapData.monthPositions.map((pos, i) => (
                            <span key={i} style={{
                                position: 'absolute',
                                left: `${pos.index * 16}px`,
                                whiteSpace: 'nowrap'
                            }}>
                                {pos.month}
                            </span>
                        ))}
                    </div>
                </div>
            </div>

            {/* Legend */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '15px', paddingLeft: '35px', fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: '600' }}>
                <span>Less</span>
                {[0, 1, 5000, 10000].map(s => (
                    <div key={s} style={{ width: '10px', height: '10px', borderRadius: '2px', backgroundColor: getColor(s, s >= 0) }} />
                ))}
                <span>More</span>
            </div>
        </div>
    );
};

export default ActivityHeatmap;
