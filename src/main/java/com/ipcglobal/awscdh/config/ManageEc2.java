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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.ipcglobal.awscdh.util.Utils;


/**
 * The Class ManageEc2.
 * Start/Stop EC2 instances via the AWS SDK
 */
public class ManageEc2 {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(ManageEc2.class);
	
	/** The Constant POLLING_INTERVAL_SECONDS. */
	private static final Long POLLING_INTERVAL_SECONDS = 30L;
	
	/** The polling verbose. */
	private static boolean POLLING_VERBOSE = true;
	
	/**
	 * The Enum InstanceTargetStatus.
	 */
	private enum InstanceTargetStatus { 
 /** The running. */
 RUNNING, 
 /** The stopped. */
 STOPPED };
	
	/** The credentials provider. */
	private AWSCredentialsProvider credentialsProvider;
	
	/** The ec2 client. */
	private AmazonEC2Client ec2Client;
	
	/** The properties. */
	private Properties properties;
	
	/** The vpc id. */
	private String vpcId;
	
	/** The instance ids. */
	private List<String> instanceIds;
	
	/** The timeout minutes start ec2 instances. */
	private int timeoutMinutesStartEc2Instances;
	
	/** The timeout minutes stop ec2 instances. */
	private int timeoutMinutesStopEc2Instances;
	
	
	/**
	 * Instantiates a new manage ec2.
	 *
	 * @param properties the properties
	 * @throws Exception the exception
	 */
	public ManageEc2( Properties properties ) throws Exception {
		this.properties = properties;
		String credentialsProfileName = this.properties.getProperty("credentialsProfileName").trim();
		String credentialsRegion = this.properties.getProperty("credentialsRegion").trim();
		this.vpcId = this.properties.getProperty("vpcId").trim();
		this.instanceIds = new ArrayList<String>( Arrays.asList(properties.getProperty("instanceIds").split("[ ]")) );
		this.timeoutMinutesStartEc2Instances = Integer.parseInt( this.properties.getProperty("timeoutMinutesStartEc2Instances") );
		this.timeoutMinutesStopEc2Instances = Integer.parseInt( this.properties.getProperty("timeoutMinutesStopEc2Instances") );
		
		if( credentialsProfileName == null ) this.credentialsProvider = Utils.initCredentials();
		else this.credentialsProvider = Utils.initProfileCredentialsProvider( credentialsProfileName );

		this.ec2Client = new AmazonEC2Client(credentialsProvider);
		if( credentialsRegion != null ) {
			Regions regions = Regions.fromName( credentialsRegion );
			this.ec2Client.setRegion(Region.getRegion( regions ));
		}
	}

	/**
	 * Start EC2 Instances.
	 *
	 * @throws Exception the exception
	 */
	public void start( ) throws Exception {
		try {
			List<Instance> instances = findInstances( );
			startAllInstances( instances );
	
		} catch (AmazonServiceException ase) {
			log.error("Caught Exception: " + ase.getMessage());
			log.error("Reponse Status Code: " + ase.getStatusCode());
			log.error("Error Code: " + ase.getErrorCode());
			log.error("Request ID: " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			log.error("Error Message: " + ace.getMessage());
			throw ace;
		} catch( Exception e ) {
			log.error(e);
			throw e;
		}
	}	

	
	/**
	 * Stop EC2 Instances.
	 *
	 * @throws Exception the exception
	 */
	public void stop( ) throws Exception {
		try {
			List<Instance> instances = findInstances( );
			stopAllInstances( instances );
	
		} catch (AmazonServiceException ase) {
			log.error("Caught Exception: " + ase.getMessage());
			log.error("Reponse Status Code: " + ase.getStatusCode());
			log.error("Error Code: " + ase.getErrorCode());
			log.error("Request ID: " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			log.error("Error Message: " + ace.getMessage());
			throw ace;
		} catch( Exception e ) {
			log.error(e);
			throw e;
		}
	}	
	
	
	/**
	 * Start all EC2 instances.
	 *
	 * @param instances the instances
	 * @throws Exception the exception
	 */
	public void startAllInstances(List<Instance> instances ) throws Exception {
		List<String> startInstanceIds = new ArrayList<String>();
		for( Instance instance : instances ) {
			if( "stopped".equals( instance.getState().getName() ) )
				startInstanceIds.add( instance.getInstanceId() );
			else if( "running".equals( instance.getState().getName() ) );
			else throw new Exception("Can't start instance " + instance.getInstanceId() + " while in state: " + instance.getState().getName());
		}
		
		if( startInstanceIds.size() > 0 ) {
			log.info("Initiating Start for " + startInstanceIds.size() + " instances");
			// Had strange issues trying to start all instances at once, no error messages but not all instances started
			// so do one at a time
			for( String startInstanceId : startInstanceIds ) {
				StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
				startInstancesRequest.setInstanceIds( Arrays.asList(startInstanceId) );
				StartInstancesResult startInstancesResult = ec2Client.startInstances(startInstancesRequest);
				// log.info( "startInstancesResult: " + startInstancesResult );
			}
			
			log.info("Sleeping for 1 minute after Start initiated");
			Thread.sleep( 1 * 60000L );
			
			long before = System.currentTimeMillis();
			log.info("Verifying Instances Running - Begin" );
			verifyInstancesRunning( instances );
			log.info("Verifying Instances Running - Complete, elapsed=" + Utils.convertMSecsToHMmSs(System.currentTimeMillis()-before) );
		} else {
			log.info("Nothing to start, all instances already running");
		}
		
	}

	
	/**
	 * Stop all EC2 instances.
	 *
	 * @param instances the instances
	 * @throws Exception the exception
	 */
	public void stopAllInstances( List<Instance> instances ) throws Exception {
		List<String> instanceIds = new ArrayList<String>();
		for( Instance instance : instances ) instanceIds.add( instance.getInstanceId() );

		log.info("Initiating Stop for " + instanceIds.size() + " instances");
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
		stopInstancesRequest.setInstanceIds(instanceIds);
		StopInstancesResult stopInstancesResult = ec2Client.stopInstances(stopInstancesRequest);
		log.info( "stopInstancesResult: " + stopInstancesResult );
		
		log.info("Sleeping for 1 minute after Stop initiated");
		Thread.sleep( 1 * 60000L );
		
		long before = System.currentTimeMillis();
		log.info("Verifying Instances Stopped - Begin" );
		verifyInstancesStopped( instances );
		log.info("Verifying Instances Stopped - Complete, elapsed=" + Utils.convertMSecsToHMmSs(System.currentTimeMillis()-before) );
	}
	
	
	/**
	 * Find EC2 instances.
	 *
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<Instance> findInstances( ) throws Exception {
		List<Instance> instances = new ArrayList<Instance>();
     	DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest( );
     	Filter filterVpcId = new Filter().withName("vpc-id").withValues( Arrays.asList(vpcId) ); 
     	Filter filterInstances = new Filter().withName("instance-id").withValues( instanceIds ); 
     	describeInstancesRequest.setFilters( Arrays.asList( filterVpcId, filterInstances ) );
    	DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances( describeInstancesRequest );
    	for (Reservation reservation : describeInstancesResult.getReservations()) {
    		for( Instance instance : reservation.getInstances() ) {
    			instances.add( instance );
    		}
    	}

		return instances;
	}
	
	/**
	 * Verify EC2 instances stopped.
	 *
	 * @param instances the instances
	 * @throws Exception the exception
	 */
	private void verifyInstancesStopped( List<Instance> instances ) throws Exception {
		List<String> instanceIds = new ArrayList<String>();
		Map<String,InstanceStatusCheck> instanceStatusChecksByInstanceId = new HashMap<String,InstanceStatusCheck>();
		for( Instance instance : instances ) {
			instanceIds.add( instance.getInstanceId() );
			instanceStatusChecksByInstanceId.put( instance.getInstanceId(), new InstanceStatusCheck( instance ));
		}
		
		boolean isSuccess = false;
		long pollExpiresAt = System.currentTimeMillis() + (60000 * timeoutMinutesStopEc2Instances);
		while( true ) {
			for( InstanceStatusCheck instanceStatusCheck : instanceStatusChecksByInstanceId.values() ) 
				instanceStatusCheck.reset();
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest( ).withInstanceIds( instanceIds );
	    	DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances( describeInstancesRequest );
	    	for (Reservation reservation : describeInstancesResult.getReservations()) {
	    		for( Instance instance : reservation.getInstances() ) {
	    			InstanceStatusCheck instanceStatusCheck = instanceStatusChecksByInstanceId.get( instance.getInstanceId() );
	    			if( "stopped".equals( instance.getState().getName() ) ) instanceStatusCheck.setInstanceStateName("stopped");
	    			if( POLLING_VERBOSE ) log.info("Status: id=" + instanceStatusCheck.getInstanceId() +", instance state=" + instance.getState().getName() );
	    		}
	    	}
			// Check if all instances are stopped
			int numInstancesStopped = 0;
			for( InstanceStatusCheck instanceStatusCheck : instanceStatusChecksByInstanceId.values() ) {
				if( instanceStatusCheck.isInstanceStopped() ) numInstancesStopped++;
			}
			if( numInstancesStopped == instanceStatusChecksByInstanceId.size() ) {
				isSuccess = true;
				break;
			}

			if( System.currentTimeMillis() > pollExpiresAt ) break;
			Thread.sleep( POLLING_INTERVAL_SECONDS * 1000L );
		}
		
		if( !isSuccess ) throw new InstanceStatusCheckException( 
				InstanceTargetStatus.STOPPED.toString(), new ArrayList<InstanceStatusCheck>(instanceStatusChecksByInstanceId.values()) );
	}
	
	/**
	 * Verify EC2 instances running.
	 *
	 * @param instances the instances
	 * @throws Exception the exception
	 */
	private void verifyInstancesRunning( List<Instance> instances ) throws Exception {
		List<String> instanceIds = new ArrayList<String>();
		Map<String,InstanceStatusCheck> instanceStatusChecksByInstanceId = new HashMap<String,InstanceStatusCheck>();
		for( Instance instance : instances ) {
			instanceIds.add( instance.getInstanceId() );
			instanceStatusChecksByInstanceId.put( instance.getInstanceId(), new InstanceStatusCheck( instance ));
		}
		boolean isSuccess = false;
		long pollExpiresAt = System.currentTimeMillis() + (60000 * timeoutMinutesStartEc2Instances);
		while( true ) {
			for( InstanceStatusCheck instanceStatusCheck : instanceStatusChecksByInstanceId.values() ) 
				instanceStatusCheck.reset();
			DescribeInstanceStatusRequest describeInstanceStatusRequest = 
					new DescribeInstanceStatusRequest().withInstanceIds(instanceIds);
			while( true ) {
				DescribeInstanceStatusResult describeInstanceStatusResult = ec2Client.describeInstanceStatus(describeInstanceStatusRequest);
				for( InstanceStatus instanceStatus : describeInstanceStatusResult.getInstanceStatuses() ) {
					InstanceStatusCheck instanceStatusCheck = instanceStatusChecksByInstanceId.get( instanceStatus.getInstanceId() );
					instanceStatusCheck.updateState(instanceStatus.getInstanceState().getName(), instanceStatus.getSystemStatus().getStatus() );
					if( POLLING_VERBOSE ) log.info("Status: id=" + instanceStatus.getInstanceId() +", instance state=" + instanceStatus.getInstanceState().getName() + 
							", status checks=" + instanceStatus.getSystemStatus().getStatus() );
				}		
				if( describeInstanceStatusResult.getNextToken() == null ) break;
				describeInstanceStatusRequest.setNextToken( describeInstanceStatusResult.getNextToken() );
			}
			// Check if all instances are running or stopped
			int numInstancesRunning = 0;
			for( InstanceStatusCheck instanceStatusCheck : instanceStatusChecksByInstanceId.values() ) {
				if( instanceStatusCheck.isInstanceRunning() ) numInstancesRunning++;
			}
			if( numInstancesRunning == instanceStatusChecksByInstanceId.size() ) {
				isSuccess = true;
				break;
			}
			
//			log.info(">>>>> verifyCdhInstancesRunning Instance Status - Start"
//					+ ", numInstances=" + instanceStatusChecksByInstanceId.size() 
//					+ ", numInstancesInTargetStatus=" + numInstancesRunning );
//			for( InstanceStatusCheck instanceStatusCheck : instanceStatusChecksByInstanceId.values() ) 
//				log.info( instanceStatusCheck.toString() );
//			log.info(">>>>> verifyCdhInstancesRunning Instance Status - End" );
			
			if( System.currentTimeMillis() > pollExpiresAt ) break;
			Thread.sleep( POLLING_INTERVAL_SECONDS * 1000L );
		}
		if( !isSuccess ) throw new InstanceStatusCheckException( 
				InstanceTargetStatus.RUNNING.toString(), new ArrayList<InstanceStatusCheck>(instanceStatusChecksByInstanceId.values()) );
	}

	
	/**
	 * Find tag name.
	 *
	 * @param instance the instance
	 * @return the string
	 */
	public static String findTagName( Instance instance ) {
		for( Tag tag : instance.getTags() ) 
			if( "name".equalsIgnoreCase(tag.getKey()) ) return tag.getValue();
		return "";
	}

}
