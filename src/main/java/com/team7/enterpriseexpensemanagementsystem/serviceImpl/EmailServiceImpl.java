package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.cloudinary.Cloudinary;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.service.EmailService;
import com.team7.enterpriseexpensemanagementsystem.utils.pdfGeneration.PdfExportUtils;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final Cloudinary cloudinary;

    @Override
    public void sendPasswordResetEmail(String email, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Link");
        message.setText("Click the link below to reset your password:\n\n" + resetUrl +
                "\n\nIf you didn’t request this, please ignore this email.");
        mailSender.send(message);
    }


    @Override
    public void sendInvoiceEmail(Invoice invoice, String email) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PdfExportUtils.exportInvoice(invoice, stream);
            byte[] pdfBytes = stream.toByteArray();

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject("Your Invoice - " + invoice.getInvoiceNumber());

            String body = String.format("""
                Hello %s,

                Please find attached your invoice.

                Invoice Number: %s
                Generated At: %s
                Total Amount: ₹%.2f

                Thank you for using Trex Expense Manager.
                """,
                    invoice.getUser().getFullName(),
                    invoice.getInvoiceNumber(),
                    invoice.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                    invoice.getTotalAmount()
            );

            helper.setText(body);
            helper.addAttachment("invoice-" + invoice.getInvoiceNumber() + ".pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new ApiException("Failed to generate or send invoice email: " + e.getMessage());
        }
    }


}
