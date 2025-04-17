package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.expense.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.expense.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.CategoryRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        Category category = getCategoryById(dto.getCategoryId());
        Expense expense = modelMapper.map(dto, Expense.class);
        expense.setCategory(category);

        return modelMapper
                .map(expenseRepository.save(expense), ExpenseDTO.class);
    }

    @Override
    public ExpenseDTO updateExpense(ExpenseDTO dto) {
        Expense expense = modelMapper.map(getExpenseById(dto.getId()), Expense.class);

        expense.setTitle(dto.getTitle() != null ? dto.getTitle() : expense.getTitle());
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : expense.getExpenseDate());
        expense.setAmount(dto.getAmount() != null ? dto.getAmount() : expense.getAmount());
        expense.setCategory(dto.getCategoryId() != null ? getCategoryById(dto.getCategoryId()) : expense.getCategory());

        expense = expenseRepository.save(expense);
        return modelMapper.map(expense, ExpenseDTO.class);
    }

    @Override
    public void deleteExpense(Long id) {
        ExpenseDTO expenseDTO = getExpenseById(id);
        expenseRepository.delete(modelMapper.map(expenseDTO, Expense.class));
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: "+id+" not found"));

        return modelMapper.map(expense, ExpenseDTO.class);
    }

    @Override
    public ExpenseResponse getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        List<ExpenseDTO> response = expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();

        return ExpenseResponse.builder()
                .expenses(response)
                .build();
    }

    @Override
    public ExpenseResponse getExpensesByCategoryName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category with name: " +
                        categoryName + " not found"));

        List<Expense> expenses = expenseRepository.findByCategory(category);

        List<ExpenseDTO> response =  expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();

        return ExpenseResponse.builder()
                .expenses(response)
                .build();
    }


    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: "+id+" not found"));
    }
}
