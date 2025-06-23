package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String email, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Link");
        message.setText("Click the link to reset your password: " + resetUrl);
        mailSender.send(message);
    }

    @Override
    public void sendInvoiceEmail(Invoice invoice, byte[] byteArray) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(invoice.getUser().getEmail());
            helper.setSubject("Your Invoice - " + invoice.getInvoiceNumber());

            String body = String.format("""
                Hello %s,

                Please find attached your invoice.

                Invoice Number: %s
                Generated At: %s
                Total Amount: ₹%.2f
                Status: %s

                Thank you for using Trex Expense Manager.
                """,
                    invoice.getUser().getFullName(),
                    invoice.getInvoiceNumber(),
                    invoice.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                    invoice.getTotalAmount(),
                    invoice.getStatus().name()
            );
            helper.setText(body);

            helper.addAttachment("invoice-" + invoice.getInvoiceNumber() + ".pdf",
                    new ByteArrayResource(byteArray));

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new ApiException("Failed to send invoice email: " + e.getMessage());
        }
    }
}
