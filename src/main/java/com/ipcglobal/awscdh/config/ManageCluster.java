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

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ipcglobal.awscdh.util.LogTool;
import com.ipcglobal.awscdh.util.Utils;

/**
 * The Class ManageCluster.
 * Starts/Stops CDH Services and EC2 Instances based on values in passed properties file
 */
public class ManageCluster {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(ManageCluster.class);

	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			long before = System.currentTimeMillis();
			LogTool.initConsole();
			if( args.length <= 1 ) throw new Exception("Action and Properties file name are required parameter - format: start|stop example.properties");
			String action = args[0];
			log.info("Begin: action=" + action );
			Properties properties = new Properties( );
			properties.load(new FileInputStream( args[1]) );
			ManageEc2 manageEc2 = new ManageEc2( properties );
			ManageCdh manageCdh = new ManageCdh( properties );
			
			if( "start".equals(action)) {
				manageEc2.start();
				manageCdh.start();
			} else if( "stop".equals(action)) {
				manageCdh.stop( );
				manageEc2.stop();				
			} else throw new Exception("Invalid action: " + action + ", must be start or stop");
			
			log.info("Complete: action=" + action + ", elapsed " + Utils.convertMSecsToHMmSs(System.currentTimeMillis()-before) );

		} catch( Exception e ) {
			log.error(e);
			e.printStackTrace();
		}
	}

}
