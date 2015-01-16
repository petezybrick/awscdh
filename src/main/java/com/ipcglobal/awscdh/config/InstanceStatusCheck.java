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

import com.amazonaws.services.ec2.model.Instance;


/**
 * The Class InstanceStatusCheck.
 * Contains the 
 * 		State - "Instance State" value from AWS EC2 console, i.e. running, stopped, etc.
 * 		Status - "Status Checks" value from AWS EC2 console, i.e. ok, initializing - 
 */
public class InstanceStatusCheck {
	
	/** The instance. */
	private Instance instance;
	
	/** The instance state name. */
	private String instanceStateName;
	
	/** The instance status. */
	private String instanceStatus;

	/**
	 * Instantiates a new instance status check.
	 *
	 * @param instance the instance
	 */
	public InstanceStatusCheck( Instance instance ) {
		this.instance = instance;
		this.instanceStateName = "";
		this.instanceStatus = "";
	}

	/**
	 * Checks if is instance running.
	 *
	 * @return true, if is instance running
	 */
	public boolean isInstanceRunning() {
		if ("running".equals(instanceStateName) && "ok".equals(instanceStatus)) return true;
		else return false;
	}

	/**
	 * Checks if is instance stopped.
	 *
	 * @return true, if is instance stopped
	 */
	public boolean isInstanceStopped() {
		if ("stopped".equals(instanceStateName) )return true;
		else return false;
	}

	/**
	 * Update state.
	 *
	 * @param instanceStateName the instance state name
	 * @param instanceStatus the instance status
	 */
	public void updateState(String instanceStateName, String instanceStatus) {
		this.instanceStateName = instanceStateName;
		this.instanceStatus = instanceStatus;
	}

	/**
	 * Reset.
	 */
	public void reset() {
		this.instanceStateName = "";
		this.instanceStatus = "";
	}

	/**
	 * Gets the instance id.
	 *
	 * @return the instance id
	 */
	public String getInstanceId() {
		return instance.getInstanceId();
	}

	/**
	 * Gets the single instance of InstanceStatusCheck.
	 *
	 * @return single instance of InstanceStatusCheck
	 */
	public Instance getInstance() {
		return instance;
	}

	/**
	 * Sets the instance.
	 *
	 * @param instance the new instance
	 */
	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	/**
	 * Gets the instance state name.
	 *
	 * @return the instance state name
	 */
	public String getInstanceStateName() {
		return instanceStateName;
	}

	/**
	 * Sets the instance state name.
	 *
	 * @param instanceStateName the new instance state name
	 */
	public void setInstanceStateName(String instanceStateName) {
		this.instanceStateName = instanceStateName;
	}

	/**
	 * Gets the instance status.
	 *
	 * @return the instance status
	 */
	public String getInstanceStatus() {
		return instanceStatus;
	}

	/**
	 * Sets the instance status.
	 *
	 * @param instanceStatus the new instance status
	 */
	public void setInstanceStatus(String instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InstanceStatusCheck [instanceStateName=" + instanceStateName
				+ ", instanceStatus=" + instanceStatus 
				+ ", instanceId=" + instance.getInstanceId() 
				+ "]";
	}

}