package com.XAMMER.HRMS.dto;

public class S3UploadResult {
    private String storageKey;
    private String originalFilename;
    private String fileUrl; // Optional, if you want to store the full URL
    
    public S3UploadResult() {
    }
    
    public S3UploadResult(String storageKey, String originalFilename) {
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
    }
    
    public S3UploadResult(String storageKey, String originalFilename, String fileUrl) {
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.fileUrl = fileUrl;
    }
    
    // Getters and Setters
    public String getStorageKey() {
        return storageKey;
    }
    
    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}