package hluo.fun.playground.psi.testing.kvs;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/kvs")
public class KeyValueStoreResource
{
    private final KeyValueStore db;

    @Inject
    public KeyValueStoreResource(KeyValueStore db)
            throws Exception
    {
        this.db = db;
        db.start();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(KvsRequest request)
    {
        switch (request.getOperation()) {
            case GET:
                return Response.ok(db.get(request)).build();
            case PUT:
                return Response.ok(db.put(request)).build();
            case DELETE:
                return Response.ok(db.delete(request)).build();
            default:
                throw new UnsupportedOperationException();
        }
    }
}
