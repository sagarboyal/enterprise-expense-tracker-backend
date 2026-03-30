package com.main.trex.expense.dashboard.service.impl;

import com.main.trex.expense.dashboard.service.DashboardService;
import com.main.trex.expense.dto.CategoryExpenseDTO;
import com.main.trex.expense.dto.MonthlyExpenseDTO;
import com.main.trex.expense.dto.StatusExpenseDTO;
import com.main.trex.expense.dto.SummaryDTO;
import com.main.trex.expense.dto.WeeklyExpenseDTO;
import com.main.trex.expense.repository.ExpenseRepository;
import com.main.trex.identity.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final AuthUtils authUtils;

    @Override
    public List<MonthlyExpenseDTO> getMonthlyAnalytics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = expenseRepository.getMonthlyExpenseTotals(authUtils.loggedInUser().getId(), startDate, endDate);
        return data.stream().map(obj -> {
            int monthNumber = (int) obj[0];
            String monthName = Month.of(monthNumber).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            Double total = (Double) obj[1];
            return new MonthlyExpenseDTO(monthName, total);
        }).toList();
    }

    @Override
    public List<WeeklyExpenseDTO> getWeeklyAnalytics() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        List<Object[]> results = expenseRepository.findTotalByDayOfWeekForUser(startOfWeek, today, authUtils.loggedInUser().getId());

        Map<String, Double> dayMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            dayMap.put(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), 0.0);
        }

        for (Object[] row : results) {
            String day = (String) row[0];
            Double total = (Double) row[1];
            dayMap.put(day, total);
        }

        return dayMap.entrySet().stream()
                .map(entry -> new WeeklyExpenseDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<CategoryExpenseDTO> getCategoryAnalytics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = expenseRepository.getCategoryExpenseTotals(authUtils.loggedInUser().getId(), startDate, endDate);
        return data.stream()
                .map(obj -> new CategoryExpenseDTO((String) obj[0], (Double) obj[1]))
                .toList();
    }

    @Override
    public List<StatusExpenseDTO> getStatusAnalytics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = expenseRepository.getStatusAnalytics(authUtils.loggedInUser().getId(), startDate, endDate);

        return rawData.stream()
                .map(obj -> new StatusExpenseDTO(
                        obj[0].toString(),
                        ((Number) obj[1]).doubleValue()
                ))
                .toList();
    }

    @Override
    public SummaryDTO getSummary() {
        Long userId = authUtils.loggedInUser().getId();
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByUserId(userId);
        totalExpenses = (totalExpenses == null) ? BigDecimal.ZERO : totalExpenses;

        LocalDate now = LocalDate.now();

        Long approvedCount = expenseRepository.countApprovedExpenses(userId);
        Long pendingCount = expenseRepository.countPendingApprovals(userId);
        Long rejectedCount = expenseRepository.countRejectedExpenses(userId);

        List<StatusExpenseDTO> statusList = getStatusAnalytics(
                now.withDayOfMonth(1),
                now.withDayOfMonth(now.lengthOfMonth())
        );

        return new SummaryDTO(totalExpenses.doubleValue(), approvedCount, pendingCount, rejectedCount, statusList);
    }
}
