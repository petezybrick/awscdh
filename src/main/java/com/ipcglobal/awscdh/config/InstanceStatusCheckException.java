package com.ipcglobal.awscdh.config;

import java.util.List;

public class InstanceStatusCheckException extends Exception {
	private static final long serialVersionUID = 1354197199974161175L;
	private String instanceTargetStatus;
	private List<InstanceStatusCheck> instanceStatusChecks;

	public InstanceStatusCheckException( String instanceTargetStatus,
			List<InstanceStatusCheck> instanceStatusChecks) {
		super();
		this.instanceTargetStatus = instanceTargetStatus;
		this.instanceStatusChecks = instanceStatusChecks;
	}

	public String getInstanceTargetStatus() {
		return instanceTargetStatus;
	}

	public void setInstanceTargetStatus(String instanceTargetStatus) {
		this.instanceTargetStatus = instanceTargetStatus;
	}

	public List<InstanceStatusCheck> getInstanceStatusChecks() {
		return instanceStatusChecks;
	}

	public void setInstanceStatusChecks(
			List<InstanceStatusCheck> instanceStatusChecks) {
		this.instanceStatusChecks = instanceStatusChecks;
	}
}
