/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.help.internal.standalone.*;

/**
 * This program is used to start or stop Eclipse
 * Infocenter application.
 * It should be launched from command line.
 */
public class EclipseController {

	// control servlet path
	private static final String CONTROL_SERVLET_PATH =
		"/helpControl/control.html";

	// application to launch
	protected String applicationId;
	
	// Eclipse connection params
	protected EclipseConnection connection;


	/**
	 * Constructs help system
	 * @param applicationID ID of Eclipse help application
	 * @param args array of String options and their values
	 * 	Option <code>-eclipseHome dir</code> specifies Eclipse
	 *  installation directory.
	 *  It must be provided, when current directory is not the same
	 *  as Eclipse installation directory.
	 *  Additionally, most options accepted by Eclipse execuable are supported.
	 */
	public EclipseController(String applicationId, String[] args) {

		this.applicationId = applicationId;
		Options.init(applicationId, args);
		connection = initConnection();
	}

	/**
	 * Creates a connection to Eclipse. May need to override this to pass retry parameters.
	 */
	protected EclipseConnection initConnection() {
		return new EclipseConnection();
	}
	
	/**
	 * @see org.eclipse.help.standalone.Help#shutdown()
	 */
	public void shutdown() {
		sendHelpCommand("shutdown", new String[0]);
		connection.reset();
	}

	/**
	 * @see org.eclipse.help.standalone.Help#start()
	 */
	public void start() {
		connection.reset();
		startEclipse();
	}

	/**
	 * Ensures the application is running, and sends command
	 * to the control servlet.
	 * If connection fails, retries several times,
	 * in case webapp is starting up.
	 */
	protected boolean sendHelpCommand(String command, String[] parameters) {
		if (!"shutdown".equalsIgnoreCase(command)) {
			startEclipse();
		}
		if (!connection.isValid()) {
			connection.renew();
			if (!connection.isValid())
				return false;
		}

		URL url = createCommandURL(command, parameters);
		return connection.connect(url);
	}

	/**
	 * Builds a URL that communicates the specified command
	 * to help control servlet.
	 * @param command standalone help system command e.g. "displayHelp"
	 * @param parameters array of parameters of the command e.g. {"http://www.eclipse.org"}
	 */
	private URL createCommandURL(String command, String[] parameters) {
		StringBuffer urlStr = new StringBuffer();
		urlStr.append("http://");
		urlStr.append(connection.getHost());
		urlStr.append(":");
		urlStr.append(connection.getPort());
		urlStr.append(CONTROL_SERVLET_PATH);
		urlStr.append("?command=");
		urlStr.append(command);
		for (int i = 0; i < parameters.length; i++) {
			urlStr.append("&");
			urlStr.append(parameters[i]);
		}
		if (Options.isDebug()) {
			System.out.println("Control servlet URL=" + urlStr.toString());
		}
		try {
			return new URL(urlStr.toString());
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
			return null;
		}

	}

	/**
	 * Starts Eclipse if not yet running.
	 */
	private void startEclipse() {
		if (Options.isDebug()) {
			System.out.println(
				"Using workspace " + Options.getWorkspace().getAbsolutePath());
			System.out.println(
				"Checking if file " + Options.getLockFile() + " exists.");
		}
		if (Options.getLockFile().exists()) {
			if (!Options.getLockFile().delete()) {
				// already running
				if (Options.isDebug()) {
					System.out.println(
						"File "
							+ Options.getLockFile()
							+ " exists and is locked.");
				}
				return;
			} else {
				if (Options.isDebug()) {
					System.out.println("Deleted file " + Options.getLockFile());
				}
				// left over files
				Options.getConnectionFile().delete();
				if (Options.isDebug()) {
					System.out.println(
						"Deleted file " + Options.getConnectionFile());
				}
			}
		}
		if (Options.isDebug()) {
			System.out.println("Launching Eclipse.");
			for (Iterator it = Options.getEclipseArgs().iterator();
				it.hasNext();
				) {
				System.out.println("  " + (String) it.next());
			}
		}
		Eclipse eclipse =
			new Eclipse(Options.getEclipseHome(), Options.getEclipseArgs());
		eclipse.start();
		if (Options.isDebug()) {
			System.out.println("Eclipse launched");
		}
	}

}
