package com.team7.enterpriseexpensemanagementsystem.service;


import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

public interface FileStorageService {
    public String storeFile(MultipartFile file);
    public Resource loadFileAsResource(String fileName) throws MalformedURLException;

    void deleteFile(String fileName) throws IOException;
}
