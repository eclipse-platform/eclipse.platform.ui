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
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

public class DataArea {
	/* package */static final String F_DESCRIPTION = ".platform"; //$NON-NLS-1$	//TODO Not used
	/* package */static final String F_META_AREA = ".metadata"; //$NON-NLS-1$
	/* package */static final String F_PLUGIN_PATH = ".plugin-path"; //$NON-NLS-1$	//TODO Not used
	/* package */static final String F_PLUGIN_DATA = ".plugins"; //$NON-NLS-1$
	/* package */static final String F_LOG = ".log"; //$NON-NLS-1$
	/* package */static final String F_BACKUP = ".bak"; //$NON-NLS-1$
	/* package */static final String F_KEYRING = ".keyring"; //$NON-NLS-1$	//TODO Consider for removal
	/* package */static final String F_LOCK_FILE = ".lock"; //$NON-NLS-1$	//TODO To consider for removal
	/* package */static final String F_VERSION = "version.ini"; //$NON-NLS-1$
	/**
	 * Internal name of the preference storage file (value <code>"pref_store.ini"</code>) in this plug-in's (read-write) state area.
	 */
	/* package */static final String PREFERENCES_FILE_NAME = "pref_store.ini"; //$NON-NLS-1$

	private IPath location; //The location of the instance data
	private PlatformMetaAreaLock metaAreaLock = null;

	//Authorization related informations
	private AuthorizationDatabase keyring = null;
	private long keyringTimeStamp;
	private String keyringFile = null;
	private String password = ""; //$NON-NLS-1$
	private boolean initialized = false;

	protected void assertLocationInitialized() throws IllegalStateException {
		if (location != null && initialized)
			return;
		Location service = InternalPlatform.getDefault().getInstanceLocation();
		if (service == null)
			throw new IllegalStateException(Policy.bind("meta.noDataModeSpecified")); //$NON-NLS-1$
		try {
			URL url = service.getURL();
			if (url == null)
				throw new IllegalStateException(Policy.bind("meta.instanceDataUnspecified")); //$NON-NLS-1$
			// TODO assume the URL is a file: 
			location = new Path(url.getFile());
			initializeLocation();
		} catch (CoreException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public IPath getBackupFilePathFor(IPath file) throws IllegalStateException { //TODO Does not seems to be used
		assertLocationInitialized();
		return file.removeLastSegments(1).append(file.lastSegment() + F_BACKUP);
	}

	public IPath getMetadataLocation() throws IllegalStateException {
		assertLocationInitialized();
		return location.append(F_META_AREA);
	}

	public IPath getInstanceDataLocation() throws IllegalStateException {
		assertLocationInitialized();
		return location;
	}

	public IPath getLogLocation() throws IllegalStateException {
		return new Path(InternalPlatform.getDefault().getFrameworkLog().getFile().getAbsolutePath());
	}

	/**
	 * Returns the read/write location in which the given bundle can manage private state.
	 */
	public IPath getStateLocation(Bundle bundle) throws IllegalStateException {
		assertLocationInitialized();
		return getStateLocation(bundle.getSymbolicName());
	}

	public IPath getStateLocation(String bundleName) throws IllegalStateException {
		assertLocationInitialized();
		return getMetadataLocation().append(F_PLUGIN_DATA).append(bundleName);
	}

	/**
	 * Returns the read/write location of the file for storing plugin preferences.
	 */
	public IPath getPreferenceLocation(Bundle bundle, boolean create) throws IllegalStateException {
		assertLocationInitialized();
		return getPreferenceLocation(bundle.getSymbolicName(), create);
	}

	public IPath getPreferenceLocation(String bundleName, boolean create) throws IllegalStateException {
		IPath result = getStateLocation(bundleName);
		if (create)
			result.toFile().mkdirs();
		return result.append(PREFERENCES_FILE_NAME);
	}

	/**
	 * Return the path to the version.ini file.
	 */
	public IPath getVersionPath() throws IllegalStateException {
		assertLocationInitialized();
		return getMetadataLocation().append(F_VERSION);
	}

	private void initializeLocation() throws CoreException {
		// check if the location can be created
		if (location.toFile().exists()) {
			if (!location.toFile().isDirectory()) {
				String message = Policy.bind("meta.notDir", location.toString()); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IPlatform.FAILED_WRITE_METADATA, message, null));
			}
		}
		//try infer the device if there isn't one (windows)
		if (location.getDevice() == null)
			location = new Path(location.toFile().getAbsolutePath());
		createLocation();
		initialized = true;
	}

	private void createLocation() throws CoreException {
		// append on the metadata location so that the whole structure is created.  
		File file = location.append(F_META_AREA).toFile();
		try {
			file.mkdirs();
		} catch (Exception e) {
			String message = Policy.bind("meta.couldNotCreate", file.getAbsolutePath()); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IPlatform.FAILED_WRITE_METADATA, message, e));
		}
		if (!file.canWrite()) {
			String message = Policy.bind("meta.readonly", file.getAbsolutePath()); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IPlatform.FAILED_WRITE_METADATA, message, null));
		}
		// set the log file location now that we created the data area
		IPath path = location.append(F_META_AREA).append(F_LOG);
		try {
			InternalPlatform.getDefault().getFrameworkLog().setFile(path.toFile(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO remove this method by M8
	/**
	 * Creates a lock file in the meta-area that indicates the meta-area is in use, preventing other eclipse instances from concurrently using the same meta-area.
	 */
	public synchronized void createLockFile() throws CoreException, IllegalStateException {
		// temporary testing hook to allow the UI team to turn runtime locking off and test their locking.
		if (System.getProperty("eclipse.ui.testing") != null)
			return;
		assertLocationInitialized();
		if (System.getProperty("org.eclipse.core.runtime.ignoreLockFile") != null) //$NON-NLS-1$
			return;
		String lockLocation = getMetadataLocation().append(F_LOCK_FILE).toOSString();
		metaAreaLock = new PlatformMetaAreaLock(new File(lockLocation));
		try {
			if (!metaAreaLock.acquire()) {
				String message = Policy.bind("meta.inUse", lockLocation); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IPlatform.FAILED_WRITE_METADATA, message, null));
			}
		} catch (IOException e) {
			String message = Policy.bind("meta.failCreateLock", lockLocation); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IPlatform.FAILED_WRITE_METADATA, message, e));
		}
	}

	// TODO remove this method by M8
	/**
	 * Closes the open lock file handle, and makes a silent best attempt to delete the file.
	 */
	public synchronized void clearLockFile() throws IllegalStateException {
		// temporary testing hook to allow the UI team to turn runtime locking off and test their locking.
		if (System.getProperty("eclipse.ui.testing") != null)
			return;
		assertLocationInitialized();
		if (metaAreaLock != null)
			metaAreaLock.release();
	}

	/**
	 * @see Platform
	 */
	public void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
		loadKeyring();
		keyring.addAuthorizationInfo(serverUrl, realm, authScheme, new HashMap(info));
		keyring.save();
	}

	/**
	 * @see Platform
	 */
	public void addProtectionSpace(URL resourceUrl, String realm) throws CoreException {
		loadKeyring();
		keyring.addProtectionSpace(resourceUrl, realm);
		keyring.save();
	}

	/**
	 * @see Platform
	 */
	public void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
		loadKeyring();
		keyring.flushAuthorizationInfo(serverUrl, realm, authScheme);
		keyring.save();
	}

	/**
	 * @see Platform
	 */
	public Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		loadKeyring();
		Map info = keyring.getAuthorizationInfo(serverUrl, realm, authScheme);
		return info == null ? null : new HashMap(info);
	}

	/**
	 * @see Platform
	 */
	public String getProtectionSpace(URL resourceUrl) {
		loadKeyring();
		return keyring.getProtectionSpace(resourceUrl);
	}

	/**
	 * Opens the password database (if any) initally provided to the platform at startup.
	 */
	private void loadKeyring() {
		if (keyring != null && new File(keyringFile).lastModified() == keyringTimeStamp)
			return;
		if (keyringFile == null) {
			File file = new File(InternalPlatform.getDefault().getConfigurationLocation().getURL().getPath());
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
			try {
				keyring = new AuthorizationDatabase(keyringFile, password);
			} catch (CoreException e) {
				//don't bother logging a second failure
			}
		}
		keyringTimeStamp = new File(keyringFile).lastModified();
	}

	public void setKeyringFile(String keyringFile) {
		if (this.keyringFile != null)
			throw new IllegalStateException(Policy.bind("meta.keyringFileAlreadySpecified", this.keyringFile)); //$NON-NLS-1$
		this.keyringFile = keyringFile;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}