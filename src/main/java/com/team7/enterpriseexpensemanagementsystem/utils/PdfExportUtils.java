package com.team7.enterpriseexpensemanagementsystem.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
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
