package org.portland.wizbangapi.ldap;

import com.google.common.collect.Sets;
import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;

/**
 * The <code>LdapConfiguration</code> class is the target class
 * for conversion of the yaml ldapConfiguration settings to an
 * object.
 */
public class LdapConfiguration {
    @NotNull
    @Valid
    private String user = "";

    @NotNull
    @Valid
    private String pwd = "";

    @NotNull
    @Valid
    private URI uri = URI.create("ldaps://www.example.com:636");

    @NotNull
    @NotEmpty
    private String userFilter = "ou=people,dc=example,dc=com";

    @NotNull
    @NotEmpty
    private String userNameAttribute = "cn";

    @NotNull
    @Valid
    private Duration connectTimeout = Duration.milliseconds(500);

    @NotNull
    @Valid
    private Duration readTimeout = Duration.milliseconds(500);

    @NotNull
    @Valid
    private TlsOption negotiateTls = TlsOption.NONE;

    @NotNull
    @Valid
    private Set<String> allowedRoles = Sets.newHashSet();


    public String getUser() {
        return user;
    }

    public LdapConfiguration setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPwd() {
        return pwd;
    }

    public LdapConfiguration setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    public URI getUri() {
        return uri;
    }

    public LdapConfiguration setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public LdapConfiguration setUserFilter(String userFilter) {
        this.userFilter = userFilter;
        return this;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public LdapConfiguration setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
        return this;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public LdapConfiguration setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public LdapConfiguration setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public TlsOption getNegotiateTls() {
        return negotiateTls;
    }

    public void setNegotiateTls(TlsOption negotiateTls) {
        this.negotiateTls = negotiateTls;
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }
}
