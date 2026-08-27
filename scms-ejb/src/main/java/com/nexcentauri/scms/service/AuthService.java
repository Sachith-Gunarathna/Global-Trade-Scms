package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.SystemUser;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.security.MessageDigest;
import java.util.Base64;

@Stateless
public class AuthService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    public SystemUser registerUser(
            String firstName,
            String lastName,
            String email,
            String mobileNumber,
            String organizationOrCompany,
            String primaryHub,
            String department,
            String role,
            String password
    ){
        if(entityManager.find(SystemUser.class, email) != null){
            throw new RuntimeException("An account with this email already exists!");
        }

        SystemUser user = new SystemUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);
        user.setOrganizationOrCompany(organizationOrCompany);
        user.setPrimaryHub(primaryHub);
        user.setDepartment(department);

        user.setPasswordHash(hashPassword(password));

        String backedRole = "VENDOR";
        if(role != null){
            if(role.contains("Operations") || role.contains("Logistics") || role.contains("Analyst")){
                backedRole = "LOGISTICS_PERSONNEL";
            } else if(role.contains("CUSTOMS")){
                backedRole = "CUSTOMS_OFFICIAL";
            }
        }
        user.setRole(backedRole);

        entityManager.persist(user);
        return user;
    }

    public SystemUser authenticate(String email, String password) throws Exception{
        SystemUser user = entityManager.find(SystemUser.class, email);

        if(user == null || !user.getPasswordHash().equals(hashPassword(password))){
            throw new Exception("Invalid email or password.");
        }
        return user;
    }

    private String hashPassword(String password){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error securely hashing password", e);
        }

    }

}

