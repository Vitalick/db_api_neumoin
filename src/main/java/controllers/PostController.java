package controllers;

import dao.ForumDAO;
import dao.PostDAO;
import dao.impl.ForumDAOimpl;
import dao.impl.PostDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;


@Singleton
@Path("/post")
public class PostController {
    private Connection connection;
    private ObjectMapper mapper;
    private final PostDAO postDAO;

    public PostController(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
        postDAO = new PostDAOimpl(connection);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response create(String postString) throws IOException {
        CustomResponse response = postDAO.create(postString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("post") String postId,
                            @QueryParam("related") final List<String> related) throws IOException {
        CustomResponse response = postDAO.details(postId, related);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    public Response list(@QueryParam("forum") String forumShortName,
                         @QueryParam("thread") String threadId,
                         @QueryParam("since") String since,
                         @QueryParam("limit") String limit,
                         @QueryParam("order") String order) throws IOException {
        CustomResponse response = postDAO.list(forumShortName, threadId, since, limit, order);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("remove")
    public Response remove(String postString) throws IOException {
        CustomResponse response = postDAO.removeOrRestore(postString, "remove");
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("restore")
    public Response restore(String postString) throws IOException {
        CustomResponse response = postDAO.removeOrRestore(postString, "restore");
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("vote")
    public Response vote(String voteString) throws IOException {
        CustomResponse response = postDAO.vote(voteString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    public Response update(String postString) throws IOException {
        CustomResponse response = postDAO.update(postString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}
