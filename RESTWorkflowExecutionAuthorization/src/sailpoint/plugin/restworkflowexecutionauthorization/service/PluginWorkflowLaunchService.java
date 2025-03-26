package sailpoint.plugin.restworkflowexecutionauthorization.service;

import java.util.List;
import java.util.Map;

import sailpoint.api.SailPointContext;
import sailpoint.api.Workflower;
import sailpoint.authorization.UnauthorizedAccessException;
import sailpoint.object.Capability;
import sailpoint.object.Custom;
import sailpoint.object.WorkflowLaunch;
import sailpoint.plugin.restworkflowexecutionauthorization.dto.WorkflowLaunchDto;

@SuppressWarnings("unchecked")
public class PluginWorkflowLaunchService {
	
	public WorkflowLaunchDto launchWorkflow(Map<Object, Object> body, List<String> userRights, List<Capability> userCapabilities, SailPointContext context) throws Exception {
		
		if (body == null) {
			throw new Exception("Empty body");
		}
		
		// Workflow name
		String wf = (String) body.get("workflowName");
		
		Custom config = context.getObjectByName(Custom.class, "RESTWorkflowExecutionAuthorizationConfig");
		
		if(isAuthorizationEnabled(config)) {
			if (!isAuthorized(wf, userRights, userCapabilities, config)) {
				throw new UnauthorizedAccessException("Unauthorized");
			}
		}
		
		// Arguments
		Map<String, Object> args =  (Map<String, Object>) body.get("args");
		
		// Create WorkflowLaunch and set values
		WorkflowLaunch wflaunch = new WorkflowLaunch();
		wflaunch.setWorkflowName(wf);
		wflaunch.setWorkflowRef(wf);
		wflaunch.setCaseName(wf + " - REST");
		wflaunch.setVariables(args);
	
		// Create Workflower and launch workflow from WorkflowLaunch
		Workflower workflower = new Workflower(context);
		WorkflowLaunch launch = workflower.launch(wflaunch);
		
		return new WorkflowLaunchDto(launch);
	}

	private boolean isAuthorized(String wf, List<String> rights, List<Capability> capabilities, Custom config) throws Exception {
		Map<String, Object> authConfig = (Map<String, Object>) config.get("authorizationConfig");
		Map<String, String> wfAuthConfig = (Map<String, String>) authConfig.get(wf);
		
		if(null == wfAuthConfig) {
			if ("false".equalsIgnoreCase((String) config.get("denyRequestsByDefault"))) {
				return true;
			} else {
				throw new UnauthorizedAccessException("Unauthorized");
			}
		}
		
		String type = (String) wfAuthConfig.get("type");
		String name = (String) wfAuthConfig.get("name");
		
		boolean result = false;
		
		if("sailpoint.object.Capability".equalsIgnoreCase(type)) {
			for (Capability capability : capabilities) {
				if (capability.getName().equalsIgnoreCase(name)) {
					result = true;
				}
			}
		}
		
		if("sailpoint.object.SPRight".equalsIgnoreCase(type)) {
			if (rights.contains(name)) {
				result = true;
			}
		}
		
		
		return result;
	}

	private boolean isAuthorizationEnabled(Custom config) {
		String value = (String) config.get("enabled");
		return "true".equalsIgnoreCase(value);
	}
}
