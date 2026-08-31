package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Locale;
import org.mindrot.jbcrypt.BCrypt;

@Stateless
public class AuthService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SystemUser registerUser(String firstName, String lastName, String email, String mobileNumber, String organization, String primaryHub, String department, String requestedRole, String password) throws SupplyChainApplicationException {
        validateRegistration(firstName, lastName, email, password);
        String normalized = normalizeEmail(email);
        if (findByEmail(normalized) != null) {
            throw new SupplyChainApplicationException("An account with this email already exists.");
        }
        SystemUser user = new SystemUser();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(normalized);
        user.setMobileNumber(clean(mobileNumber));
        user.setOrganizationOrCompany(clean(organization));
        user.setPrimaryHub(clean(primaryHub));
        user.setDepartment(clean(department));
        user.setRole(mapRequestedRole(requestedRole));
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);
        return user;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public SystemUser authenticate(String email, String password) throws SupplyChainApplicationException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new SupplyChainApplicationException("Email and password are required.");
        }
        SystemUser user = findByEmail(normalizeEmail(email));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled()) || !validPassword(password, user.getPasswordHash())) {
            throw new SupplyChainApplicationException("Invalid email or password.");
        }
        return user;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public SystemUser findByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            return entityManager.createQuery("SELECT u FROM SystemUser u WHERE u.email = :email", SystemUser.class)
                    .setParameter("email", normalizeEmail(email))
                    .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SystemUser updateProfile(String currentEmail, String firstName, String lastName, String mobileNumber, String department, String primaryHub, String organization) throws SupplyChainApplicationException {
        SystemUser user = findByEmail(currentEmail);
        if (user == null) throw new SupplyChainApplicationException("User account was not found.");
        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName.trim());
        if (lastName != null && !lastName.isBlank()) user.setLastName(lastName.trim());
        user.setMobileNumber(clean(mobileNumber));
        user.setDepartment(clean(department));
        user.setPrimaryHub(clean(primaryHub));
        user.setOrganizationOrCompany(clean(organization));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void changePassword(String email, String currentPassword, String newPassword) throws SupplyChainApplicationException {
        SystemUser user = authenticate(email, currentPassword);
        if (newPassword == null || newPassword.length() < 8) {
            throw new SupplyChainApplicationException("New password must contain at least 8 characters.");
        }
        if (validPassword(newPassword, user.getPasswordHash())) {
            throw new SupplyChainApplicationException("New password must be different from the current password.");
        }
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
        user.setUpdatedAt(LocalDateTime.now());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SystemUser createBootstrapUser(String firstName, String lastName, String email, String role, String password) {
        SystemUser existing = findByEmail(email);
        if (existing != null) return existing;
        SystemUser user = new SystemUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(normalizeEmail(email));
        user.setMobileNumber("+94 11 000 0000");
        user.setOrganizationOrCompany("GlobalTrade Logistics Corporation");
        user.setPrimaryHub("Port of Colombo HQ");
        user.setDepartment(role.replace('_', ' '));
        user.setRole(role);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);
        return user;
    }

    public String mapRequestedRole(String role) {
        return "VENDOR_REP";
    }

    private void validateRegistration(String firstName, String lastName, String email, String password) throws SupplyChainApplicationException {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) throw new SupplyChainApplicationException("First name and last name are required.");
        if (email == null || !email.contains("@")) throw new SupplyChainApplicationException("A valid email is required.");
        if (password == null || password.length() < 8) throw new SupplyChainApplicationException("Password must contain at least 8 characters.");
    }

    private boolean validPassword(String password, String hash) {
        try { return hash != null && BCrypt.checkpw(password, hash); } catch (IllegalArgumentException exception) { return false; }
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private String clean(String value) { return value == null ? null : value.trim(); }
}
