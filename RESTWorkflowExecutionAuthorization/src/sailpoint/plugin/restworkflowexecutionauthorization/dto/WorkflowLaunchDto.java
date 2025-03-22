package sailpoint.plugin.restworkflowexecutionauthorization.dto;

import java.util.Map;

import sailpoint.object.WorkflowLaunch;

public class WorkflowLaunchDto {
	private String taskResultId;
	private Map<String, Object> attributes;
	
	public WorkflowLaunchDto(WorkflowLaunch launch) {
		this.taskResultId = launch.getTaskResult() != null ? launch.getTaskResult().getId() : null;
		this.attributes = launch.getTaskResult().getAttributes().getMap();
	}

	public String getTaskResultId() {
		return taskResultId;
	}

	public void setTaskResultId(String taskResultId) {
		this.taskResultId = taskResultId;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	
}
