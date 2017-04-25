package org.portland.wizbangapi.ldap.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.portland.wizbangapi.ldap.LdapAuthenticator;

import java.security.Principal;

import static com.google.common.base.Preconditions.checkNotNull;

public class LdapHealthCheck<T extends Principal> extends HealthCheck {
    private final Authenticator<BasicCredentials, T> ldapAuthenticator;

    public LdapHealthCheck(Authenticator<BasicCredentials, T> ldapAuthenticator) {
        this.ldapAuthenticator = checkNotNull(ldapAuthenticator, "ldapAuthenticator cannot be null");
    }

    @Override
    public Result check() throws AuthenticationException {
        String userDN = ((LdapAuthenticator)ldapAuthenticator).checkLdap();
        if (!"".equals(userDN)) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Cannot contact authentication service");
        }
    }
}