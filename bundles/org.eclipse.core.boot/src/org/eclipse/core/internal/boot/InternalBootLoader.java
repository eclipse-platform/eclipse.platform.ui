package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.boot.*;
import org.eclipse.core.internal.boot.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.zip.ZipFile;
import java.util.*;

/**
 * Special boot loader class for the Eclipse Platform. This class cannot
 * be instantiated; all functionality is provided by static methods.
 * <p>
 * The Eclipse Platform makes heavy use of Java class loaders for
 * loading plug-ins. Even the Platform Core Runtime itself, including
 * the <code>Platform</code> class, needs to be loaded by a special 
 * class loader. The upshot is that a client program (such as a Java main
 * program, a servlet) cannot directly reference even the 
 * <code>Platform</code> class. Instead, a client must use this
 * loader class for initializing the platform, invoking functionality
 * defined in plug-ins, and shutting down the platform when done.
 * </p>
 *
 * @see org.eclipse.core.runtime.Platform
 */
public final class InternalBootLoader {
	private static boolean running = false;
	private static boolean starting = false;
	private static String[] commandLine;
	private static ClassLoader loader = null;
	private static String baseLocation = null;
	private static URL plugins = null;
	private static String application = null;
	private static URL installURL = null;
	private static boolean debugRequested = false;
	private static boolean usage = false;
	private static String devClassPath = null;
	private static String debugOptionsFilename = null;
	private static Properties options = null;
	private static boolean inDevelopmentMode = false;

	// state for tracking the Platform context (e.g., the OS, Window system, locale, ...)
	private static String NL = null;
	private static String WS = null;
	private static String OS = null;
	private static final String[] OS_LIST = { BootLoader.OS_WIN32, BootLoader.OS_LINUX, BootLoader.OS_AIX, BootLoader.OS_SOLARIS, BootLoader.OS_HPUX };
	
	private static final String PLATFORM_ENTRYPOINT = "org.eclipse.core.internal.runtime.InternalPlatform";
	private static final String BOOTNAME = "org.eclipse.core.boot";
	private static final String RUNTIMENAME = "org.eclipse.core.runtime";
	private static final String PLUGINSDIR = "plugins/";
	private static final String LIBRARY = "library";
	private static final String EXPORT = "export";
	private static final String EXPORT_PUBLIC = "public";
	private static final String EXPORT_PROTECTED = "protected";
	private static final String META_AREA = ".metadata";
	private static final String PLUGIN_PATH = ".plugin-path";
	private static final String BOOTDIR = PLUGINSDIR + BOOTNAME + "/";
	private static final String RUNTIMEDIR = PLUGINSDIR + RUNTIMENAME + "/";
	private static final String CONFIG = "platform.properties";
	private static final String OPTIONS = ".options";

	/** 
	 * Execution options for the Runtime plug-in.  They are defined here because
	 * we need to load them into the PlatformClassLoader which is created by the
	 * boot system.  Users should see these options as Runtime options since there
	 * boot does not figure into normal Platform operation.
	 */
	private static final String PI_RUNTIME = "org.eclipse.core.runtime";
	private static final String OPTION_STARTTIME = PI_RUNTIME + "/starttime";
	private static final String OPTION_LOADER_DEBUG = PI_RUNTIME + "/loader/debug";
	private static final String OPTION_LOADER_SHOW_CREATE = PI_RUNTIME + "/loader/debug/create";
	private static final String OPTION_LOADER_SHOW_ACTIVATE = PI_RUNTIME + "/loader/debug/activateplugin";
	private static final String OPTION_LOADER_SHOW_ACTIONS = PI_RUNTIME + "/loader/debug/actions";
	private static final String OPTION_LOADER_SHOW_SUCCESS = PI_RUNTIME + "/loader/debug/success";
	private static final String OPTION_LOADER_SHOW_FAILURE = PI_RUNTIME + "/loader/debug/failure";
	private static final String OPTION_LOADER_FILTER_CLASS = PI_RUNTIME + "/loader/debug/filter/class";
	private static final String OPTION_LOADER_FILTER_LOADER = PI_RUNTIME + "/loader/debug/filter/loader";
	private static final String OPTION_LOADER_FILTER_RESOURCE = PI_RUNTIME + "/loader/debug/filter/resource";
	private static final String OPTION_LOADER_FILTER_NATIVE = PI_RUNTIME + "/loader/debug/filter/native";
	private static final String OPTION_URL_DEBUG = PI_RUNTIME+ "/url/debug";
	private static final String OPTION_URL_DEBUG_CONNECT = PI_RUNTIME+ "/url/debug/connect";
	private static final String OPTION_URL_DEBUG_CACHE_LOOKUP = PI_RUNTIME+ "/url/debug/cachelookup";
	private static final String OPTION_URL_DEBUG_CACHE_COPY = PI_RUNTIME+ "/url/debug/cachecopy";
	private static final String OPTION_UPDATE_DEBUG = PI_RUNTIME+ "/update/debug";

	// command line arguments
	private static final String DEBUG = "-debug";
	private static final String PLATFORM = "-platform";
	private static final String PLUGINS = "-plugins";
	private static final String APPLICATION = "-application";
	private static final String DEV = "-dev";
	private static final String USAGE = "-?";

	// Development mode constants
	private static final String PLUGIN_JARS = "plugin.jars";
	private static final String VA_PROPERTIES = ".va.properties";
	private static final String KEY_LIBRARY = "library";
	private static final String KEY_EXPORT = "export";
	private static final String KEY_PROJECT = "projects";

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
			Class.forName("com.ibm.eclipse.core.VAME");
			inVAME = true;
		} catch (Exception e) {
			inVAME = false;
		}
	}

/**
 * Private constructor to block instance creation.
 */
private InternalBootLoader() {
}
private static void assertNotRunning() {
	if (running)
		throw new RuntimeException("The Platform must not be running");
}
private static void assertRunning() {
	if (!running)
		throw new RuntimeException("The Platform is not running");
}
/**
 * Configure the loader.
 * Semantically, the configuration properties are similar to plugin.xml
 * They contains the equivalent to <runtime> specification.
 * The exact format of the properties is as follows ([a] is optional, 
 * a* is repeating 0 or more times, {a|b} is a choice)
 * <ul>
 * <li>library.n= name[,name]*
 * <li>export.n=mask[,mask]*
 * <eul>
 *
 *
 * index <b>.n</b> is an integer, starting from 1. If multiple <b>library</b>
 * entries are present, they must be numbered consecutively. <b>export</b>
 * entries are matched to their corresponding <b>library</b> entries.
 *
 * <b>library</b> defines a list of .jar(s), .zip(s) or directory roots to
 * search. These are specified relative to the platform install location.
 *
 * <b>export</b> optionally specifies exported classes. The mask follows
 * Java import conventions. The value indicates which classes are
 * visible to other plug-ins. Export mask value of "*" indicates a fully-exported
 * runtime. Absent export mask indicates a private runtime.
 */
private static PlatformClassLoader configurePlatformLoader(Properties config) {
	Object[] loadPath = getPlatformClassLoaderPath(config);
	URL base = null;
	try {
		base = new URL(PlatformURLBaseConnection.PLATFORM_URL_STRING+RUNTIMEDIR);
	} catch (MalformedURLException e) {
	}
	return new PlatformClassLoader((URL[]) loadPath[0], (URLContentFilter[]) loadPath[1], InternalBootLoader.class.getClassLoader(), base);
}
/**
 * @see BootLoader
 */
public static boolean containsSavedPlatform(String location) {
	return new File(location + "/" + META_AREA).exists();
}
private static URL[] defaultPluginPath() {
	URL[] result = new URL[1];
	try {
		// at this point use "real" (internal) URL to allow registry manager to discover plugins.
		result[0] = new URL(getInstallURL(),PLUGINSDIR);
	} catch (MalformedURLException e) {
	}
	return result;
}
/**
 * convert a list of comma-separated tokens into an array
 */
private static String[] getArrayFromList(String prop) {
	if (prop == null || prop.trim().equals(""))
		return new String[0];
	Vector list = new Vector();
	StringTokenizer tokens = new StringTokenizer(prop, ",");
	while (tokens.hasMoreTokens()) {
		String token = tokens.nextToken().trim();
		if (!token.equals(""))
			list.addElement(token);
	}
	return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[0]);
}
private static boolean getBooleanOption(String option, boolean defaultValue) {
	String optionValue = options.getProperty(option);
	return (optionValue == null) ? defaultValue : optionValue.equalsIgnoreCase("true");
}
/**
 * @see BootLoader#getCommandLineArgs
 */
public static String[] getCommandLineArgs() {
	return commandLine;
}
/**
 * @see BootLoader
 */
public static IInstallInfo getInstallationInfo() {
	return LaunchInfo.getCurrent();
}
/**
 * @see BootLoader
 */
public static URL getInstallURL() {
	if (installURL != null)
		return installURL;

	// Get the location of this class and compute the install location.
	// this involves striping off last element (jar or directory) and adjusting 
	// for VAJ/VAME peculiarities.
	URL url = InternalBootLoader.class.getProtectionDomain().getCodeSource().getLocation();
	String path = url.getFile();
	if (path.endsWith("/"))
		path = path.substring(0, path.length() - 1);
	int ix = path.lastIndexOf("/");
	path = path.substring(0, ix + 1);
	if (!(inVAJ || inVAME)) {
		// in jdk ... strip off BOOTDIR
		path = path.substring(0, path.length() - BOOTDIR.length());
	}

	try {
		if (url.getProtocol().equals("jar")) installURL = new URL(path);
		else installURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
		if (debugRequested) System.out.println("Install URL: "+installURL.toExternalForm());
	} catch (MalformedURLException e) {
		throw new RuntimeException("Fatal Error: Unable to determine platform installation URL "+e);
	}
	return installURL;
}
private static String[] getListOption(String option) {
	String filter = options.getProperty(option);
	if (filter == null)
		return new String[0];
	List result = new ArrayList(5);
	StringTokenizer tokenizer = new StringTokenizer(filter, " ,\t\n\r\f");
	while (tokenizer.hasMoreTokens())
		result.add(tokenizer.nextToken());
	return (String[]) result.toArray(new String[result.size()]);
}
/**
 * @see BootLoader
 */
public static String getNL() {
	return NL;
}
/**
 * @see BootLoader
 */
public static String getOS() {
	return OS;
}
private static Object[] getPlatformClassLoaderPath(Properties config) {
	// If running in va, check for va properties file. 
	// The property file has entries corresponding to <library>
	// elements from the plugin.xml. Each defined property is of form
	//
	//    projects[.libname]=proj1,proj2,proj3
	//
	// The optional ".libname" suffix allows a project mapping to be specified
	// for the corresponding <library>. The default "projects=" (w/o suffix)
	// applies to the first/ only <library> element in plugin.xml. Example:
	//
	// plugin.xml
	//   <runtime>
	//     <library name=baseapi.jar>
	//       <export name="*"/>
	//     </library>
	//     <library name=another.jar/>
	//     <library name=base.jar/>
	//   </runtime>
	//
	// .va.properties
	//    projects=Base API Project
	//    projects.base.jar=Base Project 1, Base Project 2
	//    ** OR **
	//    projects.baseapi.jar=Base API Project
	//    projects.base.jar=Base Project 1, Base Project 2
	//
	// The above results in a loader search path consisting of
	//    Base API Project
	//    another.jar
	//    Base Project 1, Base Project 2
	//
	// Any library access filters specified in the plugin.xml are
	// applied to the corresponding loader search path entries
	//
	Properties jarDefinitions = loadJarDefinitions();
	ArrayList urls = new ArrayList(5);
	ArrayList cfs = new ArrayList(5);
	String execBase = getInstallURL() + RUNTIMEDIR;
	String devBase = null;
	if (InternalBootLoader.inVAJ || InternalBootLoader.inVAME)
		devBase = getInstallURL().toExternalForm();
	else
		devBase = execBase;
	ArrayList list = new ArrayList(5);

	// process the config and create a list of alternating library and the library's export filter
	for (int i = 1; true; i++) {
		// get library specification, if we have run out of library specs, break and return
		String key = KEY_LIBRARY + "." + Integer.toString(i);
		String value = config.getProperty(key);
		if (value == null)
			break;
		String[] libList = getArrayFromList(value);
		// get the corresponding exports if any
		key = KEY_EXPORT + "." + Integer.toString(i);
		String[] filterList = getArrayFromList(config.getProperty(key));
		for (int j = 0; j < libList.length; j++) {
			list.add(libList[j]);
			list.add(filterList);
		}
	}

	// build a list alternating lib spec and export spec
	ArrayList libSpecs = new ArrayList(5);
	String[] exportAll = new String[] { "*" };

	// add in any development mode class paths and the export all filter
	if (DelegatingURLClassLoader.devClassPath != null) {
		String[] specs = getArrayFromList(DelegatingURLClassLoader.devClassPath);
		// convert dev class path into url strings
		for (int j = 0; j < specs.length; j++) {
			libSpecs.add(devBase + specs[j] + "/");
			libSpecs.add(exportAll);
		}
	}

	// add in the class path entries spec'd in the config.  If in development mode, 
	// add the entries from the plugin.jars first.
	for (Iterator i = list.iterator(); i.hasNext();) {
		String library = (String) i.next();
		String[] filters = (String[]) i.next();
		// check for jar definitions
		if (jarDefinitions != null) {
			String key = library.substring(library.lastIndexOf('/') + 1);
			String[] specs = getArrayFromList(jarDefinitions.getProperty(key));
			for (int j = 0; j < specs.length; j++) {
				libSpecs.add(devBase + specs[j] + "/");
				libSpecs.add(filters);
			}
		}

		// convert plugin.xml library entries to url strings if running in JDK
		if (!(InternalBootLoader.inVAJ || InternalBootLoader.inVAME)) {
			String libSpec = execBase + library.replace(File.separatorChar, '/');
			if (!libSpec.endsWith("/")) {
				if (libSpec.startsWith(PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR))
					libSpec += PlatformURLHandler.JAR_SEPARATOR;
				else
					libSpec = PlatformURLHandler.JAR + PlatformURLHandler.PROTOCOL_SEPARATOR + libSpec + PlatformURLHandler.JAR_SEPARATOR;
			}
			libSpecs.add(libSpec);
			libSpecs.add(filters);
		}
	}

	// create path entries for libraries
	for (Iterator it = libSpecs.iterator(); it.hasNext();) {
		try {
			urls.add(new URL((String) it.next()));
			cfs.add(new URLContentFilter((String[]) it.next()));
		} catch (MalformedURLException e) {
			// skip bad URLs
		}
	}

	Object[] result = new Object[2];
	result[0] = urls.toArray(new URL[urls.size()]);
	result[1] = cfs.toArray(new URLContentFilter[cfs.size()]);
	return result;
}
/**
 * @see BootLoader
 */
public static URL[] getPluginPath(URL pluginPathLocation) {
	InputStream input = null;
	// first try and see if the given plugin path location exists.
	if (pluginPathLocation == null)
		return defaultPluginPath();
	try {
		input = pluginPathLocation.openStream();
	} catch (IOException e) {
	}

	// if the given path was null or did not exist, look for a plugin path
	// definition in the install location.
	if (input == null)
		try {
			URL url = new URL(PlatformURLBaseConnection.PLATFORM_URL_STRING + PLUGIN_PATH);
			input = url.openStream();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}

	// if nothing was found at the supplied location or in the install 
	// location, compute the default plugin path.
	if (input == null)
		return defaultPluginPath();
	// if we found a plugin path definition somewhere so read it and close the location.
	URL[] result = null;
	try {
		try {
			result = readPluginPath(input);
		} finally {
			input.close();
		}
	} catch (IOException e) {
	}
	return result;
}
private static URL[] getPluginPathVa(URL[] pluginPath) {
	Vector result = new Vector(Arrays.asList(pluginPath));
	if (inVAME) {
		// check for projects with plugins on Java classpath
		String classpath = System.getProperty("java.class.path");
		StringTokenizer paths = new StringTokenizer(classpath, File.pathSeparator);
		while (paths.hasMoreTokens()) {
			String curr = (String) paths.nextToken();
			if (!curr.endsWith(File.separator))
				curr += File.separator;
			curr += "plugins" + File.separator;
			File dir = new File(curr);
			if (dir.isDirectory()) {
				try {
					result.add(dir.toURL());
				} catch (MalformedURLException e) {
				}
			}
		}
	} else
		if (inVAJ /*disabled*/
			&& false) {
			// check for projects with plugins in project_resources		
			File pr = new File(getInstallURL().getFile());
			String[] projects = ((projects = pr.list()) == null) ? new String[0] : projects;
			for (int i = 0; i < projects.length; i++) {
				File dir = new File(pr, projects[i] + File.separator + "plugins");
				if (dir.isDirectory()) {
					try {
						result.add(dir.toURL());
					} catch (MalformedURLException e) {
					}
				}
			}
		}

	// if there are no new entries, return original plugin path.  Otherwise, return the new path
	if (pluginPath.length == result.size())
		return pluginPath;
	else
		return (URL[]) result.toArray(new URL[result.size()]);
}
/**
 * @see BootLoader
 */
public static IPlatformRunnable getRunnable(String applicationName) throws Exception {
	assertRunning();
	Class platform = loader.loadClass(PLATFORM_ENTRYPOINT);
	Method method = platform.getDeclaredMethod("loaderGetRunnable", new Class[] {String.class});
	try {
		return (IPlatformRunnable) method.invoke(platform, new Object[] {applicationName});
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		else
			throw e;
	}
}
/**
 * @see BootLoader
 */
public static IPlatformRunnable getRunnable(String pluginId, String className, Object args) throws Exception {
	assertRunning();
	Class platform = loader.loadClass(PLATFORM_ENTRYPOINT);
	Method method = platform.getDeclaredMethod("loaderGetRunnable", new Class[] {String.class, String.class, Object.class});
	try {
		return (IPlatformRunnable) method.invoke(platform, new Object[] {pluginId, className, args});
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		else
			throw e;
	}
}
/**
 * @see BootLoader
 */
public static String getWS() {
	return WS;
}
/**
 * @see BootLoader
 */
public static boolean inDebugMode() {
	return debugRequested;
}
/**
 * @see BootLoader
 */
public static boolean inDevelopmentMode() {
	return inDevelopmentMode || inVAJ || inVAME;
}
private static String[] initialize(URL pluginPathLocation, String location, String[] args) throws Exception {
	if (running)
		throw new RuntimeException("The platform is already running");
	// preset the locations so the command line processor does not overwrite.
	plugins = pluginPathLocation;
	baseLocation = location;
	String[] appArgs = processCommandLine(args);
	// setup the devClassPath if any
	DelegatingURLClassLoader.devClassPath = devClassPath;
	setupSystemContext();

	// if a platform location was not found in the arguments, set it to user.dir.		
	// In VAJ, set user.dir to be <code>eclipse</code> in the parent of the install 
	// directory.  This typically makes the platform working directory:
	//		.../ide/eclipse
	if (baseLocation == null) {
		if (inVAJ || inVAME) {
			String dir = new File(new File(getInstallURL().getFile()).getParent(), "eclipse").getAbsolutePath();
			System.setProperty("user.dir", dir);
		}
		baseLocation = System.getProperty("user.dir");
	}

	// load any debug options
	loadOptions();

	// load update profile
	LaunchInfo.startup(getInstallURL());

	// initialize eclipse URL handling
	PlatformURLHandlerFactory.startup(baseLocation + File.separator + META_AREA);
	PlatformURLBaseConnection.startup(getInstallURL()); // past this point we can use eclipse:/platform/ URLs
	PlatformURLConfigurationConnection.startup(getInstallURL()); // past this point we can use eclipse:/configuration/ URLs
	PlatformURLComponentConnection.startup(getInstallURL()); // past this point we can use eclipse:/component/ URLs

	// create and configure platform class loader
	Properties config = loadConfiguration();
	if (config == null)
		throw new RuntimeException("Fatal Error: The platform was unable to locate/read platform.properties");
	loader = configurePlatformLoader(config);

	return appArgs;
}
/**
 * Returns the complete plugin path defined by the file at the given location.
 * If the given location is <code>null</code> or does not indicate a valid 
 * pluginPath definition file, the returned value is the default
 * pluginPath computed relative to the location of the platform being started.
 * If in development mode, the returned value may have additional VA
 * values added.
 */
private static URL[] internalGetPluginPath(URL pluginPathLocation) {
	URL[] result = getPluginPath(pluginPathLocation);
	if (result == null)
		result = defaultPluginPath();
	// augment with additional VA entries if in development mode.
	if (inDevelopmentMode())
		result = getPluginPathVa(result);
	return result;
}
/**
 * @see BootLoader
 */
public static boolean isRunning() {
	return running;
}
public static boolean isStarting() {
	return starting;
}
/**
 * Load platform configuration. Implemented as property file
 * because at this point we do not have access to xml support.
 */
private static Properties loadConfiguration() {
	Properties config = new Properties();
	try {
		URL url = BootLoader.class.getResource(CONFIG);
		InputStream input = url.openStream();
		try {
			config.load(input);
			return config;
		} finally {
			input.close();
		}
	} catch (IOException e) {
	}
	return null;
}
private static Properties loadJarDefinitions() {
	if (!inDevelopmentMode())
		return null;
	Properties result = null;
	InputStream is;
	try {
		result = new Properties();
		URL props = new URL(getInstallURL(),  PLUGINSDIR + RUNTIMENAME + "/" + PLUGIN_JARS);
		is = props.openStream();
		try {
			result.load(is);
			return result;
		} finally {
			is.close();
		}
	} catch (IOException e) {
		result = null;
	}
	return result;
}
private static void loadOptions() {
	// if no debug option was specified, don't even bother to try.
	// Must ensure that the options slot is null as this is the signal to the
	// platform that debugging is not enabled.
	if (!debugRequested) {
		options = null;
		return;
	}
	options = new Properties();
	URL optionsFile;
	if (debugOptionsFilename == null)
		debugOptionsFilename = getInstallURL().toExternalForm() + OPTIONS;
	try {
		optionsFile = new URL(debugOptionsFilename);
	} catch (MalformedURLException e) {
		System.out.println("Unable to construct URL for options file: " + debugOptionsFilename);
		e.printStackTrace(System.out);
		return;
	}
	System.out.println("Debug-Options: " + debugOptionsFilename);
	try {
		InputStream input = optionsFile.openStream();
		try {
			options.load(input);
		} finally {
			input.close();
		}
	} catch (FileNotFoundException e) {
		//	Its not an error to not find the options file
	} catch (IOException e) {
		System.out.println("Could not parse the options file: " + optionsFile);
		e.printStackTrace(System.out);
	}
	// trim off all the blanks since properties files don't do that.
	for (Iterator i = options.keySet().iterator(); i.hasNext();) {
		Object key = i.next();
		options.put(key, ((String) options.get(key)).trim());
	}
	InternalBootLoader.setupOptions();
}
private static String[] processCommandLine(String[] args) throws Exception {
	int[] configArgs = new int[100];
	configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
	int configArgIndex = 0;
	for (int i = 0; i < args.length; i++) {
		boolean found = false;
		// check for args without parameters (i.e., a flag arg)
		// check if debug should be enabled for the entire platform
		// If this is the last arg or there is a following arg (i.e., arg+1 has a leading -), 
		// simply enable debug.  Otherwise, assume that that the following arg is
		// actually the filename of an options file.  This will be processed below.
		if (args[i].equalsIgnoreCase(DEBUG) && ((i + 1 == args.length) || ((i + 1 < args.length) && (args[i + 1].startsWith("-"))))) {
			found = true;
			debugRequested = true;
		}

		// check if development mode should be enabled for the entire platform
		// If this is the last arg or there is a following arg (i.e., arg+1 has a leading -), 
		// simply enable development mode.  Otherwise, assume that that the following arg is
		// actually some additional development time class path entries.  This will be processed below.
		if (args[i].equalsIgnoreCase(DEV) && ((i + 1 == args.length) || ((i + 1 < args.length) && (args[i + 1].startsWith("-"))))) {
			inDevelopmentMode = true;
			// do not mark the arg as found so it will be passed through
			continue;
		}

		// look for the usage flag
		if (args[i].equalsIgnoreCase(USAGE)) {
			usage = true;
			found = true;
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

		// look for the debug options file location.  
		if (args[i - 1].equalsIgnoreCase(DEBUG)) {
			found = true;
			debugRequested = true;
			debugOptionsFilename = arg;
		}

		// look for the development mode and class path entries.  
		if (args[i - 1].equalsIgnoreCase(DEV)) {
			inDevelopmentMode = true;
			devClassPath = arg;
			// do not mark the arg as found so it will be passed through
			continue;
		}

		// look for the platform location.  Only set it if not already set. This 
		// preserves the value set in the startup() parameter.  Be sure however
		// to consume the command-line argument.
		if (args[i - 1].equalsIgnoreCase(PLATFORM)) {
			found = true;
			if (baseLocation == null)
				baseLocation = arg;
		}

		// look for the plugins location.  Only set it if not already set. This 
		// preserves the value set in the startup() parameter.  Be sure however
		// to consume the command-line argument.
		if (args[i - 1].equalsIgnoreCase(PLUGINS)) {
			found = true;
			// if the arg can be made into a URL use it.  Otherwise assume that
			// it is a file path so make a file URL.
			try {
				if (plugins == null)
					plugins = new URL(arg);
			} catch (MalformedURLException e) {
				try {
					plugins = new URL("file:" + arg);
				} catch (MalformedURLException e2) {
				}
			}
		}

		// look for the application to run.  Only heed the value if the application is
		// not already set.
		if (args[i - 1].equalsIgnoreCase(APPLICATION)) {
			found = true;
			if (application == null)
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
private static URL[] readPluginPath(InputStream input) {
	Properties ini = new Properties();
	try {
		ini.load(input);
	} catch (IOException e) {
		return null;
	}
	Vector result = new Vector(5);
	for (Enumeration groups = ini.propertyNames(); groups.hasMoreElements();) {
		String group = (String) groups.nextElement();
		for (StringTokenizer entries = new StringTokenizer(ini.getProperty(group), ";"); entries.hasMoreElements();) {
			String entry = (String) entries.nextElement();
			if (!entry.equals(""))
				try {
					result.addElement(new URL(entry));
				} catch (MalformedURLException e) {
				}
		}
	}
	return (URL[]) result.toArray(new URL[result.size()]);
}
/**
 * @see BootLoader
 */
public static Object run(String applicationName, URL pluginPathLocation, String location, String[] args) throws Exception {
	Object result = null;
	if (applicationName != null)
		application = applicationName;
	String[] applicationArgs = null;
	try {
		applicationArgs = startup(pluginPathLocation, location, args);
	} catch (Exception e) {
		throw e;
	}
	IPlatformRunnable runnable = getRunnable(application);
	if (runnable == null)
		throw new IllegalArgumentException("Application not found: " + application);
	try {
		result = runnable.run(applicationArgs);
	} catch (Throwable e) {
		e.printStackTrace();
		throw e;
	} finally {
		shutdown();
		return result;
	}
}
/**
 * Setup the debug flags for the given debug options.  This method will likely
 * be called twice.  Once when loading the options file from the command
 * line or install dir and then again when we have loaded options from the
 * specific platform metaarea. 
 */
public static void setupOptions() {
	// if no debug option was specified, don't even bother to try.
	// Must ensure that the options slot is null as this is the signal to the
	// platform that debugging is not enabled.
	if (!debugRequested)
		return;
	options.put(OPTION_STARTTIME, Long.toString(System.currentTimeMillis()));
	DelegatingURLClassLoader.DEBUG = getBooleanOption(OPTION_LOADER_DEBUG, false);
	DelegatingURLClassLoader.DEBUG_SHOW_CREATE = getBooleanOption(OPTION_LOADER_SHOW_CREATE, true);
	DelegatingURLClassLoader.DEBUG_SHOW_ACTIVATE = getBooleanOption(OPTION_LOADER_SHOW_ACTIVATE, true);
	DelegatingURLClassLoader.DEBUG_SHOW_ACTIONS = getBooleanOption(OPTION_LOADER_SHOW_ACTIONS, true);
	DelegatingURLClassLoader.DEBUG_SHOW_SUCCESS = getBooleanOption(OPTION_LOADER_SHOW_SUCCESS, true);
	DelegatingURLClassLoader.DEBUG_SHOW_FAILURE = getBooleanOption(OPTION_LOADER_SHOW_FAILURE, true);
	DelegatingURLClassLoader.DEBUG_FILTER_CLASS = getListOption(OPTION_LOADER_FILTER_CLASS);
	DelegatingURLClassLoader.DEBUG_FILTER_LOADER = getListOption(OPTION_LOADER_FILTER_LOADER);
	DelegatingURLClassLoader.DEBUG_FILTER_RESOURCE = getListOption(OPTION_LOADER_FILTER_RESOURCE);
	DelegatingURLClassLoader.DEBUG_FILTER_NATIVE = getListOption(OPTION_LOADER_FILTER_NATIVE);
	PlatformURLConnection.DEBUG = getBooleanOption(OPTION_URL_DEBUG, false);
	PlatformURLConnection.DEBUG_CONNECT = getBooleanOption(OPTION_URL_DEBUG_CONNECT, true);
	PlatformURLConnection.DEBUG_CACHE_LOOKUP = getBooleanOption(OPTION_URL_DEBUG_CACHE_LOOKUP, true);
	PlatformURLConnection.DEBUG_CACHE_COPY = getBooleanOption(OPTION_URL_DEBUG_CACHE_COPY, true);
	LaunchInfo.DEBUG = getBooleanOption(OPTION_UPDATE_DEBUG, false);
}
/**
 * Initializes the execution context for this run of the platform.  The context
 * includes information about the locale, operating system and window system.
 */
private static void setupSystemContext() {
	if (NL == null)
		NL = Locale.getDefault().toString();
	if (OS == null) {
		String name = System.getProperty("os.name");
		for (int i = 0; i < OS_LIST.length; i++)
			if (name.regionMatches(true, 0, OS_LIST[i], 0, 3))
				OS = OS_LIST[i];
		if (OS == null)
			OS = BootLoader.OS_UNKNOWN;
	}
	if (WS == null)
		WS = BootLoader.WS_UNKNOWN;
}
/**
 * @see BootLoader
 */
public static void shutdown() throws Exception {
	assertRunning();
	// no matter what happens, record that its no longer running
	running = false;
	Class platform = loader.loadClass(PLATFORM_ENTRYPOINT);
	Method method = platform.getDeclaredMethod("loaderShutdown", new Class[0]);
	try {
		method.invoke(platform, new Object[0]);
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		else
			throw e;
	} finally {
		PlatformURLHandlerFactory.shutdown();
		LaunchInfo.shutdown();
		loader = null;
	}
}
/**
 * @see BootLoader
 */
public static String[] startup(URL pluginPathLocation, String location, String[] args) throws Exception {
	assertNotRunning();
	starting = true;
	commandLine = args;
	String[] applicationArgs = initialize(pluginPathLocation, location, args);
	Class platform = loader.loadClass(PLATFORM_ENTRYPOINT);
	Method method = platform.getDeclaredMethod("loaderStartup", new Class[] { URL[].class, String.class, Properties.class, String[].class });
	try {
		URL[] pluginPath = internalGetPluginPath(plugins);
		method.invoke(platform, new Object[] { pluginPath, baseLocation, options, args });
	} catch (InvocationTargetException e) {
		if (e.getTargetException() instanceof Error)
			throw (Error) e.getTargetException();
		else
			throw e;
	}
	// only record the platform as running if everything went swimmingly
	running = true;
	starting = false;
	return applicationArgs;
}
}
