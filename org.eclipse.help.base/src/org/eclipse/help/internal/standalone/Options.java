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
import java.util.*;

/**
 * Options for starting stand alone help and infocenter.
 */
public class Options {
	// debugging
	private static boolean debug = false;
	// use eclipse.exe
	private static boolean useExe = true;
	// Eclipse installation directory
	private static File eclipseHome;
	// workspace directory to be used by Eclipse
	private static File workspace;
	// Eclipse .lock file
	private static File lockFile;
	// .hostport file to obtain help server host and port from Eclipse help
	// application
	private static File hostPortFile;
	// vm to use
	private static String vm;
	// arguments to pass to Eclipse
	private static List vmArgs;
	// arguments to pass to VM
	private static List eclipseArgs;
	// help command to execute
	private static List helpCommand;
	// host to override appserver preferences
	private static String host;
	// port to override appserver preferences
	private static String port;
	/**
	 * Initializes options.
	 * 
	 * @param appId
	 *            eclipse application id
	 * @param args
	 *            array of String options and their values Option
	 *            <code>-eclipseHome dir</code> specifies Eclipse installation
	 *            directory. It must be provided, when current directory is not
	 *            the same as Eclipse installation directory. Additionally, most
	 *            options accepted by Eclipse execuable are supported.
	 */
	public static void init(String appId, String[] args) {
		// convert array of arguments to a list
		List list = new ArrayList();
		for (int i = 0; i < args.length; i++) {
			list.add(args[i]);
		}

		init(appId, list);
	}
	/**
	 * Initializes options.
	 * 
	 * @param appId
	 *            eclipse application id
	 * @param options
	 *            list of options and their values Option
	 *            <code>-eclipseHome dir</code> specifies Eclipse installation
	 *            directory. It must be provided, when current directory is not
	 *            the same as Eclipse installation directory. Additionally, most
	 *            options accepted by Eclipse execuable are supported.
	 */
	public static void init(String appId, List options) {
		// Initialize eclipseArgs with all passed options
		eclipseArgs = new ArrayList();
		eclipseArgs.addAll(options);

		// consume -command option
		helpCommand = extractOption(eclipseArgs, "-command"); //$NON-NLS-1$
		if (helpCommand == null) {
			helpCommand = new ArrayList(0);
		}

		// read -debug option
		if (getOption(eclipseArgs, "-debug") != null) { //$NON-NLS-1$
			debug = true;
			System.out.println("Debugging is on."); //$NON-NLS-1$
		}
		// consume -noexec option
		if (extractOption(eclipseArgs, "-noexec") != null) { //$NON-NLS-1$
			useExe = false;
		}
		// consume -eclipsehome (accept eclipse_home too) option
		List homes = extractOption(eclipseArgs, "-eclipseHome"); //$NON-NLS-1$
		if (homes == null || homes.isEmpty()) {
			homes = extractOption(eclipseArgs, "-eclipse_Home"); //$NON-NLS-1$
		}
		if (homes != null && !homes.isEmpty()) {
			eclipseHome = new File((String) homes.get(0));
		} else {
			eclipseHome = new File(System.getProperty("user.dir")); //$NON-NLS-1$
		}

		// read -data option
		List workspaces = getOption(eclipseArgs, "-data"); //$NON-NLS-1$
		if (workspaces != null && !workspaces.isEmpty()) {
			workspace = new File((String) workspaces.get(0));
		} else {
			workspace = new File(eclipseHome, "workspace"); //$NON-NLS-1$
		}
		lockFile = new File(workspace, "/.metadata/.helplock"); //$NON-NLS-1$
		hostPortFile = new File(workspace, "/.metadata/.connection"); //$NON-NLS-1$

		// consume -host option
		List hosts = extractOption(eclipseArgs, "-host"); //$NON-NLS-1$
		if (hosts != null && hosts.size() > 0) {
			host = (String) hosts.get(0);
		}

		// consume -port option
		List ports = extractOption(eclipseArgs, "-port"); //$NON-NLS-1$
		if (ports != null && ports.size() > 0) {
			port = (String) ports.get(0);
		}

		// consume -vm option
		List vms = extractOption(eclipseArgs, "-vm"); //$NON-NLS-1$
		if (vms != null && !vms.isEmpty()) {
			vm = (String) vms.get(0);
		} else {
			String vmName = System.getProperty("java.vm.name"); //$NON-NLS-1$
			String executable = "J9".equals(vmName) ? "j9" : "java"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (System.getProperty("os.name").startsWith("Win")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (!debug) {
					executable += "w.exe"; //$NON-NLS-1$
				} else {
					executable += ".exe"; //$NON-NLS-1$
				}
			}
			vm = System.getProperty("java.home") //$NON-NLS-1$
					+ File.separator + "bin" //$NON-NLS-1$
					+ File.separator + executable;
		}

		// consume -vmargs option
		vmArgs = new ArrayList(0);
		List passedVmArgs = extractOption(eclipseArgs, "-vmargs"); //$NON-NLS-1$
		if (passedVmArgs != null && passedVmArgs.size() > 0) {
			vmArgs = passedVmArgs;
		}

		// modify the options for passing them to eclipse
		// add -application option
		extractOption(eclipseArgs, "-application"); //$NON-NLS-1$
		eclipseArgs.add(0, "-application"); //$NON-NLS-1$
		eclipseArgs.add(1, appId);

		// add -nosplash option (prevent splash)
		extractOption(eclipseArgs, "-showsplash"); //$NON-NLS-1$
		extractOption(eclipseArgs, "-endsplash"); //$NON-NLS-1$
		extractOption(eclipseArgs, "-nosplash"); //$NON-NLS-1$
		eclipseArgs.add(0, "-nosplash"); //$NON-NLS-1$

		// add server_host and/or port to -vmargs option
		if (host != null || port != null) {
			if (host != null) {
				vmArgs.add("-Dserver_host=" + host); //$NON-NLS-1$
			}
			if (port != null) {
				vmArgs.add("-Dserver_port=" + port); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Returns true if debugging is enabled
	 */
	public static boolean isDebug() {
		return debug;
	}

	public static File getConnectionFile() {
		return hostPortFile;
	}

	public static File getLockFile() {
		return lockFile;
	}

	public static File getEclipseHome() {
		return eclipseHome;
	}

	public static File getWorkspace() {
		return workspace;
	}

	public static List getHelpCommand() {
		return helpCommand;
	}

	public static List getEclipseArgs() {
		return eclipseArgs;
	}

	/**
	 * Removes specified option and its list of values from a list of options
	 * 
	 * @param optionName
	 *            name of the option e.g. -data
	 * @return List of String values of the specified option, or null if option
	 *         is not present
	 */
	private static List extractOption(List options, String optionName) {
		List values = null;
		for (int i = 0; i < options.size();) {
			if (optionName.equalsIgnoreCase((String) options.get(i))) {
				if (values == null) {
					values = new ArrayList(1);
				}
				// found the option, remove option
				options.remove(i);
				// remove option parameters
				while (i < options.size()) {
					if (((String) options.get(i)).startsWith("-") //$NON-NLS-1$
							&& !optionName.equals("-vmargs")) { //$NON-NLS-1$
						// start of next option
						break;
					}
					// note, and remove option value
					values.add(options.get(i));
					options.remove(i);
				}
			} else {
				i++;
			}
		}
		return values;
	}

	/**
	 * Obtains specified option and its list of values from a list of options
	 * 
	 * @param optionName
	 *            name of the option e.g. -data
	 * @param options
	 *            List of Eclipse options
	 * @return List of String values of the specified option, or null if option
	 *         is not present
	 */
	private static List getOption(List options, String optionName) {
		List values = null;
		for (int i = 0; i < options.size(); i++) {
			if (optionName.equalsIgnoreCase((String) options.get(i))) {
				if (values == null) {
					values = new ArrayList(1);
				}
				// read option parameters
				for (int j = i + 1; j < options.size(); j++) {
					if (((String) options.get(j)).startsWith("-") //$NON-NLS-1$
							&& !optionName.equals("-vmargs")) { //$NON-NLS-1$
						// start of next option
						i = j;
						break;
					}
					values.add(options.get(j));
				}
			}
		}
		return values;
	}
	public static String getVm() {
		return vm;
	}
	public static List getVmArgs() {
		return vmArgs;
	}
	/**
	 * Returns the useExe.
	 * 
	 * @return boolean
	 */
	public static boolean useExe() {
		return useExe;
	}

}