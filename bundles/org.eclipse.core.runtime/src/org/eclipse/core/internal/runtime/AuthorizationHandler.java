/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.lang.reflect.InvocationTargetException;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * This class factors out the management of the .keyring location, and manages
 * the case where the legacy keyring implementation is not present (i.,e 4.0 and beyond).
 * @since 3.2
 */
public class AuthorizationHandler {
	/* package */static final String F_KEYRING = ".keyring"; //$NON-NLS-1$

	//Authorization related informations
	private static long keyringTimeStamp;
	private static String password = ""; //$NON-NLS-1$
	private static String keyringFile = null;

	private static Object keyring = null;
	//reflective access to legacy authentication implementation
	private static Class authClass;
	private static boolean authNotAvailableLogged = false;

	/**
	 * Get the legacy class that implemented the authorization API. Return <code>null</code>
	 * if the legacy implementation is not present.
	 */
	private static Class getAuthClass() {
		if (authClass == null) {
			try {
				authClass = Class.forName("org.eclipse.core.internal.runtime.auth.AuthorizationDatabase"); //$NON-NLS-1$
			} catch (ClassNotFoundException e) {
				logAuthNotAvailable(e);
			}
		}
		return authClass;
	}

	private static void logAuthNotAvailable(Throwable e) {
		if (authNotAvailableLogged)
			return;
		authNotAvailableLogged = true;
		RuntimeLog.log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, Messages.auth_notAvailable, e));
	}

	/**
	 * Opens the password database (if any) initially provided to the platform at startup.
	 * Returns <code>true</code> if the authentication implementation was successfully initialized, and
	 * <code>false</code> if no authentication implementation is available.
	 */
	private static boolean loadKeyring() throws CoreException {
		if (getAuthClass() == null)
			return false;
		if (keyring != null && new File(keyringFile).lastModified() == keyringTimeStamp)
			return true;
		if (keyringFile == null) {
			ServiceReference[] refs = null;
			try {
				refs = PlatformActivator.getContext().getServiceReferences(Location.class.getName(), Location.CONFIGURATION_FILTER);
				if (refs == null || refs.length == 0)
					return true;
			} catch (InvalidSyntaxException e) {
				// ignore this.  It should never happen as we have tested the above format.
				return true;
			}
			Location configurationLocation = (Location) PlatformActivator.getContext().getService(refs[0]);
			if (configurationLocation == null)
				return true;
			File file = new File(configurationLocation.getURL().getPath() + "/org.eclipse.core.runtime"); //$NON-NLS-1$
			PlatformActivator.getContext().ungetService(refs[0]);
			file = new File(file, F_KEYRING);
			keyringFile = file.getAbsolutePath();
		}
		try {
			Constructor constructor = authClass.getConstructor(new Class[] {String.class, String.class});
			keyring = constructor.newInstance(new Object[] {keyringFile, password});
		} catch (Exception e) {
			log(e);
		}
		if (keyring == null) {
			//try deleting the file and loading again - format may have changed
			new java.io.File(keyringFile).delete();
			try {
				Constructor constructor = authClass.getConstructor(new Class[] {String.class, String.class});
				keyring = constructor.newInstance(new Object[] {keyringFile, password});
			} catch (Exception e) {
				//don't bother logging a second failure and let it flows to the callers
			}
		}
		keyringTimeStamp = new File(keyringFile).lastModified();
		return true;
	}

	/**
	 * Propagate invocation target exceptions but log anything else
	 */
	private static void log(Exception e) throws CoreException {
		if (e instanceof InvocationTargetException) {
			Throwable cause = ((InvocationTargetException) e).getTargetException();
			if (cause instanceof CoreException) {
				throw (CoreException) cause;
			}
		}
		//otherwise it is a reflective error
		logAuthNotAvailable(e);
	}

	/**
	 * Saves the keyring file to disk.
	 * @exception CoreException 
	 */
	private static void saveKeyring() throws CoreException {
		try {
			Method method = authClass.getMethod("save", new Class[0]); //$NON-NLS-1$
			method.invoke(keyring, null);
		} catch (Exception e) {
			log(e);
		}
		keyringTimeStamp = new File(keyringFile).lastModified();
	}

	/**
	 * Adds the given authorization information to the key ring. The
	 * information is relevant for the specified protection space and the
	 * given authorization scheme. The protection space is defined by the
	 * combination of the given server URL and realm. The authorization 
	 * scheme determines what the authorization information contains and how 
	 * it should be used. The authorization information is a <code>Map</code> 
	 * of <code>String</code> to <code>String</code> and typically
	 * contains information such as user names and passwords.
	 *
	 * @param serverUrl the URL identifying the server for this authorization
	 *		information. For example, "http://www.example.com/".
	 * @param realm the subsection of the given server to which this
	 *		authorization information applies.  For example,
	 *		"realm1@example.com" or "" for no realm.
	 * @param authScheme the scheme for which this authorization information
	 *		applies. For example, "Basic" or "" for no authorization scheme
	 * @param info a <code>Map</code> containing authorization information 
	 *		such as user names and passwords (key type : <code>String</code>, 
	 *		value type : <code>String</code>)
	 * @exception CoreException if there are problems setting the
	 *		authorization information. Reasons include:
	 * <ul>
	 * <li>The keyring could not be saved.</li>
	 * </ul>
	 * XXX Move to a plug-in to be defined (JAAS plugin).
	 */
	public static synchronized void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
		if (!loadKeyring())
			return;
		try {
			Method method = authClass.getMethod("addAuthorizationInfo", new Class[] {URL.class, String.class, String.class, Map.class}); //$NON-NLS-1$
			method.invoke(keyring, new Object[] {serverUrl, realm, authScheme, new HashMap(info)});
		} catch (Exception e) {
			log(e);
		}
		saveKeyring();
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
	 * <li>The key ring could not be saved.</li>
	 * </ul>
	 * XXX Move to a plug-in to be defined (JAAS plugin).
	 */
	public static synchronized void addProtectionSpace(URL resourceUrl, String realm) throws CoreException {
		if (!loadKeyring())
			return;
		try {
			Method method = authClass.getMethod("addProtectionSpace", new Class[] {URL.class, String.class}); //$NON-NLS-1$
			method.invoke(keyring, new Object[] {resourceUrl, realm});
		} catch (Exception e) {
			log(e);
		}
		saveKeyring();
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
	 * XXX Move to a plug-in to be defined (JAAS plugin).
	 */
	public static synchronized void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
		if (!loadKeyring())
			return;
		try {
			Method method = authClass.getMethod("flushAuthorizationInfo", new Class[] {URL.class, String.class, String.class}); //$NON-NLS-1$
			method.invoke(keyring, new Object[] {serverUrl, realm, authScheme});
		} catch (Exception e) {
			log(e);
		}
		saveKeyring();
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
	 *XXX Move to a plug-in to be defined (JAAS plugin).
	 */
	public static synchronized Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		Map info = null;
		try {
			if (!loadKeyring())
				return null;
			try {
				Method method = authClass.getMethod("getAuthorizationInfo", new Class[] {URL.class, String.class, String.class}); //$NON-NLS-1$
				info = (Map) method.invoke(keyring, new Object[] {serverUrl, realm, authScheme});
			} catch (Exception e) {
				log(e);
			}
		} catch (CoreException e) {
			// The error has already been logged in loadKeyring()
		}
		return info == null ? null : new HashMap(info);
	}

	/**
	 * Returns the protection space (realm) for the specified resource, or
	 * <code>null</code> if the realm is unknown.
	 *
	 * @param resourceUrl the URL of the resource whose protection space is
	 *		returned. For example, "http://www.example.com/folder/".
	 * @return the protection space (realm) for the specified resource, or
	 *		<code>null</code> if the realm is unknown
	 *	 * XXX Move to a plug-in to be defined (JAAS plugin).
	 */
	public static synchronized String getProtectionSpace(URL resourceUrl) {
		try {
			if (!loadKeyring())
				return null;
			try {
				Method method = authClass.getMethod("getProtectionSpace", new Class[] {URL.class}); //$NON-NLS-1$
				return (String)method.invoke(keyring, new Object[] {resourceUrl});
			} catch (Exception e) {
				log(e);
			}
		} catch (CoreException e) {
			// The error has already been logged in loadKeyring()
		}
		return null;
	}

	public static void setKeyringFile(String file) {
		if (keyringFile != null)
			throw new IllegalStateException(NLS.bind(Messages.auth_alreadySpecified, keyringFile));
		keyringFile = file;
	}

	public static void setPassword(String keyringPassword) {
		password = keyringPassword;
	}
}
