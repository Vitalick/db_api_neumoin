package controllers;

import dao.ForumDAO;
import dao.PostDAO;
import dao.ThreadDAO;
import dao.UserDAO;
import dao.impl.ForumDAOimpl;
import dao.impl.PostDAOimpl;
import dao.impl.ThreadDAOimpl;
import dao.impl.UserDAOimpl;
import dataSets.ForumDataSet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


@Singleton
@Path("/forum")
public class ForumController {
    private Connection connection;
    private ObjectMapper mapper;
    private final ForumDAO forumDAO;

    public ForumController(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
        forumDAO = new ForumDAOimpl(connection);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response create(String forumString) throws IOException {
        CustomResponse response = forumDAO.create(forumString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("forum") String forumShortName,
                            @QueryParam("related") final List<String> related) throws IOException {

        CustomResponse response = forumDAO.details(forumShortName, related);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

}
