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
package com.ipcglobal.awscdh.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;


/**
 * The Class LogTool handles initializing Log4J.
 */
public class LogTool {
	
	/** The Constant PATTERN. */
	private static final String PATTERN = "%d [%p] [%t] [%c] [%M] [%m]%n";

	/**
	 * Inits the console.
	 */
	public static void initConsole() {
		initConsole(Level.INFO);
	}

	/**
	 * Inits the console.
	 *
	 * @param level the level
	 */
	public static void initConsole(Level level) {
		ConsoleAppender consoleAppender = new ConsoleAppender( new PatternLayout(PATTERN) );
		consoleAppender.setThreshold(level);
		consoleAppender.activateOptions();
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender( consoleAppender );
	}

	/**
	 * Inits the file.
	 *
	 * @param level the level
	 * @param logFilePathNameExt the log file path name ext
	 * @throws Exception the exception
	 */
	public static void initFile(Level level, String logFilePathNameExt) throws Exception {
		
		RollingFileAppender rollingFileAppender = new RollingFileAppender( new PatternLayout(PATTERN), logFilePathNameExt );
		rollingFileAppender.setThreshold(level);
		rollingFileAppender.activateOptions();
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setMaxBackupIndex(7);
		rollingFileAppender.setMaxFileSize("10MB");
		
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(rollingFileAppender);
	}

	/**
	 * Inits the console and file.
	 *
	 * @param level the level
	 * @param logFilePathNameExt the log file path name ext
	 * @throws Exception the exception
	 */
	public static void initConsoleFile(Level level, String logFilePathNameExt) throws Exception {
		ConsoleAppender consoleAppender = new ConsoleAppender( new PatternLayout(PATTERN) );
		consoleAppender.setThreshold(level);
		consoleAppender.activateOptions();
		
		RollingFileAppender rollingFileAppender = new RollingFileAppender( new PatternLayout(PATTERN), logFilePathNameExt );
		rollingFileAppender.setThreshold(level);
		rollingFileAppender.activateOptions();
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setMaxBackupIndex(7);
		rollingFileAppender.setMaxFileSize("10MB");
		
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(consoleAppender);
		Logger.getRootLogger().addAppender(rollingFileAppender);
	}

}
