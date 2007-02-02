/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime.auth;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

// This class factors out the management of the .keyring location
public class AuthorizationHandler {
	/* package */static final String F_KEYRING = ".keyring"; //$NON-NLS-1$

	//Authorization related informations
	private static AuthorizationDatabase keyring = null;
	private static long keyringTimeStamp;
	private static String keyringFile = null;
	private static String password = ""; //$NON-NLS-1$

	/**
	 * Opens the password database (if any) initially provided to the platform at startup.
	 */
	private static void loadKeyring() throws CoreException {
		if (keyring != null && new File(keyringFile).lastModified() == keyringTimeStamp)
			return;
		if (keyringFile == null) {
			ServiceReference[] refs = null;
			try {
				refs = Activator.getContext().getServiceReferences(Location.class.getName(), Location.CONFIGURATION_FILTER);
				if (refs == null || refs.length == 0)
					return;
			} catch (InvalidSyntaxException e) {
				// ignore this.  It should never happen as we have tested the above format.
				return;
			}
			Location configurationLocation = (Location) Activator.getContext().getService(refs[0]);
			if (configurationLocation == null)
				return;
			File file = new File(configurationLocation.getURL().getPath() + "/org.eclipse.core.runtime"); //$NON-NLS-1$
			Activator.getContext().ungetService(refs[0]);
			file = new File(file, F_KEYRING);
			keyringFile = file.getAbsolutePath();
		}
		try {
			keyring = new AuthorizationDatabase(keyringFile, password);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
		if (keyring == null) {
			//try deleting the file and loading again - format may have changed
			new java.io.File(keyringFile).delete();
			keyring = new AuthorizationDatabase(keyringFile, password);
			//don't bother logging a second failure and let it flows to the callers
		}
		keyringTimeStamp = new File(keyringFile).lastModified();
	}
	
	/**
	 * Saves the keyring file to disk.
	 * @exception CoreException 
	 */
	private static void saveKeyring() throws CoreException {
		keyring.save();
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
		loadKeyring();
		keyring.addAuthorizationInfo(serverUrl, realm, authScheme, new HashMap(info));
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
		loadKeyring();
		keyring.addProtectionSpace(resourceUrl, realm);
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
		loadKeyring();
		keyring.flushAuthorizationInfo(serverUrl, realm, authScheme);
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
			loadKeyring();
			info = keyring.getAuthorizationInfo(serverUrl, realm, authScheme);
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
			loadKeyring();
		} catch (CoreException e) {
			// The error has already been logged in loadKeyring()
			return null;
		}
		return keyring.getProtectionSpace(resourceUrl);
	}

	public static void setKeyringFile(String file) {
		if (keyringFile != null)
			throw new IllegalStateException(NLS.bind(Messages.meta_keyringFileAlreadySpecified, keyringFile));
		keyringFile = file;
	}

	public static void setPassword(String keyringPassword) {
		password = keyringPassword;
	}
}
