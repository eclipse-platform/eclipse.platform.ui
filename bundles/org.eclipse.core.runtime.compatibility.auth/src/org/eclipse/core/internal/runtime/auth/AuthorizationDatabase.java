/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime.auth;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * A database that remembers information, such as user-names and
 * passwords.  The information is stored in memory and can be saved
 * to disk in an encrypted format.  While the API is phrased in terms of
 * URLs, realms and authentication schemes, not all of these must have
 * significant values.  For example, if "realm" is not relevant to a
 * particular application, it can be left blank (though not
 * <code>null</code>).
 */
public class AuthorizationDatabase {
	public static final String PI_RUNTIME_AUTH = "org.eclipse.core.runtime.auth.compatibility"; //$NON-NLS-1$

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
	 * Version number for the format of the key-ring file.
	 */
	private static final int KEYRING_FILE_VERSION = 1;

	/**
	 * A nested hashtable that stores authorization information. The
	 * table maps server URLs to realms to authentication schemes to
	 * authorization information.
	 */
	private Hashtable authorizationInfo = new Hashtable(5);

	/**
	 * A hashtable mapping resource URLs to realms.
	 */
	private Hashtable protectionSpace = new Hashtable(5);

	private File file = null;
	private String password = null;
	private boolean needsSaving = true;

	/**
	 * Creates a new authorization database whose data cannot be saved to
	 * disk.
	 */
	public AuthorizationDatabase() {
		super();
	}

	/**
	 * Creates a new authorization database, or opens an existing one, whose
	 * data is, or can be, saved to a file with the given filename. A
	 * password must be given to create a new database and an existing
	 * database is opened by supplying the password that was given to create
	 * it.
	 *
	 * @param filename the location of the database on disk. For example,
	 *		"c:/temp/database"
	 * @param password the password to access the database. For example,
	 *		"secret"
	 * @exception CoreException if there are problems creating the database.
	 *		Reasons include:
	 * <ul>
	 * <li>The database could not be opened because the wrong password was given.
	 * <li>The database could not be opened because the specified file is corrupt.
	 * </ul>
	 */
	public AuthorizationDatabase(String filename, String password) throws CoreException {
		Assert.isNotNull(filename);
		Assert.isNotNull(password);
		this.password = password;
		file = new File(filename).getAbsoluteFile();
		load();
	}

	/**
	 * Adds the given authorization information to the database. The
	 * information is relevant for the specified protection space and the
	 * given authorization scheme. The protection space is defined by the
	 * combination of the given server URL and realm. The authorization 
	 * scheme determines what the authorization information contains and how 
	 * it should be used. The authorization information is a <code>Map</code> 
	 * of <code>String</code> to <code>String</code> and typically
	 * contain information such as usernames and passwords.
	 *
	 * @param serverUrl the URL identifying the server for this authorization
	 *		information. For example, "http://www.hostname.com/".
	 * @param realm the subsection of the given server to which this
	 *		authorization information applies.  For example,
	 *		"realm1@hostname.com" or "" for no realm.
	 * @param authScheme the scheme for which this authorization information
	 *		applies. For example, "Basic" or "" for no authorization scheme
	 * @param info a <code>Map</code> containing authorization information 
	 *		such as usernames and passwords
	 */
	public void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) {
		Assert.isNotNull(serverUrl);
		Assert.isNotNull(realm);
		Assert.isNotNull(authScheme);
		Assert.isNotNull(info);

		String url = serverUrl.toString();
		Hashtable realmToAuthScheme = (Hashtable) authorizationInfo.get(url);
		if (realmToAuthScheme == null) {
			realmToAuthScheme = new Hashtable(5);
			authorizationInfo.put(url, realmToAuthScheme);
		}

		Hashtable authSchemeToInfo = (Hashtable) realmToAuthScheme.get(realm);
		if (authSchemeToInfo == null) {
			authSchemeToInfo = new Hashtable(5);
			realmToAuthScheme.put(realm, authSchemeToInfo);
		}

		authSchemeToInfo.put(authScheme.toLowerCase(), info);
		needsSaving = true;
	}

	/**
	 * Adds the specified resource to the protection space specified by the
	 * given realm. All resources at or deeper than the depth of the last
	 * symbolic element in the path of the given resource URL are assumed to
	 * be in the same protection space.
	 *
	 * @param resourceUrl the URL identifying the resources to be added to
	 *		the specified protection space. For example,
	 *		"http://www.hostname.com/folder/".
	 * @param realm the name of the protection space. For example,
	 *		"realm1@hostname.com"
	 */
	public void addProtectionSpace(URL resourceUrl, String realm) {
		Assert.isNotNull(resourceUrl);
		Assert.isNotNull(realm);

		if (!resourceUrl.getFile().endsWith("/")) { //$NON-NLS-1$
			resourceUrl = URLTool.getParent(resourceUrl);
		}

		String oldRealm = getProtectionSpace(resourceUrl);
		if (oldRealm != null && oldRealm.equals(realm)) {
			return;
		}

		String url1 = resourceUrl.toString();
		Enumeration urls = protectionSpace.keys();
		while (urls.hasMoreElements()) {
			String url2 = (String) urls.nextElement();
			if (url1.startsWith(url2) || url2.startsWith(url1)) {
				protectionSpace.remove(url2);
				break;
			}
		}

		protectionSpace.put(url1, realm);
		needsSaving = true;
	}

	/**
	 * Removes the authorization information for the specified protection
	 * space and given authorization scheme. The protection space is defined
	 * by the given server URL and realm.
	 *
	 * @param serverUrl the URL identifying the server to remove the
	 *		authorization information for. For example,
	 *		"http://www.hostname.com/".
	 * @param realm the subsection of the given server to remove the
	 *		authorization information for. For example,
	 *		"realm1@hostname.com" or "" for no realm.
	 * @param authScheme the scheme for which the authorization information
	 *		to remove applies. For example, "Basic" or "" for no
	 *		authorization scheme.
	 */
	public void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		Hashtable realmToAuthScheme = (Hashtable) authorizationInfo.get(serverUrl.toString());

		if (realmToAuthScheme == null) {
			return;
		}

		Hashtable authSchemeToInfo = (Hashtable) realmToAuthScheme.get(realm);

		if (authSchemeToInfo == null) {
			return;
		}

		authSchemeToInfo.remove(authScheme.toLowerCase());

		needsSaving = true;
	}

	/**
	 * Returns the authorization information for the specified protection
	 * space and given authorization scheme. The protection space is defined
	 * by the given server URL and realm. Returns <code>null</code> if no
	 * such information exists.
	 *
	 * @param serverUrl the URL identifying the server for the authorization
	 *		information. For example, "http://www.hostname.com/".
	 * @param realm the subsection of the given server to which the
	 *		authorization information applies.  For example,
	 *		"realm1@hostname.com" or "" for no realm.
	 * @param authScheme the scheme for which the authorization information
	 *		applies. For example, "Basic" or "" for no authorization scheme
	 * @return the authorization information for the specified protection
	 *		space and given authorization scheme, or <code>null</code> if no
	 *		such information exists
	 */
	public Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		Hashtable realmToAuthScheme = (Hashtable) authorizationInfo.get(serverUrl.toString());
		if (realmToAuthScheme == null) {
			return null;
		}

		Hashtable authSchemeToInfo = (Hashtable) realmToAuthScheme.get(realm);
		if (authSchemeToInfo == null) {
			return null;
		}

		return (Map) authSchemeToInfo.get(authScheme.toLowerCase());
	}

	/**
	 * Returns the protection space (realm) for the specified resource, or
	 * <code>null</code> if the realm is unknown.
	 *
	 * @param resourceUrl the URL of the resource whose protection space is
	 *		returned. For example, "http://www.hostname.com/folder/".
	 * @return the protection space (realm) for the specified resource, or
	 *		<code>null</code> if the realm is unknown
	 */
	public String getProtectionSpace(URL resourceUrl) {
		while (resourceUrl != null) {
			String realm = (String) protectionSpace.get(resourceUrl.toString());
			if (realm != null) {
				return realm;
			}
			resourceUrl = URLTool.getParent(resourceUrl);
		}

		return null;
	}

	private void load() throws CoreException {
		if (file == null)
			return;
		if (!file.exists()) {
			save();
			return;
		}
		try {
			InputStream input = new FileInputStream(file);
			try {
				load(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PI_RUNTIME_AUTH, FAILED_READ_METADATA, NLS.bind(Messages.meta_unableToReadAuthorization, file), e));
		} catch (ClassNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, PI_RUNTIME_AUTH, FAILED_READ_METADATA, NLS.bind(Messages.meta_unableToReadAuthorization, file), e));
		}
	}

	private void load(InputStream is) throws IOException, ClassNotFoundException, CoreException {
		//try to read the file version number. Pre 2.0 versions had no number
		int version = is.read();
		if (version == KEYRING_FILE_VERSION) {
			//read the authorization data
			CipherInputStream cis = new CipherInputStream(is, password);
			ObjectInputStream ois = new ObjectInputStream(cis);
			try {
				authorizationInfo = (Hashtable) ois.readObject();
				protectionSpace = (Hashtable) ois.readObject();
			} finally {
				ois.close();
			}
		} else {
			//the format has changed, just log a warning
			Activator.log(new Status(IStatus.WARNING, PI_RUNTIME_AUTH, FAILED_READ_METADATA, Messages.meta_authFormatChanged, null));
			//close the stream and save a new file in the correct format
			try {
				is.close();
			} catch (IOException e) {
				//ignore failure to close
			}
			needsSaving = true;
			save();
		}
	}

	/**
	 * Saves the authorization database to disk.
	 */
	public void save() throws CoreException {
		if (!needsSaving || file == null)
			return;
		try {
			file.delete();
			if ((!file.getParentFile().exists() && !file.getParentFile().mkdirs()) || !canWrite(file.getParentFile()))
				throw new CoreException(new Status(IStatus.ERROR, PI_RUNTIME_AUTH, FAILED_WRITE_METADATA, NLS.bind(Messages.meta_unableToWriteAuthorization, file), null));
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			try {
				save(out);
			} finally {
				out.close();
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PI_RUNTIME_AUTH, FAILED_WRITE_METADATA, NLS.bind(Messages.meta_unableToWriteAuthorization, file), e));
		}
		needsSaving = false;
	}

	private static boolean canWrite(File installDir) {
		if (!installDir.canWrite())
			return false;

		if (!installDir.isDirectory())
			return false;

		File fileTest = null;
		try {
			fileTest = File.createTempFile("writtableArea", null, installDir); //$NON-NLS-1$
		} catch (IOException e) {
			// If an exception occurred while trying to create the file, it means that it is not writable
			return false;
		} finally {
			if (fileTest != null)
				fileTest.delete();
		}
		return true;
	}

	private void save(FileOutputStream os) throws IOException {
		//write the version number
		os.write(KEYRING_FILE_VERSION);

		CipherOutputStream cos = new CipherOutputStream(os, password);
		ObjectOutputStream oos = new ObjectOutputStream(cos);
		//write the data
		try {
			oos.writeObject(authorizationInfo);
			oos.writeObject(protectionSpace);
			os.flush();
			os.getFD().sync();
		} finally {
			oos.close();
		}
	}

	/**
	 * Sets the password to use for accessing this database.  If the database
	 * is subsequently saved, this new password is used.
	 */
	public boolean setPassword(String oldValue, String newValue) {
		if (!oldValue.equals(password))
			return false;
		password = newValue;
		needsSaving = true;
		return true;
	}
}
