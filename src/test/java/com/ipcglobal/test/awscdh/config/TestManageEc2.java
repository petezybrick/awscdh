package com.ipcglobal.test.awscdh.config;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.Instance;
import com.ipcglobal.awscdh.config.ManageEc2;
import com.ipcglobal.awscdh.util.LogTool;

public class TestManageEc2 {
	private static final Log log = LogFactory.getLog(TestManageEc2.class);
	private Properties properties;

	@Test
	public void testFind() throws Exception {
		ManageEc2 manageEc2 = new ManageEc2( properties );
		List<Instance> instances = manageEc2.findInstances( );
		for( Instance instance : instances ) log.info(instance);
	}

	@Test
	public void testStop() throws Exception {
		ManageEc2 manageEc2 = new ManageEc2( properties );
		manageEc2.stop( );
	}

	@Test
	public void testStart() throws Exception {
		ManageEc2 manageEc2 = new ManageEc2( properties );
		manageEc2.start( );
	}
	
	@Before
	public void before() throws Exception {
		LogTool.initConsole();
		properties = new Properties( );
		properties.load(new FileInputStream( "/home/pete.zybrick/Development/Workspaces/HadoopSandbox/awscdh/properties/ipc-cdh53.properties" ) );
	}

}

