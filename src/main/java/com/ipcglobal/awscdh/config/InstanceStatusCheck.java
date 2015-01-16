package com.ipcglobal.awscdh.config;

import com.amazonaws.services.ec2.model.Instance;


public class InstanceStatusCheck {
	private Instance instance;
	private String instanceStateName;
	private String instanceStatus;

	public InstanceStatusCheck( Instance instance ) {
		this.instance = instance;
		this.instanceStateName = "";
		this.instanceStatus = "";
	}

	public boolean isInstanceRunning() {
		if ("running".equals(instanceStateName) && "ok".equals(instanceStatus)) return true;
		else return false;
	}

	public boolean isInstanceStopped() {
		if ("stopped".equals(instanceStateName) )return true;
		else return false;
	}

	public void updateState(String instanceStateName, String instanceStatus) {
		this.instanceStateName = instanceStateName;
		this.instanceStatus = instanceStatus;
	}

	public void reset() {
		this.instanceStateName = "";
		this.instanceStatus = "";
	}

	public String getInstanceId() {
		return instance.getInstanceId();
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public String getInstanceStateName() {
		return instanceStateName;
	}

	public void setInstanceStateName(String instanceStateName) {
		this.instanceStateName = instanceStateName;
	}

	public String getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(String instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	@Override
	public String toString() {
		return "InstanceStatusCheck [instanceStateName=" + instanceStateName
				+ ", instanceStatus=" + instanceStatus 
				+ ", instanceId=" + instance.getInstanceId() 
				+ "]";
	}

}