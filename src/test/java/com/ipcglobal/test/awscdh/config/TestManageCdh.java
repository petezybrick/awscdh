package com.ipcglobal.test.awscdh.config;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.v6.ClustersResourceV6;
import com.cloudera.api.v6.RootResourceV6;
import com.cloudera.api.v6.ServicesResourceV6;
import com.ipcglobal.awscdh.config.ManageCdh;
import com.ipcglobal.awscdh.util.LogTool;

public class TestManageCdh {
	private static final Log log = LogFactory.getLog(TestManageCdh.class);
	private Properties properties;

	
	//@Test
	public void testFindApiClusters( ) throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		List<ApiCluster> apiClusters = manageCdh.findApiClusters( );
		log.info(apiClusters);
	}
	
	//@Test
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
	public void testStop() throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		manageCdh.stop( );
	}

	@Test
	public void testStart() throws Exception {
		ManageCdh manageCdh = new ManageCdh( properties );
		manageCdh.start( );
	}
	
	
	@Before
	public void before() throws Exception {
		LogTool.initConsole();
		properties = new Properties( );
		properties.load(new FileInputStream( "/home/pete.zybrick/Development/Workspaces/HadoopSandbox/awscdh/properties/ipc-cdh53.properties" ) );
	}
	
}
