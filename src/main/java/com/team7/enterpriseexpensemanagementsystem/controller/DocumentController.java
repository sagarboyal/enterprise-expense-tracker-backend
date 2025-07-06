package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.FileDocument;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.FileDocumentRepository;
import com.team7.enterpriseexpensemanagementsystem.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {
    private final FileStorageService fileStorageService;
    private final FileDocumentRepository fileDocumentRepository;
    private final ExpenseRepository expenseRepository;

    @PostMapping("/{expenseId}/upload")
    public ResponseEntity<String> uploadInvoice(@PathVariable Long expenseId,
                                                @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        Optional<FileDocument> existingDocOpt = fileDocumentRepository.findByExpenseId(expenseId);
        existingDocOpt.ifPresent(existingDoc -> {
            try {
                fileStorageService.deleteFile(existingDoc.getFileName());
                fileDocumentRepository.delete(existingDoc);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete existing file: " + e.getMessage());
            }
        });

        String fileName;
        try {
            fileName = fileStorageService.storeFile(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload file: " + e.getMessage());
        }

        FileDocument doc = FileDocument.builder()
                .fileName(fileName)
                .filePath("uploads/invoices/" + fileName)
                .uploadedAt(LocalDateTime.now())
                .expense(expense)
                .build();

        fileDocumentRepository.save(doc);

        return ResponseEntity.ok("Uploaded: " + fileName);
    }


    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName, HttpServletRequest request) throws MalformedURLException {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream"; // fallback
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }


    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws MalformedURLException {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/{expenseId:.+}")
    public ResponseEntity<FileDocument> downloadFile(@PathVariable Long expenseId){
        return ResponseEntity.ok()
                .body(fileDocumentRepository.findByExpenseId(expenseId)
                        .orElse(null));
    }
}

