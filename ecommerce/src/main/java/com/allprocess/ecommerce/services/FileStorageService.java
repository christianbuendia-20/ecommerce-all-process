package com.allprocess.ecommerce.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    void deleteFile(String filename);
    Resource loadAsResource(String filename);
}
