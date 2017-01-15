package main;

import controllers.*;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("db/api")
public class MyApplication extends Application {
    @Override
    public Set<Object> getSingletons() {
        final HashSet<Object> objects = new HashSet<>();
        objects.add(new CommonController());
        objects.add(new UserController());
        objects.add(new ForumController());
        objects.add(new PostController());
        objects.add(new ThreadController());
        return objects;
    }
}
