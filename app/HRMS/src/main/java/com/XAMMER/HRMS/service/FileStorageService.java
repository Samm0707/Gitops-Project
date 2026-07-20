package com.XAMMER.HRMS.service;

import org.springframework.web.multipart.MultipartFile; // <-- Check this import
import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException; // <-- Check this signature

    void deleteFile(String certFilename);
}