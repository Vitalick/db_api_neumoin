package controllers;

import dao.ThreadDAO;
import dao.impl.ThreadDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;


@SuppressWarnings("OverlyBroadThrowsClause")
@Singleton
@Path("/thread")
public class ThreadController {
    private final ObjectMapper mapper;

    private final ThreadDAO threadDAO;

    public ThreadController() {
        mapper = new ObjectMapper();
        threadDAO = new ThreadDAOimpl();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response create(String threadString) throws IOException {
        final CustomResponse response = threadDAO.create(threadString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("thread") String threadId,
                            @QueryParam("related") final List<String> related) throws IOException {
        final CustomResponse response = threadDAO.details(threadId, related);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("close")
    public Response close(String threadString) throws IOException {
        final CustomResponse response = threadDAO.close(threadString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("open")
    public Response open(String threadString) throws IOException {
        final CustomResponse response = threadDAO.open(threadString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    public Response list(@QueryParam("forum") String forumShortName,
                         @QueryParam("user") String email,
                         @QueryParam("since") String since,
                         @QueryParam("limit") String limit,
                         @QueryParam("order") String order) throws IOException {
        final CustomResponse response = threadDAO.list(forumShortName, email, since, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("remove")
    public Response remove(String threadString) throws IOException {
        final CustomResponse response = threadDAO.remove(threadString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("restore")
    public Response restore(String threadString) throws IOException {
        final CustomResponse response = threadDAO.restore(threadString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("subscribe")
    public Response subscribe(String subscribeString) throws IOException {
        final CustomResponse response = threadDAO.subscribe(subscribeString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("unsubscribe")
    public Response unsubscribe(String unsubscribeString) throws IOException {
        final CustomResponse response = threadDAO.unsubscribe(unsubscribeString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    public Response update(String threadString) throws IOException {
        final CustomResponse response = threadDAO.update(threadString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("vote")
    public Response vote(String voteString) throws IOException {
        final CustomResponse response = threadDAO.vote(voteString);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listPosts")
    public Response listPosts(@QueryParam("thread") String threadId,
                         @QueryParam("sort") String sort,
                         @QueryParam("since") String since,
                         @QueryParam("limit") String limit,
                         @QueryParam("order") String order) throws IOException {
        final CustomResponse response = threadDAO.listPosts(threadId, sort, since, limit, order);
        final String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}
