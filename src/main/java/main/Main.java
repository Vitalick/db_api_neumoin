package main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.sql.DataSource;
import java.net.InetSocketAddress;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    private static final int PORT = 8081;

    @SuppressWarnings("PublicField")
    public static DataSource connection;

    public static void main(String[] args) throws Exception {
        System.out.append("Starting at port: ").append(String.valueOf(PORT)).append('\n');

        final Connector connector = new Connector();
        connection = connector.createSource();

        final InetSocketAddress address = new InetSocketAddress("0.0.0.0", PORT);
        final Server server = new Server(address);
        final ServletContextHandler contextHandler = new ServletContextHandler(server, "/db/api/", ServletContextHandler.SESSIONS);

        final ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("javax.ws.rs.Application","main.MyApplication");

        contextHandler.addServlet(servletHolder, "/*");
        server.start();
        server.join();
    }
}
