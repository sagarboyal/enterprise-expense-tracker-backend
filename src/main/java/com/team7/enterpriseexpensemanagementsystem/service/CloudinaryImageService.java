package com.team7.enterpriseexpensemanagementsystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryImageService {
    Map uploadImage(MultipartFile file);
    Map deleteImage(String imageId);
}
