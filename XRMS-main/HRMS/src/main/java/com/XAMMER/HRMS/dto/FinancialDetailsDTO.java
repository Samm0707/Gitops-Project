package com.XAMMER.HRMS.dto;

public class FinancialDetailsDTO {

    private String bankName;
    private String accountNumber;
    private String ifsc;
    private String aadhaar;
    private String pan;

    // Getters and Setters...

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }
    public String getAadhaar() { return aadhaar; }
    public void setAadhaar(String aadhaar) { this.aadhaar = aadhaar; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
}