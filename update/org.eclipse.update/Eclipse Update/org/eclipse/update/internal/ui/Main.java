package org.eclipse.update.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.net.*;
import java.lang.reflect.*;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;
/**
 * Startup class for Eclipse. Creates a class loader using
 * supplied URL of platform installation, loads and calls
 * the Eclipse Boot Loader
 */
public class Main {
	protected boolean debug = false;
	protected boolean usage = false;
	protected String bootLocation = null;
	protected String application;
	protected URL pluginPathLocation;
	protected String location;

	// constants
	private static final String APPLICATION = "-application";
	private static final String BOOT = "-boot";
	private static final String DEBUG = "-debug";
	private static final String USAGE = "-?";
	private static final String BOOTLOADER = "org.eclipse.core.boot.BootLoader";

	// The project containing the boot loader code.  This is used to construct
	// the correct class path for running in VAJ and VAME.
	private static final String PROJECT_NAME = "Eclipse Core Boot";

	private static boolean inVAJ;
	static {
		try {
			Class.forName("com.ibm.uvm.lang.ProjectClassLoader");
			inVAJ = true;
		} catch (Exception e) {
			inVAJ = false;
		}
	}
	private static boolean inVAME;
	static {
		try {
			Class.forName("com.ibm.eclipse.VAME");
			inVAME = true;
		} catch (Exception e) {
			inVAME = false;
		}
	}

protected void basicRun(String[] args) throws Exception {
	Class clazz = getBootLoader(bootLocation);
	Method method = clazz.getDeclaredMethod("run", new Class[] { String.class, URL.class, String.class, String[].class });
	try {
		method.invoke(clazz, new Object[] { application, pluginPathLocation, location, args });
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		else
			throw e;
	}
}
public Class getBootLoader(String base) throws Exception {
	URLClassLoader loader = new URLClassLoader(new URL[] { getBootURL(base)}, null);
	return loader.loadClass(BOOTLOADER);
}
/**
 * Returns the <code>URL</code> where the boot classes are located.
 */
protected URL getBootURL(String base) throws MalformedURLException {
	URL url = null;
	if (base != null) 
		return new URL(base);
	// Create a URL based on the location of this class' code.
	// strip off jar file and/or last directory to get 
	// to the directory containing projects.
	url = getClass().getProtectionDomain().getCodeSource().getLocation();
	String path = url.getFile();
	if (path.endsWith(".jar"))
		path = path.substring(0, path.lastIndexOf("/"));
	if (path.endsWith("/"))
		path = path.substring(0, path.length() - 1);
	if (inVAJ || inVAME) {
		int ix = path.lastIndexOf("/");
		path = path.substring(0, ix + 1);
		path = path + PROJECT_NAME + "/";
	} else
		path = path + "/plugins/org.eclipse.core.boot/boot.jar";
	return new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
}
/**
 * Print the usage of this launcher on the system console
 */
protected void printUsage(PrintWriter out) {
	out.println("The general form of using the Platform bootstrap main is:");
	out.println("      java <main class> -application <name> [option list]");
	out.println("where the required arguments are:");
	out.println("      -application <name> : the application to run ");
	out.println("and where the option list can be any number of the following:");
	out.println("      -platform <location> : run in the given location");
	out.println("      -debug [debug options file] : turn on debug mode.  Read debug options from ");
	out.println("          the specified file or from ./.options if not specified.");
	out.println("      -? : print this message");
	out.flush();
}
protected String[] processCommandLine(String[] args) throws Exception {
	int[] configArgs = new int[100];
	configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
	int configArgIndex = 0;
	for (int i = 0; i < args.length; i++) {
		boolean found = false;
		// check for args without parameters (i.e., a flag arg)
		// check if debug should be enabled for the entire platform
		if (args[i].equalsIgnoreCase(DEBUG)) {
			debug = true;
			// passed thru this arg (i.e., do not set found = true
		}

		// look for the usage flag
		if (args[i].equalsIgnoreCase(USAGE)) {
			usage = true;
			// passed thru this arg (i.e., do not set found = true
		}

		if (found) {
			configArgs[configArgIndex++] = i;
			continue;
		}
		// check for args with parameters. If we are at the last argument or if the next one
		// has a '-' as the first character, then we can't have an arg with a parm so continue.
		if (i == args.length - 1 || args[i + 1].startsWith("-")) {
			continue;
		}
		String arg = args[++i];

		// look for the laucher to run
		if (args[i - 1].equalsIgnoreCase(BOOT)) {
			found = true;
			bootLocation = arg;
		}

		// look for the application to run
		if (args[i - 1].equalsIgnoreCase(APPLICATION)) {
			found = true;
			application = arg;
		}

		// done checking for args.  Remember where an arg was found 
		if (found) {
			configArgs[configArgIndex++] = i - 1;
			configArgs[configArgIndex++] = i;
		}
	}

	// remove all the arguments consumed by this argument parsing
	if (configArgIndex == 0)
		return args;
	String[] passThruArgs = new String[args.length - configArgIndex];
	configArgIndex = 0;
	int j = 0;
	for (int i = 0; i < args.length; i++) {
		if (i == configArgs[configArgIndex])
			configArgIndex++;
		else
			passThruArgs[j++] = args[i];
	}
	return passThruArgs;
}
public void run(String[] args) throws Exception {
	String[] passThruArgs = processCommandLine(args);
	if (usage) {
		printUsage(new PrintWriter(System.out));
		return;
	}
	basicRun(passThruArgs);
}
}
