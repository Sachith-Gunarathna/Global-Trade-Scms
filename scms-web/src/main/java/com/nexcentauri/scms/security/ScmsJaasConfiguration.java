package com.nexcentauri.scms.security;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public final class ScmsJaasConfiguration extends Configuration {
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        if (!"SCMS".equals(name)) return null;
        AppConfigurationEntry entry = new AppConfigurationEntry(
                ScmsJaasLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                Map.of()
        );
        return new AppConfigurationEntry[]{entry};
    }
}
