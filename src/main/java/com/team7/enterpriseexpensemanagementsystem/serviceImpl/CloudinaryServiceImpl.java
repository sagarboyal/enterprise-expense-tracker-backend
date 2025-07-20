package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.cloudinary.Cloudinary;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final DataSize maxFileSize;

    public CloudinaryServiceImpl(
            Cloudinary cloudinary,
            @Value("${cloudinary.max-file-size}") DataSize maxFileSize) {
        this.cloudinary = cloudinary;
        this.maxFileSize = maxFileSize;
    }

    @Override
    public Map uploadImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new ApiException("File is empty! Please select a file to upload.");
        }

        if (file.getSize() > this.maxFileSize.toBytes()) {
            throw new ApiException(String.format("File size cannot exceed %d MB.",
                    this.maxFileSize.toMegabytes()));
        }

        try {
            return this.cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "supporting docs"));
        } catch (IOException e) {
            throw new ApiException("Image upload failed! error -> " + e.getMessage());
        }
    }

    @Override
    public Map deleteImage(String imageId) {
        try {
            return this.cloudinary.uploader().destroy(imageId, Map.of());
        } catch (IOException e) {
            throw new ApiException("Image deletion failed! error -> " + e.getMessage());
        }
    }

    @Override
    public Map uploadInvoice(MultipartFile file, String customFileName) {
        if (file.isEmpty()) {
            throw new ApiException("File is empty! Please select a file to upload.");
        }
        try {
            return this.cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder", "invoices",
                    "public_id", customFileName,
                    "type", "upload"
            ));
        } catch (IOException e) {
            throw new ApiException("Invoice upload failed! Error: " + e.getMessage(), e);
        }
    }

    @Override
    public Map deleteInvoice(String invoiceCloudId) {
        try {
            return this.cloudinary.uploader().destroy(invoiceCloudId, Map.of());
        } catch (IOException e) {
            throw new ApiException("Invoice deletion failed! Error: " + e.getMessage(), e);
        }
    }
}