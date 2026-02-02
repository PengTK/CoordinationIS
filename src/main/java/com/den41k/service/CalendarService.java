package com.den41k.service;

import com.den41k.model.Priority;
import com.den41k.model.Project;
import com.den41k.model.Task;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class CalendarService {

    private static final Map<String, String> PRIORITY_COLORS = Map.of(
        "URGENT", "#F44336",
        "HIGH", "#FF9800",
        "MEDIUM", "#FFC107",
        "LOW", "#4CAF50"
    );

    private static final String PROJECT_DEADLINE_COLOR = "#9C27B0";

    public Map<String, Object> generateCalendarData(Project project, List<Task> tasks, YearMonth currentMonth) {
        Map<String, Object> calendarData = new HashMap<>();

        calendarData.put("currentMonth", currentMonth);
        calendarData.put("monthName", currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru")));
        calendarData.put("year", currentMonth.getYear());

        List<DayInfo> days = generateDays(currentMonth, project, tasks);
        calendarData.put("days", days);

        calendarData.put("prevMonth", currentMonth.minusMonths(1).toString());  // "2026-01"
        calendarData.put("nextMonth", currentMonth.plusMonths(1).toString());   // "2026-03"

        return calendarData;
    }

    private List<DayInfo> generateDays(YearMonth month, Project project, List<Task> tasks) {
        List<DayInfo> days = new ArrayList<>();

        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
            .filter(task -> task.getDeadLine() != null)
            .filter(task -> YearMonth.from(task.getDeadLine()).equals(month))
            .collect(Collectors.groupingBy(Task::getDeadLine));

        LocalDate projectDeadline = project.getDeadLine();
        
        int daysInMonth = month.lengthOfMonth();
        LocalDate firstDay = month.atDay(1);

        int dayOfWeek = firstDay.getDayOfWeek().getValue(); //
        for (int i = dayOfWeek - 1; i > 0; i--) {
            LocalDate prevMonthDay = firstDay.minusDays(i);
            days.add(new DayInfo(prevMonthDay, false, null, 0));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            boolean isCurrentMonth = true;

            boolean isProjectDeadline = projectDeadline != null && projectDeadline.equals(date);

            List<Task> dayTasks = tasksByDate.getOrDefault(date, Collections.emptyList());
            int taskCount = dayTasks.size();

            String color = null;
            if (isProjectDeadline) {
                color = PROJECT_DEADLINE_COLOR;
            } else if (!dayTasks.isEmpty()) {
                color = getHighestPriorityColor(dayTasks);
            }
            
            days.add(new DayInfo(date, isCurrentMonth, color, taskCount));
        }

        LocalDate lastDay = month.atEndOfMonth();
        int daysToAdd = 7 - lastDay.getDayOfWeek().getValue();
        for (int i = 1; i <= daysToAdd; i++) {
            LocalDate nextMonthDay = lastDay.plusDays(i);
            days.add(new DayInfo(nextMonthDay, false, null, 0));
        }
        
        return days;
    }

    private String getHighestPriorityColor(List<Task> tasks) {
        if (tasks.isEmpty()) return null;

        Priority highest = tasks.stream()
            .map(Task::getPriority)
            .filter(Objects::nonNull)
            .min(Comparator.comparing(p -> {
                switch (p) {
                    case URGENT: return 0;
                    case HIGH: return 1;
                    case MEDIUM: return 2;
                    case LOW: return 3;
                    default: return 4;
                }
            }))
            .orElse(null);
        
        return highest != null ? PRIORITY_COLORS.get(highest.name()) : null;
    }

    public static class DayInfo {
        private final LocalDate date;
        private final boolean isCurrentMonth;
        private final String color;
        private final int taskCount;

        public DayInfo(LocalDate date, boolean isCurrentMonth, String color, int taskCount) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            this.color = color;
            this.taskCount = taskCount;
        }

        public LocalDate getDate() { return date; }
        public boolean isCurrentMonth() { return isCurrentMonth; }
        public String getColor() { return color; }
        public int getTaskCount() { return taskCount; }
        public int getDayOfMonth() { return date.getDayOfMonth(); }
    }


}