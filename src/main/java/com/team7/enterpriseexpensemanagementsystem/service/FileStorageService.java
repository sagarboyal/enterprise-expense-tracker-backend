package com.team7.enterpriseexpensemanagementsystem.service;


import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

public interface FileStorageService {
    public String storeFile(MultipartFile file);
    public Resource loadFileAsResource(String fileName) throws MalformedURLException;
}
