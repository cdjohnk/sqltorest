package org.portland.sqltorest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.InputStream;

// TODO: Unit testing
// TODO: Documentation
// TODO: Logging
// TODO: Finish recursive query set options
// TODO: Authentication

public class SqlToRest {
    private static Server jettyServer = null;

    public static void main(String[] args) throws Exception {
        InputStream in = SqlToRest.class.getClassLoader().getResourceAsStream("sqltorest.properties");
        System.getProperties().load(in);
        in.close();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server(Integer.parseInt(System.getProperty("sqltorest.port")));
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
             org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        
        jerseyServlet.setInitParameter("javax.ws.rs.Application", "org.portland.sqltorest.APIConfig");

        try {
            jettyServer.start();
            jettyServer.join();
            // if the server is stopped, give it some time to stop gracefully before destroying it.
            Thread.sleep(Integer.parseInt(System.getProperty("sqltorest.server.delay")));
        } finally {
            jettyServer.destroy();
        }
    }

    public static void shutdown() throws Exception {
        if (jettyServer != null && jettyServer.isRunning()) {
            jettyServer.stop();
        }
    }
}
