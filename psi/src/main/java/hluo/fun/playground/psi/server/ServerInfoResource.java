package hluo.fun.playground.psi.server;

import hluo.fun.playground.psi.cluster.NodeVersion;
import io.airlift.node.NodeInfo;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static hluo.fun.playground.psi.server.NodeState.ACTIVE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/v1/info")
public class ServerInfoResource
{
    private final NodeVersion version;
    private final String environment;
    private final boolean master;
    private final ShutdownHandler shutdownHandler;

    //    private final long startTime = System.nanoTime();
    @Inject
    public ServerInfoResource(NodeVersion nodeVersion, NodeInfo nodeInfo, ServerConfig serverConfig, ShutdownHandler shutdownHandler)
    {
        this.version = requireNonNull(nodeVersion, "nodeVersion is null");
        this.environment = requireNonNull(nodeInfo, "nodeInfo is null").getEnvironment();
        this.master = requireNonNull(serverConfig, "serverConfig is null").isGroupMaster();
        this.shutdownHandler = requireNonNull(shutdownHandler, "serverConfig is null");
    }

    @GET
    @Produces(APPLICATION_JSON)
    public ServerInfo getInfo()
    {
        return new ServerInfo(version, environment, master);
    }

    @PUT
    @Path("state")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response updateState(NodeState state)
            throws WebApplicationException
    {
        requireNonNull(state, "state is null");
        switch (state) {
            case SHUTTING_DOWN:
                shutdownHandler.requestShutdown();
                return Response.ok().build();
            case ACTIVE:
            case INACTIVE:
                throw new WebApplicationException(Response
                        .status(BAD_REQUEST)
                        .type(MediaType.TEXT_PLAIN)
                        .entity(format("Invalid state transition to %s", state))
                        .build());
            default:
                return Response.status(BAD_REQUEST)
                        .type(TEXT_PLAIN)
                        .entity(format("Invalid state %s", state))
                        .build();
        }
    }

    @GET
    @Path("state")
    @Produces(APPLICATION_JSON)
    public NodeState getServerState()
    {
        // FIXME: should return the correct state. For the minimal viable solution, psi only allow one job running in one cluster, therefore all running nodes are active. There
        // should be and IDLE state which refers to the nodes that are not running any tasks.
        return ACTIVE;
    }

    @GET
    @Path("master")
    @Produces(TEXT_PLAIN)
    public Response getServerMaster()
    {
        if (master) {
            return Response.ok().build();
        }
        // return 404 to allow load balancers to only send traffic to the coordinator
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * FOR TESTING.
     */
    @POST
    @Consumes(APPLICATION_JSON)
    public Response testRequest(TaskUpdateRequest taskUpdateRequest)
    {
        System.out.println(taskUpdateRequest);
        return Response.ok().build();
    }
}
