package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.dto.*;
import com.team7.enterpriseexpensemanagementsystem.entity.*;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.ApprovalRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.CategoryRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import com.team7.enterpriseexpensemanagementsystem.service.InvoiceService;
import com.team7.enterpriseexpensemanagementsystem.service.NotificationService;
import com.team7.enterpriseexpensemanagementsystem.specification.ExpenseSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.ObjectMapperUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.PdfExportUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.UserUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthUtils authUtils;
    private final ObjectMapperUtils mapperUtils;
    private final NotificationService notificationService;
    private final ApprovalRepository approvalRepository;
    private final InvoiceService invoiceService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, ModelMapper modelMapper, UserRepository userRepository, AuditLogService auditLogService, AuthUtils authUtils, ObjectMapperUtils objectMapperUtils, NotificationService notificationService, ApprovalRepository approvalRepository, InvoiceService invoiceService) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.authUtils = authUtils;
        this.mapperUtils = objectMapperUtils;
        this.notificationService = notificationService;
        this.approvalRepository = approvalRepository;
        this.invoiceService = invoiceService;
    }

    @Override
    public ExpenseResponse addExpense(ExpenseDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User is not logged in. Please log in to continue."));

        Category category = getCategoryById(dto.getCategoryId());
        Expense expense = modelMapper.map(dto, Expense.class);
        expense.setCategory(category);
        expense.setUser(user);
        expense.setDescription(dto.getDescription());
        Approval approvalRecords = Approval.builder()
                .userId(user.getId())
                .level(ApprovalLevel.MANAGER)
                .status(ApprovalStatus.PENDING)
                .actionTime(LocalDateTime.now())
                .comment("Wait for manager approval!")
                .build();

        if(dto.getExpenseDate() == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(LocalDate.now().format(formatter));
            expense.setExpenseDate(localDate);
        }

        approvalRecords.setExpense(expense);
        expense.getApprovals().add(approvalRecords);
        expense = expenseRepository.save(expense); // Only one save


        ExpenseResponse response = ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(category.getName())
                .status(approvalRecords.getStatus())
                .level(approvalRecords.getLevel())
                .message(approvalRecords.getComment())
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

        List<Approval> approvals = expense.getApprovals();
        Approval latest = approvals != null && !approvals.isEmpty()
                ? approvals.get(approvals.size() - 1)
                : null;

        AuditLog auditLog = AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .action("UPDATED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue(mapperUtils.convertToJson(getExpenseResponseWithLatestApproval(expense, latest)))
                .newValue("")
                .build();

        expense.setTitle(dto.getTitle() != null ? dto.getTitle() : expense.getTitle());
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : expense.getExpenseDate());
        expense.setAmount(dto.getAmount() != null ? dto.getAmount() : expense.getAmount());
        expense.setDescription(dto.getDescription() != null ? dto.getDescription() : expense.getDescription());
        expense.setCategory(category);
        expense = expenseRepository.save(expense);

        ExpenseResponse response = getExpenseResponseWithLatestApproval(expense, latest);
        auditLog.setNewValue(mapperUtils.convertToJson(response));
        auditLogService.log(auditLog);
        return response;
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        Category category = expense.getCategory();

        List<Approval> approvals = expense.getApprovals();
        Approval latest = approvals != null && !approvals.isEmpty()
                ? approvals.get(approvals.size() - 1)
                : null;

        expenseRepository.delete(expense);
        auditLogService.log(AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .action("DELETED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(getExpenseResponseWithLatestApproval(expense, latest)))
                .build());
    }

    @Override
    public ExpenseResponse getExpenseByExpenseId(Long id) {
        Expense expense = getExpenseById(id);

        List<Approval> approvals = expense.getApprovals();
        Approval latest = approvals != null && !approvals.isEmpty()
                ? approvals.get(approvals.size() - 1)
                : null;

        return getExpenseResponseWithLatestApproval(expense, latest);
    }

    @Override
    public ExpenseResponse updateExpenseStatus(Long id, ApprovalRequest approvalRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User is not logged in. Please log in to continue."));
        Expense expense = getExpenseById(id);

        List<Approval> approvals = expense.getApprovals();
        Approval latest = approvals != null && !approvals.isEmpty()
                ? approvals.get(approvals.size() - 1)
                : null;

        AuditLog auditLog = AuditLog.builder()
                .entityName("expense")
                .entityId(expense.getId())
                .performedBy(authUtils.loggedInEmail())
                .oldValue(mapperUtils.convertToJson(getExpenseResponseWithLatestApproval(expense, latest)))
                .newValue("")
                .build();

        Approval approval = resolveApprovalStatus(user, expense, approvalRequest.getDecision());
        if(approvalRequest.getMessage() != null) {
            approval.setComment(approvalRequest.getMessage());
        }
        approval = approvalRepository.save(approval);

        expense.getApprovals().add(approval);
        expense = expenseRepository.save(expense);

        approvals = expense.getApprovals();
        latest = approvals != null && !approvals.isEmpty()
                ? approvals.get(approvals.size() - 1)
                : null;

        ExpenseResponse response = ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory().getName())
                .status(latest != null ? latest.getStatus() : null)
                .level(latest != null ? latest.getLevel() : null)
                .message(latest != null ? latest.getComment() : null)
                .build();

        auditLog.setAction(approvalRequest.getDecision().equalsIgnoreCase("approve")
                ? "APPROVED" : "REJECTED");
        auditLog.setNewValue(mapperUtils.convertToJson(response));
        auditLogService.log(auditLog);

        String message;
        if (approvalRequest.getDecision().equalsIgnoreCase("approve")) {
            if (UserUtils.isAdmin(user)) {
                invoiceService.generateInvoice(expense.getUser(), expense);
                message = "🎉 Congratulations! Your expense with ID " + expense.getId() + " has been approved by Admin.";
            } else if (UserUtils.isManager(user)) {
                message = "✅ Your expense with ID " + expense.getId() + " has been approved by Manager. Please wait for Admin approval.";
            } else {
                message = "✅ Your expense with ID " + expense.getId() + " has been approved.";
            }
        } else {
            message = "❌ Oops! Your expense with ID " + expense.getId() + " was rejected.";
        }
        notificationService.saveNotification(new Notification(message), expense.getUser().getId());

        return response;
    }

    @Override
    public PagedResponse<ExpenseResponse> getFilteredExpenses(String title, String categoryName, String status, String level, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Long userId,
                                             Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,
                                                              Boolean export, HttpServletResponse response) {
        Specification<Expense> specs = Specification.where(ExpenseSpecification.hasCategory(categoryName))
                .and(ExpenseSpecification.hasStatus(convertStatus(status)))
                .and(ExpenseSpecification.hasLevel(convertLevel(level)))
                .and(ExpenseSpecification.expenseDateBetween(startDate, endDate))
                .and(ExpenseSpecification.amountBetween(minAmount, maxAmount))
                .and(ExpenseSpecification.user(userId))
                .and(ExpenseSpecification.hasTitle(title));



        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Expense> expensePage = expenseRepository.findAll(specs, pageDetails);
        List<Expense> expenseList = expensePage.getContent();
        PagedResponse<ExpenseResponse> expenseResponseList = getExpensePagedResponse(expensePage, expenseList);
        if(export)
            try{
                List<Expense> allFilteredExpenses = expenseRepository.findAll(specs);
                List<ExpenseResponse> responseList = allFilteredExpenses.stream().map(
                        expense ->{
                            List<Approval> approvals = expense.getApprovals();
                            Approval latest = approvals != null && !approvals.isEmpty()
                                    ? approvals.get(approvals.size() - 1)
                                    : null;
                            return getExpenseResponseWithLatestApproval(expense, latest);
                        }
                ).toList();
                exportFilteredExpenses(responseList, response);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }


        return expenseResponseList;
    }

    private ApprovalStatus convertStatus(String status) {
        if (status == null || status.isEmpty()) return ApprovalStatus.PENDING;;
        System.out.println("Filtering by status: " + status);

        return switch (status.toLowerCase()) {
            case "approved" -> ApprovalStatus.APPROVED;
            case "rejected" -> ApprovalStatus.REJECTED;
            default -> ApprovalStatus.PENDING;
        };
    }

    private ApprovalLevel convertLevel(String level) {
        if (level == null || level.isEmpty()) return null;
        return switch (level.toLowerCase()) {
            case "manager" -> ApprovalLevel.MANAGER;
            case "admin" -> ApprovalLevel.ADMIN;
            default -> null;
        };
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

    @Override
    public List<CategoryExpenseDTO> getCategoryAnalytics(Long id, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = expenseRepository.getCategoryExpenseTotals(id, startDate, endDate);
        return data.stream()
                .map(obj -> new CategoryExpenseDTO((String) obj[0], (Double) obj[1]))
                .toList();
    }

    @Override
    public List<StatusExpenseDTO> getStatusAnalytics(Long id, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = expenseRepository.getStatusAnalytics(id, startDate, endDate);

        return rawData.stream()
                .map(obj -> new StatusExpenseDTO(
                        obj[0].toString(),                  // or cast to (Approval) if using enum
                        ((Number) obj[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public SummaryDTO getSummary(Long id) {
        Double totalExpenses = expenseRepository.getTotalExpensesByUser(id);
        totalExpenses = (totalExpenses == null) ? 0.0 : totalExpenses;

        LocalDate now = LocalDate.now();

        Long approvedCount = expenseRepository.countApprovedExpenses(id);
        Long pendingCount = expenseRepository.countPendingApprovals(id);
        Long rejectedCount = expenseRepository.countRejectedExpenses(id);

        List<StatusExpenseDTO> rawStatusList = getStatusAnalytics(id, now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));

        // then just pass the status string directly
        List<StatusExpenseDTO> mappedStatusList = rawStatusList.stream()
                .map(dto -> new StatusExpenseDTO(
                        dto.getStatus(),  // already string
                        dto.getTotal()
                ))
                .collect(Collectors.toList());


        // Build and return combined DTO
        return new SummaryDTO(totalExpenses, approvedCount, pendingCount, rejectedCount, mappedStatusList);
    }

    @Override
    public void exportFilteredExpenses(
            List<ExpenseResponse> expenseList,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=expense_report.pdf");

        PdfExportUtils.exportExpenses("Expense Report", expenseList, response.getOutputStream());
    }

    @Override
    public List<ExpenseDTO> saveAll(List<ExpenseDTO> expenseDTOs) {
        List<Expense> expenses = expenseDTOs.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());

        List<Expense> savedExpenses = expenseRepository.saveAll(expenses);

        return savedExpenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Approval> getApprovalStack(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).
                orElseThrow(() -> new ApiException("Expense not found"));
        return expense.getApprovals();
    }

    @Override
    public List<WeeklyExpenseDTO> getWeeklyAnalytics() {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

            List<Object[]> results = expenseRepository.findTotalByDayOfWeek(startOfWeek, today);

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
                    .collect(Collectors.toList());
    }

    private Expense mapToEntity(ExpenseDTO dto) {
        Category category = getCategoryById(dto.getCategoryId());
        Expense expense = modelMapper.map(dto, Expense.class);
        expense.setCategory(category);
        expense.setUser(authUtils.loggedInUser());
        expense.setDescription(dto.getDescription());

        Approval approvalRecords = Approval.builder()
                .userId(expense.getUser().getId())
                .level(ApprovalLevel.MANAGER)
                .status(ApprovalStatus.PENDING)
                .actionTime(LocalDateTime.now())
                .comment("Wait for manager approval!")
                .build();
        approvalRecords.setExpense(expense);
        expense.getApprovals().add(approvalRecords);
        return expenseRepository.save(expense);
    }

    private PagedResponse<ExpenseResponse> getExpensePagedResponse(Page<Expense> expensePage, List<Expense> expenses) {
        List<ExpenseResponse> response = expenses.stream()
                .map(expense -> {
                    List<Approval> approvals = expense.getApprovals();
                    Approval latest = (approvals != null && !approvals.isEmpty())
                            ? approvals.get(approvals.size() - 1)
                            : null;

                    return getExpenseResponseWithLatestApproval(expense, latest);
                })
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

    private ExpenseResponse getExpenseResponseWithLatestApproval(Expense expense, Approval latest) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .userId(expense.getUser().getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .category(expense.getCategory().getName())
                .status(latest != null ? latest.getStatus() : null)
                .level(latest != null ? latest.getLevel() : null)
                .message(latest != null ? latest.getComment() : null)
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

    private Approval resolveApprovalStatus(User user, Expense expense, String decision) {
        boolean isApproved = decision.equalsIgnoreCase("approve");

        Approval approval = Approval.builder()
                .userId(user.getId())
                .expense(expense)
                .actionTime(LocalDateTime.now())
                .status(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED)
                .build();

        List<Approval> approvals = expense.getApprovals();
        Approval latest = (approvals != null && !approvals.isEmpty())
                ? approvals.get(approvals.size() - 1)
                : null;

        if (latest != null && latest.getStatus() == ApprovalStatus.APPROVED && latest.getLevel() == ApprovalLevel.ADMIN) {
            throw new ApiException("Expense has already been approved by admin.");
        }

        if (UserUtils.isAdmin(user)) {
            if (latest != null && latest.getStatus() == ApprovalStatus.REJECTED) {
                throw new ApiException("Expense approval is already rejected.");
            } else if (latest != null && (latest.getLevel() != ApprovalLevel.MANAGER || latest.getStatus() != ApprovalStatus.APPROVED)) {
                throw new ApiException("Manager approval is required before admin can approve.");
            }

            approval.setLevel(ApprovalLevel.ADMIN);
            approval.setComment(isApproved
                    ? "Final review passed. Approved by admin."
                    : "Final approval denied. Expense not aligned with financial rules.");
        } else if (UserUtils.isManager(user)) {
            if (latest != null && latest.getStatus() == ApprovalStatus.REJECTED) {
                throw new ApiException("Expense approval is already rejected.");
            } else if (latest != null && (latest.getLevel() == ApprovalLevel.MANAGER && latest.getStatus() == ApprovalStatus.APPROVED)){
                throw new ApiException("Manager cannot reject this expense. It's either already reviewed or not in your level.");
            }

            approval.setLevel(ApprovalLevel.MANAGER);
            approval.setComment(isApproved
                    ? "Initial verification successful. Forwarded to admin for final approval."
                    : "Rejected for exceeding department-level budget.");
        } else {
            throw new ApiException("User has no valid approval role.");
        }

        return approval;
    }
}
