package controllers;

import dao.ForumDAO;
import dao.PostDAO;
import dao.ThreadDAO;
import dao.UserDAO;
import dao.impl.ForumDAOimpl;
import dao.impl.PostDAOimpl;
import dao.impl.ThreadDAOimpl;
import dao.impl.UserDAOimpl;
import dataSets.UserDataSet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;


@Singleton
@Path("/")
public class CommonController {
    private Connection connection;
    private ObjectMapper mapper;
    private final UserDAO userDAO;
    private final ForumDAO forumDAO;
    private final ThreadDAO threadDAO;
    private final PostDAO postDAO;
    
    public CommonController(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
        userDAO = new UserDAOimpl(connection);
        forumDAO = new ForumDAOimpl(connection);
        threadDAO = new ThreadDAOimpl(connection);
        postDAO = new PostDAOimpl(connection);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("clear")
    public Response clear() throws IOException {
        userDAO.truncateTable();
        forumDAO.truncateTable();
        threadDAO.truncateTable();
        postDAO.truncateTable();

        final CustomResponse response = new CustomResponse();
        response.setResponse("OK");
        response.setCode(CustomResponse.OK);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("status")
    public Response status() throws IOException {
        Map<String, Integer> responseBody = new HashMap<>();
        responseBody.put("user", userDAO.count());
        responseBody.put("thread", threadDAO.count());
        responseBody.put("forum", forumDAO.count());
        responseBody.put("post", postDAO.count());

        final CustomResponse response = new CustomResponse();
        response.setResponse(responseBody);
        response.setCode(CustomResponse.OK);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}
