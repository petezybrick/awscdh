package com.ipcglobal.test.awscdh.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.ipcglobal.awscdh.config.ManageCluster;
import com.ipcglobal.awscdh.util.LogTool;

public class TestManageCluster {
	private static final Log log = LogFactory.getLog(TestManageCluster.class);


	//@Test
	public void testStop() throws Exception {
		String[] args = { "stop", "/home/pete.zybrick/Development/Workspaces/HadoopSandbox/awscdh/properties/ipc-cdh53.properties" };
		ManageCluster.main(args);
	}

	@Test
	public void testStart() throws Exception {
		String[] args = { "start", "/home/pete.zybrick/Development/Workspaces/HadoopSandbox/awscdh/properties/ipc-cdh53.properties" };
		ManageCluster.main(args);
	}
	
	
	@Before
	public void before() throws Exception {
		LogTool.initConsole();
	}
	
}
