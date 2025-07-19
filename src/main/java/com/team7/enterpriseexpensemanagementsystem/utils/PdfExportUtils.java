package com.team7.enterpriseexpensemanagementsystem.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.team7.enterpriseexpensemanagementsystem.entity.Approval;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PdfExportUtils {

    private static final Color COLOR_PRIMARY = new Color(79, 70, 229);
    private static final Color COLOR_TEXT_DARK = new Color(17, 24, 39);
    private static final Color COLOR_TEXT_MEDIUM = new Color(75, 85, 99);
    private static final Color COLOR_TEXT_LIGHT = new Color(156, 163, 175);
    private static final Color COLOR_BACKGROUND_TABLE_HEADER = new Color(243, 244, 246);

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COLOR_TEXT_DARK);
    private static final Font FONT_HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_TEXT_DARK);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT_MEDIUM);
    private static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_DARK);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_LIGHT);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXT_MEDIUM);

    public static void exportInvoice(Invoice invoice, OutputStream outputStream) throws IOException {
        if (invoice == null) {
            throw new ApiException("Invoice not found!.");
        }

        try (Document document = new Document(PageSize.A4, 36, 36, 36, 54)) {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            addHeader(document, invoice);
            addBillToInfo(document, invoice);
            addExpenseTable(document, invoice);
            addTotals(document, invoice);
            addFooter(writer);

            if ("PAID".equalsIgnoreCase(invoice.getStatus().name())) {
                addPaidStamp(writer);
            }

        } catch (DocumentException e) {
            throw new IOException("Error generating invoice PDF", e);
        }
    }

    private static void addHeader(Document document, Invoice invoice) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        Paragraph companyName = new Paragraph("Trex Corp.", FONT_HEADING);
        companyName.add(new Chunk("\n123 Innovation Drive, Tech City", FONT_BODY));
        PdfPCell companyCell = new PdfPCell(companyName);
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(companyCell);

        Paragraph invoiceTitle = new Paragraph("INVOICE", FONT_TITLE);
        invoiceTitle.setAlignment(Element.ALIGN_RIGHT);
        invoiceTitle.add(new Chunk("\n#" + invoice.getInvoiceNumber(), FONT_BODY_BOLD));
        PdfPCell titleCell = new PdfPCell(invoiceTitle);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(titleCell);

        document.add(headerTable);
        addSpacer(document, 20f);
    }

    private static void addBillToInfo(Document document, Invoice invoice) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell billToCell = new PdfPCell();
        billToCell.setBorder(Rectangle.NO_BORDER);
        billToCell.addElement(new Paragraph("BILL TO", FONT_LABEL));
        billToCell.addElement(new Paragraph(invoice.getUser().getFullName(), FONT_BODY_BOLD));
        billToCell.addElement(new Paragraph(invoice.getUser().getEmail(), FONT_BODY));
        infoTable.addCell(billToCell);

        PdfPCell datesCell = new PdfPCell();
        datesCell.setBorder(Rectangle.NO_BORDER);
        datesCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph issueDate = new Paragraph();
        issueDate.setAlignment(Element.ALIGN_RIGHT);
        issueDate.add(new Chunk("Issue Date\n", FONT_LABEL));
        issueDate.add(new Chunk(formatDate(invoice.getGeneratedAt().toLocalDate()), FONT_BODY_BOLD));
        datesCell.addElement(issueDate);

        datesCell.addElement(new Paragraph("\n"));

        Paragraph status = new Paragraph();
        status.setAlignment(Element.ALIGN_RIGHT);
        status.add(new Chunk("Status\n", FONT_LABEL));
        status.add(new Chunk(invoice.getStatus().name(), FONT_BODY_BOLD));
        datesCell.addElement(status);

        infoTable.addCell(datesCell);

        document.add(infoTable);
        addSpacer(document, 25f);
    }

    private static void addExpenseTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f, 1.5f});
        table.setHeaderRows(1);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        addTableHeaderCell(table, "ITEM DESCRIPTION");
        addTableHeaderCell(table, "CATEGORY", Element.ALIGN_CENTER);
        addTableHeaderCell(table, "DATE", Element.ALIGN_CENTER);
        addTableHeaderCell(table, "STATUS", Element.ALIGN_CENTER);
        addTableHeaderCell(table, "AMOUNT", Element.ALIGN_RIGHT);

        for (Expense expense : invoice.getExpenses()) {
            addTableCell(table, expense.getTitle());
            addTableCell(table, expense.getCategory().getName(), Element.ALIGN_CENTER);
            addTableCell(table, formatDate(expense.getExpenseDate()), Element.ALIGN_CENTER);
            addTableCell(table, getHighestApprovalStatus(expense), Element.ALIGN_CENTER);
            addTableCell(table, formatCurrency(expense.getAmount()), Element.ALIGN_RIGHT);
        }

        document.add(table);
    }

    private static void addTotals(Document document, Invoice invoice) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[]{1, 1});
        totalsTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        addSpacer(document, 10f);

        totalsTable.addCell(createTotalsCell("Subtotal", FONT_BODY, Element.ALIGN_LEFT));
        totalsTable.addCell(createTotalsCell(formatCurrency(invoice.getTotalAmount()), FONT_BODY, Element.ALIGN_RIGHT));

        totalsTable.addCell(createTotalsCell("Tax (0%)", FONT_BODY, Element.ALIGN_LEFT));
        totalsTable.addCell(createTotalsCell(formatCurrency(0.0), FONT_BODY, Element.ALIGN_RIGHT));

        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setBorder(Rectangle.NO_BORDER);
        separatorCell.setColspan(2);
        separatorCell.setPaddingTop(8f);
        separatorCell.setPaddingBottom(8f);
        separatorCell.setBorder(Rectangle.TOP);
        totalsTable.addCell(separatorCell);

        totalsTable.addCell(createTotalsCell("Total", FONT_BODY_BOLD, Element.ALIGN_LEFT));
        totalsTable.addCell(createTotalsCell(formatCurrency(invoice.getTotalAmount()), FONT_BODY_BOLD, Element.ALIGN_RIGHT));

        document.add(totalsTable);
    }

    private static void addFooter(PdfWriter writer) throws DocumentException {
        Paragraph footerText = new Paragraph("Thank you for your business!", FONT_BODY);
        footerText.add(new Chunk("\nFor any questions, contact us at support@trex-corp.com", FONT_LABEL));
        footerText.setAlignment(Element.ALIGN_CENTER);

        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setTotalWidth(PageSize.A4.getWidth() - 72);
        PdfPCell cell = new PdfPCell(footerText);
        cell.setBorder(Rectangle.TOP);
        cell.setPaddingTop(10f);
        cell.setBorderColor(COLOR_BACKGROUND_TABLE_HEADER);
        footerTable.addCell(cell);

        footerTable.writeSelectedRows(0, -1, 36, 54, writer.getDirectContent());
    }

    private static void addTableHeaderCell(PdfPTable table, String text) {
        addTableHeaderCell(table, text, Element.ALIGN_LEFT);
    }

    private static void addTableHeaderCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEADER));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(COLOR_BACKGROUND_TABLE_HEADER);
        cell.setPadding(10f);
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(COLOR_BACKGROUND_TABLE_HEADER);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text) {
        addTableCell(table, text, Element.ALIGN_LEFT);
    }

    private static void addTableCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(COLOR_BACKGROUND_TABLE_HEADER);
        cell.setPadding(8f);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private static PdfPCell createTotalsCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private static void addSpacer(Document document, float height) throws DocumentException {
        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(height);
        document.add(spacer);
    }

    private static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private static String formatCurrency(Double amount) {
        if (amount == null) return "₹0.00";
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(amount);
    }

    private static String getHighestApprovalStatus(Expense expense) {
        if (expense.getApprovals() == null || expense.getApprovals().isEmpty()) {
            return "Pending";
        }
        boolean adminApproved = expense.getApprovals().stream()
                .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getLevel().name()) && "APPROVED".equals(a.getStatus().name()));
        if (adminApproved) return "Approved";

        boolean managerApproved = expense.getApprovals().stream()
                .anyMatch(a -> "MANAGER".equalsIgnoreCase(a.getLevel().name()) && "APPROVED".equals(a.getStatus().name()));
        if (managerApproved) return "Manager Approved";

        return "Pending";
    }

    private static void addPaidStamp(PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContent();
        Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 52, new Color(0, 128, 0, 40));
        Phrase stamp = new Phrase("PAID", stampFont);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, stamp, 297, 421, 45);
    }

    public static void exportExpenses(String headingTitle, List<ExpenseResponse> expenseList, OutputStream outputStream) throws IOException {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Paragraph title = new Paragraph(headingTitle, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);
            Paragraph exportedOn = new Paragraph("Exported on: " + LocalDate.now(), cellFont);
            exportedOn.setAlignment(Element.ALIGN_RIGHT);
            exportedOn.setSpacingAfter(10f);
            document.add(exportedOn);
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{2f, 2.5f, 1.5f, 2f, 2f, 2f, 2f, 3f});
            addHeaderCell(table, "Expense ID", headerFont);
            addHeaderCell(table, "Title", headerFont);
            addHeaderCell(table, "Amount", headerFont);
            addHeaderCell(table, "Date", headerFont);
            addHeaderCell(table, "Category", headerFont);
            addHeaderCell(table, "Status", headerFont);
            addHeaderCell(table, "Level", headerFont);
            addHeaderCell(table, "Message", headerFont);
            boolean alternate = false;
            Color rowColor1 = new Color(245, 245, 245);
            Color rowColor2 = new Color(255, 255, 255);
            for (ExpenseResponse expense : expenseList) {
                Color bgColor = alternate ? rowColor1 : rowColor2;
                addDataCell(table, String.valueOf(expense.getId()), cellFont, bgColor);
                addDataCell(table, expense.getTitle(), cellFont, bgColor);
                addDataCell(table, String.valueOf(expense.getAmount()), cellFont, bgColor);
                addDataCell(table, String.valueOf(expense.getExpenseDate()), cellFont, bgColor);
                addDataCell(table, expense.getCategory(), cellFont, bgColor);
                addDataCell(table, expense.getStatus() != null ? expense.getStatus().name() : "N/A", cellFont, bgColor);
                addDataCell(table, expense.getLevel() != null ? expense.getLevel().name() : "N/A", cellFont, bgColor);
                addDataCell(table, expense.getMessage() != null ? expense.getMessage() : "—", cellFont, bgColor);
                alternate = !alternate;
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(63, 81, 181));
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private static void addDataCell(PdfPTable table, String text, Font font, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
