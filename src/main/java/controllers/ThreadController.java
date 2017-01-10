package controllers;

import dao.ThreadDAO;
import dao.impl.ThreadDAOimpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;


@Singleton
@Path("/thread")
public class ThreadController {
    private Connection connection;
    private ObjectMapper mapper;
    private final ThreadDAO threadDAO;

    public ThreadController(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
        threadDAO = new ThreadDAOimpl(connection);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response create(String threadString) throws IOException {
        CustomResponse response = threadDAO.create(threadString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(@QueryParam("thread") String threadId,
                            @QueryParam("related") final List<String> related) throws IOException {
        CustomResponse response = threadDAO.details(threadId, related);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("close")
    public Response close(String threadString) throws IOException {
        CustomResponse response = threadDAO.close(threadString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("open")
    public Response open(String threadString) throws IOException {
        CustomResponse response = threadDAO.open(threadString);
        String json = mapper.writeValueAsString(response);
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
        CustomResponse response = threadDAO.list(forumShortName, email, since, limit, order);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("remove")
    public Response remove(String threadString) throws IOException {
        CustomResponse response = threadDAO.remove(threadString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("restore")
    public Response restore(String threadString) throws IOException {
        CustomResponse response = threadDAO.restore(threadString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("subscribe")
    public Response subscribe(String subscribeString) throws IOException {
        CustomResponse response = threadDAO.subscribe(subscribeString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("unsubscribe")
    public Response unsubscribe(String unsubscribeString) throws IOException {
        CustomResponse response = threadDAO.unsubscribe(unsubscribeString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    public Response update(String threadString) throws IOException {
        CustomResponse response = threadDAO.update(threadString);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("vote")
    public Response vote(String voteString) throws IOException {
        CustomResponse response = threadDAO.vote(voteString);
        String json = mapper.writeValueAsString(response);
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
        CustomResponse response = threadDAO.listPosts(threadId, sort, since, limit, order);
        String json = mapper.writeValueAsString(response);
        return Response.ok().entity(json).build();
    }
}
