package com.nexcentauri.scms.security;

import java.io.IOException;
import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public final class ScmsJaasCallbackHandler implements CallbackHandler {
    private final String username;
    private final char[] password;

    public ScmsJaasCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password == null ? new char[0] : password.clone();
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        try {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback nameCallback) {
                    nameCallback.setName(username);
                } else if (callback instanceof PasswordCallback passwordCallback) {
                    passwordCallback.setPassword(password);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
