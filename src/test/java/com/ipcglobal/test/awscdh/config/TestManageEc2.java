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

/**
 * The Class TestManageEc2 performs jUnit tests on the class ManageEc2
 */
public class TestManageEc2 {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(TestManageEc2.class);
	
	/** The properties. */
	private Properties properties;

	/**
	 * Test find.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFind() throws Exception {
		ManageEc2 manageEc2 = new ManageEc2( properties );
		List<Instance> instances = manageEc2.findInstances( );
		for( Instance instance : instances ) log.info(instance);
	}

	/**
	 * Test stop.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testStop() throws Exception {
		ManageEc2 manageEc2 = new ManageEc2( properties );
		manageEc2.stop( );
	}

	/**
	 * Test start.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testStart() throws Exception {
		ManageEc2 manageEc2 = new ManageEc2( properties );
		manageEc2.start( );
	}
	
	/**
	 * Before.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void before() throws Exception {
		LogTool.initConsole();
		properties = new Properties( );
		properties.load(new FileInputStream( "/<yourPath>/awscdh/properties/first.properties"  ) );
	}

}

