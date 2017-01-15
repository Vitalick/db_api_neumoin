package controllers;

import dao.ForumDAO;
import dao.PostDAO;
import dao.ThreadDAO;
import dao.UserDAO;
import dao.impl.ForumDAOimpl;
import dao.impl.PostDAOimpl;
import dao.impl.ThreadDAOimpl;
import dao.impl.UserDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("OverlyBroadThrowsClause")
@Singleton
@Path("/")
public class CommonController {

    private final ObjectMapper mapper;

    private final UserDAO userDAO;

    private final ForumDAO forumDAO;

    private final ThreadDAO threadDAO;

    private final PostDAO postDAO;
    
    public CommonController() {
        mapper = new ObjectMapper();
        userDAO = new UserDAOimpl();
        forumDAO = new ForumDAOimpl();
        threadDAO = new ThreadDAOimpl();
        postDAO = new PostDAOimpl();
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

        final CustomResponse response = new CustomResponse("OK", CustomResponse.OK);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("status")
    public Response status() throws IOException {
        final Map<String, Integer> responseBody = new HashMap<>();
        responseBody.put("user", userDAO.count());
        responseBody.put("thread", threadDAO.count());
        responseBody.put("forum", forumDAO.count());
        responseBody.put("post", postDAO.count());

        final CustomResponse response = new CustomResponse(responseBody, CustomResponse.OK);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}