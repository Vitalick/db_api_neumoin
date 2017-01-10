package main;

import controllers.CommonController;
import controllers.UserController;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("db/api")
public class MyApplication extends Application {
    @Override
    public Set<Object> getSingletons() {
        final HashSet<Object> objects = new HashSet<>();
        Connection connection = Connector.getConnection();
        objects.add(new CommonController(connection));
        objects.add(new UserController(connection));
        return objects;
    }
}
