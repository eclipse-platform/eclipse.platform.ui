package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.plugins.DefaultPlugin;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map;
import java.net.*;
import java.io.*;

/**
 * The abstract superclass of all plug-in runtime class
 * implementations. A plug-in subclasses this class and overrides
 * the <code>startup</code> and <code>shutdown</code> methods 
 * in order to react to life cycle requests automatically issued
 * by the platform.
 * <p>
 * Conceptually, the plug-in runtime class represents the entire plug-in
 * rather than an implementation of any one particular extension the
 * plug-in declares. A plug-in is not required to explicitly
 * specify a plug-in runtime class; if none is specified, the plug-in
 * will be given a default plug-in runtime object that ignores all life 
 * cycle requests (it still provides access to the corresponding
 * plug-in descriptor).
 * </p>
 * <p>
 * In the case of more complex plug-ins, it may be desireable
 * to define a concrete subclass of <code>Plugin</code>.
 * However, just subclassing <code>Plugin</code> is not
 * sufficient. The name of the class must be explicitly configured
 * in the plug-in's manifest (<code>plugin.xml</code>) file
 * with the class attribute of the <code>&ltplugin&gt</code> element markup.
 * </p>
 * <p>
 * Instances of plug-in runtime classes are automatically created 
 * by the platform in the course of plug-in activation.
 * <b>Clients must never explicitly instantiate a plug-in runtime class</b>.
 * </p>
 * <p>
 * A typical implementation pattern for plug-in runtime classes is to
 * provide a static convenience method to gain access to a plug-in's
 * runtime object. This way, code in other parts of the plug-in
 * implementation without direct access to the plug-in runtime object
 * can easily obtain a reference to it, and thence to any plug-in-wide
 * resources recorded on it. An example follows:
 * <pre>
 *     package myplugin;
 *     public class MyPluginClass extends Plugin {
 *         private static MyPluginClass instance;
 *
 *         public static MyPluginClass getInstance() { return instance; }
 *
 *         public void MyPluginClass(IPluginDescriptor descriptor) {
 *             super(descriptor);
 *             instance = this;
 *             // ... other initialization
 *         }
 *         // ... other methods
 *     }
 * </pre>
 * In the above example, a call to <code>MyPluginClass.getInstance()</code>
 * will always return an initialized instance of <code>MyPluginClass</code>.
 * </p>
 * <p>
 * The static method <code>Platform.getPlugin()</code>
 * can be used to locate a plug-in's runtime object by name.
 * The extension initialization would contain the following code:
 * <pre>
 *     Plugin myPlugin = Platform.getPlugin("com.example.myplugin");
 * </pre>
 * 
 * Another typical implementation pattern for plug-in classes
 * is handling of any initialization files required by the plug-in.
 * Typically, each plug-in will ship one or more default files
 * as part of the plug-in install. The executing plug-in will
 * use the defaults on initial startup (or when explicitly requested
 * by the user), but will subsequently rewrite any modifications
 * to the default settings into one of the designated plug-in
 * working directory locations. An example of such an implementation
 * pattern is illustrated below:
 * <pre>
 * package myplugin;
 * public class MyPlugin extends Plugin {
 *
 *     private static final String INI = "myplugin.ini"; 
 *     private Properties myProperties = null;
 *
 *     public void startup() throws CoreException {
 *         try {
 *             InputStream input = null;
 *             // look for working properties.  If none, use shipped defaults 
 *             File file = getStateLocation().append(INI).toFile();
 *             if (!file.exists()) {			
 *                 URL base = getDescriptor().getInstallURL();
 *                 input = (new URL(base,INI)).openStream();
 *             } else 
 *                 input = new FileInputStream(file);
 * 
 *             // load properties 
 *             try {
 *                 myProperties = new Properties();
 *                 myProperties.load(input);
 *             } finally {
 *                 try {
 *                     input.close();
 *                 } catch (IOException e) {
 *                     // ignore failure on close
 *                 }
 *             }
 *         } catch (Exception e) {
 *             throw new CoreException(
 *                 new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(),
 *                     0, "Problems starting plug-in myplugin", e));
 *         }
 *     }
 *
 *     public void shutdown() throws CoreException { 
 *         // save properties in plugin state location (r/w)
 *         try {
 *             FileOutputStream output = null; 
 *             try {
 *                 output = new FileOutputStream(getStateLocation().append(INI)); 
 *                 myProperties.store(output, null);
 *             } finally {
 *                 try {
 *                     output.close();
 *                 } catch (IOException e) {
 *                     // ignore failure on close
 *                 }
 *             }
 *         } catch (Exception e) {
 *             throw new CoreException(
 *                 new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(),
 *                     0, "Problems shutting down plug-in myplugin", e));
 *         }
 *     }
 *
 *     public Properties getProperties() {	
 *         return myProperties; 
 *     }
 * }
 * </pre>
 * </p>
 */
public abstract class Plugin  {

	/**
	 * The debug flag for this plug-in.  The flag is false by default.
	 * It can be set to true either by the plug-in itself or in the platform 
	 * debug options.
	 */
	private boolean debug = false;
	
	/** The plug-in descriptor.
	 */
	private IPluginDescriptor descriptor;
/**
 * Creates a new plug-in runtime object for the given plug-in descriptor.
 * <p>
 * Instances of plug-in runtime classes are automatically created 
 * by the platform in the course of plug-in activation.
 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
 * </p>
 * <p>
 * Note: The class loader typically has monitors acquired during invocation of this method.  It is 
 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
 * as this would lead to deadlock vulnerability.
 * </p>
 *
 * @param descriptor the plug-in descriptor
 * @see #getDescriptor
 */
public Plugin(IPluginDescriptor descriptor) {
	Assert.isNotNull(descriptor);
	Assert.isTrue(!descriptor.isPluginActivated(), Policy.bind("plugin.deactivatedLoad", this.getClass().getName(), descriptor.getUniqueIdentifier() + " is not activated"));
	String className = ((PluginDescriptor) descriptor).getPluginClass();
	if (this.getClass() == DefaultPlugin.class) 
		Assert.isTrue(className == null || className.equals(""), Policy.bind("plugin.mismatchRuntime", descriptor.getUniqueIdentifier()));
	else 
		Assert.isTrue(this.getClass().getName().equals(className), Policy.bind("plugin.mismatchRuntime", descriptor.getUniqueIdentifier()));
	this.descriptor = descriptor;
	String key = descriptor.getUniqueIdentifier() + "/debug";
	String value = Platform.getDebugOption(key);
	this.debug = value == null ? false : value.equalsIgnoreCase("true");
}
/**
 * Returns a URL for the given path.  Returns <code>null</code> if the URL
 * could not be computed or created.
 * 
 * @param file path relative to plug-in installation location 
 * @return a URL for the given path or <code>null</code>
 */
public final URL find(IPath path) {
	return find(path, null);
}
/**
 * Returns a URL for the given path.  Returns <code>null</code> if the URL
 * could not be computed or created.
 * 
 * @param path file path relative to plug-in installation location
 * @param override map of override substitution arguments to be used for
 * any $arg$ path elements. The map keys correspond to the substitution
 * arguments (eg. "$nl$" or "$os$"). The resulting
 * values must be of type java.lang.String. If the map is <code>null</code>,
 * or does not contain the required substitution argument, the default
 * is used.
 * @return a URL for the given path or <code>null</code>
 */
public final URL find(IPath path, Map override) {
	URL install = getDescriptor().getInstallURL();
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
		try { // check for override
			os = (String) override.get("$os$");
		} catch (ClassCastException e) {
		}
	if (os == null)
		os = BootLoader.getOS(); // use default
	String filePath = "os/" + os + "/" + path.toString();	
	URL result = findInPlugin(install, filePath);
	if (result != null)
		return result;	
	return findInFragments(filePath);
}

private URL findWS(URL install, IPath path, Map override) {
	String ws = null;
	if (override != null)
		try { // check for override
			ws = (String) override.get("$ws$");
		} catch (ClassCastException e) {
		}
	if (ws == null)
		ws = BootLoader.getWS(); // use default
	String filePath = "ws/" + ws + "/" + path.toString();	
	URL result = findInPlugin(install, filePath);
	if (result != null)
		return result;	
	return findInFragments(filePath);
}

private URL findNL(URL install, IPath path, Map override) {
	String nl = null;
	if (override != null)
		try { // check for override
			nl = (String) override.get("$nl$");
		} catch (ClassCastException e) {
		}
	if (nl == null)
		nl = BootLoader.getNL(); // use default
	nl = nl.replace('_', '/');
	URL result = null;
	boolean done = false;
	
	while (!done) {		
		String filePath = "nl/" + (nl.equals("") ? nl : nl + "/") + path.toString();
		result = findInPlugin(install, filePath);
		if (result != null)
			return result;
		result = findInFragments(filePath);
		if (result != null)
			return result;
		if (nl.length() == 0)
			done = true;
		else {
			int i = nl.lastIndexOf('_');
			if (i < 0)
				nl = "";
			else
				nl = nl.substring(0, i);
		}
	}
	return null;
}

private URL findInPlugin(URL install, String filePath) {
	try {
		URL result = new URL(install, filePath);
		URL location = Platform.resolve(result);
		String file = getFileFromURL(location);
		if (file != null && new File(file).exists())
			return result;						
	} catch (IOException e) {
	}
	return null;
}

private URL findInFragments(String path) {
	PluginFragmentModel[] fragments = ((PluginDescriptor)getDescriptor()).getFragments();
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

private String getFileFromURL(URL target) {
	String protocol = target.getProtocol();
	if (protocol.equals(PlatformURLHandler.FILE))
		return target.getFile();
	if (protocol.equals(PlatformURLHandler.VA))
		return target.getFile();
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
 * Returns the plug-in descriptor for this plug-in runtime object.
 *
 * @return the plug-in descriptor for this plug-in runtime object
 */
public final IPluginDescriptor getDescriptor() {
	return descriptor;
}
/**
 * Returns the log for this plug-in.  If no such log exists, one is created.
 *
 * @return the log for this plug-in
 */
public final ILog getLog() {
	return InternalPlatform.getLog(this);
}
/**
 * Returns the location in the local file system of the 
 * plug-in state area for this plug-in.
 * If the plug-in state area did not exist prior to this call,
 * it is created.
 * <p>
 * The plug-in state area is a file directory within the
 * platform's metadata area where a plug-in is free to create files.
 * The content and structure of this area is defined by the plug-in,
 * and the particular plug-in is solely responsible for any files
 * it puts there. It is recommended for plug-in preference settings and 
 * other configuration parameters.
 * </p>
 *
 * @return a local file system path
 */
public final IPath getStateLocation() {
	return InternalPlatform.getPluginStateLocation(this);
}
/**
 * Returns whether this plug-in is in debug mode.
 * By default plug-ins are not in debug mode.  A plug-in can put itself
 * into debug mode or the user can set an execution option to do so.
 *
 * @return whether this plug-in is in debug mode
 */
public boolean isDebugging() {
	return debug;
}
/**
 * Returns an input stream for the specified file. The file path
 * must be specified relative this the plug-in's installation location.
 *
 * @param file path relative to plug-in installation location
 * @return an input stream
 * @see #openStream(IPath,boolean)
 */
public final InputStream openStream(IPath file) throws IOException {
	return openStream(file, false);
}
/**
 * Returns an input stream for the specified file. The file path
 * must be specified relative to this plug-in's installation location.
 * Optionally, the platform searches for the correct localized version
 * of the specified file using the users current locale, and Java
 * naming convention for localized resource files (locale suffix appended 
 * to the specified file extension).
 * <p>
 * The caller must close the returned stream when done.
 * </p>
 *
 * @param file path relative to plug-in installation location
 * @param localized <code>true</code> for the localized version
 *   of the file, and <code>false</code> for the file exactly
 *   as specified
 * @return an input stream
 */
public final InputStream openStream(IPath file, boolean localized) throws IOException {
	URL target = new URL(getDescriptor().getInstallURL() + file.toString());
	return target.openStream();
}
/**
 * Sets whether this plug-in is in debug mode.
 * By default plug-ins are not in debug mode.  A plug-in can put itself
 * into debug mode or the user can set a debug option to do so.
 *
 * @param value whether or not this plugi-in is in debug mode
 */
public void setDebugging(boolean value) {
	debug = value;
}
/**
 * Shuts down this plug-in and discards all plug-in state.
 * <p>
 * This method should be re-implemented in subclasses that need to do something
 * when the plug-in is shut down.  Implementors should call the inherited method
 * to ensure that any system requirements can be met.
 * </p>
 * <p>
 * Plug-in shutdown code should be robust. In particular, this method
 * should always make an effort to shut down the plug-in. Furthermore,
 * the code should not assume that the plug-in was started successfully,
 * as this method will be invoked in the event of a failure during startup.
 * </p>
 * <p>
 * Note 1: If a plug-in has been started, this method will be automatically
 * invoked by the platform when the platform is shut down.
 * </p>
 * <p>
 * Note 2: This method is intended to perform simple termination
 * of the plug-in environment. The platform may terminate invocations
 * that do not complete in a timely fashion.
 * </p>
 * <b>Clients must never explicitly call this method.</b>
 *
 * @exception CoreException if this method fails to shut down
 *   this plug-in 
 */
public void shutdown() throws CoreException {
}
/**
 * Starts up this plug-in.
 * <p>
 * This method should be overridden in subclasses that need to do something
 * when this plug-in is started.  Implementors should call the inherited method
 * to ensure that any system requirements can be met.
 * </p>
 * <p>
 * If this method throws an exception, it is taken as an indication that
 * plug-in initialization has failed; as a result, the plug-in will not
 * be activated; moreover, the plug-in will be marked as disabled and 
 * ineligible for activation for the duration.
 * </p>
 * <p>
 * Plug-in startup code should be robust. In the event of a startup failure,
 * the plug-in's <code>shutdown</code> method will be invoked automatically,
 * in an attempt to close open files, etc.
 * </p>
 * <p>
 * Note 1: This method is automatically invoked by the platform 
 * the first time any code in the plug-in is executed.
 * </p>
 * <p>
 * Note 2: This method is intended to perform simple initialization 
 * of the plug-in environment. The platform may terminate initializers 
 * that do not complete in a timely fashion.
 * </p>
 * <p>
 * Note 3: The class loader typically has monitors acquired during invocation of this method.  It is 
 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
 * as this would lead to deadlock vulnerability.
 * </p>
 * <b>Cliens must never explicitly call this method.</b>
 *
 * @exception CoreException if this plug-in did not start up properly
 */
public void startup() throws CoreException {
}
/**
 * Returns a string representation of the plug-in, suitable 
 * for debugging purposes only.
 */
public String toString() {
	return descriptor.toString();
}
}
