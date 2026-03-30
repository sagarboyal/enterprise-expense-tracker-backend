package com.main.trex.expense.dashboard.service;

import com.main.trex.expense.dto.CategoryExpenseDTO;
import com.main.trex.expense.dto.MonthlyExpenseDTO;
import com.main.trex.expense.dto.StatusExpenseDTO;
import com.main.trex.expense.dto.SummaryDTO;
import com.main.trex.expense.dto.WeeklyExpenseDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    List<MonthlyExpenseDTO> getMonthlyAnalytics(LocalDate startDate, LocalDate endDate);

    List<WeeklyExpenseDTO> getWeeklyAnalytics();

    List<CategoryExpenseDTO> getCategoryAnalytics(LocalDate startDate, LocalDate endDate);

    List<StatusExpenseDTO> getStatusAnalytics(LocalDate startDate, LocalDate endDate);

    SummaryDTO getSummary();
}
