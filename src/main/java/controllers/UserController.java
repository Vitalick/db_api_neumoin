package controllers;

import dao.UserDAO;
import dao.impl.UserDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;


@SuppressWarnings("OverlyBroadThrowsClause")
@Singleton
@Path("/user")
public class UserController {
    private final ObjectMapper mapper;

    private final UserDAO userDAO;

    public UserController() {
        mapper = new ObjectMapper();
        userDAO = new UserDAOimpl();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("user") String email) throws IOException {
        final CustomResponse response = userDAO.details(email);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response create(String userString) throws IOException {
        final CustomResponse response = userDAO.create(userString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("follow")
    public Response follow(String followString) throws IOException {
        final CustomResponse response = userDAO.follow(followString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("unfollow")
    public Response unfollow(String unfollowString) throws IOException {
        final CustomResponse response = userDAO.unfollow(unfollowString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listFollowers")
    public Response listFollowers(@QueryParam("user") String email,
                                  @QueryParam("since_id") String since_id,
                                  @QueryParam("order") String order,
                                  @QueryParam("limit") String limit) throws IOException {
        final CustomResponse response = userDAO.listFollowers(email, since_id, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listFollowing")
    public Response listFollowing(@QueryParam("user") String email,
                                  @QueryParam("since_id") String since_id,
                                  @QueryParam("order") String order,
                                  @QueryParam("limit") String limit) throws IOException {
        final CustomResponse response = userDAO.listFollowing(email, since_id, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listPosts")
    public Response listPosts(@QueryParam("user") String email,
                                  @QueryParam("since") String since,
                                  @QueryParam("order") String order,
                                  @QueryParam("limit") String limit) throws IOException {
        final CustomResponse response = userDAO.listPosts(email, since, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("updateProfile")
    public Response updateProfile(String userString) throws IOException {
        final CustomResponse response = userDAO.updateProfile(userString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}
