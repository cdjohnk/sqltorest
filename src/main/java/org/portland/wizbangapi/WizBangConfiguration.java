package org.portland.wizbangapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.portland.wizbangapi.ldap.LdapConfiguration;

/**
 * This class is the target for conversion of the YAML configuration
 * to an object.
 */
public class WizBangConfiguration extends Configuration {
    private DataSourceFactory database = new DataSourceFactory();
    private LdapConfiguration ldap = new LdapConfiguration();

    @JsonProperty("database")
    public DataSourceFactory getDatabase() {
        return database;
    }

    @JsonProperty("database")
    public void setDatabase(DataSourceFactory database) {
        this.database = database;
    }

    @JsonProperty("ldapConfiguration")
    public LdapConfiguration getLdapConfiguration() {
        return ldap;
    }

    @JsonProperty("ldapConfiguration")
    public void setLdapConfiguration(LdapConfiguration ldap) {
        this.ldap = ldap;
    }
}
