package com.nexcentauri.scms.security;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.service.AuthService;
import java.util.Arrays;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class ScmsJaasLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private ScmsUserPrincipal userPrincipal;
    private ScmsRolePrincipal rolePrincipal;
    private boolean authenticated;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) throw new LoginException("A callback handler is required.");
        NameCallback nameCallback = new NameCallback("Email");
        PasswordCallback passwordCallback = new PasswordCallback("Password", false);
        char[] passwordValue = null;
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
            passwordValue = passwordCallback.getPassword();
            String password = passwordValue == null ? "" : new String(passwordValue);
            SystemUser user = lookupAuthService().authenticate(nameCallback.getName(), password);
            userPrincipal = new ScmsUserPrincipal(user.getEmail());
            rolePrincipal = new ScmsRolePrincipal(user.getRole());
            authenticated = true;
            return true;
        } catch (SupplyChainApplicationException exception) {
            throw new FailedLoginException(exception.getMessage());
        } catch (Exception exception) {
            LoginException loginException = new LoginException("Authentication service is unavailable.");
            loginException.initCause(exception);
            throw loginException;
        } finally {
            if (passwordValue != null) Arrays.fill(passwordValue, '\0');
            passwordCallback.clearPassword();
        }
    }

    @Override
    public boolean commit() {
        if (!authenticated) return false;
        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        return true;
    }

    @Override
    public boolean abort() {
        clear();
        return true;
    }

    @Override
    public boolean logout() {
        if (subject != null) {
            if (userPrincipal != null) subject.getPrincipals().remove(userPrincipal);
            if (rolePrincipal != null) subject.getPrincipals().remove(rolePrincipal);
        }
        clear();
        return true;
    }

    private AuthService lookupAuthService() throws NamingException {
        InitialContext context = new InitialContext();
        String interfaceName = AuthService.class.getName();
        String[] names = {
                "java:app/scms-ejb-1.0/AuthService!" + interfaceName,
                "java:global/scms-ear-1.0/scms-ejb-1.0/AuthService!" + interfaceName
        };
        NamingException last = null;
        for (String name : names) {
            try {
                return (AuthService) context.lookup(name);
            } catch (NamingException exception) {
                last = exception;
            }
        }
        throw last == null ? new NamingException("AuthService was not found.") : last;
    }

    private void clear() {
        authenticated = false;
        userPrincipal = null;
        rolePrincipal = null;
    }
}
