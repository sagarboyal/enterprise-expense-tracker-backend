package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.FileDocument;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.FileDocumentRepository;
import com.team7.enterpriseexpensemanagementsystem.service.CloudinaryImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {
    private final FileDocumentRepository fileDocumentRepository;
    private final ExpenseRepository expenseRepository;
    private final CloudinaryImageService  cloudinaryImageService;

    @PostMapping("/cloudinary/upload/{expenseId}")
    public ResponseEntity<FileDocument> uploadCloudinaryImage(@PathVariable Long expenseId, @RequestBody MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("File is empty")    ;
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        Optional<FileDocument> existingDocOpt = fileDocumentRepository.findByExpenseId(expenseId);
        existingDocOpt.ifPresent(existingDoc -> {
            cloudinaryImageService.deleteImage(existingDoc.getImageId());
            fileDocumentRepository.delete(existingDoc);
        });

        String fileName = UUID.randomUUID() + "_"
                + StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        Map data = cloudinaryImageService.uploadImage(file);

        FileDocument doc = FileDocument.builder()
                .fileName(fileName)
                .uploadedAt(LocalDateTime.now())
                .expense(expense)
                .imageId(data.get("public_id").toString())
                .imageUrl(data.get("secure_url").toString())
                .build();
        return ResponseEntity.ok(fileDocumentRepository.save(doc));
    }

    @PostMapping("/cloudinary/delete/{expenseId}")
    public ResponseEntity<String> deleteCloudinaryImage(@PathVariable Long expenseId) {
        Optional<FileDocument> existingDocOpt = fileDocumentRepository.findByExpenseId(expenseId);
        existingDocOpt.ifPresent(existingDoc -> {
            cloudinaryImageService.deleteImage(existingDoc.getImageId());
            fileDocumentRepository.delete(existingDoc);
        });
        return ResponseEntity.ok("Image deleted successfully");
    }
}

