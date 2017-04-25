package org.portland.wizbangapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.params.IntParam;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.portland.wizbangapi.WizBangApplication;
import org.portland.wizbangapi.ldap.User;
import org.portland.wizbangapi.model.entities.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Objects;


@Path("/person")
public class PersonDao {
    private static final Logger LOG = LoggerFactory.getLogger(PersonDao.class);
    private final SessionFactory sessionFactory;

    public PersonDao(SessionFactory factory) {
            this.sessionFactory = Objects.requireNonNull(factory);
    }

    private Query getQuery(Session session, String sql) {
        return session.createQuery(sql)
                .setMaxResults(WizBangApplication.queryLimit);
    }

    private String getJson(List result) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = "";
        try {
            json = ow.writeValueAsString(result);
        }
        catch (Throwable t) {
            LOG.debug("Failed to create json from query result.", t);
        }
        return json;
    }

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public String getAll(@Auth User user) {
        Session session = sessionFactory.openSession();
        List result = getQuery(session, "from Person" )
                .list();
        session.close();
        return getJson(result);
    }

    @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public int create(Person person) {
        Session session = sessionFactory.openSession();
        session.saveOrUpdate(Objects.requireNonNull(person));
        return person.getId();
    }

    @RolesAllowed("staff")
    @GET @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOne(@Context SecurityContext securityContext, @PathParam("id") IntParam id) {
        Session session = sessionFactory.openSession();
        List result = getQuery(session, "from Person where id = :id" )
                .setParameter("id", id.get())
                .list();
        session.close();
        return getJson(result);
    }
}
