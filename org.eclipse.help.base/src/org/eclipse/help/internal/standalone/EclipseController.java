/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.net.*;
import java.nio.channels.*;

/**
 * This program is used to start or stop Eclipse
 * Infocenter application.
 * It should be launched from command line.
 */
public class EclipseController implements EclipseLifeCycleListener {

	// control servlet path
	private static final String CONTROL_SERVLET_PATH =
		"/helpControl/control.html";

	// application to launch
	protected String applicationId;

	// Eclipse connection params
	protected EclipseConnection connection;

	public Eclipse eclipse = null;
	// Inter process lock
	private FileLock lock;
	private boolean eclipseEnded = false;
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
		connection = new EclipseConnection();
	}

	/**
	 * @see org.eclipse.help.standalone.Help#shutdown()
	 */
	public final synchronized void shutdown() throws Exception {
		try {
			obtainLock();
			sendHelpCommandInternal("shutdown", new String[0]);
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (InterruptedException ie) {
		} finally {
			releaseLock();
		}
	}

	/**
	 * @see org.eclipse.help.standalone.Help#start()
	 */
	public final synchronized void start() throws Exception {
		try {
			obtainLock();
			startEclipse();
		} finally {
			releaseLock();
		}

	}

	/**
	 * Ensures the application is running, and sends command
	 * to the control servlet.
	 * If connection fails, retries several times,
	 * in case webapp is starting up.
	 */
	protected final synchronized void sendHelpCommand(
		String command,
		String[] parameters)
		throws Exception {
		try {
			obtainLock();
			sendHelpCommandInternal(command, parameters);
		} finally {
			releaseLock();
		}

	}

	/**
	 * Starts Eclipse if not yet running.
	 */
	private void startEclipse() throws Exception {
		boolean fullyRunning = isApplicationRunning();
		if (fullyRunning) {
			return;
		}
		if (Options.isDebug()) {
			System.out.println(
				"Using workspace " + Options.getWorkspace().getAbsolutePath());
		}
		// delete old connection file
		Options.getConnectionFile().delete();
		connection.reset();

		if (Options.isDebug()) {
			System.out.println(
				"Ensured old .connection file is deleted.  Launching Eclipse.");
		}
		eclipseEnded = false;
		eclipse = new Eclipse(this);
		eclipse.start();
		fullyRunning = isApplicationRunning();
		while (!eclipseEnded && !fullyRunning) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ie) {
			}
			fullyRunning = isApplicationRunning();
		}
		if (eclipseEnded) {
			if (eclipse.getStatus() == Eclipse.STATUS_ERROR) {
				throw eclipse.getException();
			}
			return;
		}
		if (Options.isDebug()) {
			System.out.println("Eclipse launched");
		}
		// in case controller is killed
		Runtime.getRuntime().addShutdownHook(new EclipseCleaner());
	}
	private void sendHelpCommandInternal(String command, String[] parameters)
		throws Exception {
		if (!"shutdown".equalsIgnoreCase(command)) {
			startEclipse();
		}
		if (!isApplicationRunning()) {
			return;
		}
		if (!connection.isValid()) {
			connection.renew();
		}
		try {
			URL url = createCommandURL(command, parameters);
			if ("shutdown".equalsIgnoreCase(command)
				&& Options.getConnectionFile().exists()) {
				connection.connect(url);
				long timeLimit = System.currentTimeMillis() + 60 * 1000;
				while (Options.getConnectionFile().exists()) {
					Thread.sleep(200);
					if (System.currentTimeMillis() > timeLimit) {
						System.out.println(
							"Shutting down is taking too long.  Will not wait.");
						break;
					}
				}

			} else {
				connection.connect(url);
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (InterruptedException ie) {
		}
	}

	/**
	 * Builds a URL that communicates the specified command
	 * to help control servlet.
	 * @param command standalone help system command e.g. "displayHelp"
	 * @param parameters array of parameters of the command e.g. {"http://www.eclipse.org"}
	 */
	private URL createCommandURL(String command, String[] parameters)
		throws MalformedURLException {
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
		return new URL(urlStr.toString());
	}

	public void eclipseEnded() {
		connection.reset();
	}
	private void obtainLock() throws IOException {
		if (lock != null) {
			// we already have lock
			return;
		}
		if (!Options.getLockFile().exists()) {
			Options.getLockFile().getParentFile().mkdirs();
		}
		RandomAccessFile raf =
			new RandomAccessFile(Options.getLockFile(), "rw");
		lock = raf.getChannel().lock();
		if (Options.isDebug()) {
			System.out.println("Lock obtained.");
		}
	}
	private void releaseLock() {
		if (lock != null) {
			try {
				lock.channel().close();
				if (Options.isDebug()) {
					System.out.println("Lock released.");
				}
				lock = null;
			} catch (IOException ioe) {
			}
		}
	}
	/** Tests whether HelpApplication is running
	 * by testing if .applicationlock is locked
	 */
	private boolean isApplicationRunning() {
		File applicationLockFile =
			new File(Options.getLockFile().getParentFile(), ".applicationlock");
		RandomAccessFile randomAccessFile = null;
		FileLock applicationLock = null;
		try {
			randomAccessFile = new RandomAccessFile(applicationLockFile, "rw");
			applicationLock = randomAccessFile.getChannel().tryLock();
		} finally {
			if (applicationLock != null) {
				try {
					applicationLock.release();
				} catch (IOException ioe) {
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException ioe) {
				}
			}
			if (Options.isDebug()) {
				System.out.println(
					"isApplicationRunning? " + (applicationLock == null));
			}
			return applicationLock == null;
		}
	}
	public class EclipseCleaner extends Thread {
		public void run() {
			if (eclipse != null) {
				eclipse.killProcess();
			}
		}
	}
}
