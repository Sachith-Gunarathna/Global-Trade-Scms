package com.nexcentauri.scms.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public final class ScmsUserPrincipal implements Principal, Serializable {
    private final String name;

    public ScmsUserPrincipal(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ScmsUserPrincipal principal && name.equals(principal.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
