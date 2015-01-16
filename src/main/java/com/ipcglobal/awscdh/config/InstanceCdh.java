package com.ipcglobal.awscdh.config;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

public class InstanceCdh {
	public enum InstanceCdhType {
		LAUNCHER, NAT, MANAGER, MASTER, GATEWAY, WORKER, MONITOR, OTHER
	};

	private Instance instance;
	private String name;
	private InstanceCdhType instanceCdhType;

	public InstanceCdh(Instance instance) throws Exception {
		this.instance = instance;
		updateFromInstance(instance);
	}

	private void updateFromInstance(Instance instance) {
		Map<String, Tag> mapTags = new HashMap<String, Tag>();
		for (Tag tag : instance.getTags())
			mapTags.put(tag.getKey(), tag);
		this.name = mapTags.get("Name").getValue();
		String groupValue = (mapTags.get("group") != null ? mapTags
				.get("group").getValue() : "");
		String logicalId = (mapTags.get("aws:cloudformation:logical-id") != null ? mapTags
				.get("aws:cloudformation:logical-id").getValue() : "");
		String directorTemplateName = (mapTags
				.get("Cloudera-Director-Template-Name") != null ? mapTags.get(
				"Cloudera-Director-Template-Name").getValue() : "");

		if ("master".equals(groupValue))
			this.instanceCdhType = InstanceCdhType.MASTER;
		else if ("worker".equals(groupValue))
			this.instanceCdhType = InstanceCdhType.WORKER;
		else if ("gateway".equals(groupValue))
			this.instanceCdhType = InstanceCdhType.GATEWAY;
		else if ("monitor".equals(groupValue))
			this.instanceCdhType = InstanceCdhType.MONITOR;
		else if ("manager".equals(directorTemplateName))
			this.instanceCdhType = InstanceCdhType.MANAGER;
		else if ("NATInstance".equals(logicalId))
			this.instanceCdhType = InstanceCdhType.NAT;
		else if ("ClusterLauncherInstance".equals(logicalId))
			this.instanceCdhType = InstanceCdhType.LAUNCHER;
		else
			this.instanceCdhType = InstanceCdhType.OTHER;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InstanceCdhType getInstanceCdhType() {
		return instanceCdhType;
	}

	public void setInstanceCdhType(InstanceCdhType instanceCdhType) {
		this.instanceCdhType = instanceCdhType;
	}

}