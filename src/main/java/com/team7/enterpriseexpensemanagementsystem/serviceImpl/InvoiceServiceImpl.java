package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.*;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.InvoiceRepository;
import com.team7.enterpriseexpensemanagementsystem.service.EmailService;
import com.team7.enterpriseexpensemanagementsystem.service.InvoiceService;
import com.team7.enterpriseexpensemanagementsystem.specification.InvoiceSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.PdfExportUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

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

        invoice.setStatus(InvoiceStatus.GENERATED);
        invoiceRepository.save(invoice);
    }

    @Override
    public Invoice exportInvoice(Long userId, HttpServletResponse response) {
        Invoice invoice = invoiceRepository.findByUserIdAndStatus(userId, InvoiceStatus.IN_PROGRESS);
        if (invoice == null)
            invoice = invoiceRepository
                    .findTopByUserIdAndStatusOrderByGeneratedAtDesc(userId, InvoiceStatus.GENERATED)
                    .orElseThrow(() -> new ApiException("No invoice found"));

        try {
            generatePdfStream(response, invoice);

            invoice.setStatus(InvoiceStatus.GENERATED);
            invoice = invoiceRepository.save(invoice);

            return invoice;

        } catch (IOException e) {
            throw new ApiException("Error generating PDF");
        }
    }

    public void sendInvoice(Long userId, HttpServletResponse response) {
        Invoice invoice = exportInvoice(userId, response);

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
    try{
        PdfExportUtils.exportInvoice(invoice, pdfStream);
    } catch (IOException e) {
        throw new ApiException("Error generating PDF");
    }
        emailService.sendInvoiceEmail(invoice, pdfStream.toByteArray());
    }

    @Override
    public PagedResponse<Invoice> findAllInvoices(Long id, String email, String invoiceNumber, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<Invoice> spec = Specification.where(InvoiceSpecification.byUserId(id))
                .and(InvoiceSpecification.byUserEmail(email))
                .and(InvoiceSpecification.byInvoiceNumber(invoiceNumber));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Invoice> invoicePage = invoiceRepository.findAll(spec, pageDetails);
        List<Invoice> invoiceList = invoicePage.getContent();

        return PagedResponse.<Invoice>builder()
                .content(invoiceList)
                .pageNumber(invoicePage.getNumber())
                .pageSize(invoicePage.getSize())
                .totalElements(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .lastPage(invoicePage.isLast())
                .build();
    }

    @Override
    public byte[] exportInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("Invoice not found with ID: " + invoiceId));

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        try {
            PdfExportUtils.exportInvoice(invoice, pdfStream);
        } catch (IOException e) {
            throw new ApiException("Error generating PDF for invoice ID: " + invoiceId);
        }

        return pdfStream.toByteArray();
    }

    public void streamInvoicePdfById(Long invoiceId, HttpServletResponse response, boolean asInline) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("Invoice not found"));

        try {
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            PdfExportUtils.exportInvoice(invoice, pdfStream);
            byte[] pdfBytes = pdfStream.toByteArray();

            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");

            String dispositionType = asInline ? "inline" : "attachment";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "invoice-" + invoice.getInvoiceNumber() + "-" + timestamp + ".pdf";
            response.setHeader("Content-Disposition", dispositionType + "; filename=" + filename);

            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (IOException e) {
            throw new ApiException("Error generating invoice PDF");
        }
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


    private void generatePdfStream(HttpServletResponse response, Invoice invoice) throws IOException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        PdfExportUtils.exportInvoice(invoice, pdfStream);

        byte[] pdfBytes = pdfStream.toByteArray();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "invoice-" + invoice.getInvoiceNumber() + "-" + timestamp + ".pdf";

        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    private static String generateInvoiceNumber() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "INV-" + uuid;
    }
}
