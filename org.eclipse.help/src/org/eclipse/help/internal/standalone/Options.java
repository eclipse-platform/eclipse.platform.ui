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
 * Options for starting stand alone help and infocenter.
 */
public class Options {
	// debugging
	private static boolean debug = false;
	// Eclipse installation directory
	private static File eclipseHome;
	// workspace directory to be used by Eclipse
	private static File workspace;
	// Eclipse .lock file
	private static File lockFile;
	// .hostport file to obtain help server host and port from Eclipse help application
	private static File hostPortFile;
	// arguments to pass to Eclipse
	private static List eclipseArgs;
	// help command to execute
	private static List helpCommand;
	// input list of args
	private static List argsList;
	// host to override appserver preferences
	private static String host;
	// port to override appserver preferences
	private static String port;
	/**
	 * Initializes options.
	 * @param appId eclipse application id
	 * @param args array of String options and their values
	 * 	Option <code>-eclipseHome dir</code> specifies Eclipse
	 *  installation directory.
	 *  It must be provided, when current directory is not the same
	 *  as Eclipse installation directory.
	 *  Additionally, most options accepted by Eclipse execuable are supported.
	 */
	public static void init(String appId, String[] args) {

		// convert array of arguments to a list
		argsList = new ArrayList();
		for (int i = 0; i < args.length; i++) {
			argsList.add(args[i]);
		}

		init(appId, argsList);
	}
	/**
	 * Initializes options.
	 * @param appId eclipse application id
	 * @param options list of options and their values
	 * 	Option <code>-eclipseHome dir</code> specifies Eclipse
	 *  installation directory.
	 *  It must be provided, when current directory is not the same
	 *  as Eclipse installation directory.
	 *  Additionally, most options accepted by Eclipse execuable are supported.
	 */
	public static void init(String appId, List options) {

		argsList = options;

		// consume -command option
		helpCommand = extractOption("-command");

		// read -debug option
		if (getOption("-debug") != null) {
			debug = true;
			System.out.println("Debugging is on.");
		}
		// consume -eclipsehome (accept eclipse_home too) option
		List homes = extractOption("-eclipseHome");
		if (homes.isEmpty()) {
			homes = extractOption("-eclipse_Home");
		}
		if (!homes.isEmpty()) {
			eclipseHome = new File((String) homes.get(0));
		} else {
			eclipseHome = new File(System.getProperty("user.dir"));
		}

		// read -data option
		List workspaces = getOption("-data");
		if (workspaces != null && !workspaces.isEmpty()) {
			workspace = new File((String) workspaces.get(0));
		} else {
			workspace = new File(eclipseHome, "workspace");
		}
		lockFile = new File(workspace, "/.metadata/.lock");
		hostPortFile = new File(workspace, "/.metadata/.connection");

		// consume -host option
		List hosts = extractOption("-host");
		if (hosts != null && hosts.size() > 0) {
			host = (String) hosts.get(0);
		}

		// consume -port option
		List ports = extractOption("-port");
		if (ports != null && ports.size() > 0) {
			port = (String) ports.get(0);
		}

		// modify the options for passing them to eclipse
		eclipseArgs = prepareEclipseOptions(appId);
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
	 * Removes specified option and its list of values
	 * from a list of options
	 * @param optionName name of the option e.g. -data
	 * @return List of String values of the specified option
	 */
	private static List extractOption(String optionName) {
		List values = new ArrayList(1);
		for (int i = 0; i < argsList.size();) {
			if (optionName.equalsIgnoreCase((String) argsList.get(i))) {
				// found the option, remove option
				argsList.remove(i);
				while (i < argsList.size()) {
					if (((String) argsList.get(i)).startsWith("-")) {
						// start of next option
						break;
					}
					// note, and remove option value
					values.add(argsList.get(i));
					argsList.remove(i);
				}
			} else {
				i++;
			}
		}
		return values;
	}

	private static List prepareEclipseOptions(String appId) {
		// add -vm option if not present
		List vms = getOption("-vm");
		if (vms == null || vms.isEmpty()) {
			String vm = System.getProperty("java.vm.name");
			String executable = "J9".equals(vm) ? "j9" : "java";
			if (System.getProperty("os.name").startsWith("Win")) {
				executable += "w.exe";
			}
			String javaExe =
				System.getProperty("java.home")
					+ File.separator
					+ "bin"
					+ File.separator
					+ executable;
			argsList.add(0, "-vm");
			argsList.add(1, javaExe);

		}

		// add -application option
		extractOption("-application");
		argsList.add(0, "-application");
		argsList.add(1, appId);

		// add -nosplash option (prevent splash)
		extractOption("-showsplash");
		extractOption("-endsplash");
		extractOption("-nosplash");
		argsList.add(0, "-nosplash");

		// add server_host and/or port to -vmargs option
		if (host != null || port != null) {
			List vmargs = extractOption("-vmargs");
			argsList.add("-vmargs");
			for (Iterator i = vmargs.iterator(); i.hasNext();) {
				argsList.add((String) i.next());
			}
			if (host != null) {
				argsList.add("-Dserver_host=" + host);
			}
			if (port != null) {
				argsList.add("-Dserver_port=" + port);
			}
		}

		return argsList;
	}

	/**
	 * Obtains specified option and its list of values
	 * from a list of options
	 * @param optionName name of the option e.g. -data
	 * @param options List of Eclipse options
	 * @return List of String values of the specified option,
	 *  or null if option is not present
	 */
	private static List getOption(String optionName) {
		List values = null;
		for (int i = 0; i < argsList.size(); i++) {
			if (optionName.equalsIgnoreCase((String) argsList.get(i))) {
				if (values == null) {
					values = new ArrayList(1);
				}
				for (int j = i + 1; j < argsList.size(); j++) {
					if (((String) argsList.get(j)).startsWith("-")) {
						// start of next option
						i = j;
						break;
					}
					values.add(argsList.get(j));
				}
			}
		}
		return values;
	}
}
