package com.ipcglobal.awscdh.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ipcglobal.awscdh.util.LogTool;
import com.ipcglobal.awscdh.util.Utils;

public class ManageCluster {
	private static final Log log = LogFactory.getLog(ManageCluster.class);

	
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
