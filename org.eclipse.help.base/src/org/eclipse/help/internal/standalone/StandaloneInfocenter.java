/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.standalone;

import java.util.*;

import org.eclipse.help.internal.base.*;

/**
 * This program is used to start or stop Eclipse Infocenter application.
 */
public class StandaloneInfocenter extends EclipseController {
	// ID of the application to run
	private static final String INFOCENTER_APPLICATION_ID = HelpBasePlugin.PLUGIN_ID
			+ ".infocenterApplication"; //$NON-NLS-1$

	/**
	 * Constructs help system
	 * 
	 * @param args
	 *            array of String options and their values Option
	 *            <code>-eclipseHome dir</code> specifies Eclipse installation
	 *            directory. It must be provided, when current directory is not
	 *            the same as Eclipse installation directory. Additionally, most
	 *            options accepted by Eclipse execuable are supported.
	 */
	public StandaloneInfocenter(String[] args) {
		super(INFOCENTER_APPLICATION_ID, args);
	}

	/**
	 * @see org.eclipse.help.standalone.Infocenter#main(String[])
	 */
	public static void main(String[] args) {
		try {
			StandaloneInfocenter infocenter = new StandaloneInfocenter(args);

			List helpCommand = Options.getHelpCommand();

			if (infocenter.executeCommand(helpCommand)) {
				return;
			} else {
				printMainUsage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return true if commands contained a known command and it was executed
	 */
	private boolean executeCommand(List helpCommand) throws Exception {
		if (helpCommand.size() <= 0) {
			return false;
		}
		String command = (String) helpCommand.get(0);
		if ("start".equalsIgnoreCase(command)) { //$NON-NLS-1$
			start();
			return true;
		} else if ("shutdown".equalsIgnoreCase(command)) { //$NON-NLS-1$
			shutdown();
			return true;
		} else if (CMD_INSTALL.equalsIgnoreCase(command)
				|| CMD_ENABLE.equalsIgnoreCase(command)
				|| CMD_DISABLE.equalsIgnoreCase(command)
				|| CMD_UNINSTALL.equalsIgnoreCase(command)
				|| CMD_SEARCH.equalsIgnoreCase(command)
				|| CMD_LIST.equalsIgnoreCase(command)
				|| CMD_ADDSITE.equalsIgnoreCase(command)
				|| CMD_REMOVESITE.equalsIgnoreCase(command)
				|| CMD_APPLY.equalsIgnoreCase(command)) {
			return executeUpdateCommand(command);
		}
		return false;
	}

	/**
	 * Prints usage of this class as a program.
	 */
	private static void printMainUsage() {
		System.out.println("Parameters syntax:");
		System.out.println();
		System.out
				.println("-command start | shutdown [-eclipsehome eclipseInstallPath] [-host helpServerHost] [-port helpServerPort] [-noexec] [platform options] [-vmargs [Java VM arguments]]"); //$NON-NLS-1$
		System.out.println();
		System.out.println("where:");
		System.out
				.println(" eclipseInstallPath specifies Eclipse installation directory; this directory is a parent to \"plugins\" directory and eclipse executable;  the option must be provided, when current directory from which infocenter is launched, is not the same as Eclipse installation directory,");
		System.out
				.println(" helpServerHost specifies host name of the interface that help server will use,");
		System.out
				.println(" helpServerPort specifies port number that help server will use,");
		System.out
				.println(" noexec option indicates that Eclipse executable should not be used, ");
		System.out
				.println(" platform options are other options that are supported by Eclipse Executable.");
	}

}