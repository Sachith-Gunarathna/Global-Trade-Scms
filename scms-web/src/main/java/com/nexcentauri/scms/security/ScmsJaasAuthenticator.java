package com.nexcentauri.scms.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.Optional;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

@ApplicationScoped
public class ScmsJaasAuthenticator {
    public Optional<ScmsJaasIdentity> authenticate(String username, char[] password) {
        char[] copy = password == null ? new char[0] : password.clone();
        Subject subject = new Subject();
        try {
            LoginContext loginContext = new LoginContext("SCMS", subject, new ScmsJaasCallbackHandler(username, copy), new ScmsJaasConfiguration());
            loginContext.login();
            String user = subject.getPrincipals(ScmsUserPrincipal.class).stream().findFirst().map(ScmsUserPrincipal::getName).orElse(null);
            String role = subject.getPrincipals(ScmsRolePrincipal.class).stream().findFirst().map(ScmsRolePrincipal::getName).orElse(null);
            if (user == null || role == null) return Optional.empty();
            return Optional.of(new ScmsJaasIdentity(user, role));
        } catch (LoginException exception) {
            return Optional.empty();
        } finally {
            Arrays.fill(copy, '\0');
        }
    }
}
