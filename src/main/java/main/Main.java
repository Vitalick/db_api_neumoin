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
        Connection connection = Connector.getConnection();
            String query = "INSERT IGNORE INTO subscribed (user, thread) VALUES (?,?)";
            /*PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, "qaz@qaz");
            stmt.setString(2, "1");
            stmt.executeUpdate();
            stmt.close();*/
        Statement stmt = connection.createStatement();
        stmt.execute("SET NAMES UTF8;");
        stmt.execute("SET CHARACTER SET UTF8;");
        //stmt.execute("INSERT INTO FORUM (name, short_name, user) VALUES ('ывы', 'ывалро', 'example@mail.ru')");
        //stmt.executeQuery("SET CHARACTER SET 'UTF8'");
        stmt.close();

        int q = 65;
        byte code = (byte)q;
        System.out.println((char)code);


        final Server server = new Server(PORT);
        final ServletContextHandler contextHandler = new ServletContextHandler(server, "/db/api/", ServletContextHandler.SESSIONS);

        final ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("javax.ws.rs.Application","main.MyApplication");

        contextHandler.addServlet(servletHolder, "/*");
        server.start();
        server.join();
    }
}
