package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.runtime.*;
import java.io.*;
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

	private ResourceBundle bundle = null; // plugin.properties
	private Locale locale = null; // bundle locale
	private boolean bundleNotFound = false; // marker to prevent unnecessary lookups

	// constants
	private static final String ECLIPSE_URL = EclipseURLHandler.ECLIPSE + EclipseURLHandler.PROTOCOL_SEPARATOR + "/" + EclipseURLPluginConnection.PLUGIN + "/";

	private static final String DEFAULT_BUNDLE_NAME = "plugin";
	private static final String KEY_PREFIX = "%";
	private static final String KEY_DOUBLE_PREFIX = "%%";

	private static final String URL_PROTOCOL_FILE = "file";

	private static final String VERSION_SEPARATOR_OPEN = "(";
	private static final String VERSION_SEPARATOR_CLOSE = ")";

	// Development mode constants
	private static final String PLUGIN_JARS = "plugin.jars";
	private static final String VA_PROPERTIES = ".va.properties";
	private static final String KEY_PROJECT = "projects";
public PluginDescriptor() {
	super();
}
/**
 * concatenates start and end.  If end has a '.' construct at the beginning
 * trim off any leading '.' constructs.  Since the libSpec was a path, we
 * know that it was canonicalized and will only have at most one set
 * of '.' constructs at the beginning.
 */
private String concat(String start, String end) {
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
		throwException("Unable to load class " + className, e1);
	}

	// create a new instance
	Object result = null;
	try {
		result = classInstance.newInstance();
	} catch (InstantiationException e2) {
		throwException(Policy.bind("noInstanceCreate", new String[] { className }), e2);
	} catch (IllegalAccessException e3) {
		throwException(Policy.bind("noInstanceCreate", new String[] { className }), e3);
	} catch (Exception e4) {
		// default constructor caused exception
		throwException(Policy.bind("constructorError", new String[] { className }), e4);
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
			throwException(Policy.bind("setError", new String[] { className }), te);
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
		return new URL(ECLIPSE_URL + getUniqueIdentifier() + "/");
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
	loader = new PluginClassLoader((URL[]) path[0], (URLContentFilter[]) path[1], PlatformClassLoader.getDefault(), this);
	loader.initializeImportedLoaders();
	// Note: need to be able to give out a loader reference before
	// its prereqs are initialized. Otherwise loops in prereq
	// definition will cause endless loop in initializePrereqs()
	return loader;
}
private Object[] getPluginClassLoaderPath(boolean eclipseURLs) {
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
	ArrayList urls = new ArrayList(5);
	ArrayList cfs = new ArrayList(5);
	// compute the base of the classpath urls.  If <code>eclipseURLs</code> is
	// true, we should use eclipse: URLs.  Otherwise the native URLs are used.
	URL install = eclipseURLs ? getInstallURL() : getInstallURLInternal();
	String execBase = install.toExternalForm();
	String devBase = null;
	if (InternalPlatform.inVAJ() || InternalPlatform.inVAME())
		devBase = EclipseURLPlatformConnection.PLATFORM_URL_STRING;
	else
		devBase = execBase;

	// build a list alternating lib spec and export spec
	ArrayList libSpecs = new ArrayList(5);
	String[] exportAll = new String[] { "*" };

	// add in any development mode class paths and the export all filter
	if (DelegatingURLClassLoader.devClassPath != null) {
		String[] specs = getArrayFromList(DelegatingURLClassLoader.devClassPath);
		// convert dev class path into url strings
		for (int j = 0; j < specs.length; j++) {
			String spec = devBase + specs[j];
			char lastChar = spec.charAt(spec.length() - 1);
			if ((spec.endsWith(".jar") || (lastChar == '/' || lastChar == '\\')))
				libSpecs.add(spec);
			else
				libSpecs.add(spec + "/");
			libSpecs.add(exportAll);
		}
	}

	// add in the class path entries spec'd in the plugin.xml.  If in development mode, 
	// add the entries from the plugin.jars first.
	ILibrary[] list = getRuntimeLibraries();
	for (int i = 0; i < list.length; i++) {
		ILibrary library = list[i];
		if (library.getPath().isEmpty())
			continue;
		String[] filters = library.isFullyExported() ? exportAll : library.getContentFilters();
		// add in the plugin.jars entries
		String libSpec = library.getPath().toString();
		String jarDefinition = null;
		if (jarDefinitions != null && libSpec != null) {
			jarDefinition = jarDefinitions.getProperty(libSpec);
			String[] specs = getArrayFromList(jarDefinition );
			// convert jar spec into url strings
			for (int j = 0; j < specs.length; j++) {
				libSpecs.add(devBase + specs[j] + "/");
				libSpecs.add(filters);
			}
		}

		libSpec = concat(execBase, libSpec);
		if (libSpec != null) {
			// if the libspec is NOT considered a directory, treat as a jar
			if (!libSpec.endsWith("/")) {
				// if running in VAJ or VAME and there was a plugin.jars definition, ignore the plugin.xml
				// library entry (assume the plugin.jars entries covered all the bases.  Otherwise, 
				// convert the plugin.xml entry into a URL.
				if ((InternalPlatform.inVAJ() || InternalPlatform.inVAME()) && jarDefinition != null) {
					libSpec = null;
				} else {
					if (libSpec.startsWith(EclipseURLHandler.ECLIPSE + EclipseURLHandler.PROTOCOL_SEPARATOR))
						libSpec += EclipseURLHandler.JAR_SEPARATOR;
					else
						libSpec = EclipseURLHandler.JAR + EclipseURLHandler.PROTOCOL_SEPARATOR + libSpec + EclipseURLHandler.JAR_SEPARATOR;
				}
			}
			// if we still have a libspec, add it to the list of classpath entries
			if (libSpec != null) {
				libSpecs.add(libSpec);
				libSpecs.add(filters);
			}
		}
	}

	// create path entries for all libraries except those which are files
	// and do not exist.
	for (Iterator it = libSpecs.iterator(); it.hasNext();) {
		String spec = (String) it.next();
		String[] filter = (String[]) it.next();
		try {
			URL entry = new URL(spec);
			URL resolved = Platform.resolve(entry);
			boolean add = true;
			if (resolved.getProtocol().equals(EclipseURLHandler.FILE))
				add = new File(resolved.getFile()).exists();
			if (add) {
				urls.add(entry);
				cfs.add(new URLContentFilter(filter));
			}
		} catch (IOException e) {
			// skip bad URLs
		}
	}

	Object[] result = new Object[2];
	result[0] = urls.toArray(new URL[urls.size()]);
	result[1] = cfs.toArray(new URLContentFilter[cfs.size()]);
	return result;
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
public ResourceBundle getResourceBundle(Locale locale) throws MissingResourceException {
	// we cache the bundle for a single locale 
	if (bundle != null && locale.equals(locale))
		return bundle;

	// check if we already tried and failed
	if (bundleNotFound)
		throw new MissingResourceException(Policy.bind("resourceNotFound", new String[] { DEFAULT_BUNDLE_NAME + "_" + locale }), DEFAULT_BUNDLE_NAME + "_" + locale, "");

	// try to load bundle from this plugin install directory
	ClassLoader resourceLoader = new URLClassLoader(new URL[] { getInstallURL()}, null);
	ResourceBundle newBundle = null;
	try {
		newBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, locale, resourceLoader);
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
 * @see IPluginDescriptor
 */
public PluginVersionIdentifier getVersionIdentifier() {
	String version = getVersion();
	if (version == null)
		return new PluginVersionIdentifier("1.0.0");
	try {
		return new PluginVersionIdentifier(version);
	} catch (Throwable e) {
		return new PluginVersionIdentifier("1.0.0");
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
		errorMsg = Policy.bind("noLoadClass", new String[] { pluginClassName });
		throwException(errorMsg, e);
	}

	// find the correct constructor
	Constructor construct = null;
	try {
		construct = runtimeClass.getConstructor(new Class[] { IPluginDescriptor.class });
	} catch (NoSuchMethodException eNoConstructor) {
		errorMsg = Policy.bind("constructorError", new String[] { pluginClassName });
		throwException(errorMsg, eNoConstructor);
	}

	// create a new instance
	Plugin result = null;
	try {
		pluginObject = (Plugin) construct.newInstance(new Object[] { this });
	} catch (ClassCastException e) {
		errorMsg = Policy.bind("notPluginChild", new String[] { pluginClassName });
		throwException(errorMsg, null);
	} catch (InstantiationException e) {
		errorMsg = Policy.bind("noInstanceCreate", new String[] { pluginClassName });
		throwException(errorMsg, e);
	} catch (IllegalAccessException e) {
		errorMsg = Policy.bind("noInstanceCreate", new String[] { pluginClassName });
		throwException(errorMsg, e);
	} catch (InvocationTargetException e) {
		// user code caused exception
		errorMsg = Policy.bind("constructorError", new String[] { pluginClassName });
		throwException(errorMsg, e);
	} catch (Exception e) {
		// user code caused exception
		errorMsg = Policy.bind("constructorError", new String[] { pluginClassName });
		throwException(errorMsg, e);
	}

	// run startup()
	final String message = Policy.bind("startupProblems", new String[] {
	});
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
	if (!multiStatus.isOK())
		throw new CoreException(multiStatus);
}
/**
 * @see IPluginDescriptor
 */
public synchronized boolean isPluginActivated() {
	return active;
}
public synchronized boolean isPluginDeactivated() {
	return deactivated;
}
private Properties loadJarDefinitions() {
	if (!InternalPlatform.inDevelopmentMode())
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
		String errorMsg = Policy.bind("pluginDisabled", new String[] { getUniqueIdentifier()});
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
public String toString() {
	return getUniqueIdentifier()+VERSION_SEPARATOR_OPEN+getVersionIdentifier().toString()+VERSION_SEPARATOR_CLOSE;
}
}
