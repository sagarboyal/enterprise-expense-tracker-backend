package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.dto.MonthlyExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.entity.*;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.CategoryRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import com.team7.enterpriseexpensemanagementsystem.service.NotificationService;
import com.team7.enterpriseexpensemanagementsystem.specification.ExpenseSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.ObjectMapperUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.UserUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
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
    private final AuditLogService auditLogService;
    private final AuthUtils authUtils;
    private final ObjectMapperUtils mapperUtils;
    private final NotificationService notificationService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, ModelMapper modelMapper, UserRepository userRepository, AuditLogService auditLogService, AuthUtils authUtils, ObjectMapperUtils objectMapperUtils, NotificationService notificationService) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.authUtils = authUtils;
        this.mapperUtils = objectMapperUtils;
        this.notificationService = notificationService;
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
        ExpenseResponse response = ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(category.getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();

        auditLogService.log(AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .action("CREATED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(response))
                .build());

        return response;
    }

    @Override
    public ExpenseResponse updateExpense(ExpenseUpdateRequest dto) {
        Expense expense = getExpenseById(dto.getId());
        Category category = dto.getCategoryId() != null ? getCategoryById(dto.getCategoryId()) : expense.getCategory();
        AuditLog auditLog = AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .action("UPDATED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue(mapperUtils.convertToJson(ExpenseResponse.builder()
                        .id(expense.getId())
                        .title(expense.getTitle())
                        .amount(expense.getAmount())
                        .expenseDate(expense.getExpenseDate())
                        .category(category.getName())
                        .status(expense.getStatus())
                        .message(expense.getMessage())
                        .build()))
                .newValue("")
                .build();

        expense.setTitle(dto.getTitle() != null ? dto.getTitle() : expense.getTitle());
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : expense.getExpenseDate());
        expense.setAmount(dto.getAmount() != null ? dto.getAmount() : expense.getAmount());
        expense.setCategory(category);
        expense = expenseRepository.save(expense);

        ExpenseResponse response = ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(category.getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();
        auditLog.setNewValue(mapperUtils.convertToJson(response));
        auditLogService.log(auditLog);
        return response;
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        Category category = expense.getCategory();
        expenseRepository.delete(expense);
        auditLogService.log(AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .action("DELETED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(ExpenseResponse.builder()
                        .id(expense.getId())
                        .title(expense.getTitle())
                        .amount(expense.getAmount())
                        .expenseDate(expense.getExpenseDate())
                        .category(category.getName())
                        .status(expense.getStatus())
                        .message(expense.getMessage())
                        .build()))
                .build());
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
        AuditLog auditLog = AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .performedBy(authUtils.loggedInEmail())
                .oldValue(mapperUtils.convertToJson(ExpenseResponse.builder()
                        .id(expense.getId())
                        .title(expense.getTitle())
                        .amount(expense.getAmount())
                        .expenseDate(expense.getExpenseDate())
                        .category(expense.getCategory().getName())
                        .status(expense.getStatus())
                        .message(expense.getMessage())
                        .build()))
                .newValue("")
                .build();

        Approval status = resolveApprovalStatus(user, approvalRequest.getDecision());
        String message = approvalRequest.getMessage() != null
                && !approvalRequest.getMessage().trim().isEmpty()
                ? approvalRequest.getMessage() :
                status.getMessage();


        expense.setStatus(status);
        expense.setMessage(message);

        expense = expenseRepository.save(expense);
        ExpenseResponse response = ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory().getName())
                .status(expense.getStatus())
                .message(expense.getMessage())
                .build();
        auditLog.setAction(approvalRequest.getDecision().equalsIgnoreCase("approve")
                ? "APPROVED" : "REJECTED");
        auditLog.setNewValue(mapperUtils.convertToJson(response));
        auditLogService.log(auditLog);
        notificationService.saveNotification(
                new Notification(approvalRequest.getDecision().equalsIgnoreCase("approve")?
                        user.getRoles().contains(new Role(Roles.ROLE_ADMIN))?
                        "Congratulations! Your expense with id: "+expense.getId()+" successfully approved by Admin😁.":
                                "Congratulations! Your expense with id: "+expense.getId()+" successfully approved " +
                                        "by manager please wait for admin approval🤩.":
                        "Oops! sorry  Your expense with id: "+expense.getId()+" got rejected 💔."),
                user.getId()
        );
        return response;
    }

    @Override
    public PagedResponse<ExpenseResponse> getFilteredExpenses(String categoryName, String status, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Long userId,
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

    @Override
    public List<MonthlyExpenseDTO> getMonthlyAnalytics(Long id, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = expenseRepository.getMonthlyExpenseTotals(id, startDate, endDate);
        return data.stream().map(obj -> {
            int monthNumber = (int) obj[0];
            String monthName = Month.of(monthNumber).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            Double total = (Double) obj[1];
            return new MonthlyExpenseDTO(monthName, total);
        }).toList();
    }


    private PagedResponse<ExpenseResponse> getExpenseResponse(Page<Expense> expensePage) {
        List<Expense> expenses = expensePage.getContent();

        if (expenses.isEmpty()) {
            throw new ResourceNotFoundException("No expenses found!");
        }

        return getExpensePagedResponse(expensePage, expenses);
    }

    private PagedResponse<ExpenseResponse> getExpensePagedResponse(Page<Expense> expensePage, List<Expense> expenses) {
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

        return PagedResponse.<ExpenseResponse>builder()
                .content(response)
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
