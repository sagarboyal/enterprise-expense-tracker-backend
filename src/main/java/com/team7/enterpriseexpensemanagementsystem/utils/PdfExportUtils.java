package com.team7.enterpriseexpensemanagementsystem.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.team7.enterpriseexpensemanagementsystem.entity.Approval;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportUtils {

    public static void exportExpenses(String headingTitle, List<ExpenseResponse> expenseList, OutputStream outputStream) throws IOException {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph(headingTitle, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);

            // Export date
            Paragraph exportedOn = new Paragraph("Exported on: " + LocalDate.now(), cellFont);
            exportedOn.setAlignment(Element.ALIGN_RIGHT);
            exportedOn.setSpacingAfter(10f);
            document.add(exportedOn);

            // Table with 8 columns (added Message column)
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{2f, 2.5f, 1.5f, 2f, 2f, 2f, 2f, 3f});

            // Header row
            addHeaderCell(table, "Expense ID", headerFont);
            addHeaderCell(table, "Title", headerFont);
            addHeaderCell(table, "Amount", headerFont);
            addHeaderCell(table, "Date", headerFont);
            addHeaderCell(table, "Category", headerFont);
            addHeaderCell(table, "Status", headerFont);
            addHeaderCell(table, "Level", headerFont);
            addHeaderCell(table, "Message", headerFont);

            // Data rows
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
    public static void exportInvoice(Invoice invoice, OutputStream outputStream) throws IOException {
        try (Document document = new Document(PageSize.A4, 36, 36, 36, 36)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            Paragraph company = new Paragraph("Trex Expense Manager", labelFont);
            company.setAlignment(Element.ALIGN_TOP);
            company.setSpacingAfter(10f);
            document.add(company);

            // ---------- Header ----------
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);
            document.add(title);

            // ---------- Invoice Info ----------
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15f);

            infoTable.addCell(getInfoCell("Invoice No:", labelFont));
            infoTable.addCell(getInfoCell(invoice.getInvoiceNumber(), normalFont, Element.ALIGN_RIGHT));

            infoTable.addCell(getInfoCell("Issued At:", labelFont));
            infoTable.addCell(getInfoCell(invoice.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), normalFont, Element.ALIGN_RIGHT));

            infoTable.addCell(getInfoCell("Total Amount:", labelFont));
            infoTable.addCell(getInfoCell("₹" + invoice.getTotalAmount(), normalFont, Element.ALIGN_RIGHT));

            infoTable.addCell(getInfoCell("Name:", labelFont));
            infoTable.addCell(getInfoCell(invoice.getUser().getFullName(), normalFont, Element.ALIGN_RIGHT));

            infoTable.addCell(getInfoCell("Email:", labelFont));
            infoTable.addCell(getInfoCell(invoice.getUser().getEmail(), normalFont, Element.ALIGN_RIGHT));

            document.add(infoTable);

            // ---------- Expense Table ----------
            PdfPTable expenseTable = new PdfPTable(7);
            expenseTable.setWidthPercentage(100);
            expenseTable.setWidths(new float[]{1f, 2.5f, 2f, 2f, 2f, 2.5f, 2.5f});
            expenseTable.setSpacingBefore(10f);

            String[] headers = {"#", "Expense Name", "Date", "Category", "Amount", "Manager Approved", "Admin Approved"};
            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                cell.setBackgroundColor(new Color(63, 81, 181));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6f);
                expenseTable.addCell(cell);
            }

            double total = 0;
            int count = 1;

            for (Expense expense : invoice.getExpenses()) {
                String managerDate = "-";
                String adminDate = "-";

                if (expense.getApprovals() != null) {
                    for (Approval approval : expense.getApprovals()) {
                        if (approval.getLevel().name().equalsIgnoreCase("MANAGER") && approval.getStatus().name().equals("APPROVED")) {
                            managerDate = approval.getActionTime().toLocalDate().toString();
                        }
                        if (approval.getLevel().name().equalsIgnoreCase("ADMIN") && approval.getStatus().name().equals("APPROVED")) {
                            adminDate = approval.getActionTime().toLocalDate().toString();
                        }
                    }
                }

                expenseTable.addCell(getTableCell(String.valueOf(count++), normalFont));
                expenseTable.addCell(getTableCell(expense.getDescription(), normalFont));
                expenseTable.addCell(getTableCell(expense.getExpenseDate().toString(), normalFont));
                expenseTable.addCell(getTableCell(expense.getCategory().getName(), normalFont));
                expenseTable.addCell(getTableCell("₹" + expense.getAmount(), normalFont));
                expenseTable.addCell(getTableCell(managerDate, normalFont));
                expenseTable.addCell(getTableCell(adminDate, normalFont));

                total += expense.getAmount();
            }

            document.add(expenseTable);

            // ---------- Total ----------
            Paragraph totalPara = new Paragraph("Total Amount: ₹" + total, titleFont);
            totalPara.setSpacingBefore(20f);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);

            // ---------- Footer ----------
            Paragraph footer = new Paragraph("Thank you for using Trex Expense Manager", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30f);
            document.add(footer);

        } catch (DocumentException e) {
            throw new IOException("Error generating invoice PDF", e);
        }
    }

    private static PdfPCell getInfoCell(String text, Font font) {
        return getInfoCell(text, font, Element.ALIGN_LEFT);
    }

    private static PdfPCell getInfoCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(5f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }


    private static PdfPCell getTableCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }



    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(63, 81, 181)); // Indigo
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
