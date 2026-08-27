package com.nexcentauri.scms.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "system_users")
public class SystemUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "organization_or_company")
    private String organizationOrCompany;

    @Column(name = "primary_hub")
    private String primaryHub;

    @Column(name = "department")
    private String department;

    @Column(nullable = false)
    private String role;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    public SystemUser(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getMobileNumber() {return mobileNumber;}
    public void setMobileNumber(String mobileNumber) {this.mobileNumber = mobileNumber;}
    public String getOrganizationOrCompany() {return organizationOrCompany;}
    public void setOrganizationOrCompany(String organizationOrCompany) {this.organizationOrCompany = organizationOrCompany;}
    public String getPrimaryHub() {return primaryHub;}
    public void setPrimaryHub(String primaryHub) {this.primaryHub = primaryHub;}
    public String getDepartment() {return department;}
    public void setDepartment(String department) {this.department = department;}
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}
    public String getPasswordHash() {return passwordHash;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}
}
