package org.portland.wizbangapi.ldap;

import io.dropwizard.auth.Authorizer;

/**
 * The <code>LdapAuthorizer</code> class is used by the
 * authentication framework to check if a user has the
 * required role to access an endpoint.
 */
public class LdapAuthorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String role) {
        return user.getRoles().contains(role);
    }
}

