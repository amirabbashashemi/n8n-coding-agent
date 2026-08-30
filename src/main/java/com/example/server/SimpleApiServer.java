import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.ArrayList;

@Path("/api/messages")
public class SimpleApiServer {

    private List<String> messages = new ArrayList<>();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Message body cannot be empty").build();
        }
        messages.add(message);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    public Response deleteMessages() {
        messages.clear();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}