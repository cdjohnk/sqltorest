package org.portland.wizbangapi.ldap;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.apache.log4j.MDC;
import org.portland.wizbangapi.WizBangApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Set;

/**
 * The <code>LdapAuthenticator</code> class authenticates users against an LDAP
 * directory. It binds to the directory using an admin account, finds the DN
 * of the user attempting to log in based on a configured attribute, attempts
 * to bind that DN, and then checks that the user, if valid, belongs to
 * an 'ou' that is configured for access to the API. If all of those steps
 * succeed, the user is returned as authenticated with all of its associated
 * roles so that authorization can also be checked at each individual API endpoint
 * if desired.
 */
public class LdapAuthenticator implements Authenticator<BasicCredentials, User> {
    private static final Logger LOG = LoggerFactory.getLogger(LdapAuthenticator.class);
    protected final LdapConfiguration configuration;

    public LdapAuthenticator(LdapConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    private static String sanitizeEntity(String name) {
        return name.replaceAll("[^A-Za-z0-9-_.]", "");
    }

    public String checkLdap() throws io.dropwizard.auth.AuthenticationException {
        // admin user validates against 'cn' attribute
        configuration.setUserNameAttribute("cn");
        return ldapAuthenticate(new BasicCredentials(configuration.getUser(), configuration.getPwd()));
    }

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) {
        MDC.put("user", sanitizeEntity(credentials.getUsername()));
        Optional<User> optional = Optional.empty();
        try {
            String userDN = ldapAuthenticate(credentials);
            if (!"".equals(userDN)) {
                // Get set of organizational units out of userDN.
                // In this application, organizational units are used to
                // control authorization. If an ldap attribute
                // is required for authorization, then some of this
                // logic needs to be moved into the ldapAuthenticate method
                // where all of the ldap attributes are available.
                String[] ldapNodes = userDN.split(",");
                Set<String> roles = new HashSet<>();
                boolean isAllowed = false;
                for (String nodeStr : ldapNodes) {
                    String[] node = nodeStr.split("=");
                    if ("ou".equals(node[0])) {
                        roles.add(node[1]);
                        // check that at least on of the roles of this user
                        // are in the configured list of allowed roles.
                        if (WizBangApplication.getAllowedRoles().contains(node[1])) {
                            isAllowed = true;
                        }
                    }
                }
                if (isAllowed) {
                    // set up valid user with roles as authorized
                    User user = new User(credentials.getUsername(), roles);
                    optional = Optional.of(user);
                }
            }
        }
        catch (io.dropwizard.auth.AuthenticationException ae) {
            LOG.debug("Failed to authenticate with message: " + ae.getMessage(), ae);
        }
        finally {
            if (optional.equals(Optional.empty())) {
                LOG.info("Authentication Failed.");
            }
            else {
                LOG.info("Authentication Succeeded.");
            }
        }
        return optional;
    }

    private String ldapAuthenticate(BasicCredentials credentials) throws io.dropwizard.auth.AuthenticationException {
        final String uname = sanitizeEntity(credentials.getUsername());
        final String adminDN = String.format("cn=%s,%s", configuration.getUser(), configuration.getUserFilter());
        final String adminPwd = configuration.getPwd();
        SearchResult searchResult = null;
        int pwdExpired = 0;

        // bind admin user and search for user who is attempting to log in
        try (AutoclosingLdapContext context = new AutoclosingLdapContext(contextConfiguration(adminDN, adminPwd), configuration.getNegotiateTls())) {
            final String uattr = configuration.getUserNameAttribute();
            NamingEnumeration<SearchResult> namingEnum = context.search("", "(&(" + uattr + "=" + uname + "))", getSimpleSearchControls());
            searchResult = namingEnum.next();
            if (searchResult != null) {
                if (searchResult.getNameInNamespace().equals(adminDN)) {
                    // if we're checking the adminDN, we're not checking 'shadowlastchange'
                    pwdExpired = -1;
                }
                else {
                    // shadowlastchange = 0 means the password is not valid, even if it matches the ldap value
                    pwdExpired = Integer.parseInt(searchResult.getAttributes().get("shadowlastchange").get(0).toString());
                }
            }
        }
        catch (Throwable t) {
            LOG.debug("Failed to bind and search with LDAP configuration", t);
        }

        String userDN = "";
        String userPwd = "";
        // if search successfully returned user and their password is not expired, populate the DN and password
        if (searchResult != null && pwdExpired != 0) {
            userDN = searchResult.getNameInNamespace();
            userPwd = credentials.getPassword();
        }
        Hashtable<String, String> userEnv = contextConfiguration(userDN, userPwd);

        // try binding the userDN, if it works the correct password was supplied
        try (AutoclosingLdapContext context = new AutoclosingLdapContext(userEnv, configuration.getNegotiateTls())) {
            // Empty! Authentication and closing resources are all handled within the try-with-resources line.
        }
        catch (IOException | NamingException err) {
            if (err instanceof AuthenticationException) LOG.debug("{} failed to authenticate. {}", uname, err);
            throw new io.dropwizard.auth.AuthenticationException(String.format("LDAP Authentication failure (username: %s)",
                    uname), err);
        }
        return userDN;
    }

    private SearchControls getSimpleSearchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setTimeLimit(30000);
        return searchControls;
    }

    private Hashtable<String, String> contextConfiguration(String userDN, String userPwd) {
        final Hashtable<String, String> env = new Hashtable<>();

        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, userPwd);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, configuration.getUri().toString());
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(configuration.getConnectTimeout().toMilliseconds()));
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(configuration.getReadTimeout().toMilliseconds()));
        env.put("com.sun.jndi.ldap.connect.pool", "true");

        return env;
    }
}
