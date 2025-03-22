package sailpoint.plugin.restworkflowexecutionauthorization.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.authorization.UnauthorizedAccessException;
import sailpoint.object.Capability;
import sailpoint.plugin.restworkflowexecutionauthorization.dto.WorkflowLaunchDto;
import sailpoint.plugin.restworkflowexecutionauthorization.service.PluginWorkflowLaunchService;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.rest.plugin.BasePluginResource;


@Path("workflowplugin/v1")
public class AuthorizedLaunchedWorkflowsResource extends BasePluginResource {
	public static final Log log = LogFactory.getLog(AuthorizedLaunchedWorkflowsResource.class);
	private final PluginWorkflowLaunchService service = new PluginWorkflowLaunchService();

	@POST
	@Path("launchWorkflow")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@AllowAll
	public Response postLaunchedWorkflows(Map<Object, Object> launchWorkflowRequest) throws Exception {
		log.debug("POST LaunchedWorkflows");
		SailPointContext context = getContext();
		try {
			WorkflowLaunchDto dto = service.launchWorkflow(launchWorkflowRequest, 
					new ArrayList<String>(getLoggedInUserRights()), 
					new ArrayList<Capability>(getLoggedInUserCapabilities()), 
					context
				   );
			ResponseBuilder responseBuilder = Response.ok(dto);
			return responseBuilder.build();
		} catch (UnauthorizedAccessException e) {
			return Response.status(403).build();
		}
		
	}

	@Override
	public String getPluginName() {
		return "restworkflowexecutionauthorization";
	}
}
