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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.ipcglobal.awscdh.config.ManageCluster;
import com.ipcglobal.awscdh.util.LogTool;

/**
 * The Class TestManageCluster performs jUnit tests on the class ManageCluster
 */
public class TestManageCluster {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(TestManageCluster.class);


	//@Test
	/**
	 * Test stop.
	 *
	 * @throws Exception the exception
	 */
	public void testStop() throws Exception {
		String[] args = { "stop", "/<yourPath>/awscdh/properties/first.properties" };
		ManageCluster.main(args);
	}

	/**
	 * Test start.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testStart() throws Exception {
		String[] args = { "start", "/<yourPath>/awscdh/properties/first.properties"  };
		ManageCluster.main(args);
	}
	
	
	/**
	 * Before.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void before() throws Exception {
		LogTool.initConsole();
	}
	
}
