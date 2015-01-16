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

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.v6.ClustersResourceV6;
import com.cloudera.api.v6.RootResourceV6;
import com.cloudera.api.v6.ServicesResourceV6;
import com.ipcglobal.awscdh.config.ManageCdh;
import com.ipcglobal.awscdh.util.LogTool;


/**
 * The Class TestManageCdh performs jUnit tests on the class ManageCdh
 */
public class TestManageCdh {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(TestManageCdh.class);
	
	/** The properties. */
	private Properties properties;

	
	//@Test
	/**
	 * Test find api clusters.
	 *
	 * @throws Exception the exception
	 */
	public void testFindApiClusters( ) throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		List<ApiCluster> apiClusters = manageCdh.findApiClusters( );
		log.info(apiClusters);
	}
	
	//@Test
	/**
	 * Test service names.
	 *
	 * @throws Exception the exception
	 */
	public void testServiceNames( ) throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		ApiCluster apiCluster = manageCdh.findCluster( );
		RootResourceV6 apiRoot = new ClouderaManagerClientBuilder().withHost( manageCdh.getClouderaManagerDnsNameOrIpAddress() )
				.withUsernamePassword( manageCdh.getClouderaManagerAdminLogin(), manageCdh.getClouderaManagerAdminPassword() ).build().getRootV6();
		ClustersResourceV6 clustersResource = apiRoot.getClustersResource();
		ServicesResourceV6 servicesResource = apiRoot.getClustersResource().getServicesResource(
				apiCluster.getName());
		
		List<String> serviceNames = manageCdh.findServiceNamesInCluster( apiCluster, servicesResource );
		log.info( "Service Names: " + serviceNames );
	}

	

	//@Test
	/**
	 * Test stop.
	 *
	 * @throws Exception the exception
	 */
	public void testStop() throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		manageCdh.stop( );
	}

	/**
	 * Test start.
	 *
	 * @throws Exception the exception
	 */
	//@Test
	public void testStart() throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		manageCdh.start( );
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
		properties.load(new FileInputStream( "/<yourPath>/awscdh/properties/first.properties" ) );
	}
	
}
