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
package org.eclipse.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.runtime.FindSupport;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.osgi.framework.Bundle;

// TODO clarify the javadoc below 
/**
 * The central class of the Eclipse Platform Runtime. This class cannot
 * be instantiated or subclassed by clients; all functionality is provided 
 * by static methods.  Features include:
 * <ul>
 * <li>the platform registry of installed plug-ins</li>
 * <li>the platform adapter manager</li>
 * <li>the platform log</li>
 * <li>the authorization info management</li>
 * </ul>
 * <p>
 * The platform is in one of two states, running or not running, at all
 * times. The only ways to start the platform running, or to shut it down,
 * are on the bootstrap <code>BootLoader</code> class. Code in plug-ins will
 * only observe the platform in the running state. The platform cannot
 * be shutdown from inside (code in plug-ins have no access to
 * <code>BootLoader</code>).
 * </p>
 */
public final class Platform {
	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.runtime</code>")
	 * of the Core Runtime (pseudo-) plug-in.
	 */
	public static final String PI_RUNTIME = "org.eclipse.core.runtime"; //$NON-NLS-1$
	
	// TODO was this API anywhere?  On Plugin?
	public static final String PI_BOOT = "org.eclipse.core.boot"; //$NON-NLS-1$
	/** 
	 * The simple identifier constant (value "<code>applications</code>") of
	 * the extension point of the Core Runtime plug-in where plug-ins declare
	 * the existence of runnable applications. A plug-in may define any
	 * number of applications; however, the platform is only capable
	 * of running one application at a time.
	 * 
	 * @see org.eclipse.core.boot.BootLoader#run
	 */
	public static final String PT_APPLICATIONS = "applications"; //$NON-NLS-1$

	/** 
	 * Debug option value denoting the time at which the platform runtime
	 * was started.  This constant can be used in conjunction with
	 * <code>getDebugOption</code> to find the string value of
	 * <code>System.currentTimeMillis()</code> when the platform was started.
		 */
	public static final String OPTION_STARTTIME = PI_RUNTIME + "/starttime"; //$NON-NLS-1$

	/**
	 * Name of a preference for configuring the performance level for this system.
	 *
	 * <p>
	 * This value can be used by all components to customize features to suit the 
	 * speed of the user's machine.  The platform job manager uses this value to make
	 * scheduling decisions about background jobs.
	 * </p>
	 * <p>
	 * The preference value must be an integer between the constant values
	 * MIN_PERFORMANCE and MAX_PERFORMANCE
	 * </p>
	 * @see #MIN_PERFORMANCE
	 * @see #MAX_PERFORMANCE
	 * @since 3.0
	 */
	public static final String PREF_PLATFORM_PERFORMANCE = "runtime.performance"; //$NON-NLS-1$

	/** 
	 * Constant (value 1) indicating the minimum allowed value for the 
	 * <code>PREF_PLATFORM_PERFORMANCE</code> preference setting.
	 * @since 3.0
	 */
	public static final int MIN_PERFORMANCE = 1;

	/** 
	 * Constant (value 5) indicating the maximum allowed value for the 
	 * <code>PREF_PLATFORM_PERFORMANCE</code> preference setting.
	 * @since 3.0
	 */
	public static final int MAX_PERFORMANCE = 5;

	/** 
	 * Status code constant (value 1) indicating a problem in a plug-in
	 * manifest (<code>plugin.xml</code>) file.
		 */
	public static final int PARSE_PROBLEM = 1;

	/**
	 * Status code constant (value 2) indicating an error occurred while running a plug-in.
		 */
	public static final int PLUGIN_ERROR = 2;

	/**
	 * Status code constant (value 3) indicating an error internal to the
	 * platform has occurred.
		 */
	public static final int INTERNAL_ERROR = 3;

	/**
	 * Status code constant (value 4) indicating the platform could not read
	 * some of its metadata.
		 */
	public static final int FAILED_READ_METADATA = 4;

	/**
	 * Status code constant (value 5) indicating the platform could not write
	 * some of its metadata.
		 */
	public static final int FAILED_WRITE_METADATA = 5;

	/**
	 * Status code constant (value 6) indicating the platform could not delete
	 * some of its metadata.
		 */
	public static final int FAILED_DELETE_METADATA = 6;
	/**
	 * Private constructor to block instance creation.
	 */
	private Platform() {
	}
	/**
	 * Adds the given authorization information to the keyring. The
	 * information is relevant for the specified protection space and the
	 * given authorization scheme. The protection space is defined by the
	 * combination of the given server URL and realm. The authorization 
	 * scheme determines what the authorization information contains and how 
	 * it should be used. The authorization information is a <code>Map</code> 
	 * of <code>String</code> to <code>String</code> and typically
	 * contains information such as usernames and passwords.
	 *
	 * @param serverUrl the URL identifying the server for this authorization
	 *		information. For example, "http://www.example.com/".
	 * @param realm the subsection of the given server to which this
	 *		authorization information applies.  For example,
	 *		"realm1@example.com" or "" for no realm.
	 * @param authScheme the scheme for which this authorization information
	 *		applies. For example, "Basic" or "" for no authorization scheme
	 * @param info a <code>Map</code> containing authorization information 
	 *		such as usernames and passwords (key type : <code>String</code>, 
	 *		value type : <code>String</code>)
	 * @exception CoreException if there are problems setting the
	 *		authorization information. Reasons include:
	 * <ul>
	 * <li>The keyring could not be saved.</li>
	 * </ul>
	 */
	public static void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
		InternalPlatform.getDefault().addAuthorizationInfo(serverUrl, realm, authScheme, info);
	}
	/** 
	 * Adds the given log listener to the notification list of the platform.
	 * <p>
	 * Once registered, a listener starts receiving notification as entries
	 * are added to plug-in logs via <code>ILog.log()</code>. The listener continues to
	 * receive notifications until it is replaced or removed.
	 * </p>
	 *
	 * @param listener the listener to register
	 * @see ILog#addLogListener
	 * @see #removeLogListener
	 */
	public static void addLogListener(ILogListener listener) {
		InternalPlatform.getDefault().addLogListener(listener);
	}
	/**
	 * Adds the specified resource to the protection space specified by the
	 * given realm. All targets at or deeper than the depth of the last
	 * symbolic element in the path of the given resource URL are assumed to
	 * be in the same protection space.
	 *
	 * @param resourceUrl the URL identifying the resources to be added to
	 *		the specified protection space. For example,
	 *		"http://www.example.com/folder/".
	 * @param realm the name of the protection space. For example,
	 *		"realm1@example.com"
	 * @exception CoreException if there are problems setting the
	 *		authorization information. Reasons include:
	 * <ul>
	 * <li>The keyring could not be saved.</li>
	 * </ul>
	 */
	public static void addProtectionSpace(URL resourceUrl, String realm) throws CoreException {
		InternalPlatform.getDefault().addProtectionSpace(resourceUrl, realm);
	}
	/**
	 * Returns a URL which is the local equivalent of the
	 * supplied URL. This method is expected to be used with 
	 * plug-in-relative URLs returned by IPluginDescriptor.
	 * If the specified URL is not a plug-in-relative URL, it 
	 * is returned asis. If the specified URL is a plug-in-relative
	 * URL of a file (incl. .jar archive), it is returned as 
	 * a locally-accessible URL using "file:" or "jar:file:" protocol
	 * (caching the file locally, if required). If the specified URL
	 * is a plug-in-relative URL of a directory,
	 * an exception is thrown.
	 *
	 * @param url original plug-in-relative URL.
	 * @return the resolved URL
	 * @exception IOException if unable to resolve URL
	 * @see #resolve
	 * @see IPluginDescriptor#getInstallURL
	 */
	public static URL asLocalURL(URL url) throws IOException {
		return InternalPlatform.getDefault().asLocalURL(url);
	}

	/**
	 * Takes down the splash screen if one was put up.
	 */
	public static void endSplash() {
		InternalPlatform.getDefault().endSplash();
	}

	/**
	 * Removes the authorization information for the specified protection
	 * space and given authorization scheme. The protection space is defined
	 * by the given server URL and realm.
	 *
	 * @param serverUrl the URL identifying the server to remove the
	 *		authorization information for. For example,
	 *		"http://www.example.com/".
	 * @param realm the subsection of the given server to remove the
	 *		authorization information for. For example,
	 *		"realm1@example.com" or "" for no realm.
	 * @param authScheme the scheme for which the authorization information
	 *		to remove applies. For example, "Basic" or "" for no
	 *		authorization scheme.
	 * @exception CoreException if there are problems removing the
	 *		authorization information. Reasons include:
	 * <ul>
	 * <li>The keyring could not be saved.</li>
	 * </ul>
	 */
	public static void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
		InternalPlatform.getDefault().flushAuthorizationInfo(serverUrl, realm, authScheme);
	}
	/**
	 * Returns the adapter manager used for extending
	 * <code>IAdaptable</code> objects.
	 *
	 * @return the adapter manager for this platform
	 * @see IAdapterManager
	 */
	public static IAdapterManager getAdapterManager() {
		return InternalPlatform.getDefault().getAdapterManager();
	}
	/**
	 * Returns the authorization information for the specified protection
	 * space and given authorization scheme. The protection space is defined
	 * by the given server URL and realm. Returns <code>null</code> if no
	 * such information exists.
	 *
	 * @param serverUrl the URL identifying the server for the authorization
	 *		information. For example, "http://www.example.com/".
	 * @param realm the subsection of the given server to which the
	 *		authorization information applies.  For example,
	 *		"realm1@example.com" or "" for no realm.
	 * @param authScheme the scheme for which the authorization information
	 *		applies. For example, "Basic" or "" for no authorization scheme
	 * @return the authorization information for the specified protection
	 *		space and given authorization scheme, or <code>null</code> if no
	 *		such information exists
	 */
	public static Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		return InternalPlatform.getDefault().getAuthorizationInfo(serverUrl, realm, authScheme);
	}
	/**
	 * Returns the command line args provided to the platform when it was first run.
	 * Note that individual platform runnables may be provided with different arguments
	 * if they are being run individually rather than with <code>Platform.run()</code>.
	 * 
	 * @return the command line used to start the platform
	 */
	public static String[] getCommandLineArgs() {
		return InternalPlatform.getDefault().getAppArgs();
	}
	/**
	 * Returns the identified option.  <code>null</code>
	 * is returned if no such option is found.   Options are specified
	 * in the general form <i>&ltplug-in id&gt/&ltoption-path&gt</i>.  
	 * For example, <code>org.eclipse.core.runtime/debug</code>
	 *
	 * @param option the name of the option to lookup
	 * @return the value of the requested debug option or <code>null</code>
	 */
	public static String getDebugOption(String option) {
		return InternalPlatform.getDefault().getOption(option);
	}
	/**
	 * Returns the location of the platform working directory.  This 
	 * corresponds to the <i>-data</i> command line argument if
	 * present or, if not, the current working directory when the platform
	 * was started.
	 *
	 * @return the location of the platform
	 */
	public static IPath getLocation() {
		return InternalPlatform.getDefault().getLocation();
	}
	/**
	 * Returns the location of the platform log file.  This file may contain information
	 * about errors that have previously occurred during this invocation of the Platform.
	 * 
	 * Note: it is very important that users of this method do not leave the log
	 * file open for extended periods of time.  Doing so may prevent others
	 * from writing to the log file, which could result in important error messages
	 * being lost.  It is strongly recommended that clients wanting to read the
	 * log file for extended periods should copy the log file contents elsewhere,
	 * and immediately close the original file.
	 * 
	 * @return the path of the log file on disk.
	 */
	public static IPath getLogFileLocation() {
		return InternalPlatform.getDefault().getMetaArea().getLogLocation();
	}
	/**
	 * Returns the plug-in runtime object for the identified plug-in
	 * or <code>null</code> if no such plug-in can be found.  If
	 * the plug-in is defined but not yet activated, the plug-in will
	 * be activated before being returned.
	 *
	 * @param id the unique identifier of the desired plug-in 
	 *		(e.g., <code>"com.example.acme"</code>).
	 * @return the plug-in runtime object, or <code>null</code>
	 * @deprecated TODO
	 */
	public static Plugin getPlugin(String id) {
		try {
			IPluginDescriptor pd = getPluginRegistry().getPluginDescriptor(id);
			if (pd == null)
				return null;

			return pd.getPlugin();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Returns the plug-in registry for this platform.
	 *
	 * @return the plug-in registry
	 * @see IPluginRegistry
	 * @deprecated Use @link #getExtensionRegistry() getExtensionRegistry
	 */
	public static IPluginRegistry getPluginRegistry() {
		Bundle compatibility = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundle(IPlatform.PI_RUNTIME_COMPATIBILITY);
		if (compatibility == null)
			return null;
		
		Class oldInternalPlatform = null;
		try {
			oldInternalPlatform = compatibility.loadClass("org.eclipse.core.internal.plugins.InternalPlatform"); //$NON-NLS-1$
			Method getPluginRegistry = oldInternalPlatform.getMethod("getPluginRegistry", null); //$NON-NLS-1$
			return (IPluginRegistry) getPluginRegistry.invoke(oldInternalPlatform, null);
		} catch (Exception e) {
			//Ignore the exceptions, return false
		}
		return null;

	}
	/**
	 * Returns the location in the local file system of the plug-in 
	 * state area for the given plug-in.
	 * The platform must be running.
	 * <p>
	 * The plug-in state area is a file directory within the
	 * platform's metadata area where a plug-in is free to create files.
	 * The content and structure of this area is defined by the plug-in,
	 * and the particular plug-in is solely responsible for any files
	 * it puts there. It is recommended for plug-in preference settings.
	 * </p>
	 *
	 * @param plugin the plug-in whose state location is returned
	 * @return a local file system path
	 */
	public static IPath getPluginStateLocation(Plugin plugin) {
		return plugin.getStateLocation();
	}
	/**
	 * Returns the protection space (realm) for the specified resource, or
	 * <code>null</code> if the realm is unknown.
	 *
	 * @param resourceUrl the URL of the resource whose protection space is
	 *		returned. For example, "http://www.example.com/folder/".
	 * @return the protection space (realm) for the specified resource, or
	 *		<code>null</code> if the realm is unknown
	 */
	public static String getProtectionSpace(URL resourceUrl) {
		return InternalPlatform.getDefault().getProtectionSpace(resourceUrl);
	}
	/** 
	 * Removes the indicated (identical) log listener from the notification list
	 * of the platform.  If no such listener exists, no action is taken.
	 *
	 * @param listener the listener to deregister
	 * @see ILog#removeLogListener
	 * @see #addLogListener
	 */
	public static void removeLogListener(ILogListener listener) {
		InternalPlatform.getDefault().removeLogListener(listener);
	}
	/**
	 * Returns a URL which is the resolved equivalent of the
	 * supplied URL. This method is expected to be used with 
	 * plug-in-relative URLs returned by IPluginDescriptor.
	 * If the specified URL is not a plug-in-relative URL, it is returned
	 * as is. If the specified URL is a plug-in-relative URL, it is
	 * resolved to a URL using the actual URL protocol
	 * (eg. file, http, etc)
	 *
	 * @param url original plug-in-relative URL.
	 * @return the resolved URL
	 * @exception IOException if unable to resolve URL
	 * @see #asLocalURL
	 * @see IPluginDescriptor#getInstallURL
	 */
	public static URL resolve(URL url) throws java.io.IOException {
		return InternalPlatform.getDefault().resolve(url);
	}
	/**
	 * Runs the given runnable in a protected mode.   Exceptions
	 * thrown in the runnable are logged and passed to the runnable's
	 * exception handler.  Such exceptions are not rethrown by this method.
	 *
	 * @param code the runnable to run
	 */
	public static void run(ISafeRunnable runnable) {
		InternalPlatform.getDefault().run(runnable);
	}
	/**
	 * Returns the platform job manager.
	 */
	public static IJobManager getJobManager() {
		return InternalPlatform.getDefault().getJobManager();
	}
	/**
	 * Returns the extension registry for this platform.
	 *
	 * @return the extension registry
	 * @see IExtensionRegistry
	 * @since 3.0
	 */
	public static IExtensionRegistry getExtensionRegistry() {
		return InternalPlatform.getDefault().getRegistry();
	}
	/**
	 * Returns a URL for the given path in the given bundle.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * @param bundle the bundle in which to search
	 * @param file path relative to plug-in installation location 
	 * @return a URL for the given path or <code>null</code>  It is not
	 * necessary to perform a 'resolve' on this URL.
	 * @since 3.0
	 */
	
	public static URL find(Bundle bundle, IPath path) {
		return FindSupport.find(bundle, path, null);
	}
	
	/**
	 * Returns a URL for the given path in the given bundle.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * find will look for this path under the directory structure for this plugin
	 * and any of its fragments.  If this path will yield a result outside the
	 * scope of this plugin, <code>null</code> will be returned.  Note that
	 * there is no specific order to the fragments.
	 * 
	 * The following arguments may also be used
	 * 
	 *  $nl$ - for language specific information
	 *  $os$ - for operating system specific information
	 *  $ws$ - for windowing system specific information
	 * 
	 * A path of $nl$/about.properties in an environment with a default 
	 * locale of en_CA will return a URL corresponding to the first place
	 * about.properties is found according to the following order:
	 *   plugin root/nl/en/CA/about.properties
	 *   fragment1 root/nl/en/CA/about.properties
	 *   fragment2 root/nl/en/CA/about.properties
	 *   ...
	 *   plugin root/nl/en/about.properties
	 *   fragment1 root/nl/en/about.properties
	 *   fragment2 root/nl/en/about.properties
	 *   ...
	 *   plugin root/about.properties
	 *   fragment1 root/about.properties
	 *   fragment2 root/about.properties
	 *   ...
	 * 
	 * If a locale other than the default locale is desired, use an
	 * override map.
	 * 
	 * @param bundle the bundle in which to search
	 * @param path file path relative to plug-in installation location
	 * @param override map of override substitution arguments to be used for
	 * any $arg$ path elements. The map keys correspond to the substitution
	 * arguments (eg. "$nl$" or "$os$"). The resulting
	 * values must be of type java.lang.String. If the map is <code>null</code>,
	 * or does not contain the required substitution argument, the default
	 * is used.
	 * @return a URL for the given path or <code>null</code>.  It is not
	 * necessary to perform a 'resolve' on this URL.
	 * @since 3.0
	 */
	public static URL find(Bundle b, IPath path, Map override) {
		return FindSupport.find(b, path, override);
	}
	/**
	 * Returns an input stream for the specified file in the given bundle. The file path
	 * must be specified relative this the plug-in's installation location.
	 *
	 * @param bundle the bundle in which to search
	 * @param file path relative to plug-in installation location
	 * @return an input stream
	 * @see #openStream(Bundle, IPath, boolean)
	 * @since 3.0
	 */
	public static InputStream openStream(Bundle bundle, IPath file) throws IOException {
		return FindSupport.openStream(bundle, file, false);
	}
	
	/**
	 * Returns an input stream for the specified file in the given bundle. The file path
	 * must be specified relative to this plug-in's installation location.
	 * Optionally, the platform searches for the correct localized version
	 * of the specified file using the users current locale, and Java
	 * naming convention for localized resource files (locale suffix appended 
	 * to the specified file extension).
	 * <p>
	 * The caller must close the returned stream when done.
	 * </p>
	 *
	 * @param bundle the bundle in which to search
	 * @param file path relative to plug-in installation location
	 * @param localized <code>true</code> for the localized version
	 *   of the file, and <code>false</code> for the file exactly
	 *   as specified
	 * @return an input stream
	 * @since 3.0
	 */	
	public static InputStream openStream(Bundle bundle, IPath file, boolean localized) throws IOException {
		return InternalPlatform.getDefault().openStream(bundle, file, localized);		
	}
	
	/**
	 * Returns the location in the local file system of the 
	 * plug-in state area for the given bundle.
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
	 * @param bundle the bundle whose state location if returned
	 * @return a local file system path
	 * @since 3.0
	 */
	public static IPath getStateLocation(Bundle bundle) {
		return InternalPlatform.getDefault().getStateLocation(bundle);
	}
	
	/**
	 * Returns the log for the given bundle.  If no such log exists, one is created.
	 *
	 * @param bundle the bundle whose log is returned
	 * @return the log for the given bundle
	 * @since 3.0
	 */
	public static ILog getLog(Bundle bundle) {
		return InternalPlatform.getDefault().getLog(bundle);
	}
	/**
	 * Returns the given bundle's resource bundle for the current locale. 
	 * <p>
	 * The resource bundle is stored as the <code>plugin.properties</code> file 
	 * in the plug-in install directory, and contains any translatable
	 * strings used in the plug-in manifest file (<code>plugin.xml</code>)
	 * along with other resource strings used by the plug-in implementation.
	 * </p>
	 *
	 * @return the resource bundle
	 * @exception MissingResourceException if the resource bundle was not found
	 * @since 3.0
	 */
	public static ResourceBundle getResourceBundle(Bundle bundle) throws MissingResourceException {
		return InternalPlatform.getDefault().getResourceBundle(bundle);
	}
	/**
	 * Returns a resource string corresponding to the given argument value.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the default resource bundle for the given runtime bundle. If the argument does not
	 * specify a valid key, the argument itself is returned as the
	 * resource string. The key lookup is performed in the
	 * plugin.properties resource bundle. If a resource string 
	 * corresponding to the key is not found in the resource bundle
	 * the key value, or any default text following the key in the
	 * argument value is returned as the resource string.
	 * A key is identified as a string begining with the "%" character.
	 * Note, that the "%" character is stripped off prior to lookup
	 * in the resource bundle.
	 * <p>
	 * Equivalent to <code>getResourceString(bundle, value, getResourceBundle())</code>
	 * </p>
	 *
	 * @param bundle the runtime bundle 
	 * @param value the value
	 * @return the resource string
	 * @see #getResourceBundle
	 * @since 3.0
	 */
	public static  String getResourceString(Bundle bundle, String value) {
		return InternalPlatform.getDefault().getResourceString(bundle, value);
	}
	/**
	 * Returns a resource string corresponding to the given argument 
	 * value and resource bundle in the given runtime bundle.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the given resource bundle. If the argument does not
	 * specify a valid key, the argument itself is returned as the
	 * resource string. The key lookup is performed against the
	 * specified resource bundle. If a resource string 
	 * corresponding to the key is not found in the resource bundle
	 * the key value, or any default text following the key in the
	 * argument value is returned as the resource string.
	 * A key is identified as a string begining with the "%" character.
	 * Note that the "%" character is stripped off prior to lookup
	 * in the resource bundle.
	 * <p>
	 * For example, assume resource bundle plugin.properties contains
	 * name = Project Name
	 * <pre>
	 *     getResourceString("Hello World") returns "Hello World"</li>
	 *     getResourceString("%name") returns "Project Name"</li>
	 *     getResourceString("%name Hello World") returns "Project Name"</li>
	 *     getResourceString("%abcd Hello World") returns "Hello World"</li>
	 *     getResourceString("%abcd") returns "%abcd"</li>
	 *     getResourceString("%%name") returns "%name"</li>
	 * </pre>
	 * </p>
	 *
	 * @param bundle the runtime bundle
	 * @param value the value
	 * @param bundle the resource bundle
	 * @return the resource string
	 * @see #getResourceBundle
	 * @since 3.0
	 */
	public static String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle) {
		return InternalPlatform.getDefault().getResourceString(bundle, value, resourceBundle);
	}
	/**
	 * Returns the string name of the current system architecture.  
	 * The value is a user-defined string if the architecture is 
	 * specified on the command line, otherwise it is the value 
	 * returned by <code>java.lang.System.getProperty("os.arch")</code>.
	 * 
	 * @return the string name of the current system architecture
	 */
	public static String getOSArch() {
		return InternalPlatform.getDefault().getOSArch();
	}
	
	/**
	 * Returns the string name of the current locale for use in finding files
	 * whose path starts with <code>$nl$</code>.
	 *
	 * @return the string name of the current locale
	 */
	public static String getNL() {
		return InternalPlatform.getDefault().getNL();
	}
	
	/**
	 * Returns the string name of the current operating system for use in finding
	 * files whose path starts with <code>$os$</code>.  <code>OS_UNKNOWN</code> is
	 * returned if the operating system cannot be determined.  
	 * The value may indicate one of the operating systems known to the platform
	 * (as specified in <code>knownOSValues</code>) or a user-defined string if
	 * the operating system name is specified on the command line.
	 *
	 * @return the string name of the current operating system
	 * @see #knownOSValues
	 * 
	 */
	public static String getOS() {
		return InternalPlatform.getDefault().getOS();
	}
	
	/**
	 * Returns the string name of the current window system for use in finding files
	 * whose path starts with <code>$ws$</code>.  <code>null</code> is returned
	 * if the window system cannot be determined.
	 *
	 * @return the string name of the current window system or <code>null</code>
	 */
	public static String getWS() {
		return InternalPlatform.getDefault().getWS();
	}
	
	/**
	 * Returns all command line arguments specified when the running framework was started.
	 * @return the array of command line arguments.
	 */
	public static String[] getAllArgs() {
		return InternalPlatform.getDefault().getAllArgs();
	}
	
	/**
	 * Returns the arguments consumed by the framework implementation itself.  Which
	 * arguments are consumed is implementation specific.
	 * @return the array of command line arguments consumed by the framework.
	 */
	public static String[] getFrameworkArgs() {
		return InternalPlatform.getDefault().getFrameworkArgs();
	}

	/**
	 * Returns the arguments not consumed by the framework implementation itself.  Which
	 * arguments are consumed is implementation specific. These arguments are available 
	 * for use by the application.
	 * @return the array of command line arguments not consumed by the framework.
	 */
	public static String[] getApplicationArgs() {
		return InternalPlatform.getDefault().getApplicationArgs();
	}
}
