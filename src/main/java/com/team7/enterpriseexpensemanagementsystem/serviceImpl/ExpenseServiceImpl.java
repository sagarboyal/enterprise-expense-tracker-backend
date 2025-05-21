package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.*;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpensePagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.CategoryRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import com.team7.enterpriseexpensemanagementsystem.utils.UserUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, ModelMapper modelMapper, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }

    @Override
    public ExpenseResponse addExpense(ExpenseDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User is not logged in. Please log in to continue."));

        Category category = getCategoryById(dto.getCategoryId());
        Expense expense = modelMapper.map(dto, Expense.class);
        expense.setCategory(category);
        expense.setUser(user);
        expense.setStatus(Approval.PENDING);
        expense.setMessage(Approval.PENDING.getMessage());

        if(dto.getExpenseDate() == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(LocalDate.now().format(formatter));
            expense.setExpenseDate(localDate);
        }
        expense = expenseRepository.save(expense);

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(category.getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();
    }

    @Override
    public ExpenseResponse updateExpense(ExpenseUpdateRequest dto) {
        Expense expense = getExpenseById(dto.getId());
        Category category = dto.getCategoryId() != null ? getCategoryById(dto.getCategoryId()) : expense.getCategory();

        expense.setTitle(dto.getTitle() != null ? dto.getTitle() : expense.getTitle());
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : expense.getExpenseDate());
        expense.setAmount(dto.getAmount() != null ? dto.getAmount() : expense.getAmount());
        expense.setCategory(category);
        expense = expenseRepository.save(expense);

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(category.getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

    @Override
    public ExpenseResponse getExpenseByExpenseId(Long id) {
        Expense expense = getExpenseById(id);
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory().getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();
    }

    @Override
    public ExpensePagedResponse getAllExpenses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Expense> expensePage = expenseRepository.findAll(pageable);
        return getExpenseResponse(expensePage);
    }

    @Override
    public ExpensePagedResponse getExpensesByCategoryName(String categoryName, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category with name: " +
                        categoryName + " not found"));

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Expense> expensePage = expenseRepository.findByCategory(category, pageable);
        return getExpenseResponse(expensePage);
    }

    @Override
    public ExpenseResponse updateExpenseStatus(Long id, ApprovalRequest approvalRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User is not logged in. Please log in to continue."));
        Expense expense = getExpenseById(id);

        Approval status = resolveApprovalStatus(user, approvalRequest.getDecision());
        String message = approvalRequest.getMessage() != null
                && !approvalRequest.getMessage().trim().isEmpty()
                ? approvalRequest.getMessage() :
                status.getMessage();


        expense.setStatus(status);
        expense.setMessage(message);

        expense = expenseRepository.save(expense);
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory().getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();
    }


    private ExpensePagedResponse getExpenseResponse(Page<Expense> expensePage) {
        List<Expense> expenses = expensePage.getContent();

        if (expenses.isEmpty()) {
            throw new ResourceNotFoundException("No expenses found!");
        }

        List<ExpenseDTO> response = expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();

        return ExpensePagedResponse.builder()
                .expenses(response)
                .pageNumber(expensePage.getNumber())
                .pageSize(expensePage.getSize())
                .totalElements(expensePage.getTotalElements())
                .totalPages(expensePage.getTotalPages())
                .lastPage(expensePage.isLast())
                .build();
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: "+id+" not found"));
    }

    private Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    private Approval resolveApprovalStatus(User user, String decision) {
        boolean isApproved = decision.equalsIgnoreCase("approve");

        if (UserUtils.isAdmin(user)) {
            return isApproved ? Approval.APPROVED_BY_ADMIN : Approval.REJECTED_BY_ADMIN;
        } else if (UserUtils.isManager(user)) {
            return isApproved ? Approval.APPROVED_BY_MANAGER : Approval.REJECTED_BY_MANAGER;
        } else {
            throw new ApiException("Only managers or admins can update expense status.");
        }
    }

}
