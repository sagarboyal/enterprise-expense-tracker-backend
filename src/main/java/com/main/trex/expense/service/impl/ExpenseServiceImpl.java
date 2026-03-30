package com.main.trex.expense.service.impl;

import com.main.trex.catalog.entity.Category;
import com.main.trex.catalog.repository.CategoryRepository;
import com.main.trex.expense.dto.ExpenseDTO;
import com.main.trex.expense.entity.Approval;
import com.main.trex.expense.entity.ApprovalLevel;
import com.main.trex.expense.entity.ApprovalStatus;
import com.main.trex.expense.entity.Expense;
import com.main.trex.expense.entity.FileDocument;
import com.main.trex.expense.entity.Invoice;
import com.main.trex.shared.exception.ApiException;
import com.main.trex.shared.exception.ResourceNotFoundException;
import com.main.trex.expense.payload.request.ApprovalRequest;
import com.main.trex.expense.payload.request.ExpenseUpdateRequest;
import com.main.trex.expense.payload.response.ExpenseResponse;
import com.main.trex.expense.repository.ApprovalRepository;
import com.main.trex.expense.repository.ExpenseRepository;
import com.main.trex.expense.repository.FileDocumentRepository;
import com.main.trex.expense.service.ExpenseService;
import com.main.trex.expense.service.InvoiceService;
import com.main.trex.identity.entity.User;
import com.main.trex.identity.repository.UserRepository;
import com.main.trex.shared.payload.response.PagedResponse;
import com.main.trex.notification.entity.Notification;
import com.main.trex.notification.service.NotificationService;
import com.main.trex.expense.specification.ExpenseSpecification;
import com.main.trex.identity.util.AuthUtils;
import com.main.trex.shared.util.ObjectMapperUtils;
import com.main.trex.shared.cloudinary.CloudinaryService;
import com.main.trex.support.audit.entity.AuditLog;
import com.main.trex.support.audit.service.AuditLogService;
import com.main.trex.expense.util.pdfGeneration.PdfExportUtils;
import com.main.trex.identity.util.UserUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private final FileDocumentRepository fileDocumentRepository;
    private final CloudinaryService cloudinaryService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, ModelMapper modelMapper, UserRepository userRepository, AuditLogService auditLogService, AuthUtils authUtils, ObjectMapperUtils objectMapperUtils, NotificationService notificationService, ApprovalRepository approvalRepository, InvoiceService invoiceService, FileDocumentRepository fileDocumentRepository, CloudinaryService cloudinaryService) {
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
        this.fileDocumentRepository = fileDocumentRepository;
        this.cloudinaryService = cloudinaryService;
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
        FileDocument document = fileDocumentRepository.findByExpenseId(expense.getId())
                .orElseThrow(() -> new ApiException("Expense with id: " + expense.getId() + " not found."));
        cloudinaryService.deleteInvoice(document.getImageId());
        fileDocumentRepository.delete(document);

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
                .userId(user.getId())
                .description(expense.getDescription())
                .fullName(expense.getUser().getFullName())
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
                .and(ExpenseSpecification.hasTitle(title))
                .and(ExpenseSpecification.excludeAdminApprovedIfManagerLevel(status, level))
                .and(ExpenseSpecification.onlyPendingForManagerLevel(status, level));


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
                return null;
            }catch(Exception e){
                System.out.println(e.getMessage());
            }

        return expenseResponseList;
    }

    private ApprovalStatus convertStatus(String status) {
        if (status == null || status.isEmpty()) return ApprovalStatus.PENDING;
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
                .fullName(expense.getUser().getFullName())
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


