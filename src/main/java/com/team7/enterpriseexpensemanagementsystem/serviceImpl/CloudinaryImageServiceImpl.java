package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.cloudinary.Cloudinary;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.service.CloudinaryImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageServiceImpl implements CloudinaryImageService {
    private final Cloudinary cloudinary;

    @Override
    public Map uploadImage(MultipartFile file) {
        try {
           return this.cloudinary.uploader().upload(file.getBytes(), Map.of());
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
}
