package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.dto.InvoiceDTO;
import com.team7.enterpriseexpensemanagementsystem.entity.*;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.InvoiceRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.*;
import com.team7.enterpriseexpensemanagementsystem.specification.InvoiceSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.pdfGeneration.ByteArrayMultipartFile;
import com.team7.enterpriseexpensemanagementsystem.utils.pdfGeneration.PdfExportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Override
    public void generateInvoice(User user, Expense expense) {
        Invoice invoice = invoiceRepository
                .findByUserIdAndStatus(user.getId(), InvoiceStatus.IN_PROGRESS);

        if (invoice == null) {
            invoice = Invoice.builder()
                    .invoiceNumber(generateInvoiceNumber())
                    .generatedAt(LocalDateTime.now())
                    .user(user)
                    .totalAmount(0d)
                    .expenses(new ArrayList<>())
                    .status(InvoiceStatus.IN_PROGRESS)
                    .build();
        }

        invoice.getExpenses().add(expense);

        double newTotal = invoice.getExpenses().stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        invoice.setTotalAmount(newTotal);

        invoiceRepository.save(invoice);
    }

    @Override
    public void generateInvoice(Long userId) {
        Invoice invoice = invoiceRepository.findByUserIdAndStatus(userId, InvoiceStatus.IN_PROGRESS);

        if (invoice.getInvoiceUrl() != null && !invoice.getInvoiceUrl().isEmpty()) {
            throw new ApiException("Operation failed: All invoices are already processed or approved.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfExportUtils.exportInvoice(invoice, outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            String customFileName = "invoice-" + invoice.getInvoiceNumber() + "-user-" + invoice.getUser().getId();

            MultipartFile multipartFile = new ByteArrayMultipartFile(
                    pdfBytes,
                    "file",
                    customFileName + ".pdf",
                    "application/pdf"
            );

            Map uploadResult = cloudinaryService.uploadInvoice(multipartFile, customFileName);

            String fileUrl = (String) uploadResult.get("secure_url");
            invoice.setInvoiceUrl(fileUrl);
            invoice.setInvoiceCloudId((String) uploadResult.get("public_id"));
            invoice.setStatus(InvoiceStatus.GENERATED);
            invoiceRepository.save(invoice);

            notificationService.saveNotification(new Notification("✅ Your invoice is ready. You can now view or download it."),
                    invoice.getUser().getId());
            System.out.println("Successfully uploaded invoice " + invoice.getId() + " to: " + fileUrl);
            emailService.sendInvoiceEmail(invoice, invoice.getUser().getEmail());
            System.out.println("Email sent to " + invoice.getUser().getEmail());

        } catch (IOException e) {
            throw new ApiException("Failed to generate or upload invoice PDF for ID " + invoice.getId(), e);
        }
    }

    @Override
    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        cloudinaryService.deleteInvoice(invoice.getInvoiceCloudId());
        invoiceRepository.delete(invoice);
    }

    @Override
    public Invoice regenerateInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        if(invoice.getInvoiceUrl() != null && !invoice.getInvoiceUrl().isEmpty()) {
            cloudinaryService.deleteInvoice(invoice.getInvoiceCloudId());
        }
        try {
            String customFileName = "invoice-" + invoice.getInvoiceNumber() + "-user-" + invoice.getUser().getId();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PdfExportUtils.exportInvoice(invoice, stream);
            byte[] pdfBytes = stream.toByteArray();

            MultipartFile multipartFile = new ByteArrayMultipartFile(
                    pdfBytes,
                    "file",
                    customFileName + ".pdf",
                    "application/pdf"
            );

            Map uploadResult = cloudinaryService.uploadInvoice(multipartFile, customFileName);

            String fileUrl = (String) uploadResult.get("secure_url");
            invoice.setInvoiceUrl(fileUrl);
            invoice.setInvoiceCloudId((String) uploadResult.get("public_id"));

            System.out.println("Successfully re-uploaded invoice " + invoice.getId() + " to: " + fileUrl);
        } catch (IOException e) {
            throw new ApiException("Failed to generate or upload invoice PDF for ID " + invoice.getId(), e);
        }
        return invoiceRepository.save(invoice);
    }

    public void sendInvoice(Long userId, Long InvoiceId) {
        Invoice invoice = getInvoiceById(InvoiceId);
        User user = invoice.getUser();

        if (userId != null && !invoice.getUser().getId().equals(userId)) {
           user = userRepository.findById(userId).orElseThrow(
                   () -> new ApiException("User not found with ID " + userId)
           );
        }

        emailService.sendInvoiceEmail(invoice, user.getEmail());
    }

    @Override
    public PagedResponse<InvoiceDTO> findAllInvoices(Long id, String email, String invoiceNumber, String status, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<Invoice> spec = Specification.where(InvoiceSpecification.byUserId(id))
                .and(InvoiceSpecification.byUserEmail(email))
                .and(InvoiceSpecification.byInvoiceNumber(invoiceNumber))
                .and(InvoiceSpecification.byStatus(status));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Invoice> invoicePage = invoiceRepository.findAll(spec, pageDetails);
        List<InvoiceDTO> invoiceList = invoicePage.getContent().stream().map(
                invoice -> {
                    UserResponse response = userService.getUserById(invoice.getUser().getId());
                    return InvoiceDTO.builder()
                            .id(invoice.getId())
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .generatedAt(invoice.getGeneratedAt())
                            .totalAmount(invoice.getTotalAmount())
                            .user(response)
                            .status(invoice.getStatus().toString())
                            .invoiceCloudId(invoice.getInvoiceCloudId())
                            .invoiceUrl(invoice.getInvoiceUrl())
                            .build();
                }
        ).toList();

        return PagedResponse.<InvoiceDTO>builder()
                .content(invoiceList)
                .pageNumber(invoicePage.getNumber())
                .pageSize(invoicePage.getSize())
                .totalElements(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .lastPage(invoicePage.isLast())
                .build();
    }

    @Override
    public List<ExpenseResponse> getExpenseList(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("Invoice not found"));
        List<Expense> expenses = invoice.getExpenses();

        return expenses.stream()
                .map(expense -> {
                    List<Approval> approvals = expense.getApprovals();
                    Approval latest = (approvals != null && !approvals.isEmpty())
                            ? approvals.get(approvals.size() - 1)
                            : null;

                    return getExpenseResponseWithLatestApproval(expense, latest);
                })
                .toList();
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("Invoice not found"));
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

    private static String generateInvoiceNumber() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "INV-" + uuid;
    }
}
