package org.portland.wizbangapi;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.portland.wizbangapi.ldap.LdapAuthenticator;
import org.portland.wizbangapi.ldap.LdapAuthorizer;
import org.portland.wizbangapi.ldap.LdapConfiguration;
import org.portland.wizbangapi.ldap.User;
import org.portland.wizbangapi.ldap.healthcheck.LdapHealthCheck;
import org.portland.wizbangapi.model.PersonDao;
import org.portland.wizbangapi.model.entities.Person;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is the main application class for an <a src="http://www.dropwizard.io/1.1.0/docs/">DropWizard</a>
 * application configured for LDAP authentication derived from yammer/dropwizard-auth-ldap.
 * What is different about this application is that it is configured for LDAP auth based on
 * binding with an admin account, searching for a user, and then attempting to bind with
 * that user. Also, this application logs user names with all log entries, including
 * whether authentication failed or succeeded. Lastly, this application allows for authorization
 * based on roles at both the API and endpoint level. Roles are based on 'ou', but
 * could easily be refactored to use LDAP attributes.
 */
public class WizBangApplication extends Application<WizBangConfiguration> {
    public static final int queryLimit = 50;
    private static Set<String> allowedRoles;
    private final HibernateBundle<WizBangConfiguration> hBundle;

    public WizBangApplication() {
        hBundle = new HibernateBundle<WizBangConfiguration>(Person.class) {
            public PooledDataSourceFactory getDataSourceFactory(WizBangConfiguration wizBangConfiguration) {
                return wizBangConfiguration.getDatabase();
            }
        };
    }

    @Override
    public void initialize(Bootstrap<WizBangConfiguration> bootstrap) {
        bootstrap.addBundle(hBundle);
    }

    public void run(WizBangConfiguration wizBangConfiguration, Environment environment) throws Exception {
        final LdapConfiguration ldapConfiguration = wizBangConfiguration.getLdapConfiguration();

        // set up a static list of roles listed as allowed in the configuration
        allowedRoles = ldapConfiguration.getAllowedRoles();

        // set up ldap authentication and role authorization
        // I am not using a caching authenticator because I want every login attempt recorded in the logs
        Authenticator<BasicCredentials, User> ldapAuthenticator = new LdapAuthenticator(ldapConfiguration);
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(ldapAuthenticator)
                        .setAuthorizer(new LdapAuthorizer())
                        .setRealm("LDAP")
                        .buildAuthFilter()));

        // registering the ldap user class as the class to use for authentication
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        // enable allowing specific roles via annotation on resource methods
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        // set up a health check for the ldap connection
        environment.healthChecks().register("ldap", new LdapHealthCheck<>(
                new LdapAuthenticator(ldapConfiguration)));

        // re-enable the WADL for this service
        Map<String, Object> properties = new HashMap<>();
        properties.put(ServerProperties.WADL_FEATURE_DISABLE, false);
        environment.jersey().getResourceConfig().addProperties(properties);

        // add go data to rest endpoints
        final PersonDao dao = new PersonDao(hBundle.getSessionFactory());
        environment.jersey().register(dao);
    }

    public static void main(String[] args) throws Throwable {
        new WizBangApplication().run(args);
    }

    public static Set<String> getAllowedRoles () {
        return allowedRoles;
    }
}
