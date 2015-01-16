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

public class ManageCdh {
	private static final Log log = LogFactory.getLog(ManageCdh.class);
	private static final Long POLLING_INTERVAL_SECONDS = 60L;
	private static boolean POLLING_VERBOSE = true;
	private String clouderaManagerDnsNameOrIpAddress;
	private String clouderaManagerAdminLogin;
	private String clouderaManagerAdminPassword;
	private int timeoutMinutesStartCdhCluster;
	private int timeoutMinutesStopCdhCluster;
	
	
	public ManageCdh( Properties properties ) throws Exception {
		this.clouderaManagerDnsNameOrIpAddress = properties.getProperty( "clouderaManagerDnsNameOrIpAddress" ).trim();
		this.clouderaManagerAdminLogin = properties.getProperty( "clouderaManagerAdminLogin" ).trim();
		this.clouderaManagerAdminPassword = properties.getProperty( "clouderaManagerAdminPassword" ).trim();
		this.timeoutMinutesStartCdhCluster = Integer.parseInt( properties.getProperty("timeoutMinutesStartCdhCluster") );
		this.timeoutMinutesStopCdhCluster = Integer.parseInt( properties.getProperty("timeoutMinutesStopCdhCluster") );
	}
	
	
	public ApiCluster findCluster( ) throws Exception {
		List<ApiCluster> apiClusters = findApiClusters( );
		log.info( "findCluster clouderaManagerDnsNameOrIpAddress=" + clouderaManagerDnsNameOrIpAddress + ", apiClusters.size()=" + apiClusters.size() );
		if( apiClusters.size() == 0 || apiClusters.size() > 1 ) throw new Exception("ManageCdh only supported for one cluster per CM");
		return apiClusters.get(0);
	}

	
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
	
	
	// Find the names of all of the services in this cluster
	public List<String> findServiceNamesInCluster( ApiCluster apiCluster, ServicesResourceV6 servicesResource ) throws Exception {
		List<String> serviceNames = new ArrayList<String>();
		for (ApiService apiService : apiCluster.getServices()) {
			ApiService apiServiceDetail = servicesResource.readService(apiService.getName());
			serviceNames.add( apiServiceDetail.getName() );
		}
		return serviceNames;
	}
	
	public static class PollServicesTargetStateTimeoutException extends Exception {
		private static final long serialVersionUID = -816416074433143509L;
		private String servicesNotInTargetState;
		
		public PollServicesTargetStateTimeoutException( String servicesNotInTargetState ) {
			super();
			this.servicesNotInTargetState = servicesNotInTargetState;
		}
		public String getServicesNotInTargetState() {
			return servicesNotInTargetState;
		}
		public void setServicesNotInTargetState(String servicesNotInTargetState) {
			this.servicesNotInTargetState = servicesNotInTargetState;
		}
	}

	public String getClouderaManagerDnsNameOrIpAddress() {
		return clouderaManagerDnsNameOrIpAddress;
	}


	public void setClouderaManagerDnsNameOrIpAddress(String clouderaManagerDnsNameOrIpAddress) {
		this.clouderaManagerDnsNameOrIpAddress = clouderaManagerDnsNameOrIpAddress;
	}


	public String getClouderaManagerAdminLogin() {
		return clouderaManagerAdminLogin;
	}


	public void setClouderaManagerAdminLogin(String clouderaManagerAdminLogin) {
		this.clouderaManagerAdminLogin = clouderaManagerAdminLogin;
	}


	public String getClouderaManagerAdminPassword() {
		return clouderaManagerAdminPassword;
	}


	public void setClouderaManagerAdminPassword(String clouderaManagerAdminPassword) {
		this.clouderaManagerAdminPassword = clouderaManagerAdminPassword;
	}


	public int getTimeoutMinutesStartCdhCluster() {
		return timeoutMinutesStartCdhCluster;
	}


	public void setTimeoutMinutesStartCdhCluster(int timeoutMinutesStartCdhCluster) {
		this.timeoutMinutesStartCdhCluster = timeoutMinutesStartCdhCluster;
	}


	public int getTimeoutMinutesStopCdhCluster() {
		return timeoutMinutesStopCdhCluster;
	}


	public void setTimeoutMinutesStopCdhCluster(int timeoutMinutesStopCdhCluster) {
		this.timeoutMinutesStopCdhCluster = timeoutMinutesStopCdhCluster;
	}

}


