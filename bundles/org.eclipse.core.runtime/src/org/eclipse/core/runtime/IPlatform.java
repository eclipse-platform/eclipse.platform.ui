/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;

// TODO clarify the javadoc below.  Copy the signatures from Platform.
// talk to jeem about the best way to do the triplication
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
 * <p>
 * <b>Note</b>: This is an early access API to the new OSGI-based Eclipse 3.0
 * Platform Runtime. Because the APIs for the new runtime have not yet been fully
 * stabilized, they should only be used by clients needing to take particular
 * advantage of new OSGI-specific functionality, and only then with the understanding
 * that these APIs may well change in incompatible ways until they reach
 * their finished, stable form (post-3.0). </p>
 * 
 * @since 3.0
 */
public interface IPlatform {
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
	 * @see Platform#MIN_PERFORMANCE
	 * @see Platform#MAX_PERFORMANCE
	 * @since 3.0
	 */
	public static final String PREF_PLATFORM_PERFORMANCE = "runtime.performance"; //$NON-NLS-1$
	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.runtime</code>")
	 * of the Core Runtime plug-in.
	 */
	public static final String PI_RUNTIME = "org.eclipse.core.runtime"; //$NON-NLS-1$
	/** 
	 * The simple identifier constant (value "<code>applications</code>") of
	 * the extension point of the Core Runtime plug-in where plug-ins declare
	 * the existence of runnable applications. A plug-in may define any
	 * number of applications; however, the platform is only capable
	 * of running one application at a time.
	 */
	public static final String PT_APPLICATIONS = "applications"; //$NON-NLS-1$

	/** 
	 * The simple identifier constant (value "<code>products</code>") of
	 * the extension point of the Core Runtime plug-in where plug-ins declare
	 * the existence of a product. A plug-in may define any
	 * number of products; however, the platform is only capable
	 * of running one product at a time.
	 * 
	 * @see org.eclipse.core.runtime.Platform#getProduct()
	 * @since 3.0
	 */
	public static final String PT_PRODUCT = "products"; //$NON-NLS-1$
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
	public void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException;

	/** 
	 * Adds the given log listener to the notification list of the platform.
	 * <p>
	 * Once registered, a listener starts receiving notification as entries
	 * are added to plug-in logs via <code>ILog.log()</code>. The listener continues to
	 * receive notifications until it is replaced or removed.
	 * </p>
	 *
	 * @param listener the listener to register
	 * @see ILog#addLogListener(ILogListener)
	 * @see #removeLogListener(ILogListener)
	 */
	public void addLogListener(ILogListener listener);

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
	public void addProtectionSpace(URL resourceUrl, String realm) throws CoreException;

	/**
	 * Returns a URL which is the local equivalent of the
	 * supplied URL. This method is expected to be used with the
	 * plug-in-relative URLs returned by IPluginDescriptor, Bundle.getEntry()
	 * and Platform.find().
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
	 * @see #resolve(URL), #find
	 * @see IPluginDescriptor#getInstallURL()
	 * @see Bundle#getEntry(String)
	 */
	public URL asLocalURL(URL url) throws IOException;

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
	public void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException;

	/**
	 * Returns the adapter manager used for extending
	 * <code>IAdaptable</code> objects.
	 *
	 * @return the adapter manager for this platform
	 * @see IAdapterManager
	 */
	public IAdapterManager getAdapterManager();

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
	public Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme);

	/**
	 * Returns the location of the platform working directory. 
	 *
	 * @return the location of the platform's working directory
	 * @see #getInstallLocation()
	 */
	public IPath getLocation();

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
	public IPath getLogFileLocation();

	/**
	 * Returns the protection space (realm) for the specified resource, or
	 * <code>null</code> if the realm is unknown.
	 *
	 * @param resourceUrl the URL of the resource whose protection space is
	 *		returned. For example, "http://www.example.com/folder/".
	 * @return the protection space (realm) for the specified resource, or
	 *		<code>null</code> if the realm is unknown
	 */
	public String getProtectionSpace(URL resourceUrl);

	/** 
	 * Removes the indicated (identical) log listener from the notification list
	 * of the platform.  If no such listener exists, no action is taken.
	 *
	 * @param listener the listener to deregister
	 * @see ILog#removeLogListener(ILogListener)
	 * @see #addLogListener(ILogListener)
	 */
	public void removeLogListener(ILogListener listener);

	/**
	 * Returns a URL which is the resolved equivalent of the
	 * supplied URL. This method is expected to be used with the
	 * plug-in-relative URLs returned by IPluginDescriptor, Bundle.getEntry()
	 * and Platform.find().
	 * <p>
	 * If the specified URL is not a plug-in-relative URL, it is returned
	 * as is. If the specified URL is a plug-in-relative URL, this method
	 * attempts to reduce the given URL to one which is native to the Java
	 * class library (eg. file, http, etc). 
	 * </p><p>
	 * Note however that users of this API should not assume too much about the
	 * results of this method.  While it may consistently return a file: URL in certain
	 * installation configurations, others may result in jar: or http: URLs.
	 * </p>
	 * @param url original plug-in-relative URL.
	 * @return the resolved URL
	 * @exception IOException if unable to resolve URL
	 * @see #asLocalURL(URL), #find
	 * @see IPluginDescriptor#getInstallURL()
	 * @see Bundle#getEntry(String)
	 */
	public URL resolve(URL url) throws IOException;

	/**
	 * Runs the given runnable in a protected mode.   Exceptions
	 * thrown in the runnable are logged and passed to the runnable's
	 * exception handler.  Such exceptions are not rethrown by this method.
	 *
	 * @param code the runnable to run
	 */
	public void run(ISafeRunnable code);

	/**
	 * Returns the log for the given bundle.  If no such log exists, one is created.
	 *
	 * @param bundle the bundle whose log is being queried
	 * @return the log for the given bundle
	 * @since 3.0
	 */
	public ILog getLog(Bundle bundle);

	/**
	 * Returns the platform job manager.
	 *  
	 * @return the job manager
	 */
	public IJobManager getJobManager();

	/**
	 * Returns URL at which the Platform runtime executables and libraries are installed.
	 * The returned value is distinct from the location of any given platform's data.
	 * This is superceded by <code>getInstallLocation().getURL()</code>
	 *
	 * @return the URL indicating where the platform runtime is installed.
	 * @see #getInstallLocation()
	 */
	public URL getInstallURL();

	/**
	 * Returns the location of the base installation for the running platform
	 * <code>null</code> is returned if the platform is running without a configuration location.
	 * <p>
	 * This method is equivalent to acquiring the <code>org.eclipse.osgi.service.datalocation.Location</code>
	 * service with the property "type" = "osgi.install.area".
	 *</p>
	 * @return the location of the platform's installation area or <code>null</code> if none
	 */
	public Location getInstallLocation();

	/**
	 * Returns the location of the configuration information 
	 * used to run this instance of Eclipse.  The configuration area typically
	 * contains the list of plug-ins available for use, various setttings
	 * (those shared across different instances of the same configuration)
	 * and any other such data needed by plug-ins.
	 * <code>null</code> is returned if the platform is running without a configuration location.
	 * <p>
	 * This method is equivalent to acquiring the <code>org.eclipse.osgi.service.datalocation.Location</code>
	 * service with the property "type" = "osgi.configuration.area".
	 *</p>
	 * @return the location of the platform's configuration data area or <code>null</code> if none
	 * @since 3.0
	 */
	public Location getConfigurationLocation();

	/**
	 * Takes down the splash screen if one was put up.
	 */
	public void endSplash();

	/**
	 * Returns a URL for the given path in the given bundle.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * <p>
	 * <b>Note</b>: This is an early access API to the new OSGI-based Eclipse 3.0
	 * Platform Runtime. Because the APIs for the new runtime have not yet been fully
	 * stabilized, they should only be used by clients needing to take particular
	 * advantage of new OSGI-specific functionality, and only then with the understanding
	 * that these APIs may well change in incompatible ways until they reach
	 * their finished, stable form (post-3.0). </p>
	 * 
	 * @param bundle the bundle in which to search
	 * @param file path relative to plug-in installation location 
	 * @return a URL for the given path or <code>null</code>  It is not
	 * necessary to perform a 'resolve' on this URL.
	 * @since 3.0
	 */
	public URL find(Bundle bundle, IPath file);

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
	 * <p>
	 * <b>Note</b>: This is an early access API to the new OSGI-based Eclipse 3.0
	 * Platform Runtime. Because the APIs for the new runtime have not yet been fully
	 * stabilized, they should only be used by clients needing to take particular
	 * advantage of new OSGI-specific functionality, and only then with the understanding
	 * that these APIs may well change in incompatible ways until they reach
	 * their finished, stable form (post-3.0). </p>
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
	public URL find(Bundle bundle, IPath path, Map override);

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
	public IPath getStateLocation(Bundle bundle);

	/**
	 * Returns the given bundle's resource bundle for the current locale. 
	 * <p>
	 * The resource bundle is stored as the <code>plugin.properties</code> file 
	 * in the plug-in install directory, and contains any translatable
	 * strings used in the plug-in manifest file (<code>plugin.xml</code>)
	 * along with other resource strings used by the plug-in implementation.
	 * </p>
	 *
	 * @param bundle the bundle whose resource bundle is being queried
	 * @return the resource bundle
	 * @exception MissingResourceException if the resource bundle was not found
	 * @since 3.0
	 */
	public ResourceBundle getResourceBundle(Bundle bundle) throws MissingResourceException;

	/**
	 * Returns a resource string corresponding to the given argument value.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the default resource bundle. If the argument does not
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
	 * Equivalent to <code>getResourceString(value, getResourceBundle())</code>
	 * </p>
	 *
	 * @param bundle the bundle whose resource bundle is being queried
	 * @param value the value
	 * @return the resource string
	 * @see #getResourceBundle(Bundle)
	 */
	public String getResourceString(Bundle bundle, String value);

	/**
	 * Returns a resource string corresponding to the given argument 
	 * value and bundle.
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
	 * @param bundle the resource bundle
	 * @param value the value to look for
	 * @param resourceBundle the resource bundle to query
	 * @return the resource string
	 * @see #getResourceBundle(Bundle)
	 */
	public String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle);

	/**
	 * Returns the string name of the current system architecture.  
	 * The value is a user-defined string if the architecture is 
	 * specified on the command line, otherwise it is the value 
	 * returned by <code>java.lang.System.getProperty("os.arch")</code>.
	 * 
	 * @return the string name of the current system architecture
	 */
	public String getOSArch();

	/**
	 * Returns the string name of the current locale for use in finding files
	 * whose path starts with <code>$nl$</code>.
	 *
	 * @return the string name of the current locale
	 */
	public String getNL();

	/**
	 * Returns the string name of the current operating system for use in finding
	 * files whose path starts with <code>$os$</code>.  <code>OS_UNKNOWN</code> is
	 * returned if the operating system cannot be determined.  
	 * The value may indicate one of the operating systems known to the platform
	 * (as specified in <code>knownOSValues</code>) or a user-defined string if
	 * the operating system name is specified on the command line.
	 *
	 * @return the string name of the current operating system
	 */
	public String getOS();

	/**
	 * Returns the string name of the current window system for use in finding files
	 * whose path starts with <code>$ws$</code>.  <code>null</code> is returned
	 * if the window system cannot be determined.
	 *
	 * @return the string name of the current window system or <code>null</code>
	 */
	public String getWS();

	/**
	 * Returns the arguments not consumed by the framework implementation itself.  Which
	 * arguments are consumed is implementation specific. These arguments are available 
	 * for use by the application.
	 * @return the array of command line arguments not consumed by the framework.
	 */
	public String[] getApplicationArgs();

	/**
	 * Returns the platform administrator for this running Eclipse.  
	 * 
	 * @return the platform admin for this instance of Eclipse
	 */
	public PlatformAdmin getPlatformAdmin();

	/**
	 * Returns the currently registered bundle group providers
	 * @return the currently registered bundle group providers
	 */
	public IBundleGroupProvider[] getBundleGroupProviders();

	/**
	 * Return the currently registered preferences service.
	 * 
	 * @return the preferences service
	 */
	public IPreferencesService getPreferencesService();

	/**
	 * Returns the product which was selected when running this Eclipse instance
	 * or <code>null</code> if none
	 * @return the current product or null if none
	 */
	public IProduct getProduct();

	/**
	 * Registers the given bundle group provider with the platform
	 * @param provider a provider to register
	 * @since 3.0
	 */
	public void registerBundleGroupProvider(IBundleGroupProvider provider);

	/**
	 * Deregisters the given bundle group provider with the platform
	 * @param provider a provider to deregister
	 */
	public void unregisterBundleGroupProvider(IBundleGroupProvider provider);

	/**
	 * Returns the location of the platform's user data area.  The user data area is a location on the system
	 * which is specific to the system's current user.  By default it is located relative to the 
	 * location given by the System property "user.home".  
	 * <code>null</code> is returned if the platform is running without an user location.
	 * <p>
	 * This method is equivalent to acquiring the <code>org.eclipse.osgi.service.datalocation.Location</code>
	 * service with the property "type" = "osgi.user.area".
	 *</p>
	 * @return the location of the platform's user data area or <code>null</code> if none
	 */
	public Location getUserLocation();

	/**
	 * Returns the location of the platform's working directory (also known as the instance data area).  
	 * <code>null</code> is returned if the platform is running without an instance location.
	 * <p>
	 * This method is equivalent to acquiring the <code>org.eclipse.osgi.service.datalocation.Location</code>
	 * service with the property "type" = "osgi.instance.area".
	 *</p>
	 * @return the location of the platform's instance data area or <code>null</code> if none
	 */
	public Location getInstanceLocation();

	/**
	 * Checks if the specified bundle is a fragment bundle.
	 * 
	 * @param bundle the bundle to query
	 * @return true if the specified bundle is a fragment bundle; otherwise false is returned.
	 */
	public boolean isFragment(Bundle bundle);

	/**
	 * Returns an array of host bundles that the specified fragment bundle is 
	 * attached to or <tt>null</tt> if the specified bundle is not attached to a host.  
	 * If the bundle is not a fragment bundle then <tt>null</tt> is returned.
	 * 
	 * @param bundle the bundle to get the host bundles for.
	 * @return an array of host bundles or null if the bundle does not have any
	 * host bundles.
	 */
	public Bundle[] getHosts(Bundle bundle);

	/**
	 * Returns an array of attached fragment bundles for the specified bundle.  If the 
	 * specified bundle is a fragment then <tt>null</tt> is returned.  If no fragments are 
	 * attached to the specified bundle then <tt>null</tt> is returned.
	 * 
	 * @param bundle the bundle to get the attached fragment bundles for.
	 * @return an array of fragment bundles or <tt>null</tt> if the bundle does not 
	 * have any attached fragment bundles. 
	 */
	public Bundle[] getFragments(Bundle bundle);

	/**
	 * Returns the resolved bundle with the specified symbolic name that has the
	 * highest version.  If no resolved bundles are installed that have the 
	 * specified symbolic name then null is returned.
	 * 
	 * @param symbolicName the symbolic name of the bundle to be returned.
	 * @return the bundle that has the specified symbolic name with the 
	 * highest version, or <tt>null</tt> if no bundle is found.
	 */
	public Bundle getBundle(String symbolicName);

	/**
     * Gets the Bundles with the specified symbolic name.  If no bundles are 
     * resolved that have the specified symbolic name then null is returned.  
     * If the version argument is not null then only the Bundles that have 
     * the specified symbolic name and a version greater than or equal to the 
     * specified version are returned. The returned bundles are ordered in 
     * descending bundle version order.
     * 
     * @param symbolicName the symbolic name of the bundles that are to be returned.
     * @param version the version that the return bundle versions must match, 
     * or <tt>null</tt> if no version matching is to be done. 
     * @return the array of Bundles with the specified name that match the 
     * specified version and match rule, or <tt>null</tt> if no bundles are found.
     */
	public Bundle[] getBundles(String symbolicName, String version);
	/**
	 * Returns the content type manager.
	 * 
	 * @return the content type manager
	 */
	public IContentTypeManager getContentTypeManager();

	/**
	 * Returns whether the platform is running.
	 *
	 * @return <code>true</code> if the platform is running, 
	 *		and <code>false</code> otherwise
	 */
	public boolean isRunning();
}