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


}
