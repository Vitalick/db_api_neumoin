package controllers;

import dao.PostDAO;
import dao.impl.PostDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;


@SuppressWarnings("OverlyBroadThrowsClause")
@Singleton
@Path("/post")
public class PostController {
    private final ObjectMapper mapper;

    private final PostDAO postDAO;

    public PostController() {
        mapper = new ObjectMapper();
        postDAO = new PostDAOimpl();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response create(String postString) throws IOException {
        final CustomResponse response = postDAO.create(postString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("post") String postId,
                            @QueryParam("related") final List<String> related) throws IOException {
        final CustomResponse response = postDAO.details(postId, related);
        final String json = mapper.writeValueAsString(response);
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
        final CustomResponse response = postDAO.list(forumShortName, threadId, since, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("remove")
    public Response remove(String postString) throws IOException {
        final CustomResponse response = postDAO.removeOrRestore(postString, "remove");
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("restore")
    public Response restore(String postString) throws IOException {
        final CustomResponse response = postDAO.removeOrRestore(postString, "restore");
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("vote")
    public Response vote(String voteString) throws IOException {
        final CustomResponse response = postDAO.vote(voteString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    public Response update(String postString) throws IOException {
        final CustomResponse response = postDAO.update(postString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}
