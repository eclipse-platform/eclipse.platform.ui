package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
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
	return new String[] {""};
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
	// Any library access filters specified in the plugin.xml are
	// applied to the corresponding loader search path entries

	// compute the base of the classpath urls.  If <code>platformURLFlag</code> is
	// true, we should use platform: URLs.  Otherwise the native URLs are used.
	usePlatformURLs = platformURLFlag;
	// If we are using platform URLs, install will look like
	// platform:/plugin/<pluginId>_<pluginVersion>/
	// If we aren't using platform URLs install will be the URL
	// corresponding to the root directory of this plugin
	URL install = usePlatformURLs ? getInstallURL() : getInstallURLInternal();
	String execBase = install.toExternalForm();
	
	String[] basePaths = buildBasePaths(execBase);

	String[] exportAll = new String[] { "*" };
	ArrayList[] result = new ArrayList[4];
	result[0] = new ArrayList();
	result[1] = new ArrayList();
	result[2] = new ArrayList();
	result[3] = new ArrayList();

	// add in any development mode class paths and the export all filter
	// For example, if you specified "-dev bin" on the command line to
	// launch Eclipse, you want to add the bin directory of each plugin
	// to the classpath.
	if (DelegatingURLClassLoader.devClassPath != null) {
		String[] specs = getArrayFromList(DelegatingURLClassLoader.devClassPath);
		Vector baseSpecs = new Vector(specs.length);
		// convert dev class path into url strings
		for (int j = 0; j < specs.length; j++) {
			String spec = specs[j];
			char lastChar = spec.charAt(spec.length() - 1);
			// if the spec is not a jar and does not have a trailing slash, add one
			if (!(spec.endsWith(".jar") || (lastChar == '/' || lastChar == '\\')))
				spec = spec + "/";
			if (!spec.endsWith(".jar"))
				baseSpecs.add(spec);
			// add the dev path for the plugin itself
			addLibraryWithFragments(basePaths, JAR_VARIANTS, spec, exportAll, ILibrary.CODE, true, result);
		}
		// Now add all the spec directories to the basePaths so we 
		// will look in these directories for jar files, etc.
		String[] baseSuffix = (String[])baseSpecs.toArray(new String[baseSpecs.size()]);
		String[] newBasePaths = new String[basePaths.length + (basePaths.length * baseSpecs.size())];
		int newIdx = 0;
		for (int j = 0; j < baseSuffix.length; j++) {
			for (int i = 0; i < basePaths.length; i++) {
				newBasePaths[newIdx++] = basePaths[i] + baseSuffix[j];
			}
		}
		for (int i = 0; i < basePaths.length; i++) {
			newBasePaths[newIdx++] = basePaths[i];
		}
		basePaths = newBasePaths;			
	}

	// add in the class path entries spec'd in the plugin.xml.  
	ILibrary[] list = getRuntimeLibraries();
	for (int i = 0; i < list.length; i++) {
		ILibrary library = list[i];
		// if the library path is empty, skip it.
		if (library.getPath().isEmpty())
			continue;
		String[] filters = library.isFullyExported() ? exportAll : library.getContentFilters();
		String libSpec = library.getPath().toString();
		resolveAndAddLibrary(libSpec, filters, basePaths, library.getType(), result);
	}

	Object[] array = new Object[4];
	array[0] = result[0].toArray(new URL[result[0].size()]);
	array[1] = result[1].toArray(new URLContentFilter[result[1].size()]);
	array[2] = result[2].toArray(new URL[result[2].size()]);
	array[3] = result[3].toArray(new URLContentFilter[result[3].size()]);
	return array;
}

private boolean resolveAndAddLibrary(String spec, String[] filters, String[] basePaths, String type, ArrayList[] result) {
	if (spec.charAt(0) == '$') {
		IPath path = new Path(spec);
		String first = path.segment(0);
		String remainder = path.removeFirstSegments(1).toString();
		if (first.equalsIgnoreCase("$ws$"))
			return addLibraryWithFragments(basePaths, WS_JAR_VARIANTS, "/" + remainder, filters, type, false, result);
		if (first.equalsIgnoreCase("$os$"))
			return addLibraryWithFragments(basePaths, OS_JAR_VARIANTS, "/" + remainder, filters, type, false, result);
		if (first.equalsIgnoreCase("$nl$"))
			return addLibraryWithFragments(basePaths, NL_JAR_VARIANTS, "/" + remainder, filters, type, false, result);
	}
	return addLibraryWithFragments(basePaths, JAR_VARIANTS, spec, filters, type, false, result);
}

private boolean addLibraryWithFragments(String[] basePaths, String[] variants, String spec, String[] filters, String type, boolean addAll, ArrayList[] result) {
	// If addAll is set to false, only the first valid match will be
	// added to the class path.  If addAll is set to true, all valid
	// path permutations will be added to the class path.  Typically
	// you will want addAll set to true for the development mode
	// case.
	boolean added = false;
	// The only case where we want to stop looping is if 
	// addAll = false and added = true.
	// In all other cases, keep looping and adding (or attempting to
	// add) path values.
	// So keep looping if addAll = true OR added = false.
	for (int j = 0; j < variants.length && (addAll || !added); j++) {
		for (int i = 0; i < basePaths.length && (addAll || !added); i++) {
			added = addLibrary(basePaths[i], spec, variants[j], filters, type, result);
		}
	}
	return added;
}

private boolean addLibrary(String base, String libSpec, String variant, String[] filters, String type, ArrayList[] result) {
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
	if (protocol.equals(PlatformURLHandler.JAR)) {
		// strip off the jar separator at the end of the url then do a recursive call
		// to interpret the sub URL.
		String file = target.getFile();
		file = file.substring(0, file.length() - PlatformURLHandler.JAR_SEPARATOR.length());
		try {
			return getFileFromURL(new URL(file));
		} catch (MalformedURLException e) {
			// ignore bad URLs
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
		try { 
			b = getResourceBundle();
		} catch (MissingResourceException e) {
			// just return the default (dflt)
		};
	}
	
	if (b==null) return dflt;
	
	try { 
		return b.getString(key.substring(1));
	} catch(MissingResourceException e) {
		return dflt;
	}
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
private String getFragmentLocation(PluginFragmentModel fragment) {
	if (usePlatformURLs)
		return FragmentDescriptor.FRAGMENT_URL + fragment.toString() + "/";
	return fragment.getLocation();
}
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
/**
 * @see IPluginDescriptor
 */
public final URL find(IPath path) {
	return find(path, null);
}
/**
 * @see IPluginDescriptor
 */
public final URL find(IPath path, Map override) {
	if (path == null) return null;
	
	URL install = getInstallURLInternal();
	String first = path.segment(0);
	if (first.charAt(0) != '$') {		
		URL result = findInPlugin(install, path.toString());
		if (result != null)
			return result;	
		return findInFragments(path.toString());
	}
	IPath rest = path.removeFirstSegments(1);
	if (first.equalsIgnoreCase("$nl$"))
		return findNL(install, rest, override);
	if (first.equalsIgnoreCase("$os$"))
		return findOS(install, rest, override);
	if (first.equalsIgnoreCase("$ws$"))
		return findWS(install, rest, override);
	if (first.equalsIgnoreCase("$files$"))
		return null;
	return null;
}

private URL findOS(URL install, IPath path, Map override) {
	String os = null;
	if (override != null)
		try {
			// check for override
			os = (String) override.get("$os$");
		} catch (ClassCastException e) {
			// just in case
		}
	if (os == null)
		// use default
		os = BootLoader.getOS();
	if (os.length() == 0)
		return null;
	boolean done = false;
	URL result = null;
	while (!done) {
		String filePath = "os/" + os + "/" + path.toString();	
		result = findInPlugin(install, filePath);
		if (result != null)
			return result;	
		result = findInFragments(filePath);
		if (result != null)
			return result;
		int i = os.lastIndexOf('/');
		if (i < 0) 
			done = true;
		else
			os = os.substring(0, i);
	}
	// If we get to this point, we haven't found it yet.
	// Look in the plugin and fragment root directories
	result = findInPlugin(install, path.toString());
	if (result != null)
		return result;
	return findInFragments(path.toString());
}

private URL findWS(URL install, IPath path, Map override) {
	String ws = null;
	if (override != null)
		try {
			// check for override
			ws = (String) override.get("$ws$");
		} catch (ClassCastException e) {
			// just in case
		}
	if (ws == null)
		// use default
		ws = BootLoader.getWS();
	String filePath = "ws/" + ws + "/" + path.toString();
	// We know that there is only one segment to the ws path
	// e.g. ws/win32	
	URL result = findInPlugin(install, filePath);
	if (result != null)
		return result;	
	result = findInFragments(filePath);
	if (result != null)
		return result;
	// If we get to this point, we haven't found it yet.
	// Look in the plugin and fragment root directories
	result = findInPlugin(install, path.toString());
	if (result != null)
		return result;
	return findInFragments(path.toString());
}

private URL findNL(URL install, IPath path, Map override) {
	String nl = null;
	if (override != null)
		try {
			// check for override
			nl = (String) override.get("$nl$");
		} catch (ClassCastException e) {
			// just in case
		}
	if (nl == null)
		// use default
		nl = BootLoader.getNL();
	if (nl.length() == 0)
		return null;
	nl = nl.replace('_', '/');
	URL result = null;
	boolean done = false;
	
	while (!done) {		
		String filePath = "nl/" + nl + "/" + path.toString();
		result = findInPlugin(install, filePath);
		if (result != null)
			return result;
		result = findInFragments(filePath);
		if (result != null)
			return result;
		else {
			int i = nl.lastIndexOf('/');
			if (i < 0)
				done = true;
			else
				nl = nl.substring(0, i);
		}
	}
	// If we get to this point, we haven't found it yet.
	// Look in the plugin and fragment root directories
	result = findInPlugin(install, path.toString());
	if (result != null)
		return result;
	return findInFragments(path.toString());
}

private URL findInPlugin(URL install, String filePath) {
	try {
		URL location = new URL(install, filePath);
		String file = getFileFromURL(location);
		if (file != null && new File(file).exists())
			return location;						
	} catch (IOException e) {
		// ignore bad URLs
	}
	return null;
}

private URL findInFragments(String path) {
	// This method will return a 'real' URL (as opposed to a platform
	// URL).
	PluginFragmentModel[] fragments = getFragments();
	if (fragments == null)
		return null;
		
	for (int i = 0; i < fragments.length; i++) {
		try {
			URL location = new URL(fragments[i].getLocation() + path);
			String file = getFileFromURL(location);
			if (file != null && new File(file).exists())
				return location;
		} catch (IOException e) {
			// skip malformed url and urls that cannot be resolved
		}
	}
	return null;
}
}
