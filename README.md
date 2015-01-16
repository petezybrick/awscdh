# Start/Stop Cloudera CDH 5.3 and AWS EC2 Instances

This project displays basic examples of how to call the AWS SDK for EC2 and the Cloudera CDH API. I have a Development/Testing/POC installation of Cloudera CDH 5.3 running on a cluster of 5 EC2 instances.  I only bring the cluster up when I need it, keep it stopped the rest of the time.  To start or stop the cluster, I would have to login to the AWS EC2 console and Cloudera Manager (CM) console and perform the start/stop sequence.  This utility automates that process - from my desktop, I can issue a single command to start or stop both the EC2 instances and Cloudera CDH 5.3 services.  Additionally, multiple properties files can be created, so if you have multiple clusters you can have a separate properties file to start/stop each cluster.

To run:
* Prereqs
	* Java 1.7
	* For CDH 5.3
		* Access to the Cloudera Manager instance, either via IP address or DNS Name
		* Cloudera Manager Login and Password 
	* For EC2
		* AWS account, .aws/credentials file containing your keys
		* AWS account must have an EC2 IAM permission allowing actions DescribeInstances, StartInstances and StopInstances 
* Copy the run/ directory to the local file system
* Review properties/first.properties.  This is a very simple properties file with dummy instance names, addresses and password
* Update properties/first.properties based on the TODO comments in the file
* Run
	* Start EC2 instances, then CDH 5.3
		* Linux: 	./awscdh start properties/first.properties
		* Windows:	awscdh start properties/first.properties	
	* Stop CDH 5.3, then EC2 instances
		* Linux: 	./awscdh stop properties/first.properties
		* Windows:	awscdh stop properties/first.properties
	* The log will be written to the console.
* Optional: Monitor Progress of the start/stop - bring up the AWS EC2 console and Cloudera Manager before running the start or stop
	* Note: During CDH services startup, some service status' will display as red on the CM console.  Once all service are up, the status' should be green
 
Java Project Overview
 * Maven project in Eclipse, but should easily port to other IDE's
 * Three main classes
 	* ManageCdh.java - start/stop the services running on the Cloudera Manager
 	* ManageEc2.java - start/stop set of EC2 instances
 	* ManageCluster.java - start EC2 instances then CDH services, stop CDH services then EC2 instances
* ManageCdh and ManageEc2 can be run independently, feel free to copy to another project 
	* If you copy ManageCdh to another project, be sure to include the following in the pom.xml under the maven-shade-plugin<br>
		&lt;transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer"&gt;<br>
			&lt;resource&gt;META-INF/cxf/bus-extensions.txt&lt;/resource&gt;<br>
		&lt;/transformer&gt;<br>
	* If you don't, the program will run fine under Eclipse, but you may get a ServiceConstructionException or BusException when running from the jar, I burned a couple of hours tracking this one down.
* ManageCdh and ManageEc2 poll status'
	* Modify POLLING_INTERVAL_SECONDS to change the polling frequency
	* Modify POLLING VERBOSE to change the logging during polling.  If true, a status message is written at the end of each polling interval
 
 

