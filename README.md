##**Implementing Granular Authorization for REST Workflow Executions in IdentityIQ**

### Introduction
SailPoint IdentityIQ provides a [REST API for executing workflows](https://developer.sailpoint.com/docs/api/iiq/launch-workflow), but by default, it does not enforce fine-grained authorization controls on who can trigger specific workflows. The only authorization object that controls this endpoint is the SPRight with name **LaunchWorkflowWebService**, which gives permission to execute all workflows within IdentityIQ. Therefore, this can lead to security risks if sensitive workflows are executed by unauthorized users.

An alternative to address this would be developing a custom **REST IdentityIQ Plugin** that enforces authorization rules before executing workflows via REST API calls. This blog post walks through the design, implementation, and configuration of a sample plugin implementing this solution.

### Problem Statement
IdentityIQ allows users with API access to execute workflows via REST endpoints. However, organizations often require additional authorization mechanisms to ensure that only users with the appropriate permissions can execute certain workflows.

### Solution Overview
The plugin introduces an authorization layer that validates whether a user has the required permissions before executing a workflow. It leverages **SPRights** and **Capabilities** within IdentityIQ to enforce access control rules.

### Key Features
- **Authorization enforcement**: Ensures only authorized users can execute workflows.
- **Customizable rules**: Administrators can define which permission (SPRight or Capability) is required for each workflow.
- **Flexible configuration**: The plugin provides a **Custom Object** with name `RESTWorkflowExecutionAuthorizationConfig` to enable/disable enforcement and define authorization policies.

### Implementation Details

#### 1. REST Endpoint
The plugin adds a new endpoint listening in `$iiqHost/identityiq/plugin/rest/workflowplugin/v1/launchWorkflow`. It captures the user making the request and checks their permissions against a configured set of rules.

#### 2. Plugin Configuration
The plugin introduces two global configuration flags:
- **`enabled`**: Enables the plugin authorization. it can be true or false.
- **`denyRequestsByDefault`**: Determines whether requests should be denied by default if no explicit authorization rule is found.

#### 3. Defining Authorization Rules
The authorization rules are stored in a **Custom Object** named `RESTWorkflowExecutionAuthorizationConfig`. The object contains a property `authorizationConfig`, this property contains a **HashMap** where:
- The **key** is the workflow name.
- The **value** is another HashMap specifying:
  - **`type`**: Either `sailpoint.object.Capability` or `sailpoint.object.SPRight`.
  - **`name`**: The specific Capability or SPRight name required to execute the workflow.

#### 4. Example Configuration
Below is an example of how to define a rule in `RESTWorkflowExecutionAuthorizationConfig`:
```xml
<Custom name="RESTWorkflowExecutionAuthorizationConfig">
	<Attributes>
		<Map>
			<entry key="enabled" value="true"/>
			<entry key="denyRequestsByDefault" value="true"/>
			<entry key="authorizationConfig">
				<value>
					<Map>
						<!--  Config for Workflow `Workflow Example1` -->
						<entry key="Workflow Example1">
							<value>
								<Map>
									<entry key="type" value="sailpoint.object.SPRight"/>
									<entry key="name" value="LaunchWorkflowExample1WebService"/>
								</Map>
							</value>
						</entry>
						<!--  Config for Workflow `Workflow Example2` -->
						<entry key="Workflow Example2">
							<value>
								<Map>
									<entry key="type" value="sailpoint.object.Capability"/>
									<entry key="name" value="Auditor"/>
								</Map>
							</value>
						</entry>
					</Map>
				</value>
			</entry>
		</Map>
	</Attributes>
</Custom>
```
In this example, the `Workflow Example1` workflow can only be executed by users with the `LaunchWorkflowExample1WebService` SPRight. The `Workflow Example2` can only be executed by users with `Auditor` Capability.

### API Documentation

#### **Sample Workflow**

This workflow retrieves the names of members in a specified workgroup.

**Workflow Definition:**
```xml
<Workflow explicitTransitions="true" name="REST API Test Authorization" type="">
  <Variable input="true" name="workgroupName" required="true"/>
  <Variable name="membersNames" output="true"/>
  <Step icon="Default" name="Get workgroup member names" posX="146" posY="26" resultVariable="membersNames">
	<Arg name="workgroupName" value="ref:workgroupName"/>
	<Script>
	  <Source>import sailpoint.object.QueryOptions;
		import sailpoint.object.Filter;
		import sailpoint.object.Identity;
		import java.util.ArrayList;
		import java.util.List;
		
		List result = new ArrayList();
				
		QueryOptions qo = new QueryOptions();
		qo.add(Filter.contains("workgroups.name", workgroupName));
		Iterator itMembers = context.search(Identity.class, qo, "name");

		workgroupMembers = new ArrayList();
		
		while(itMembers.hasNext()){
		  Object[] member = itMembers.next();
		  String memberName = (String) member[0];
		  workgroupMembers.add(memberName);
		}
		Util.flushIterator(itMembers);
		
		return workgroupMembers;
	  </Source>
	</Script>
	<Transition to="Stop"/>
  </Step>
  <Step icon="Start" name="Start" posX="25" posY="20">
	<Transition to="Get workgroup member names"/>
  </Step>
  <Step icon="Stop" name="Stop" posX="280" posY="31"/>
</Workflow>
```

#### **Request: Execute Workflow**
```bash
curl --request POST \  
  --url 'http://localhost:8443/identityiq/plugin/rest/workflowplugin/v1/launchWorkflow' \  
  --header 'Authorization: Basic MTIzNDU2Nzg6MTIz' \  
  --header 'Content-Type: application/json' \  
  --cookie JSESSIONID=0BC9082A53FR89F72438KL0D5F2AB6B2' \  
  --data '{
	"workflowName" : "REST API Test Authorization",
	"args" : {
		"workgroupName" : "Test Workgroup"
	}
}'
```

#### **Response: Success (200 OK)**
This is the response in case the identity has the specified permissions.
```json
{
    "taskResultId": "0a4f800b95a81a0f8195b185aa5f3b3e",
    "attributes": {
        "membersNames": ["12345678", "25101351"]
    }
}
```

#### **Response: Unauthorized (403 Forbidden)**
This is the response in case the identity does not have the specified permissions.
```
HTTP/1.1 403 Forbidden
Content-Type: text/html;charset=utf-8
```

### Source Code
This plugin is **open source**, you can check the code or customize it based on your company needs. You can find the source code on GitHub:

🔗 [GitHub Repository](https://github.com/moisesest3vao/AuthorizationForRESTWorkflowExecutionsSailPointIIQPlugin)

Feel free to fork the repository, submit issues, or suggest improvements!

### Conclusion
This plugin enhances security in IdentityIQ by adding an authorization layer to REST workflow executions. By leveraging IdentityIQ built-in permission structures (SPRights and Capabilities), organizations can enforce granular access control for workflow execution.

The plugin is available for download below. Feel free to try it out and contribute feedback.

**[restworkflowexecutionauthorization.0.1.19.zip](https://github.com/moisesest3vao/AuthorizationForRESTWorkflowExecutionsSailPointIIQPlugin/blob/main/restworkflowexecutionauthorization.0.1.19.zip)**

---
