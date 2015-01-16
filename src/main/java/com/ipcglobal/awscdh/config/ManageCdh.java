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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceState;
import com.cloudera.api.v6.ClustersResourceV6;
import com.cloudera.api.v6.RootResourceV6;
import com.cloudera.api.v6.ServicesResourceV6;
import com.ipcglobal.awscdh.util.Utils;

/**
 * The Class ManageCdh.
 * Start or Stop CDH Services via the CDH API
 */
public class ManageCdh {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(ManageCdh.class);
	
	/** The Constant POLLING_INTERVAL_SECONDS. */
	private static final Long POLLING_INTERVAL_SECONDS = 60L;
	
	/** The polling verbose. */
	private static boolean POLLING_VERBOSE = true;
	
	/** The cloudera manager dns name or ip address. */
	private String clouderaManagerDnsNameOrIpAddress;
	
	/** The cloudera manager admin login. */
	private String clouderaManagerAdminLogin;
	
	/** The cloudera manager admin password. */
	private String clouderaManagerAdminPassword;
	
	/** The timeout minutes start cdh cluster. */
	private int timeoutMinutesStartCdhCluster;
	
	/** The timeout minutes stop cdh cluster. */
	private int timeoutMinutesStopCdhCluster;
	
	
	/**
	 * Instantiates a new manage cdh.
	 *
	 * @param properties the properties
	 * @throws Exception the exception
	 */
	public ManageCdh( Properties properties ) throws Exception {
		this.clouderaManagerDnsNameOrIpAddress = properties.getProperty( "clouderaManagerDnsNameOrIpAddress" ).trim();
		this.clouderaManagerAdminLogin = properties.getProperty( "clouderaManagerAdminLogin" ).trim();
		this.clouderaManagerAdminPassword = properties.getProperty( "clouderaManagerAdminPassword" ).trim();
		this.timeoutMinutesStartCdhCluster = Integer.parseInt( properties.getProperty("timeoutMinutesStartCdhCluster") );
		this.timeoutMinutesStopCdhCluster = Integer.parseInt( properties.getProperty("timeoutMinutesStopCdhCluster") );
	}
	
	
	/**
	 * Find cluster.
	 *
	 * @return the api cluster
	 * @throws Exception the exception
	 */
	public ApiCluster findCluster( ) throws Exception {
		List<ApiCluster> apiClusters = findApiClusters( );
		log.info( "findCluster clouderaManagerDnsNameOrIpAddress=" + clouderaManagerDnsNameOrIpAddress + ", apiClusters.size()=" + apiClusters.size() );
		if( apiClusters.size() == 0 || apiClusters.size() > 1 ) throw new Exception("ManageCdh only supported for one cluster per CM");
		return apiClusters.get(0);
	}

	
	/**
	 * Find api clusters.
	 *
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<ApiCluster> findApiClusters( ) throws Exception {
		List<ApiCluster> apiClusters = new ArrayList<ApiCluster>();
		try {
			RootResourceV6 apiRoot = new ClouderaManagerClientBuilder().withHost( clouderaManagerDnsNameOrIpAddress )
					.withUsernamePassword( clouderaManagerAdminLogin, clouderaManagerAdminPassword ).build().getRootV6();
			ApiClusterList apiClusterList = apiRoot.getClustersResource().readClusters(DataView.EXPORT);
			for( ApiCluster apiCluster : apiClusterList ) apiClusters.add( apiCluster );

		} catch( Exception e ) {
			e.printStackTrace();
			log.error(e);
			throw e;
		}
		return apiClusters;
	}
	
	
	/**
	 * Stop CDH Services.
	 */
	public void stop( ) {
		try {
			ApiCluster apiCluster = findCluster( );
			log.info("Stopping cluster " + apiCluster.getDisplayName() + " on " + clouderaManagerDnsNameOrIpAddress );
			RootResourceV6 apiRoot = new ClouderaManagerClientBuilder().withHost( clouderaManagerDnsNameOrIpAddress )
					.withUsernamePassword( clouderaManagerAdminLogin, clouderaManagerAdminPassword ).build().getRootV6();
			ClustersResourceV6 clustersResource = apiRoot.getClustersResource();
			ServicesResourceV6 servicesResource = apiRoot.getClustersResource().getServicesResource(
					apiCluster.getName());
			
			List<String> serviceNames = findServiceNamesInCluster( apiCluster, servicesResource );
			// Stop the cluster
			clustersResource.stopCommand( apiCluster.getName() );
			
			try {
				pollServicesTargetState( ApiServiceState.STOPPED, serviceNames, servicesResource, timeoutMinutesStopCdhCluster );
			} catch( PollServicesTargetStateTimeoutException e ) {
				throw new Exception("Cluster Stop failure - cluster: " + apiCluster.getDisplayName() + ", services not stopped: " + e.getServicesNotInTargetState() );
			}
			log.info("Stopped cluster " + apiCluster.getDisplayName() + " on " + clouderaManagerDnsNameOrIpAddress );
			
		} catch( Exception e ) {
			e.printStackTrace();
			log.error(e);
		}
	}

	
	/**
	 * Start CDH Services.
	 */
	public void start( ) {
		try {
			ApiCluster apiCluster = findCluster( );
			log.info("Starting cluster " + apiCluster.getDisplayName() + " on " + clouderaManagerDnsNameOrIpAddress );
			RootResourceV6 apiRoot = new ClouderaManagerClientBuilder().withHost( clouderaManagerDnsNameOrIpAddress )
					.withUsernamePassword( clouderaManagerAdminLogin, clouderaManagerAdminPassword ).build().getRootV6();
			ClustersResourceV6 clustersResource = apiRoot.getClustersResource();
			ServicesResourceV6 servicesResource = apiRoot.getClustersResource().getServicesResource(
					apiCluster.getName());
			
			List<String> serviceNames = findServiceNamesInCluster( apiCluster, servicesResource );
			// Stop the cluster
			clustersResource.startCommand( apiCluster.getName() );
			try {
				pollServicesTargetState( ApiServiceState.STARTED, serviceNames, servicesResource, timeoutMinutesStartCdhCluster );
			} catch( PollServicesTargetStateTimeoutException e ) {
				throw new Exception("Cluster Start failure - cluster: " + apiCluster.getDisplayName() + ", services not started: " + e.getServicesNotInTargetState() );
			}
			log.info("Started cluster " + apiCluster.getDisplayName() + " on " + clouderaManagerDnsNameOrIpAddress );
			
		} catch( WebApplicationException e ) {
			e.printStackTrace();
			log.error(e.getCause());
			log.error(e.getMessage());
			log.error(e.getResponse());
		} catch( Exception e ) {
			e.printStackTrace();
			log.error(e);
			log.error(e);
		}
	}
	
	
	/**
	 * Poll services target state.
	 *
	 * @param apiServiceStateTarget the api service state target
	 * @param serviceNames the service names
	 * @param servicesResource the services resource
	 * @param pollTimeoutMins the poll timeout mins
	 * @throws Exception the exception
	 */
	private void pollServicesTargetState( ApiServiceState apiServiceStateTarget, List<String> serviceNames, ServicesResourceV6 servicesResource,
			int pollTimeoutMins ) throws Exception {
		// Poll the services every 15 seconds until all stopped or timeout
		long before = System.currentTimeMillis();
		long pollTimeout = System.currentTimeMillis() + (pollTimeoutMins * 60000L);
		while( true ) {
			StringBuilder servicesNotStopped = new StringBuilder();
			for( String serviceName : serviceNames ) {
				ApiService apiService = servicesResource.readService(serviceName);
				if( apiServiceStateTarget != apiService.getServiceState() ) 
					servicesNotStopped.append(serviceName).append("(").append(apiService.getServiceState().toString()).append(") ");					
			}
			if( servicesNotStopped.length() == 0 ) break;	// all services stopped
			// Timeout
			if( System.currentTimeMillis() > pollTimeout ) 
				throw new PollServicesTargetStateTimeoutException( servicesNotStopped.toString() );
			if( POLLING_VERBOSE ) log.info("Services not " + apiServiceStateTarget + " yet: " + servicesNotStopped.toString() );
			Thread.sleep( POLLING_INTERVAL_SECONDS * 1000L );
		}
		log.info("All services in target state: " + apiServiceStateTarget + ", elapsed " + Utils.convertMSecsToHMmSs(System.currentTimeMillis()-before) );
	}
	
	
	/**
	 * Find service names in cluster.
	 *
	 * @param apiCluster the api cluster
	 * @param servicesResource the services resource
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<String> findServiceNamesInCluster( ApiCluster apiCluster, ServicesResourceV6 servicesResource ) throws Exception {
		List<String> serviceNames = new ArrayList<String>();
		for (ApiService apiService : apiCluster.getServices()) {
			ApiService apiServiceDetail = servicesResource.readService(apiService.getName());
			serviceNames.add( apiServiceDetail.getName() );
		}
		return serviceNames;
	}
	
	/**
	 * The Class PollServicesTargetStateTimeoutException.
	 */
	public static class PollServicesTargetStateTimeoutException extends Exception {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -816416074433143509L;
		
		/** The services not in target state. */
		private String servicesNotInTargetState;
		
		/**
		 * Instantiates a new poll services target state timeout exception.
		 *
		 * @param servicesNotInTargetState the services not in target state
		 */
		public PollServicesTargetStateTimeoutException( String servicesNotInTargetState ) {
			super();
			this.servicesNotInTargetState = servicesNotInTargetState;
		}
		
		/**
		 * Gets the services not in target state.
		 *
		 * @return the services not in target state
		 */
		public String getServicesNotInTargetState() {
			return servicesNotInTargetState;
		}
		
		/**
		 * Sets the services not in target state.
		 *
		 * @param servicesNotInTargetState the new services not in target state
		 */
		public void setServicesNotInTargetState(String servicesNotInTargetState) {
			this.servicesNotInTargetState = servicesNotInTargetState;
		}
	}

	/**
	 * Gets the cloudera manager dns name or ip address.
	 *
	 * @return the cloudera manager dns name or ip address
	 */
	public String getClouderaManagerDnsNameOrIpAddress() {
		return clouderaManagerDnsNameOrIpAddress;
	}


	/**
	 * Sets the cloudera manager dns name or ip address.
	 *
	 * @param clouderaManagerDnsNameOrIpAddress the new cloudera manager dns name or ip address
	 */
	public void setClouderaManagerDnsNameOrIpAddress(String clouderaManagerDnsNameOrIpAddress) {
		this.clouderaManagerDnsNameOrIpAddress = clouderaManagerDnsNameOrIpAddress;
	}


	/**
	 * Gets the cloudera manager admin login.
	 *
	 * @return the cloudera manager admin login
	 */
	public String getClouderaManagerAdminLogin() {
		return clouderaManagerAdminLogin;
	}


	/**
	 * Sets the cloudera manager admin login.
	 *
	 * @param clouderaManagerAdminLogin the new cloudera manager admin login
	 */
	public void setClouderaManagerAdminLogin(String clouderaManagerAdminLogin) {
		this.clouderaManagerAdminLogin = clouderaManagerAdminLogin;
	}


	/**
	 * Gets the cloudera manager admin password.
	 *
	 * @return the cloudera manager admin password
	 */
	public String getClouderaManagerAdminPassword() {
		return clouderaManagerAdminPassword;
	}


	/**
	 * Sets the cloudera manager admin password.
	 *
	 * @param clouderaManagerAdminPassword the new cloudera manager admin password
	 */
	public void setClouderaManagerAdminPassword(String clouderaManagerAdminPassword) {
		this.clouderaManagerAdminPassword = clouderaManagerAdminPassword;
	}


	/**
	 * Gets the timeout minutes start cdh cluster.
	 *
	 * @return the timeout minutes start cdh cluster
	 */
	public int getTimeoutMinutesStartCdhCluster() {
		return timeoutMinutesStartCdhCluster;
	}


	/**
	 * Sets the timeout minutes start cdh cluster.
	 *
	 * @param timeoutMinutesStartCdhCluster the new timeout minutes start cdh cluster
	 */
	public void setTimeoutMinutesStartCdhCluster(int timeoutMinutesStartCdhCluster) {
		this.timeoutMinutesStartCdhCluster = timeoutMinutesStartCdhCluster;
	}


	/**
	 * Gets the timeout minutes stop cdh cluster.
	 *
	 * @return the timeout minutes stop cdh cluster
	 */
	public int getTimeoutMinutesStopCdhCluster() {
		return timeoutMinutesStopCdhCluster;
	}


	/**
	 * Sets the timeout minutes stop cdh cluster.
	 *
	 * @param timeoutMinutesStopCdhCluster the new timeout minutes stop cdh cluster
	 */
	public void setTimeoutMinutesStopCdhCluster(int timeoutMinutesStopCdhCluster) {
		this.timeoutMinutesStopCdhCluster = timeoutMinutesStopCdhCluster;
	}

}


