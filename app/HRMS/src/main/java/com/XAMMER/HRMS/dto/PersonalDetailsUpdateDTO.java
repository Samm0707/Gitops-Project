package com.XAMMER.HRMS.dto;

// Add validation annotations if desired (e.g., @NotBlank, @Email from jakarta.validation.constraints)
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Email;

public class PersonalDetailsUpdateDTO {
    private String birthDate; 
    private String gender;
    // @Email // Example validation
    private String email;
    private String phone;
    private String address;
    // --- ADD THESE FIELDS ---
    private String nationality;
    private String bloodGroup;

    // Getters and Setters
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // --- GETTERS AND SETTERS FOR NEW FIELDS ---
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
}