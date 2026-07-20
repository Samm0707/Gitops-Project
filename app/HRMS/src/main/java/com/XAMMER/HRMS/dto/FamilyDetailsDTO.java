package com.XAMMER.HRMS.dto;

import java.time.LocalDate;

public class FamilyDetailsDTO {

    private String fatherName;
    private String motherName;
    private String maritalStatus;
    private Integer childrenCount;
    private String spouseName;
    private CharSequence spouseDob;

    // Getters and Setters for all fields...
    // You can generate these easily in your IDE (Alt+Insert > Getter and Setter)

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }
    public String getSpouseName() { return spouseName; }
    public void setSpouseName(String spouseName) { this.spouseName = spouseName; }
    public CharSequence getSpouseDob() { return spouseDob; }
    public void setSpouseDob(CharSequence spouseDob) { this.spouseDob = spouseDob; }
}