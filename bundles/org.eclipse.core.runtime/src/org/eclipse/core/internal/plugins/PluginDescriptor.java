package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.boot.DelegatingURLClassLoader;import org.eclipse.core.internal.boot.PlatformClassLoader;import org.eclipse.core.internal.boot.PlatformURLBaseConnection;import org.eclipse.core.internal.boot.PlatformURLHandler;import org.eclipse.core.internal.boot.URLContentFilter;import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;

public class PluginDescriptor extends PluginDescriptorModel implements IPluginDescriptor {

	private DelegatingURLClassLoader loader = null; // plugin loader
	private boolean active = false; // plugin is active
	private boolean activePending = false; // being activated
	private boolean deactivated = false; // plugin deactivated due to startup errors
	protected Plugin pluginObject = null; // plugin object
	private boolean useDevURLs = InternalPlatform.inVAJ() || InternalPlatform.inVAME();
	private boolean usePlatformURLs = true;
	private ResourceBundle bundle = null; // plugin.properties
	private Locale locale = null; // bundle locale
	private boolean bundleNotFound = false; // marker to prevent unnecessary lookups

	// constants
	static final String PLUGIN_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLPluginConnection.PLUGIN + "/";
	static final String VERSION_SEPARATOR = "_";

	private static final String DEFAULT_BUNDLE_NAME = "plugin";
	private static final String KEY_PREFIX = "%";
	private static final String KEY_DOUBLE_PREFIX = "%%";

	private static final String URL_PROTOCOL_FILE = "file";

	// Development mode constants
	private static final String PLUGIN_JARS = "plugin.jars";
	private static final String VA_PROPERTIES = ".va.properties";
	private static final String KEY_PROJECT = "projects";
	
	// Places to look for library files 
	private static String[] WS_JAR_VARIANTS = buildWSVariants();
	private static String[] OS_JAR_VARIANTS = buildOSVariants();
	private static String[] NL_JAR_VARIANTS = buildNLVariants();
	private static String[] JAR_VARIANTS = buildVanillaVariants();

public PluginDescriptor() {
	super();
}
private static String[] buildWSVariants() {
	ArrayList result = new ArrayList();
	result.add("ws/" + BootLoader.getWS());
	result.add("");
	return (String[])result.toArray(new String[result.size()]);
}
private static String[] buildOSVariants() {
	ArrayList result = new ArrayList();
	result.add("os/" + BootLoader.getOS() + "/" + BootLoader.getOSArch());
	result.add("os/" + BootLoader.getOS());
	result.add("");
	return (String[])result.toArray(new String[result.size()]);
}
private static String[] buildNLVariants() {
	ArrayList result = new ArrayList();
	String nl = BootLoader.getNL();
	nl = nl.replace('_', '/');
	while (nl.length() > 0) {
		result.add("nl/" + nl);
		int i = nl.lastIndexOf('/');
		nl = (i < 0) ? "" : nl.substring(0, i);
	}
	result.add("");
	return (String[])result.toArray(new String[result.size()]);
}
private static String[] buildVanillaVariants() {
	ArrayList result = new ArrayList();
	result.add("");
	return (String[])result.toArray(new String[result.size()]);
}
private String[] buildBasePaths(String pluginBase) {
	// Now build a list of all the bases to use
	ArrayList result = new ArrayList();
	result.add(pluginBase);
	PluginFragmentModel[] fragments = getFragments();
	int fragmentLength = (fragments == null) ? 0 : fragments.length;
	for (int i = 0; i < fragmentLength; i++) {
		FragmentDescriptor fragment = (FragmentDescriptor)fragments[i];
		result.add(fragment.getInstallURL().toString());
	}
	return (String[])result.toArray(new String[result.size()]);
}
/**
 * concatenates start and end.  If end has a '.' construct at the beginning
 * trim off any leading '.' constructs.  Since the libSpec was a path, we
 * know that it was canonicalized and will only have at most one set
 * of '.' constructs at the beginning.  Returns <code>null</code> if the 
 * end is null or starts with '..'.
 */
private String concat(String start, String end) {
	if (end == null)
		return null;
	if (end.startsWith(".."))
		// ISSUE: should log an error here
		// error case.  Can't '..' out of the scope of a plugin.  Signal that this
		// should be ignored (return null).
		return null;
	if (end.startsWith("./"))
		return start + (end.substring(2));
	if (end.startsWith("."))
		return start + end.substring(1);
	return start + end;
}
public Object createExecutableExtension(String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
	// load the requested class from this plugin
	Class classInstance = null;
	try {
		classInstance = getPluginClassLoader(true).loadClass(className);
	} catch (ClassNotFoundException e1) {
		throwException(Policy.bind("plugin.loadClassError", getId(), className), e1);
	}

	// create a new instance
	Object result = null;
	try {
		result = classInstance.newInstance();
	} catch (Exception e) {
		throwException(Policy.bind("plugin.instantiateClassError", getId(), className), e);
	}

	// check if we have extension adapter and initialize
	if (result instanceof IExecutableExtension) {
		try {
			// make the call even if the initialization string is null
			 ((IExecutableExtension) result).setInitializationData(cfig, propertyName, initData);
		} catch (CoreException ce) {
			// user code threw exception
			logError(ce.getStatus());
			throw new CoreException(ce.getStatus());
		} catch (Exception te) {
			// user code caused exception
			throwException(Policy.bind("policy.initObjectError", getId(), className), te);
		}
	}
	return result;
}
Object createExecutableExtension(String pluginName, String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
	String id = getUniqueIdentifier(); // this plugin id
	// check if we need to delegate to some other plugin
	if (pluginName != null && !pluginName.equals("") && !pluginName.equals(id)) {
		PluginDescriptor plugin = null;
		plugin = (PluginDescriptor) getPluginRegistry().getPluginDescriptor(pluginName);
		return plugin.createExecutableExtension(className, initData, cfig, propertyName);
	}
	return createExecutableExtension(className, initData, cfig, propertyName);
}
synchronized void doPluginActivation() throws CoreException {
	// this method is called by the class loader just prior 
	// to getting a class. It needs to handle the
	// case where it is called multiple times during the activation
	// processing itself (as a result of other classes from this
	// plugin being directly referenced by the plugin class)

	// NOTE: there is a remote scenario where the plugin class can
	// deadlock, if it starts separate thread(s) within its
	// constructor or startup() method, and waits on those
	// threads before returning (ie. calls join()).

	boolean errorExit = true;

	// check if already activated or pending
	if (pluginActivationEnter()) {
		try {
			internalDoPluginActivation();
			errorExit = false;
		} finally {
			pluginActivationExit(errorExit);
		}
	}
}
synchronized void doPluginDeactivation() {
	loader = null;
	pluginObject = null;
	active = false;
	activePending = false;
	deactivated = false;
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
/**
 * @see IPluginDescriptor
 */
public IExtension getExtension(String id) {
	if (id == null)
		return null;
	ExtensionModel[] list = getDeclaredExtensions();
	if (list == null)
		return null;
	for (int i = 0; i < list.length; i++) {
		if (id.equals(list[i].getId()))
			return (IExtension) list[i];
	}
	return null;
}
/**
 * @see IPluginDescriptor
 */
public IExtensionPoint getExtensionPoint(String extensionPointId) {
	if (extensionPointId == null)
		return null;
	ExtensionPointModel[] list = getDeclaredExtensionPoints();
	if (list == null)
		return null;
	for (int i = 0; i < list.length; i++) {
		if (extensionPointId.equals(list[i].getId()))
			return (IExtensionPoint) list[i];
	}
	return null;
}
/**
 * @see IPluginDescriptor
 */
public IExtensionPoint[] getExtensionPoints() {
	ExtensionPointModel[] list = getDeclaredExtensionPoints();
	if (list == null)
		return new IExtensionPoint[0];
	IExtensionPoint[] newValues = new IExtensionPoint[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
/**
 * @see IPluginDescriptor
 */
public IExtension[] getExtensions() {
	ExtensionModel[] list = getDeclaredExtensions();
	if (list == null)
		return new IExtension[0];
	IExtension[] newValues = new IExtension[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
/**
 * @see IPluginDescriptor
 */
public URL getInstallURL() {
	try {
		return new URL(PLUGIN_URL + toString() + "/");
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
public URL getInstallURLInternal() {
	String url = getLocation();
	try {
		return new URL(url);
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
/**
 * @see IPluginDescriptor
 */
public String getLabel() {
	String s = getName();
	return s == null ? "" : getResourceString(s);
}
/**
 * @see IPluginDescriptor
 */
public Plugin getPlugin() throws CoreException {
	if (pluginObject == null)
		doPluginActivation();
	return pluginObject;
}
/**
 * @see IPluginDescriptor
 */
public ClassLoader getPluginClassLoader() {
	return getPluginClassLoader(true);
}
public ClassLoader getPluginClassLoader(boolean eclipseURLs) {
	if (loader != null)
		return loader;

	Object[] path = getPluginClassLoaderPath(eclipseURLs);
	URL[] codePath = (URL[]) path[0];
	URLContentFilter[] codeFilters = (URLContentFilter[]) path[1];
	URL[] resourcePath = (URL[]) path[2];
	URLContentFilter[] resourceFilters = (URLContentFilter[]) path[3];
	// Create the classloader.  The parent should be the parent of the platform class loader.  
	// This allows us to decouple standard parent loading from platform loading.
	loader = new PluginClassLoader(codePath, codeFilters, resourcePath, resourceFilters, PlatformClassLoader.getDefault().getParent(), this);
	loader.initializeImportedLoaders();
	// Note: need to be able to give out a loader reference before
	// its prereqs are initialized. Otherwise loops in prereq
	// definition will cause endless loop in initializePrereqs()
	return loader;
}
private Object[] getPluginClassLoaderPath(boolean platformURLFlag) {
	// If running in development mode, check for a plugin.jars file. 
	// The file has entries corresponding to each of the <library>
	// elements in the plugin.xml. Each defined property is of form
	//    libname=source1,source2,source3,...
	// For example
	// plugin.xml
	//   <runtime>
	//     <library name=baseapi.jar>
	//       <export name="*"/>
	//     </library>
	//     <library name=another.jar/>
	//     <library name=base.jar/>
	//   </runtime>
	//
	// plugin.jars
	//    baseapi.jar=Base API Project
	//    base.jar=Base Project 1, Base Project 2
	//    another.jar=More Code
	//
	// The above results in a loader search path consisting of
	//    Base API Project
	//    another.jar
	//    Base Project 1, Base Project 2
	//
	// Any library access filters specified in the plugin.xml are
	// applied to the corresponding loader search path entries

	Properties jarDefinitions = loadJarDefinitions();
	// compute the base of the classpath urls.  If <code>eclipseURLs</code> is
	// true, we should use eclipse: URLs.  Otherwise the native URLs are used.
	usePlatformURLs = platformURLFlag;
	URL install = usePlatformURLs ? getInstallURL() : getInstallURLInternal();
	String execBase = install.toExternalForm();
	String devBase = null;
	// useDevURLs should always be false now as 
	//		InternalPlatform.inVAJ() || InternalPlatform.inVAME()
	// should never be true anymore
	//if (useDevURLs)
		//devBase = PlatformURLBaseConnection.PLATFORM_URL_STRING;
	//else
		devBase = execBase;
	
	String[] basePaths = buildBasePaths(devBase);

	String[] exportAll = new String[] { "*" };
	ArrayList[] result = new ArrayList[4];
	result[0] = new ArrayList();
	result[1] = new ArrayList();
	result[2] = new ArrayList();
	result[3] = new ArrayList();

	// add in any development mode class paths and the export all filter
	if (DelegatingURLClassLoader.devClassPath != null) {
		String[] specs = getArrayFromList(DelegatingURLClassLoader.devClassPath);
		// convert dev class path into url strings
		for (int j = 0; j < specs.length; j++) {
			String spec = specs[j];
			char lastChar = spec.charAt(spec.length() - 1);
			// if the spec is not a jar and does not have a trailing slash, add one
			if (!(spec.endsWith(".jar") || (lastChar == '/' || lastChar == '\\')))
				spec = spec + "/";
			// add the dev path for the plugin itself
			addLibraryWithFragments(basePaths, JAR_VARIANTS, spec, exportAll, ILibrary.CODE, false, result);
		}
	}

	// add in the class path entries spec'd in the plugin.xml.  If in development mode, 
	// add the entries from the plugin.jars first.  
	ILibrary[] list = getRuntimeLibraries();
	for (int i = 0; i < list.length; i++) {
		ILibrary library = list[i];
		// if the library path is empty, skip it.
		if (library.getPath().isEmpty())
			continue;
		String[] filters = library.isFullyExported() ? exportAll : library.getContentFilters();
		// add in the plugin.jars entries
		String libSpec = library.getPath().toString();
		String jarDefinition = null;
		if (jarDefinitions != null && libSpec != null) {
			jarDefinition = jarDefinitions.getProperty(libSpec);
			String[] specs = getArrayFromList(jarDefinition);
			// convert jar spec into url strings
			for (int j = 0; j < specs.length; j++)
				resolveAndAddLibrary(specs[j] + "/", filters, basePaths, library.getType(), true, result);
		}
		resolveAndAddLibrary(libSpec, filters, basePaths, library.getType(), jarDefinition != null, result);
	}

	Object[] array = new Object[4];
	array[0] = result[0].toArray(new URL[result[0].size()]);
	array[1] = result[1].toArray(new URLContentFilter[result[1].size()]);
	array[2] = result[2].toArray(new URL[result[2].size()]);
	array[3] = result[3].toArray(new URLContentFilter[result[3].size()]);
	return array;
}

private boolean resolveAndAddLibrary(String spec, String[] filters, String[] basePaths, String type, boolean hasJarSpec, ArrayList[] result) {
	if (spec.charAt(0) == '$') {
		IPath path = new Path(spec);
		String first = path.segment(0);
		String remainder = path.removeFirstSegments(1).toString();
		if (first.equalsIgnoreCase("$ws$"))
			return addLibraryWithFragments(basePaths, WS_JAR_VARIANTS, "/" + remainder, filters, type, hasJarSpec, result);
		if (first.equalsIgnoreCase("$os$"))
			return addLibraryWithFragments(basePaths, OS_JAR_VARIANTS, "/" + remainder, filters, type, hasJarSpec, result);
		if (first.equalsIgnoreCase("$nl$"))
			return addLibraryWithFragments(basePaths, NL_JAR_VARIANTS, "/" + remainder, filters, type, hasJarSpec, result);
	}
	return addLibraryWithFragments(basePaths, JAR_VARIANTS, spec, filters, type, hasJarSpec, result);
}

private boolean addLibraryWithFragments(String[] basePaths, String[] variants, String spec, String[] filters, String type, boolean hasJarSpec, ArrayList[] result) {
	boolean added = false;
	for (int j = 0; j < variants.length && !added; j++) {
		for (int i = 0; i < basePaths.length && !added; i++) {
			added = addLibrary(basePaths[i], spec, variants[j], filters, type, hasJarSpec, result);
		}
	}
	return added;
}

private boolean addLibrary(String base, String libSpec, String variant, String[] filters, String type, boolean hasJarSpec, ArrayList[] result) {
	// create path entries for all libraries except those which are files
	// and do not exist.
	String spec = null;
	// Make sure you get only one separator between each segment
	if ((variant.length() == 0) && (libSpec.startsWith("/")) &&
	     (base.endsWith("/"))) {
		spec = concat(base, libSpec.substring(1));
	} else {
		spec = concat(concat(base, variant), libSpec);
	}
	// spec is now something of the form:
	// <base><variant><libSpec>
	if (spec == null)
		return false;

	// if the libspec is NOT considered a directory, treat as a jar
	if (!spec.endsWith("/")) {
		// if running in VAJ or VAME and there was a plugin.jars definition, ignore the plugin.xml
		// library entry (assume the plugin.jars entries covered all the bases.  Otherwise, 
		// convert the plugin.xml entry into a URL.
		if ((InternalPlatform.inVAJ() || InternalPlatform.inVAME()) && hasJarSpec)
			return false;
		if (spec.startsWith(PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR))
			spec += PlatformURLHandler.JAR_SEPARATOR;
		else
			spec = PlatformURLHandler.JAR + PlatformURLHandler.PROTOCOL_SEPARATOR + spec + PlatformURLHandler.JAR_SEPARATOR;
	}
	try {
		URL entry = new URL(spec);
		URL resolved = Platform.resolve(entry);
		boolean add = true;
		String file = getFileFromURL (resolved);
		if (file != null)
			add = new File(file).exists();
		if (add) {
			if (type.equals(ILibrary.CODE)) {
				result[0].add(resolved);
				result[1].add(new URLContentFilter(filters));
			} else
				if (type.equals(ILibrary.RESOURCE)) {
					result[2].add(resolved);
					result[3].add(new URLContentFilter(filters));
				}
			return true;
		}
	} catch (IOException e) {
		// skip bad URLs
	}
	return false;
}

public String getFileFromURL(URL target) {
	String protocol = target.getProtocol();
	if (protocol.equals(PlatformURLHandler.FILE))
		return target.getFile();
//	if (protocol.equals(PlatformURLHandler.VA))
//		return target.getFile();
	if (protocol.equals(PlatformURLHandler.JAR)) {
		// strip off the jar separator at the end of the url then do a recursive call
		// to interpret the sub URL.
		String file = target.getFile();
		file = file.substring(0, file.length() - PlatformURLHandler.JAR_SEPARATOR.length());
		try {
			return getFileFromURL(new URL(file));
		} catch (MalformedURLException e) {
		}
	}
	return null;
}

/**
 * @see IPluginDescriptor
 */
public IPluginPrerequisite[] getPluginPrerequisites() {
	PluginPrerequisiteModel[] list = getRequires();
	if (list == null)
		return new IPluginPrerequisite[0];
	IPluginPrerequisite[] newValues = new IPluginPrerequisite[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
public PluginRegistry getPluginRegistry() {
	return (PluginRegistry) getRegistry();
}
/**
 * @see IPluginDescriptor
 */
public String getProviderName() {
	String s = super.getProviderName();
	return s == null ? "" : getResourceString(s);
}
/**
 * @see IPluginDescriptor
 */
public ResourceBundle getResourceBundle() throws MissingResourceException {
	return getResourceBundle(Locale.getDefault());
}
public ResourceBundle getResourceBundle(Locale targetLocale) throws MissingResourceException {
	// we cache the bundle for a single locale 
	if (bundle != null && targetLocale.equals(locale))
		return bundle;

	// check if we already tried and failed
	if (bundleNotFound)
		throw new MissingResourceException(Policy.bind("plugin.bundleNotFound", getId(), DEFAULT_BUNDLE_NAME + "_" + targetLocale), DEFAULT_BUNDLE_NAME + "_" + targetLocale, "");

	// try to load bundle from this plugin. A new loader is created to include the base 
	// install directory on the search path (to maintain compatibility with current handling
	// of plugin.properties bundles (not in code jar)).
	URL[] cp = ((URLClassLoader)getPluginClassLoader()).getURLs();
	URL[] newcp = new URL[cp.length+1];
	for (int i=0; i<cp.length; i++) newcp[i+1] = cp[i];
	try {
		newcp[0] = Platform.resolve(getInstallURL()); // always try to resolve URLs used in loaders
	} catch(IOException e) {
		newcp[0] = getInstallURL();
	}
	ClassLoader resourceLoader = new URLClassLoader(newcp, null);
	ResourceBundle newBundle = null;
	try {
		newBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, targetLocale, resourceLoader);
		bundle = newBundle;
		locale = targetLocale;
	} catch (MissingResourceException e) {
		bundleNotFound = true;
		throw e;
	}

	return newBundle;
}
/**
 * @see IPluginDescriptor
 */
public String getResourceString(String value) {
	return getResourceString(value, null);
}
/**
 * @see IPluginDescriptor
 */
public String getResourceString(String value, ResourceBundle b) {

	String s = value.trim();
	
	if (!s.startsWith(KEY_PREFIX)) return s;

	if (s.startsWith(KEY_DOUBLE_PREFIX)) return s.substring(1);

	int ix = s.indexOf(" ");
	String key = ix == -1 ? s : s.substring(0,ix);
	String dflt = ix == -1 ? s : s.substring(ix+1);

	if (b==null) {
		try { b = getResourceBundle(); }
		catch (MissingResourceException e) {};
	}
	
	if (b==null) return dflt;
	
	try { return b.getString(key.substring(1)); }
	catch(MissingResourceException e) { return dflt; }
}
/**
 * @see IPluginDescriptor
 */
public ILibrary[] getRuntimeLibraries() {
	LibraryModel[] list = getRuntime();
	if (list == null)
		return new ILibrary[0];
	ILibrary[] newValues = new ILibrary[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
/**
 * @see IPluginDescriptor
 */
public String getUniqueIdentifier() {
	return getId();
}
/**
 * @see #toString
 */
public static String getUniqueIdentifierFromString(String pluginString) {	
	int ix = pluginString.indexOf(VERSION_SEPARATOR);
	return ix==-1 ? pluginString : pluginString.substring(0,ix);
}
/**
 * @see IPluginDescriptor
 */
public PluginVersionIdentifier getVersionIdentifier() {
	String version = getVersion();
	if (version == null)
		return new PluginVersionIdentifier("1.0.0");
	try {
		return new PluginVersionIdentifier(version);
	} catch (Exception e) {
		return new PluginVersionIdentifier("1.0.0");
	}
}
/**
 * @see #toString
 */
public static PluginVersionIdentifier getVersionIdentifierFromString(String pluginString) {
	int ix = pluginString.indexOf("_");
	if (ix==-1) return null;
	String vid = pluginString.substring(ix+1);	
	try {
		return new PluginVersionIdentifier(vid);
	} catch (Exception e) {
		return null;
	}
}
private void internalDoPluginActivation() throws CoreException {
	String errorMsg;
	// load the runtime class 
	String pluginClassName = getPluginClass();
	Class runtimeClass = null;
	try {
		if (pluginClassName == null || pluginClassName.equals(""))
			runtimeClass = DefaultPlugin.class;
		else
			runtimeClass = getPluginClassLoader(true).loadClass(pluginClassName);
	} catch (ClassNotFoundException e) {
		errorMsg = Policy.bind("plugin.loadClassError", getId(), pluginClassName);
		throwException(errorMsg, e);
	}

	// find the correct constructor
	Constructor construct = null;
	try {
		construct = runtimeClass.getConstructor(new Class[] { IPluginDescriptor.class });
	} catch (NoSuchMethodException eNoConstructor) {
		errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName );
		throwException(errorMsg, eNoConstructor);
	}

	long time = 0L;
	if (InternalPlatform.DEBUG_STARTUP) {
		time = System.currentTimeMillis();
		System.out.println("Starting plugin: " + getId());
	}
	// create a new instance
	try {
		pluginObject = (Plugin) construct.newInstance(new Object[] { this });
	} catch (ClassCastException e) {
		errorMsg = Policy.bind("plugin.notPluginClass", pluginClassName);
		throwException(errorMsg, e);
	} catch (Exception e) {
		errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName);
		throwException(errorMsg, e);
	} 

	// run startup()
	final String message = Policy.bind("plugin.startupProblems", getId());
	final MultiStatus multiStatus = new MultiStatus(Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, null);
	ISafeRunnable code = new ISafeRunnable() {
		public void run() throws Exception {
			pluginObject.startup();
		}
		public void handleException(Throwable e) {
			multiStatus.add(new Status(Status.WARNING, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, e));
			try {
				pluginObject.shutdown();
			} catch (Exception ex) {
				// Ignore exceptions during shutdown. Since startup failed we are probably
				// in a weird state anyway.
			}
		}
	};
	InternalPlatform.run(code);
	if (InternalPlatform.DEBUG_STARTUP) {
		time = System.currentTimeMillis() - time;
		System.out.println("Finished plugin startup for " + getId() + " time: " + time + "ms");
	}
	if (!multiStatus.isOK())
		throw new CoreException(multiStatus);
}
/**
 * @see IPluginDescriptor
 */
public synchronized boolean isPluginActivated() {
	//note that this method is synchronized for good reason.  
	//During plugin activation, neither true nor false would be valid
	//return values for this method, so it must block until activation
	//completes.  For example, returning false during activation
	//would break the registry shutdown procedure, because a
	//plugin being activated during shutdown would never be shut down.
	return active;
}
public synchronized boolean isPluginDeactivated() {
	return deactivated;
}
private Properties loadJarDefinitions() {
	// We only need to load the plugin.jars files if we are in VAJ or VAME.
	if (!InternalPlatform.inVAJ() && !InternalPlatform.inVAME())
		return null;
	Properties result = null;
	InputStream is;
	try {
		result = new Properties();
		URL props = new URL(getInstallURLInternal(), PLUGIN_JARS);
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
private void logError(IStatus status) {
	InternalPlatform.getRuntimePlugin().getLog().log(status);
	if (InternalPlatform.DEBUG)
		System.out.println(status.getMessage());
}
/**
 * Returns <code>true</code> if we should continue with the plugin activation.
 */
private boolean pluginActivationEnter() throws CoreException {
	if (deactivated) {
		// had permanent error on startup
		String errorMsg = Policy.bind("plugin.pluginDisabled", getId());
		throwException(errorMsg, null);
	}
	if (active || activePending) {
		// already up and running 
		return false;
	}
	activePending = true;
	// go ahead and try to activate
	return true;
}
private void pluginActivationExit(boolean errorExit) {
	// we are done with with activation
	activePending = false;
	if (errorExit) {
		active = false;
		deactivated = true;
	} else
		active = true;
}
//private boolean resolveAndAddOSLibrary(String base, String spec, String[] filters, String type, boolean hasJarSpec, ArrayList[] result) {
//	IPath path = new Path(spec).removeFirstSegments(1);
//	String location = new Path("os/" + BootLoader.getOS()).append(path).toString();
//	return addLibraryWithFragments(base, location, filters, type, hasJarSpec, result);
//}
//
//private boolean resolveAndAddWSLibrary(String base, String spec, String[] filters, String type, boolean hasJarSpec, ArrayList[] result) {
//	IPath path = new Path(spec).removeFirstSegments(1);
//	String location = new Path("ws/" + BootLoader.getWS()).append(path).toString();
//	return addLibraryWithFragments(base, location, filters, type, hasJarSpec, result);
//}
//
//private boolean resolveAndAddNLLibrary(String base, String spec, String[] filters, String type, boolean hasJarSpec, ArrayList[] result) {
//	String path = new Path(spec).removeFirstSegments(1).toString();
//	boolean added = addLibraryNL(base, path, filters, type, hasJarSpec, result);
//	if (added) 
//		return true;
//	PluginFragmentModel[] fragments = getFragments();
//	if (fragments == null)
//		return false;
//	for (int i = 0; !added && i < fragments.length; i++) {
//		String subBase = getFragmentLocation (fragments[i]);
//		added = addLibraryNL(subBase, path, filters, type, hasJarSpec, result);
//	}
//	return added;
//}
//
private String getFragmentLocation(PluginFragmentModel fragment) {
	if (useDevURLs) 
		return PlatformURLBaseConnection.PLATFORM_URL_STRING;
	if (usePlatformURLs)
		return FragmentDescriptor.FRAGMENT_URL + fragment.toString() + "/";
	return fragment.getLocation();
}

//private boolean addLibraryNL(String base, String spec, String[] filters, String type, boolean hasJarSpec, ArrayList[] result) {
//	String nl = BootLoader.getNL();
//	boolean added = false;
//	while (!added && nl.length() > 0) {
//		String location = new Path("nl/" + nl).append(spec).addTrailingSeparator().toString();
//		added = addLibrary(base, location, filters, type, hasJarSpec, result);
//		int i = nl.lastIndexOf('_');
//		if (i < 0)
//			nl = "";
//		else
//			nl = nl.substring(0, i);
//	}
//	return added;
//}


public void setPluginClassLoader(DelegatingURLClassLoader value) {
	loader = value;
}
public void setPluginClassLoader(PluginClassLoader value) {
	loader = value;
}
private void throwException(String message, Throwable exception) throws CoreException {
	IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, exception);
	logError(status);
	throw new CoreException(status);
}
/**
 * @see #getUniqueIdentifierFromString
 * @see #getVersionIdentifierFromString
 */
public String toString() {
	return getUniqueIdentifier()+VERSION_SEPARATOR+getVersionIdentifier().toString();
}
public void activateDefaultPlugins(DelegatingURLClassLoader loader) {
	Object[] result = getPluginClassLoaderPath(true);
	loader.addURLs((URL[]) result[0], (URLContentFilter[]) result[1], (URL[]) result[2], (URLContentFilter[]) result[3]);
}
}
