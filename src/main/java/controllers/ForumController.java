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
import org.codehaus.jackson.node.ObjectNode;

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
    private ObjectMapper mapper;
    private final ForumDAO forumDAO;

    public ForumController() {
        mapper = new ObjectMapper();
        forumDAO = new ForumDAOimpl();
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listPosts")
    public Response listPosts(@QueryParam("forum") String forumShortName,
                         @QueryParam("related") final List<String> related,
                         @QueryParam("since") String since,
                         @QueryParam("limit") String limit,
                         @QueryParam("order") String order) throws IOException {
        CustomResponse response = forumDAO.listPosts(forumShortName, related, since, limit, order);
        return Response.ok(mapper.writeValueAsString(response)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listThreads")
    public Response listThreads(@QueryParam("forum") String forumShortName,
                              @QueryParam("related") final List<String> related,
                              @QueryParam("since") String since,
                              @QueryParam("limit") String limit,
                              @QueryParam("order") String order) throws IOException {
        CustomResponse response = forumDAO.listThreads(forumShortName, related, since, limit, order);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listUsers")
    public Response listUsers(@QueryParam("forum") String forumShortName,
                                @QueryParam("since_id") String since_id,
                                @QueryParam("limit") String limit,
                                @QueryParam("order") String order) throws IOException {
        CustomResponse response = forumDAO.listUsers(forumShortName, since_id, limit, order);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

}
