package main;

import dataSets.ForumDataSet;
import dataSets.UserDataSet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class Main {
    private final static int PORT = 3307;
    public static void main(String[] args) throws Exception {
        System.out.append("Starting at port: ").append(String.valueOf(PORT)).append('\n');
        /*Connection connection = Connector.getConnection();
        String query = "SELECT followed FROM follows WHERE follower = ?;";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, "qwe@qwe");
        ResultSet resultSet = stmt.executeQuery();
        UserDataSet user = new UserDataSet(100, "qsc", "qsc", "qsc", "qsc", false);
        ArrayList<String> following = new ArrayList<>();
        follo*/


        final Server server = new Server(PORT);
        final ServletContextHandler contextHandler = new ServletContextHandler(server, "/db/api/", ServletContextHandler.SESSIONS);

        final ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("javax.ws.rs.Application","main.MyApplication");

        contextHandler.addServlet(servletHolder, "/*");
        server.start();
        server.join();
    }
}
