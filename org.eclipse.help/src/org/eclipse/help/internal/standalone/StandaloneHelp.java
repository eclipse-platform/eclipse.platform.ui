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

import java.util.List;

import org.eclipse.help.internal.HelpPlugin;

/**
 * This is a standalone help system. It takes care of 
 * launching the eclipse with its help system implementation,
 * and controling it.
 * This class can be used instantiated and used in a Java program,
 * or can be launched from command line to execute single help action.
 * 
 * Usage as a Java component: 
 * <ul>
 * <li> create an instantance of this class and then hold onto 
 * this instance for the duration of your application</li>
 * <li> call start() </li>
 * <li> call displayHelp(...) or displayContext(..) any number of times </li>
 * <li> at the end, call shutdown(). </li>
 * </ul>
 */
public class StandaloneHelp extends EclipseController {
	// timout for .hostport file to apper since starting eclipse [ms]
	private static final int STARTUP_TIMEOUT = 40 * 1000;
	// number of retries to connectect to webapp
	private static final int CONNECTION_RETRIES = 3;
	// time between retries to connectect to webapp [ms]
	private static final int CONNECTION_RETRY_INTERVAL = 5 * 1000;
	// ID of the application to run
	private static final String HELP_APPLICATION_ID =
		HelpPlugin.PLUGIN_ID+".helpApplication";

	/**
	 * Constructs help system
	 * @param args array of String options and their values
	 * 	Option <code>-eclipseHome dir</code> specifies Eclipse
	 *  installation directory.
	 *  It must be provided, when current directory is not the same
	 *  as Eclipse installation directory.
	 *  Additionally, most options accepted by Eclipse execuable are supported.
	 * @param applicationID ID of Eclipse help application
	 */
	public StandaloneHelp(String[] args) {
		super(HELP_APPLICATION_ID, args);
	}

	/**
	 * @see org.eclipse.help.standalone.Infocenter#main(String[])
	 */
	public static void main(String[] args) {
		try {
			StandaloneHelp help = new StandaloneHelp(args);

			List helpCommand = Options.getHelpCommand();

			if (help.executeCommand(helpCommand)) {
				return;
			} else {
				printMainUsage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Overrides the initialization of the connection to pass retry parameters.
	 */
	protected EclipseConnection initConnection() {
		int timeout = STARTUP_TIMEOUT;
		if (Options.getServerTimeout() > 0) {
			timeout = 1000 * Options.getServerTimeout();
		}
		return new EclipseConnection(
			timeout,
			CONNECTION_RETRIES,
			CONNECTION_RETRY_INTERVAL);
	}
	/**
	 * @see org.eclipse.help.standalone.Help#displayContext(java.lang.String,int,int)
	 */
	public void displayContext(String contextId, int x, int y) {
	}

	/**
	 * @see org.eclipse.help.standalone.Help#displayContextInfopop(java.lang.String,int,int)
	 */
	public void displayContextInfopop(String contextId, int x, int y) {
	}
	/**
	 * @see org.eclipse.help.standalone.Help#displayHelp()
	 */
	public void displayHelp() throws Exception {
		sendHelpCommand("displayHelp", new String[0]);
	}

	/**
	 * @see org.eclipse.help.standalone.Help#displayHelp(java.lang.String)
	 */
	public void displayHelp(String href) throws Exception {
		sendHelpCommand("displayHelp", new String[] { "href=" + href });
	}

	/**
	 * @return true if commands contained a known command
	 *  and it was executed
	 */
	private boolean executeCommand(List helpCommands) throws Exception {

		if (helpCommands.size() <= 0) {
			return false;
		}
		String command = (String) helpCommands.get(0);

		if ("start".equalsIgnoreCase(command)) {
			start();
			return true;
		} else if ("shutdown".equalsIgnoreCase(command)) {
			shutdown();
			return true;
		} else if ("displayHelp".equalsIgnoreCase(command)) {
			if (helpCommands.size() >= 2) {
				displayHelp((String) helpCommands.get(1));
			} else {
				displayHelp();
			}
			return true;
		} else if ("displayContext".equalsIgnoreCase(command)) {
			if (helpCommands.size() >= 4) {
				displayContext(
					(String) helpCommands.get(1),
					Integer.parseInt((String) helpCommands.get(2)),
					Integer.parseInt((String) helpCommands.get(3)));

				return true;
			}
		} else if ("displayContextInfopop".equalsIgnoreCase(command)) {
			if (helpCommands.size() >= 4) {
				displayContextInfopop(
					(String) helpCommands.get(1),
					Integer.parseInt((String) helpCommands.get(2)),
					Integer.parseInt((String) helpCommands.get(3)));
				return true;
			}
		}

		return false;
	}
	/**
	 * Prints usage of this class as a program.
	 */
	private static void printMainUsage() {
		System.out.println("Parameters syntax:");
		System.out.println();
		System.out.println(
			"-command start | shutdown | (displayHelp [href]) [-eclipsehome eclipseInstallPath] [-host helpServerHost] [-port helpServerPort] [platform options] [-vmargs [Java VM arguments]]");
		System.out.println();
		System.out.println("where:");
		System.out.println(" href is the URL of the help resource to display,");
		System.out.println(
			" eclipseInstallPath specifies Eclipse installation directory; this directory is a parent to \"plugins\" directory and eclipse executable;  the option must be provided, when current directory from which infocenter is launched, is not the same as Eclipse installation directory,");
		System.out.println(
			" helpServerHost specifies host name of the interface that help server will use,");
		System.out.println(
			" helpServerPort specifies port number that help server will use,");
		System.out.println(
			" platform options are other options that are supported by Eclipse Executable.");
	}
}
