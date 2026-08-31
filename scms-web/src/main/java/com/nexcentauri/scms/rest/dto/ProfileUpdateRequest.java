package com.nexcentauri.scms.rest.dto;

public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String department;
    private String hub;
    private String organization;
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getHub() { return hub; }
    public void setHub(String hub) { this.hub = hub; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
}
