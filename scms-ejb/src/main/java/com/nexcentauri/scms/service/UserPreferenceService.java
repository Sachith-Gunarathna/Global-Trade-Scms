package com.nexcentauri.scms.service;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.entity.UserPreference;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Stateless
public class UserPreferenceService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;
    @EJB
    private AuthService authService;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Map<String, Object> getPreferences(String email) throws SupplyChainApplicationException {
        SystemUser user = requireUser(email);
        UserPreference preference = find(user.getId());
        if (preference == null) return new LinkedHashMap<>();
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(preference.getPreferencesJson(), new LinkedHashMap<String, Object>() {}.getClass().getGenericSuperclass());
        } catch (Exception exception) {
            return new LinkedHashMap<>();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Map<String, Object> updatePreferences(String email, Map<String, Object> values) throws SupplyChainApplicationException {
        SystemUser user = requireUser(email);
        UserPreference preference = find(user.getId());
        if (preference == null) {
            preference = new UserPreference();
            preference.setUser(user);
            entityManager.persist(preference);
        }
        Map<String, Object> safe = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
        try (Jsonb jsonb = JsonbBuilder.create()) {
            preference.setPreferencesJson(jsonb.toJson(safe));
        } catch (Exception exception) {
            throw new SupplyChainApplicationException("Unable to serialize user preferences.", exception);
        }
        preference.setUpdatedAt(LocalDateTime.now());
        return safe;
    }

    private UserPreference find(Long userId) {
        try {
            return entityManager.createQuery("SELECT p FROM UserPreference p WHERE p.user.id = :userId", UserPreference.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    private SystemUser requireUser(String email) throws SupplyChainApplicationException {
        SystemUser user = authService.findByEmail(email);
        if (user == null) throw new SupplyChainApplicationException("User account was not found.");
        return user;
    }
}
