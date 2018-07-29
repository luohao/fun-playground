package hluo.fun.playground.pica;

import hluo.fun.playground.pica.compiler.ClassInfo;
import hluo.fun.playground.pica.compiler.CompilerUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/v1/functions/")
public class PicaMasterResource
{
    private final Map<FunctionId, ClassInfo> functions;

    public PicaMasterResource() {
        functions = new HashMap<>();
    }

    // Submit a lambda function string to the server
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitFunction(String functionSourceCode, @Context HttpServletRequest servletRequest
    )
    {
        System.out.println("======== Submit function ! ========");
        // parse request
        String className = servletRequest.getHeader("X-Pica-Class-Name");

        System.out.println("Class Name : " + className);
        System.out.println("Source Code : \n" + functionSourceCode);

        // compile source code
        CompilerUtils compiler = new CompilerUtils();
        ClassInfo classInfo = compiler.compileSingleSource(className, functionSourceCode);
        FunctionId id = FunctionId.valueOf("test-function-1");
        functions.put(id, classInfo);

        try {
            FunctionAction testFunction = (FunctionAction) compiler.loadClass(classInfo).getDeclaredConstructor(String.class).newInstance("test-function-1");
            testFunction.exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // prepare response
        return Response.ok(id).build();
    }

    // List all running functions
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunctionList()
    {
        System.out.println("======== List all functions! ========");
        List<FunctionId> allFunctions = functions.keySet().stream().collect(Collectors.toList());
        return Response.ok(allFunctions).build();
    }

    // Get function info
    @GET
    @Path("{functionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunction()
    {
        System.out.println("======== List all functions! ========");
        return Response.ok().build();
    }

    @DELETE
    @Path("{functionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopFunction(@PathParam("functionId") FunctionId functionId)
    {
        System.out.println("======== Stop function " + functionId + " ! ========");
        return Response.noContent().build();
    }
}
