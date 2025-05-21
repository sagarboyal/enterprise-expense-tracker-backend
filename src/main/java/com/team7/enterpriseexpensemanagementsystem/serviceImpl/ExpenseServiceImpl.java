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
import com.team7.enterpriseexpensemanagementsystem.specification.ExpenseSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.UserUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private static final Map<String, Approval> statusMap = Map.of(
            "pending", Approval.PENDING,
            "manager approve", Approval.APPROVED_BY_MANAGER,
            "admin approve", Approval.APPROVED_BY_ADMIN,
            "manager reject", Approval.REJECTED_BY_MANAGER,
            "admin reject", Approval.REJECTED_BY_ADMIN
    );

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

    @Override
    public ExpensePagedResponse getFilteredExpenses(String categoryName, String status, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Long userId,
                                                    Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<Expense> specs = Specification.where(ExpenseSpecification.hasStatus(covertStatus(status)))
                .and(ExpenseSpecification.hasCategory(categoryName))
                .and(ExpenseSpecification.expenseDateBetween(startDate, endDate))
                .and(ExpenseSpecification.amountBetween(minAmount, maxAmount))
                .and(ExpenseSpecification.user(userId));



        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Expense> expensePage = expenseRepository.findAll(specs, pageDetails);
        List<Expense> expenseList = expensePage.getContent();
        return getExpensePagedResponse(expensePage, expenseList);
    }


    private ExpensePagedResponse getExpenseResponse(Page<Expense> expensePage) {
        List<Expense> expenses = expensePage.getContent();

        if (expenses.isEmpty()) {
            throw new ResourceNotFoundException("No expenses found!");
        }

        return getExpensePagedResponse(expensePage, expenses);
    }

    private ExpensePagedResponse getExpensePagedResponse(Page<Expense> expensePage, List<Expense> expenses) {
        List<ExpenseResponse> response = expenses.stream()
                .map(expense -> ExpenseResponse.builder()
                        .id(expense.getId())
                        .title(expense.getTitle())
                        .amount(expense.getAmount())
                        .expenseDate(expense.getExpenseDate())
                        .category(expense.getCategory().getName())
                        .status(expense.getStatus())
                        .message(expense.getMessage())
                        .build())
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

    private Approval covertStatus(String status) {
        if(status == null) return null;
        if (!statusMap.containsKey(status.toLowerCase())) {
            throw new ApiException("Invalid approval status: " + status);
        }
        return statusMap.get(status.toLowerCase());
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
