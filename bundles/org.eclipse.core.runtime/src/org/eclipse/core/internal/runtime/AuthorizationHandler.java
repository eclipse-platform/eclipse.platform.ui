/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

// This class factors out the management of the .keyring location
public class AuthorizationHandler {
	/* package */static final String F_KEYRING = ".keyring"; //$NON-NLS-1$

	//Authorization related informations
	private static AuthorizationDatabase keyring = null;
	private static long keyringTimeStamp;
	private static String keyringFile = null;
	private static String password = ""; //$NON-NLS-1$

	/**
	 * Opens the password database (if any) initally provided to the platform at startup.
	 */
	private static void loadKeyring() throws CoreException {
		if (keyring != null && new File(keyringFile).lastModified() == keyringTimeStamp)
			return;
		if (keyringFile == null) {
			File file = new File(InternalPlatform.getDefault().getConfigurationLocation().getURL().getPath() + '/' + Platform.PI_RUNTIME);
			file = new File(file, F_KEYRING);
			keyringFile = file.getAbsolutePath();
		}
		try {
			keyring = new AuthorizationDatabase(keyringFile, password);
		} catch (CoreException e) {
			InternalPlatform.getDefault().log(e.getStatus());
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
	 * @see org.eclipse.core.runtime.Platform
	 */
	public static void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
		loadKeyring();
		keyring.addAuthorizationInfo(serverUrl, realm, authScheme, new HashMap(info));
		keyring.save();
	}

	/**
	 * @see org.eclipse.core.runtime.Platform
	 */
	public static void addProtectionSpace(URL resourceUrl, String realm) throws CoreException {
		loadKeyring();
		keyring.addProtectionSpace(resourceUrl, realm);
		keyring.save();
	}

	/**
	 * @see org.eclipse.core.runtime.Platform
	 */
	public static void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
		loadKeyring();
		keyring.flushAuthorizationInfo(serverUrl, realm, authScheme);
		keyring.save();
	}

	/**
	 * @see org.eclipse.core.runtime.Platform
	 */
	public static Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
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
	 * @see org.eclipse.core.runtime.Platform
	 */
	public static String getProtectionSpace(URL resourceUrl) {
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
			throw new IllegalStateException(Policy.bind("meta.keyringFileAlreadySpecified", keyringFile)); //$NON-NLS-1$
		keyringFile = file;
	}

	public static void setPassword(String keyringPassword) {
		password = keyringPassword;
	}
}