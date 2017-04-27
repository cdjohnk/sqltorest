# wizbangapi

This project was previously named 'sqltorest', and its purpose was to bring together Jersey, Jackson, Jetty, and Hibernate to create an integrated sample application as a starting point for quickly standing up a database backed Restful API with authentication, authorization, and logging.

As it turns out, Dropwizard (http://www.dropwizard.io/1.1.0/docs/) has already produced an easy to use framework that integrates those libraries, and much more.

This application, therefore, implements a hibernate backed Dropwizard application. Authentication and authorization are implemented using a heavily modified version of the Dropwizard LDAP module provided by Yammer (https://github.com/yammer/dropwizard-auth-ldap). The LDAP authentication implemented here binds an LDAP admin account which is used to search for a user account based on a configured attribute, and then an attempt is made to bind the returned DN (assuming there is one). Authorization is based on the LDAP organizational units to which the user belongs.

Organizational units must be added to the YAML configuration file to allow users to access any API call. Additionally, the @RolesPermitted annotation can be added to individual endpoint methods in the resource classes to further restrict access to individual endpoints. Examples of endpoint specific authorizations can be seen in the PersonDAO class.

The LDAPAuthenticator class can fairly easily be altered to change how authorization works, if roles need to be based on something other than ou's.

The configured log format records the logged in user name on every log line, including all authentication attempts, and whether they succeeded or failed.

This sample application is configured to work as-is once it is properly configured against an LDAP instance. The current configured database for Hibernate is an H2 database which will be automatically created in the current working directory and automatically populated with the Person entity defined in the /model/entities directory. Hopefully this sample implementation quickly and clearly shows, by example, how to create endpoints and tie them to Hibernate entities.

Once you have an LDAP directory configured and role access on the endpoint methods in PersonDAO updated the way you want it, build the project with Maven. It will create an executable uber jar in the target directory named 'wizbangapi.jar'. You'll find the yaml configuration file 'wba_config.yml' in target/classes. Copy these two files to a directory from which you want to run the application, navigate to that directory, and execute the command:

<code>java -jar wizbangapi.jar server wba_config.yml</code>

That's it - enjoy!
