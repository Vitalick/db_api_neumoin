package controllers;

import dao.ForumDAO;
import dao.impl.ForumDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;


@SuppressWarnings("OverlyBroadThrowsClause")
@Singleton
@Path("/forum")
public class ForumController {
    private final ObjectMapper mapper;

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
        final CustomResponse response = forumDAO.create(forumString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("forum") String forumShortName,
                            @QueryParam("related") final List<String> related) throws IOException {

        final CustomResponse response = forumDAO.details(forumShortName, related);
        final String json = mapper.writeValueAsString(response);
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
        final CustomResponse response = forumDAO.listPosts(forumShortName, related, since, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listThreads")
    public Response listThreads(@QueryParam("forum") String forumShortName,
                              @QueryParam("related") final List<String> related,
                              @QueryParam("since") String since,
                              @QueryParam("limit") String limit,
                              @QueryParam("order") String order) throws IOException {
        final CustomResponse response = forumDAO.listThreads(forumShortName, related, since, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listUsers")
    public Response listUsers(@QueryParam("forum") String forumShortName,
                                @QueryParam("since_id") String since_id,
                                @QueryParam("limit") String limit,
                                @QueryParam("order") String order) throws IOException {
        final CustomResponse response = forumDAO.listUsers(forumShortName, since_id, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

}
