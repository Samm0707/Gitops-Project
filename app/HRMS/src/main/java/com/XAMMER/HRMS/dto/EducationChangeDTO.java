package com.XAMMER.HRMS.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EducationChangeDTO {

    public enum Operation {
        CREATE, UPDATE, DELETE
    }

    private Operation operation;
    private String id; // <-- CRITICAL: Change back to String!
    private String tempId; // For new entries to map files
    private EducationDTO data;

    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }
    public String getId() { return id; } // Now returns String
    public void setId(String id) { this.id = id; } // Now accepts String
    public String getTempId() { return tempId; }
    public void setTempId(String tempId) { this.tempId = tempId; }
    public EducationDTO getData() { return data; }
    public void setData(EducationDTO data) { this.data = data; }

    @Override
    public String toString() {
        return "EducationChangeDTO{" +
                "operation=" + operation +
                ", id='" + id + '\'' + // Ensure toString also reflects String
                ", tempId='" + tempId + '\'' +
                ", data=" + data +
                '}';
    }
}