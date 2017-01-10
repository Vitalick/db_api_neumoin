package controllers;

import javax.inject.Singleton;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.Collection;

@Singleton
@Path("/user")
public class UserController {
    private Connection connection;

    public UserController(Connection connection) { this.connection = connection; }

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(/*@QueryParam("user") String email) {
    }*/

}
