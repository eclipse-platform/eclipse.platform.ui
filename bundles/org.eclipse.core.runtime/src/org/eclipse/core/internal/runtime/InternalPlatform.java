/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.core.internal.runtime;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.plugins.*;
import java.io.*;
import java.io.FileOutputStream;
import java.net.*;
import java.util.*;

/**
 * Bootstrap class for the platform. It is responsible for setting up the
 * platform class loader and passing control to the actual application class
 */
public final class InternalPlatform {
	private static IAdapterManager adapterManager;
	private static PluginRegistry registry;
	// registry index - used to store last modified times for
	// registry caching
	// ASSUMPTION:  Only the plugin registry in 'registry' above
	// will be cached
	private static Map regIndex = null;
	
	private static Set logListeners = new HashSet(5);
	private static Map logs = new HashMap(5);
	private static PlatformLogWriter platformLog = null;
	private static PlatformMetaArea metaArea;
	private static boolean initialized;
	private static Runnable endOfInitializationHandler = null;
	private static IPath location;
	private static PluginClassLoader xmlClassLoader = null;
	private static long cacheReadTimeStamp;

	private static boolean debugEnabled = false;
	private static boolean consoleLogEnabled = false;
	private static ILogListener consoleLog = null;
	private static Properties options = null;
	private static AuthorizationDatabase keyring = null;
	private static String keyringFile = null;
	private static String password = "";
	private static boolean splashDown = false;
	private static boolean cacheRegistry = true;
	private static String pluginCustomizationFile = null;

	private static File lockFile = null;
	private static RandomAccessFile lockRAF = null;
	
	/**
	 * Name of the plug-in customization file (value "plugin_customization.ini")
	 * located in the root of the primary feature plug-in.
	 */
	private static final String PLUGIN_CUSTOMIZATION_FILE_NAME = "plugin_customization.ini";

	// default plugin data
	private static final String PI_XML = "org.apache.xerces";
	private static final String PLUGINSDIR = "plugins/";
	private static final String XML_LOCATION = "plugins/" + PI_XML + "/";
	
	// execution options
	private static final String OPTION_DEBUG = Platform.PI_RUNTIME + "/debug";
	private static final String OPTION_DEBUG_SYSTEM_CONTEXT = Platform.PI_RUNTIME + "/debug/context";
	private static final String OPTION_DEBUG_STARTUP = Platform.PI_RUNTIME + "/timing/startup";
	private static final String OPTION_DEBUG_SHUTDOWN = Platform.PI_RUNTIME + "/timing/shutdown";
	private static final String OPTION_DEBUG_PLUGINS = Platform.PI_RUNTIME + "/registry/debug";
	private static final String OPTION_DEBUG_PLUGINS_DUMP = Platform.PI_RUNTIME + "/registry/debug/dump";
	private static final String OPTION_DEBUG_PREFERENCES = Platform.PI_RUNTIME + "/preferences/debug";

	// command line options
	private static final String LOG = "-consolelog";
	private static final String KEYRING = "-keyring";
	private static final String PASSWORD = "-password";
	private static final String NOREGISTRYCACHE = "-noregistrycache";
	private static final String PLUGIN_CUSTOMIZATION = "-plugincustomization";
	
	// debug support:  set in loadOptions()
	public static boolean DEBUG = false;
	public static boolean DEBUG_CONTEXT = false;
	public static boolean DEBUG_PLUGINS = false;
	public static boolean DEBUG_STARTUP = false;
	public static boolean DEBUG_SHUTDOWN = false;
	public static String DEBUG_PLUGINS_DUMP = "";
	public static boolean DEBUG_PREFERENCES = false;

/**
 * Private constructor to block instance creation.
 */
private InternalPlatform() {
}
/**
 * The runtime plug-in is not totally real due to bootstrapping problems.
 * This method builds the required constructs to activate and install
 * the runtime plug-in.
 */
private static void activateDefaultPlugins() throws CoreException {
	// for now, simply do the default activation.  This does not do the right thing
	// wrt the plugin class loader.
	PluginDescriptor descriptor = (PluginDescriptor) registry.getPluginDescriptor(Platform.PI_RUNTIME);
	DelegatingURLClassLoader loader = PlatformClassLoader.getDefault();
	descriptor.activateDefaultPlugins(loader);
	descriptor.setPluginClassLoader(loader);
	descriptor.getPlugin();

	descriptor = (PluginDescriptor) registry.getPluginDescriptor(PI_XML);
	descriptor.activateDefaultPlugins(xmlClassLoader);
	descriptor.setPluginClassLoader(xmlClassLoader);
	xmlClassLoader.setPluginDescriptor(descriptor);
	descriptor.getPlugin();
}
/**
 * @see Platform
 */
public static void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
	keyring.addAuthorizationInfo(serverUrl, realm, authScheme, new HashMap(info));
	keyring.save();
}
/**
 * @see Platform#addLogListener
 */
public static void addLogListener(ILogListener listener) {
	assertInitialized();
	synchronized (logListeners) {
		logListeners.add(listener);
	}
}
/**
 * @see Platform
 */
public static void addProtectionSpace(URL resourceUrl, String realm) throws CoreException {
	keyring.addProtectionSpace(resourceUrl, realm);
	keyring.save();
}
/**
 * @see Platform
 */
public static URL asLocalURL(URL url) throws IOException {
	if (!url.getProtocol().equals(PlatformURLHandler.PROTOCOL))
		return url;
	URLConnection connection = url.openConnection();
	if (!(connection instanceof PlatformURLConnection))
		return url;
	String file = connection.getURL().getFile();
	if (file.endsWith("/") && !file.endsWith(PlatformURLHandler.JAR_SEPARATOR))
		throw new IOException();
	return ((PlatformURLConnection) connection).getURLAsLocal();
}
private static void assertInitialized() {
	Assert.isTrue(initialized, "meta.appNotInit");
}
/**
 * Closes the open lock file handle, and makes a silent best
 * attempt to delete the file.
 */
private static synchronized void clearLockFile() {
	try {
		if (lockRAF != null) {
			lockRAF.close();
			lockRAF = null;
		}
	} catch (IOException e) {
		//don't complain, we're making a best effort to clean up
	}
	if (lockFile != null) {
		lockFile.delete();
		lockFile = null;
	}
}
/**
 * Creates a lock file in the meta-area that indicates the meta-area
 * is in use, preventing other eclipse instances from concurrently
 * using the same meta-area.
 */
private static synchronized void createLockFile() throws CoreException {
	String lockLocation = metaArea.getLocation().append(PlatformMetaArea.F_LOCK_FILE).toOSString();
	lockFile = new File(lockLocation);
	//if the lock file already exists, try to delete,
	//assume failure means another eclipse has it open
	if (lockFile.exists())
		lockFile.delete();
	if (lockFile.exists()) {
		String message = Policy.bind("meta.inUse", lockLocation);
		throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.FAILED_WRITE_METADATA, message, null));
	}
	try {
		//open the lock file so other instances can't co-exist
		lockRAF = new RandomAccessFile(lockFile, "rw");
		lockRAF.writeByte(0);
	} catch (IOException e) {
		String message = Policy.bind("meta.failCreateLock", lockLocation);
		throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.FAILED_WRITE_METADATA, message, e));
	}
}

/**
 * Creates and remembers a spoofed up class loader which loads the
 * classes from a predefined XML plugin.
 */
private static void createXMLClassLoader() {
	// create a plugin descriptor which is sufficient to be able to create
	// the class loader through the normal channels.
	Factory factory = new InternalFactory(null);
	PluginDescriptor descriptor = (PluginDescriptor) factory.createPluginDescriptor();	
	descriptor.setEnabled(true);
	descriptor.setId(PI_XML);
	PlatformConfiguration config = InternalBootLoader.getCurrentPlatformConfiguration();
	PlatformConfiguration.BootDescriptor bd = config.getPluginBootDescriptor(PI_XML);
	descriptor.setVersion(bd.getVersion());
	try {
		URL url = bd.getPluginDirectoryURL();
		if (url == null)
			url = new URL(BootLoader.getInstallURL(), XML_LOCATION);
		descriptor.setLocation(url.toExternalForm());
	} catch (MalformedURLException e) {
		// ISSUE: What to do when this fails.  It's pretty serious
		e.printStackTrace();
	}

	ArrayList libList = new ArrayList();
	String[] libs = bd.getLibraries();
	for (int i=0; i<libs.length; i++) {
		LibraryModel lib = factory.createLibrary();
		lib.setName(libs[i]);
		lib.setExports(new String[] { "*" });
		libList.add(lib);
	}
	descriptor.setRuntime((LibraryModel[]) libList.toArray(new LibraryModel[0]));

	// use the fake plugin descriptor to create the desired class loader.
	// Since this class loader will be used before the plugin registry is installed,
	// ensure that the URLs on its class path are raw as opposed to eclipse:
	xmlClassLoader = (PluginClassLoader) descriptor.getPluginClassLoader(false);
}
/**
 * @see Platform
 */
public static void endSplash() {
	if (DEBUG) {
		String startString = Platform.getDebugOption(Platform.OPTION_STARTTIME);
		if (startString != null) 
			try {
				long start = Long.parseLong(startString);
				long end = System.currentTimeMillis();
				System.out.println("Startup complete: " + (end - start) + "ms");
			} catch (NumberFormatException e) {
				//this is just debugging code -- ok to swallow exception
			}
	}	
	if (splashDown) 
		return;
		
	splashDown = true;
	run(endOfInitializationHandler);
}

/**
 * @see Platform
 */
public static void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
	keyring.flushAuthorizationInfo(serverUrl, realm, authScheme);
	keyring.save();
}
/**
 * @see Platform#getAdapterManager
 */
public static IAdapterManager getAdapterManager() {
	assertInitialized();
	return adapterManager;
}

/**
 * Augments the plugin path with extra entries.
 */
private static URL[] getAugmentedPluginPath(URL[] pluginPath) {
	
	// ISSUE: this code needs to be reworked so that the platform
	//        does not have logical reference to plug-in-specific
	//        function
		
	IPath result = metaArea.getLocation().append(PlatformMetaArea.F_PLUGIN_DATA).append("org.eclipse.scripting").append("plugin.xml");
	String userScriptName = result.toString();
	URL userScriptUrl = null;
	try {
		userScriptUrl = new URL("file",null,0,userScriptName);
	} catch(MalformedURLException e) {
		return pluginPath;
	}
		
	URL[] newPath = new URL[pluginPath.length+1];
	System.arraycopy(pluginPath,0,newPath,0, pluginPath.length);
	newPath[newPath.length-1] = userScriptUrl;	
	return newPath;
}
/**
 * @see Platform
 */
public static Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
	Map info = keyring.getAuthorizationInfo(serverUrl, realm, authScheme);
	return info == null ? null : new HashMap(info);
}
public static boolean getBooleanOption(String option, boolean defaultValue) {
	String optionValue = options.getProperty(option);
	return (optionValue != null && optionValue.equalsIgnoreCase("true"))  || defaultValue;
}
/**
 * @see Platform
 */
public static String getDebugOption(String option) {
	return debugEnabled ? options.getProperty(option) : null;
}
public static int getIntegerOption(String option, int defaultValue) {
	String value = getDebugOption(option);
	try {
		return value == null ? defaultValue : Integer.parseInt(value);
	} catch (NumberFormatException e) {
		return defaultValue;
	}
}
/**
 * @see Platform#getLocation
 */
public static IPath getLocation() {
	assertInitialized();
	return location;
}
/**
 * Returns a log for the given plugin or <code>null</code> if none exists.
 */
public static ILog getLog(Plugin plugin) {
	ILog result = (ILog) logs.get(plugin);
	if (result != null)
		return result;
	result = new Log(plugin);
	logs.put(plugin, result);
	return result;
}
/**
 * Returns the object which defines the location and organization
 * of the platform's meta area.
 */
public static PlatformMetaArea getMetaArea() {
	return metaArea;
}
/**
 * @see Platform#getPlugin
 */
public static Plugin getPlugin(String id) {
	assertInitialized();
	IPluginDescriptor descriptor = getPluginRegistry().getPluginDescriptor(id);
	if (descriptor == null)
		return null;
	try {
		return descriptor.getPlugin();
	} catch (CoreException e) {
		//failed to activate the plugin -- return null
		log(e.getStatus());
		return null;
	}
}
/**
 * @see Platform#getPluginRegistry
 */
public static IPluginRegistry getPluginRegistry() {
	assertInitialized();
	return registry;
}
/**
 * @see Platform#getPluginStateLocation
 */
public static IPath getPluginStateLocation(Plugin plugin, boolean create) {
	assertInitialized();
	IPath result = metaArea.getPluginStateLocation(plugin);
	if (create)
		result.toFile().mkdirs();
	return result;
}
/**
 * @see Platform
 */
public static String getProtectionSpace(URL resourceUrl) {
	return keyring.getProtectionSpace(resourceUrl);
}
public static Plugin getRuntimePlugin() {
	try {
		return getPluginRegistry().getPluginDescriptor(Platform.PI_RUNTIME).getPlugin();
	} catch (CoreException e) {
		//impossible for the runtime plugin to be missing
		log(e.getStatus());
		return null;
	}
}
private static void handleException(ISafeRunnable code, Throwable e) {
	if (!(e instanceof OperationCanceledException)) {
		// try to figure out which plugin caused the problem.  Derive this from the class
		// of the code arg.  Attribute to the Runtime plugin if we can't figure it out.
		Plugin plugin = getRuntimePlugin();
		try {
			plugin = ((PluginClassLoader)code.getClass().getClassLoader()).getPluginDescriptor().getPlugin();
		} catch (ClassCastException e1) {
			//ignore and attribute exception to runtime
		} catch (CoreException e1) {
			//ignore and attribute exception to runtime
		}
		String pluginId =  plugin.getDescriptor().getUniqueIdentifier();
		String message = Policy.bind("meta.pluginProblems", pluginId);
		IStatus status;
		if (e instanceof CoreException) {
			status = new MultiStatus(pluginId, Platform.PLUGIN_ERROR, message, e);
			((MultiStatus)status).merge(((CoreException)e).getStatus());
		} else {
			status = new Status(Status.ERROR, pluginId, Platform.PLUGIN_ERROR, message, e);
		}
		plugin.getLog().log(status);
	}
	code.handleException(e);
}
/**
 * Internal method for finding and returning a runnable instance of the 
 * given class as defined in the specified plug-in.
 * The returned object is initialized with the supplied arguments.
 * <p>
 * This method is used by the platform boot loader; is must
 * not be called directly by client code.
 * </p>
 * @see BootLoader
 */
public static IPlatformRunnable loaderGetRunnable(String applicationName) {
	assertInitialized();
	IExtension extension = registry.getExtension(Platform.PI_RUNTIME, Platform.PT_APPLICATIONS, applicationName);
	if (extension == null)
		return null;
	IConfigurationElement[] configs = extension.getConfigurationElements();
	if (configs.length == 0)
		return null;
	try {
		IConfigurationElement config = configs[0];
		return (IPlatformRunnable) config.createExecutableExtension("run");
	} catch (CoreException e) {
		getRuntimePlugin().getLog().log(e.getStatus());
		return null;
	}
}
/**
 * Internal method for finding and returning a runnable instance of the 
 * given class as defined in the specified plug-in.
 * The returned object is initialized with the supplied arguments.
 * <p>
 * This method is used by the platform boot loader; is must
 * not be called directly by client code.
 * </p>
 * @see BootLoader
 */
public static IPlatformRunnable loaderGetRunnable(String pluginId, String className, Object args) {
	assertInitialized();
	PluginDescriptor descriptor = (PluginDescriptor) registry.getPluginDescriptor(pluginId);
	try {
		return (IPlatformRunnable) descriptor.createExecutableExtension(className, args, null, null);
	} catch (CoreException e) {
		getRuntimePlugin().getLog().log(e.getStatus());
		return null;
	}
}
/**
 * Internal method for shutting down the platform.  All active plug-ins 
 * are notified of the impending shutdown. 
 * The exact order of notification is unspecified;
 * however, each plug-in is assured that it will be told to shut down
 * before any of its prerequisites.  Plug-ins are expected to free any
 * shared resources they manage.  Plug-ins should not store state at
 * this time; a separate <b>save</b> lifecycle event preceding the
 * shutdown notice tells plug-ins the right time to be saving their state.
 * <p>
 * On exit, the platform will no longer be initialized and any objects derived from
 * or based on the running platform are invalidated.
 * </p>
 * <p>
 * This method is used by the platform boot loader; is must
 * not be called directly by client code.
 * </p>
 * @see BootLoader
 */
public static void loaderShutdown() {
	assertInitialized();
	registry.shutdown(null);
	clearLockFile();
	if (DEBUG_PLUGINS && DEBUG_PLUGINS_DUMP.length() != 0) {
		// We are debugging so output the registry in XML
		// format.
		registry.debugRegistry(DEBUG_PLUGINS_DUMP);
	}
	
	if (cacheRegistry) {
		// Write the registry in cache format
		try {
			registry.saveRegistry();
		} catch (IOException e) {
			String message = Policy.bind("meta.unableToWriteRegistry");
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, e);
			getRuntimePlugin().getLog().log(status);
			if (DEBUG)
				System.out.println(status.getMessage());
		}
	} else {
		// get rid of the cache file if it exists
		registry.flushRegistry();
	}
	if (platformLog != null)
		platformLog.shutdown();
	initialized = false;
}
/**
 * Internal method for starting up the platform.  The platform is started at the 
 * given location.  The plug-ins found at the supplied 
 * collection of plug-in locations are loaded into the newly started platform.
 * <p>
 * This method is used by the platform boot loader; is must
 * not be called directly by client code.
 * </p>
 * @param pluginPath the list of places to look for plug-in specifications.  This may
 *		identify individual plug-in files or directories containing directories which contain
 *		plug-in files.
 * @param location the local filesystem location at which the newly started platform
 *		should be started.  If the location does not contain the saved state of a platform,
 *		the appropriate structures are created on disk (if required).
 * @param bootOptions the debug options loaded by the boot loader.  If the argument
 *		is <code>null</code> then debugging enablement was not requested by the
 *		person starting the platform.
 * @see BootLoader
 */
public static void loaderStartup(URL[] pluginPath, String locationString, Properties bootOptions, String[] args, Runnable handler) throws CoreException {
	endOfInitializationHandler = handler;
	processCommandLine(args);
	setupMetaArea(locationString);
	createLockFile();
	adapterManager = new AdapterManager();
	loadOptions(bootOptions);
	createXMLClassLoader();
	MultiStatus problems = loadRegistry(pluginPath);
	initialized = true;
	// can't register url handlers until after the plugin registry is loaded
	PlatformURLPluginHandlerFactory.startup();
	activateDefaultPlugins();
	if (DEBUG_CONTEXT)
		System.out.println("OS: " + BootLoader.getOS() + " WS: " + BootLoader.getWS() + 
			" NL: " + BootLoader.getNL() + " ARCH: " + BootLoader.getOSArch());
	// can't install the log or log problems until after the platform has been initialized.
	platformLog = new PlatformLogWriter(metaArea.getLogLocation().toFile());
	addLogListener(platformLog);
	if (consoleLogEnabled) {
		consoleLog = new PlatformLogWriter(System.out);
		addLogListener(consoleLog);
	}
	if (!problems.isOK())
		getRuntimePlugin().getLog().log(problems);
	loadKeyring();
}
/**
 * Opens the password database (if any) initally provided to the platform at startup.
 */
private static void loadKeyring() {
	if (keyringFile != null)
		try {
			keyring = new AuthorizationDatabase(keyringFile, password);
		} catch (CoreException e) {
			log(e.getStatus());
		}
	if (keyring == null)
		keyring = new AuthorizationDatabase();
}
static void loadOptions(Properties bootOptions) {
	// If the boot loader passed <code>null</code> for the boot options, the user
	// did not specify debug options so no debugging should be enabled.
	if (bootOptions == null) {
		debugEnabled = false;
		return;
	}
	debugEnabled = true;
	options = new Properties(bootOptions);
	try {
		InputStream input = new FileInputStream(InternalPlatform.getMetaArea().getOptionsLocation().toFile());
		try {
			options.load(input);
		} finally {
			input.close();
		}
	} catch (FileNotFoundException e) {
		//	Its not an error to not find the options file
	} catch (IOException e) {
		//	Platform.RuntimePlugin.getLog().log();
	}
		// trim off all the blanks since properties files don't do that.
	for (Iterator i = options.keySet().iterator(); i.hasNext();) {
		Object key = i.next();
		options.put(key, ((String) options.get(key)).trim());
	}
	DEBUG = getBooleanOption(OPTION_DEBUG, false);
	if (DEBUG) {
		DEBUG_CONTEXT = getBooleanOption(OPTION_DEBUG_SYSTEM_CONTEXT, false);
		DEBUG_STARTUP = getBooleanOption(OPTION_DEBUG_STARTUP, false);
		DEBUG_SHUTDOWN = getBooleanOption(OPTION_DEBUG_SHUTDOWN, false);
		DEBUG_PLUGINS = getBooleanOption(OPTION_DEBUG_PLUGINS, false);
		DEBUG_PLUGINS_DUMP = getDebugOption(OPTION_DEBUG_PLUGINS_DUMP);
		DEBUG_PREFERENCES = getBooleanOption(OPTION_DEBUG_PREFERENCES, false);
	}
	InternalBootLoader.setupOptions();
}
/**
 * Parses, resolves and rememberhs the plugin registry.  The multistatus returned
 * details any problems/issues encountered during this process.
 */
private static MultiStatus loadRegistry(URL[] pluginPath) {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("parse.registryProblems"), null);
	InternalFactory factory = new InternalFactory(problems);

	IPath path = getMetaArea().getRegistryPath();
	DataInputStream input = null;
	registry = null;
	// augment the plugin path with any additional platform entries
	// (eg. user scripts)
	URL[] augmentedPluginPath = getAugmentedPluginPath(pluginPath);
	if (path.toFile().exists() && cacheRegistry) {
		try {
			input = new DataInputStream(new BufferedInputStream(new FileInputStream(path.toFile())));
			try {
				long start = System.currentTimeMillis();
				RegistryCacheReader cacheReader = new RegistryCacheReader(factory);
				registry = (PluginRegistry)cacheReader.readPluginRegistry(input, augmentedPluginPath, DEBUG && DEBUG_PLUGINS);
				if (DEBUG)
					System.out.println("Read registry cache: " + (System.currentTimeMillis() - start) + "ms");
			} finally {
				input.close();
			}
		} catch (IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, Policy.bind("meta.unableToReadCache"), ioe);
			problems.merge(status);
		}
	}
	if (registry == null) {
		clearRegIndex();
		long start = System.currentTimeMillis();
		InternalPlatform.setRegistryCacheTimeStamp(BootLoader.getCurrentPlatformConfiguration().getPluginsChangeStamp());
		registry = (PluginRegistry) parsePlugins(augmentedPluginPath, factory, DEBUG && DEBUG_PLUGINS);
		IStatus resolveStatus = registry.resolve(true, true);
		problems.merge(resolveStatus);
		registry.markReadOnly();
		if (DEBUG)
			System.out.println("Parse and resolve registry: " + (System.currentTimeMillis() - start) + "ms");
	}
	registry.startup(null);
	return problems;
}
/**
 * Notifies all listeners of the platform log.  This includes the console log, if 
 * used, and the platform log file.  All Plugin log messages get funnelled
 * through here as well.
 */
public static void log(final IStatus status) {
	assertInitialized();
	// create array to avoid concurrent access
	ILogListener[] listeners;
	synchronized (logListeners) {
		listeners = (ILogListener[]) logListeners.toArray(new ILogListener[logListeners.size()]);
	}
	for (int i = 0; i < listeners.length; i++) {
		try {
			listeners[i].logging(status, Platform.PI_RUNTIME);
		} catch (Exception e) {
		} // no chance of exceptions for log listeners
	}
}
/**
 * @see Platform#parsePlugins
 */
public static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory) {
	return parsePlugins(pluginPath, factory, false);
}
/**
 * @see Platform#parsePlugins
 */
public synchronized static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory, boolean debug) {
	// If the platform is not running then simply parse the registry.  We don't need to play
	// any funny class loader games as we assume the XML classes are on the class path
	// This happens when we are running this code as part of a utility (as opposed to starting
	// or inside the platform).
	if (!(InternalBootLoader.isRunning() || InternalBootLoader.isStarting()))
		return RegistryLoader.parseRegistry(pluginPath, factory, debug);

	// If we are running the platform, we want to conserve class loaders.  
	// Temporarily install the xml class loader as a prerequisite of the platform class loader
	// This allows us to find the xml classes.  Be sure to reset the prerequisites after loading.
	PlatformClassLoader.getDefault().setImports(new DelegatingURLClassLoader[] { xmlClassLoader });
	try {
		return RegistryLoader.parseRegistry(pluginPath, factory, debug);
	} finally {
		PlatformClassLoader.getDefault().setImports(null);
	}
}
private static String[] processCommandLine(String[] args) {
	int[] configArgs = new int[100];
	configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
	int configArgIndex = 0;
	for (int i = 0; i < args.length; i++) {
		boolean found = false;
		// check for args without parameters (i.e., a flag arg)

		// look for the log flag
		if (args[i].equalsIgnoreCase(LOG)) {
			consoleLogEnabled = true;
			found = true;
		}

		// look for the no registry cache flag
		if (args[i].equalsIgnoreCase(NOREGISTRYCACHE)) {
			cacheRegistry = false;
			found = true;
		}

		// done checking for args.  Remember where an arg was found 
		if (found) {
			configArgs[configArgIndex++] = i;
			continue;
		}
		// check for args with parameters
		if (i == args.length - 1 || args[i + 1].startsWith("-")) 
			continue;
		String arg = args[++i];

		// look for the keyring file
		if (args[i - 1].equalsIgnoreCase(KEYRING)) {
			keyringFile = arg;
			found = true;
		}

		// look for the user password.  
		if (args[i - 1].equalsIgnoreCase(PASSWORD)) {
			password = arg;
			found = true;
		}

		// look for the plug-in customization file
		if (args[i - 1].equalsIgnoreCase(PLUGIN_CUSTOMIZATION)) {
			pluginCustomizationFile = arg;
			found = true;
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
/**
 * @see Platform#removeLogListener
 */
public static void removeLogListener(ILogListener listener) {
	assertInitialized();
	synchronized (logListeners) {
		logListeners.remove(listener);
	}
}
/**
 * @see Platform
 */
public static URL resolve(URL url) throws IOException {
	if (!url.getProtocol().equals(PlatformURLHandler.PROTOCOL))
		return url;
	URLConnection connection = url.openConnection();
	if (connection instanceof PlatformURLConnection)
		return ((PlatformURLConnection) connection).getResolvedURL();
	else
		return url;
}
public static void run(ISafeRunnable code) {
	Assert.isNotNull(code);
	try {
		code.run();
	} catch (Exception e) {
		handleException(code, e);
	} catch (LinkageError e) {
		handleException(code, e);
	}
}
private static void run(Runnable handler) {
	// run end-of-initialization handler
	if (handler == null)
		return; 
		
	final Runnable finalHandler = handler;
	ISafeRunnable code = new ISafeRunnable() {
		public void run() throws Exception {
			finalHandler.run();
		}
		public void handleException(Throwable e) {
			// just continue ... the exception has already been logged by
			// the platform (see handleException(ISafeRunnable)
		}
	};
	Platform.run(code);
}
public static void setDebugOption(String option, String value) {
	if (debugEnabled)
		options.setProperty(option, value);
}
/**
 * Sets the plug-in registry for the platform to the given value.
 * This method should only be called by the registry loader
 */
public static void setPluginRegistry(IPluginRegistry value) {
	registry = (PluginRegistry) value;
}
private static void setupMetaArea(String locationString) throws CoreException {
	location = new Path(locationString);
	if (!location.isAbsolute())
		location = new Path(System.getProperty("user.dir")).append(location);
	// must create the meta area first as it defines all the other locations.
	if (location.toFile().exists()) {
		if (!location.toFile().isDirectory()) {
			String message = Policy.bind("meta.notDir", location.toString());
			throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.FAILED_WRITE_METADATA, message, null));
		}
	}
	metaArea = new PlatformMetaArea(location);
	metaArea.createLocation();
	if (keyringFile == null)
		keyringFile = metaArea.getLocation().append(PlatformMetaArea.F_KEYRING).toOSString();
}
public static void addLastModifiedTime (String pathKey, long lastModTime) {
	if (regIndex == null)
		regIndex = new HashMap(30);
	regIndex.put(pathKey, new Long(lastModTime));
}
public static Map getRegIndex() {
	return regIndex;
}
public static void clearRegIndex() {
	regIndex = null;
}

/**
 * Applies primary feature-specific overrides to default preferences for the
 * plug-in with the given id.
 * <p>
 * Note that by the time this method is called, the default settings
 * for the plug-in itself should have already have been filled in.
 * </p>
 * 
 * @param id the unique identifier of the plug-in
 * @param preferences the preference store for the specified plug-in
 * 
 * @since 2.0
 */
public static void applyPrimaryFeaturePluginDefaultOverrides(
	String id,
	Preferences preferences) {

	// carefully navigate to the plug-in for the primary feature
	IPlatformConfiguration cfg = BootLoader.getCurrentPlatformConfiguration();
	if (cfg == null) {
		// bail if we don't seem to have one for whatever reason (!)
		if (DEBUG_PREFERENCES) {
			System.out.println("Plugin preferences unable to find a platform configuration");
		}
		return;
	}
	String primaryFeaturePluginId = cfg.getPrimaryFeatureIdentifier();
	if (primaryFeaturePluginId  == null) {
		// bail if we don't seem to have one of these (!)
		if (DEBUG_PREFERENCES) {
			System.out.println("Plugin preferences unable to find a primary feature plugin id");
		}
		return;
	}
	IPluginDescriptor primaryFeatureDescriptor = getPluginRegistry().getPluginDescriptor(primaryFeaturePluginId);
	if (primaryFeatureDescriptor  == null) {
		// bail if primary feature is missing (!)
		if (DEBUG_PREFERENCES) {
			System.out.println("Plugin preferences unable to find a primary feature");
		}
		return;
	}
	// locate plug-in customization file within primary feature plug-in (or fragment)
	URL pluginCustomizationURL = primaryFeatureDescriptor.find(new Path(PLUGIN_CUSTOMIZATION_FILE_NAME));
	if (pluginCustomizationURL == null) {
		if (DEBUG_PREFERENCES) {
			System.out.println("Preferences file " + PLUGIN_CUSTOMIZATION_FILE_NAME + " not found.");
		}
		return;
	}
	if (DEBUG_PREFERENCES) {
		System.out.println("Loading preferences from " + pluginCustomizationURL);
	}
	// apply any defaults for the given plug-in
	applyPluginDefaultOverrides(pluginCustomizationURL, id, preferences);
}

/**
 * Applies command line-supplied overrides to default preferences for the
 * plug-in with the given id.
 * <p>
 * Note that by the time this method is called, the default settings
 * for the plug-in itself should have already have been filled in, along
 * with any default overrides supplied by the primary feature.
 * </p>
 * 
 * @param id the unique identifier of the plug-in
 * @param preferences the preference store for the specified plug-in
 * 
 * @since 2.0
 */
public static void applyCommandLinePluginDefaultOverrides(
	String id,
	Preferences preferences) {
	
	if (pluginCustomizationFile == null) {
		// no command line overrides to process
		if (DEBUG_PREFERENCES) {
			System.out.println("Command line argument -pluginCustomization not used.");
		}
		return;
	}

	try {
		URL pluginCustomizationURL = new File(pluginCustomizationFile).toURL();
		if (DEBUG_PREFERENCES) {
			System.out.println("Loading preferences from " + pluginCustomizationURL);
		}
		applyPluginDefaultOverrides(pluginCustomizationURL, id, preferences);
	} catch (MalformedURLException e) {
		// fail silently
		if (DEBUG_PREFERENCES) {
			System.out.println("MalformedURLException creating URL for plugin customization file "
				+ pluginCustomizationFile);
			e.printStackTrace();
		}
		return;
	}
}

/**
 * Applies overrides to default preferences for the plug-in with the given id.
 * The data is contained in the <code>java.io.Properties</code> style file at 
 * the given URL. The property names consist of "/'-separated plug-in id and
 * name of preference; e.g., "com.example.myplugin/mypref".
 * 
 * @param propertiesURL the URL of a <code>java.io.Properties</code> style file
 * @param id the unique identifier of the plug-in
 * @param preferences the preference store for the specified plug-in
 * 
 * @since 2.0
 */
private static void applyPluginDefaultOverrides(
	URL propertiesURL,
	String id,
	Preferences preferences) {
	
	// read the java.io.Properties file at the given URL
	Properties overrides = new Properties();
	SafeFileInputStream in = null;
		
	try {
		File inFile = new File(propertiesURL.getFile());
		if (!inFile.exists()) {
			// We don't have a preferences file to worry about
			if (DEBUG_PREFERENCES) {
				System.out.println("Preference file " +
				propertiesURL + " not found.");
			}
			return;
		}
			
		in = new SafeFileInputStream(inFile);
		if (in == null) {
			// fail quietly
			if (DEBUG_PREFERENCES) {
				System.out.println("Failed to open " +
					propertiesURL);
			}
			return;
		}
		overrides.load(in);
	} catch (IOException e) {
		// cannot read ini file - fail silently
		if (DEBUG_PREFERENCES) {
			System.out.println("IOException reading preference file " +
			propertiesURL);
			e.printStackTrace();
		}
		return;
	} finally {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			// ignore problems closing file
			if (DEBUG_PREFERENCES) {
				System.out.println("IOException closing preference file " +
				propertiesURL);
				e.printStackTrace();
			}
		}
	}

	for (Iterator it = overrides.entrySet().iterator(); it.hasNext();) {
		Map.Entry entry = (Map.Entry) it.next();
		String qualifiedKey = (String) entry.getKey();
		// Keys consist of "/'-separated plug-in id and name of preference
		// e.g., "com.example.myplugin/mypref"
		int s = qualifiedKey.indexOf('/');
		if (s < 0 || s == 0 || s == qualifiedKey.length() - 1) {
			// skip mangled entry
			continue;
		}
		// plug-in id is non-empty string before "/" 
		String pluginId = qualifiedKey.substring(0, s);
		if (pluginId.equals(id)) {
			// override property in the given plug-in
			// plig-in-specified property name is non-empty string after "/" 
			String propertyName = qualifiedKey.substring(s + 1);
			String value = (String) entry.getValue();
			preferences.setDefault(propertyName, value);
		}
	}
	if (DEBUG_PREFERENCES) {
		System.out.println("Preferences now set as follows:");
		String[] prefNames = preferences.propertyNames();
		for (int i = 0; i < prefNames.length; i++) {
			String value = preferences.getString(prefNames[i]);
			System.out.println("\t" + prefNames[i] + " = " + value);
		}
		prefNames = preferences.defaultPropertyNames();
		for (int i = 0; i < prefNames.length; i++) {
			String value = preferences.getDefaultString(prefNames[i]);
			System.out.println("\tDefault values: " + prefNames[i] + " = " + value);
		}
	}
}
public static long getRegistryCacheTimeStamp() {
	return cacheReadTimeStamp;
}
public static void setRegistryCacheTimeStamp(long timeStamp) {
	cacheReadTimeStamp = timeStamp;
}
}