package org.eclipse.update.internal.core;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Date;
import java.util.List;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.Utilities;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * singleton pattern.
 * manages the error/recover log file
 */
public class ErrorRecoveryLog {

	private static final String ERROR_RECOVERY_LOG = "platform.cfg.log";
	private static final String LOG_ENTRY_KEY = "LogEntry.";
	private static final String RETURN_CARRIAGE = "\r\n";
	private static final String END_OF_FILE = "eof=eof";

	//
	public static final String START_INSTALL_LOG = "INSTALL_LOG";
	public static final String PLUGIN_ENTRY = "PLUGIN";
	public static final String FEATURE_ENTRY = "FEATURE";
	public static final String END_INSTALL = "END_INSTALL";
	public static final String RENAME_ENTRY = "RENAME";
	public static final String END_INSTALL_LOG = "END_INSTALL_LOG";
	public static final String START_REMOVE_LOG = "REMOVE_LOG";
	public static final String END_REMOVE = "END_REMOVE";
	public static final String DELETE_ENTRY = "DELETE";
	public static final String END_REMOVE_LOG = "END_REMOVE_LOG";

	private static ErrorRecoveryLog inst;
	private FileWriter out;
	private int index;
	private List paths;

	/**
	 * Constructor for ErrorRecoveryLog.
	 */
	private ErrorRecoveryLog() {
		super();
	}

	/**
	 * Singleton
	 */
	public static ErrorRecoveryLog getLog() {
		if (inst == null)
			inst = new ErrorRecoveryLog();
		return inst;
	}

	/**
	 * get a unique identifer for the file, ensure uniqueness up to now
	 */
	public static String getLocalRandomIdentifier(String path) {
		
		// FIXME for now always return the same
		return path;
		
		// verify if it will be a directory without creating the file
		// as it doesn't exist yet
		/*if (path.endsWith(File.separator) || path.endsWith("/"))
			return path;
		File file = new File(path);
		String newName =
			UpdateManagerUtils.getLocalRandomIdentifier(file.getName(), new Date());
		while (new File(newName).exists()) {
			newName =
				UpdateManagerUtils.getLocalRandomIdentifier(file.getName(), new Date());
		}
		File newFile = new File(file,newName);
		return newFile.getAbsolutePath();*/
	}

	/**
	 * returns the log file 
	 * We do not check if the file exists
	 */
	public File getRecoveryLogFile() {
		IPlatformConfiguration configuration =
			BootLoader.getCurrentPlatformConfiguration();
		URL location = configuration.getConfigurationLocation();
		String locationString = location.getFile();
		File platformConfiguration = new File(locationString);
		if (!platformConfiguration.isDirectory()) platformConfiguration = platformConfiguration.getParentFile();
		return new File(platformConfiguration, ERROR_RECOVERY_LOG);
	}

	/**
	 * Append the string to the log and flush
	 */
	public void append(String logEntry) throws CoreException {
		File logFile = null;
		try {
			if (out == null) {
				logFile = getRecoveryLogFile();
				out = new FileWriter(logFile);
				index = 0;
				paths=null;
			}

			StringBuffer buffer = new StringBuffer(LOG_ENTRY_KEY);
			buffer.append(index);
			buffer.append("=");
			buffer.append(logEntry);
			buffer.append(RETURN_CARRIAGE);

			out.write(buffer.toString());
			out.flush();
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind("UpdateManagerUtils.UnableToLog", new Object[] { logFile }),
				e);
		}
	}

	/**
	 * Append the string to the log and flush
	 */
	public void appendPath(String logEntry, String path) throws CoreException {
		if (path == null)
			return;
		StringBuffer buffer = new StringBuffer(logEntry);
		buffer.append(" ");
		buffer.append(path);
		append(buffer.toString());
		
		addPath(path);
	}

	/**
	 * Close any open recovery log
	 */
	public void close() {
		if (out != null) {
			try {
				out.write(END_OF_FILE);
				out.flush();
				out.close();
			} catch (Exception e) { //eat the exception
			} finally {
				out = null;
			}
		}
	}

	/**
	 * Delete the file from the file system
	 */
	public void delete() {
		File logFile = getRecoveryLogFile();
		if (logFile.exists())
			//logFile.delete();
System.out.println("DELETE FILE");			
	}

	/**
	 * 
	 */
	private void addPath(String path){
		if (paths==null) paths = new ArrayList();
		paths.add(path);
	}

}