/**
 *    Copyright 2015 IPC Global (http://www.ipc-global.com) and others.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ipcglobal.awscdh.config;

import java.util.List;


/**
 * The Class InstanceStatusCheckException.
 * Thrown when EC2 Start or Stop times out - all instances have not reached the desired state and status within the timeout period
 * 		For Start, the desired State/Status are "running" and "ok"
 * 		For Stop, the desired State is "stopped" (note: Status isn't checked for Stop)
 */
public class InstanceStatusCheckException extends Exception {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1354197199974161175L;
	
	/** The instance target status. */
	private String instanceTargetStatus;
	
	/** The instance status checks. */
	private List<InstanceStatusCheck> instanceStatusChecks;

	/**
	 * Instantiates a new instance status check exception.
	 *
	 * @param instanceTargetStatus the instance target status
	 * @param instanceStatusChecks the instance status checks
	 */
	public InstanceStatusCheckException( String instanceTargetStatus,
			List<InstanceStatusCheck> instanceStatusChecks) {
		super();
		this.instanceTargetStatus = instanceTargetStatus;
		this.instanceStatusChecks = instanceStatusChecks;
	}

	/**
	 * Gets the instance target status.
	 *
	 * @return the instance target status
	 */
	public String getInstanceTargetStatus() {
		return instanceTargetStatus;
	}

	/**
	 * Sets the instance target status.
	 *
	 * @param instanceTargetStatus the new instance target status
	 */
	public void setInstanceTargetStatus(String instanceTargetStatus) {
		this.instanceTargetStatus = instanceTargetStatus;
	}

	/**
	 * Gets the instance status checks.
	 *
	 * @return the instance status checks
	 */
	public List<InstanceStatusCheck> getInstanceStatusChecks() {
		return instanceStatusChecks;
	}

	/**
	 * Sets the instance status checks.
	 *
	 * @param instanceStatusChecks the new instance status checks
	 */
	public void setInstanceStatusChecks(
			List<InstanceStatusCheck> instanceStatusChecks) {
		this.instanceStatusChecks = instanceStatusChecks;
	}
}
