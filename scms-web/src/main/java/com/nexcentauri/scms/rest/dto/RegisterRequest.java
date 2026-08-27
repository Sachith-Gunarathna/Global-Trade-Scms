package com.nexcentauri.scms.rest.dto;

public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String organizationOrCompany;
    private String primaryHub;
    private String department;
    private String role;
    private String password;

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getMobileNumber() {
        return mobileNumber;
    }
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    public String getOrganizationOrCompany() {
        return organizationOrCompany;
    }
    public void setOrganizationOrCompany(String organizationOrCompany) {
        this.organizationOrCompany = organizationOrCompany;
    }
    public String getPrimaryHub() {
        return primaryHub;
    }
    public void setPrimaryHub(String primaryHub) {
        this.primaryHub = primaryHub;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }


}
