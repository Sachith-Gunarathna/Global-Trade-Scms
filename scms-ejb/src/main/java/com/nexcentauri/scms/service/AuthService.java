package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.dto.UserProfileDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.mindrot.jbcrypt.BCrypt;

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
        if(findByEmail(email) != null){
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

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            throw new Exception("Email and password are required.");
        }

        SystemUser user = findByEmail(email.trim());

        if (user == null) {
            throw new Exception("Invalid email or password.");
        }

        if (!checkPassword(password, user.getPasswordHash())) {
            throw new Exception("Invalid email or password.");
        }

        return user;
    }

    private String hashPassword(String password){

        return BCrypt.hashpw(password, BCrypt.gensalt());

    }

    private boolean checkPassword(String password, String hash){

        if (password == null || hash == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }

    }

    private SystemUser findByEmail(String email){

        try {
            return entityManager.createQuery(
                            "SELECT u FROM SystemUser u WHERE u.email = :email",
                            SystemUser.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }

    }

    public SystemUser updateUserProfile(UserProfileDTO dto) throws Exception {
        SystemUser user = entityManager.find(SystemUser.class, dto.getUsername());
        if (user == null) {
            throw new Exception("User not found in Database.");
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMobileNumber(dto.getPhone());
        user.setDepartment(dto.getDepartment());
        user.setPrimaryHub(dto.getHub());
        user.setRole(dto.getTitle());

        entityManager.merge(user);
        return user;
    }
}

