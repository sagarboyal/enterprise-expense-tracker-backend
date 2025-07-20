package com.team7.enterpriseexpensemanagementsystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    Map uploadImage(MultipartFile file);
    Map deleteImage(String imageId);

    Map uploadInvoice(MultipartFile file, String customFileName);

    Map deleteInvoice(String invoiceCloudId);
}
