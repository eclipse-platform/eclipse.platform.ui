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
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.jobs.IJobManager;
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
	 * @see #MIN_PERFORMANCE
	 * @see #MAX_PERFORMANCE
	 * @since 3.0
	 */
	public static final String PREF_PLATFORM_PERFORMANCE = "runtime.performance"; //$NON-NLS-1$
	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.runtime</code>")
	 * of the Core Runtime plug-in.
	 */
	public static final String PI_RUNTIME = "org.eclipse.core.runtime"; //$NON-NLS-1$
	public static final String PI_RUNTIME_COMPATIBILITY = "org.eclipse.core.runtime.compatibility"; //$NON-NLS-1$ 
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

	public static final String PT_URLHANDLERS = "urlHandlers"; //$NON-NLS-1$

	public static final String PT_SHUTDOWN_HOOK = "applicationShutdownHook"; //$NON-NLS-1$	

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
	 * @see ILog#addLogListener
	 * @see #removeLogListener
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
	 * Returns the location of the platform working directory.  This 
	 * corresponds to the <i>-data</i> command line argument if
	 * present or, if not, the current working directory when the platform
	 * was started.
	 *
	 * @return the location of the platform
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
	 * @see ILog#removeLogListener
	 * @see #addLogListener
	 */
	public void removeLogListener(ILogListener listener);

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
	 * @return the log for the given bundle
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
	 *
	 * @return the URL indicating where the platform runtime is installed.
	 */	
	public URL getInstallURL();

	/**
	 * Returns the location in the filesystem of the configuration information 
	 * used to run this instance of Eclipse.  The configuration area typically
	 * contains the list of plug-ins available for use, various user setttings
	 * (those shared across different instances of the same configuration)
	 * and any other such data needed by plug-ins.
	 * 
	 * @return the path indicating the directory containing the configuration 
	 * metadata for this running Eclipse.
	 */
	public IPath getConfigurationMetadataLocation();
	
	/**
	 * Takes down the splash screen if one was put up.
	 */
	public void endSplash();
	
	public URL find(Bundle b, IPath path);
	
	public URL find(Bundle b, IPath path, Map override);
	
	public InputStream openStream(Bundle b, IPath file)  throws IOException;
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
	public InputStream openStream(Bundle b, IPath file, boolean localized) throws IOException;
	
	public IPath getStateLocation(Bundle bundle);
	
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
	 * @param value the value
	 * @return the resource string
	 * @see #getResourceBundle
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
	 * @param value the value
	 * @param bundle the resource bundle
	 * @return the resource string
	 * @see #getResourceBundle
	 */
	public String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle);
}
