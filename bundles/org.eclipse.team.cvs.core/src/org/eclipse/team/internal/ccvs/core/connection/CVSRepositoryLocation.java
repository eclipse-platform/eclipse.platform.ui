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
package org.eclipse.team.internal.ccvs.core.connection;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IUserAuthenticator;
import org.eclipse.team.internal.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.resources.RemoteModule;

/**
 * This class manages a CVS repository location.
 * 
 * It provides the mapping between connection method name and the
 * plugged in ICunnectionMethod.
 * 
 * It parses location strings into instances.
 * 
 * It provides a method to open a connection to the server along
 * with a method to validate that connections can be made.
 * 
 * It manages its user info using the plugged in IUserAuthenticator
 * (unless a username and password are provided as part of the creation
 * string, in which case, no authenticator is used).
 * 
 * Instances must be disposed of when no longer needed in order to 
 * notify the authenticator so cached properties can be cleared
 * 
 */
public class CVSRepositoryLocation extends PlatformObject implements ICVSRepositoryLocation, IUserInfo {

	// server platform constants
	public static final int UNDETERMINED_PLATFORM = 0;
	public static final int CVS_SERVER = 1;
	public static final int CVSNT_SERVER = 2;
	public static final int UNSUPPORTED_SERVER = 3;
	public static final int UNKNOWN_SERVER = 4;
	
	// static variables for extension points
	private static IUserAuthenticator authenticator;
	private static IConnectionMethod[] pluggedInConnectionMethods = null;

	private IConnectionMethod method;
	private String user;
	private String password;
	private String host;
	private int port;
	private String root;
	private boolean userFixed;
	private boolean passwordFixed;
	private int serverPlatform = UNDETERMINED_PLATFORM;
	private String remoteCVSProgramName = DEFAULT_REMOTE_CVS_PROGRAM_NAME;
	
	public static final char COLON = ':';
	public static final char HOST_SEPARATOR = '@';
	public static final char PORT_SEPARATOR = '#';
	public static final boolean STANDALONE_MODE = (System.getProperty("eclipse.cvs.standalone")==null) ? //$NON-NLS-1$ 
		false	:(new Boolean(System.getProperty("eclipse.cvs.standalone")).booleanValue()); //$NON-NLS-1$ 
	public static final String DEFAULT_REMOTE_CVS_PROGRAM_NAME = "cvs"; //$NON-NLS-1$
	
	// command to start remote cvs in server mode
	private static final String INVOKE_SVR_CMD = "server"; //$NON-NLS-1$
	
	// fields needed for caching the password
	public static final String INFO_PASSWORD = "org.eclipse.team.cvs.core.password";//$NON-NLS-1$ 
	public static final String INFO_USERNAME = "org.eclipse.team.cvs.core.username";//$NON-NLS-1$ 
	public static final String AUTH_SCHEME = "";//$NON-NLS-1$ 
	public static final URL FAKE_URL;

	public static final String USER_VARIABLE = "{user}"; //$NON-NLS-1$
	public static final String PASSWORD_VARIABLE = "{password}"; //$NON-NLS-1$
	public static final String HOST_VARIABLE = "{host}"; //$NON-NLS-1$
	public static final String PORT_VARIABLE = "{port}"; //$NON-NLS-1$
	
	static {
		URL temp = null;
		try {
			temp = new URL("http://org.eclipse.team.cvs.core");//$NON-NLS-1$ 
		} catch (MalformedURLException e) {
		}
		FAKE_URL = temp;
	} 
	
	/*
	 * Create a CVSRepositoryLocation from its composite parts.
	 */
	private CVSRepositoryLocation(IConnectionMethod method, String user, String password, String host, int port, String root, boolean userFixed, boolean passwordFixed) {
		this.method = method;
		this.user = user;
		this.password = password;
		this.host = host;
		this.port = port;
		this.root = root;
		// The username can be fixed only if one is provided
		if (userFixed && (user != null))
			this.userFixed = true;
		// The password can only be fixed if the username is and a password is provided
		if (userFixed && passwordFixed && (password != null))
			this.passwordFixed = true;
	}
	
	/*
	 * Create the connection to the remote server.
	 * If anything fails, an exception will be thrown and must
	 * be handled by the caller.
	 */
	private Connection createConnection(String password, IProgressMonitor monitor) throws CVSException {
		// FIXME Should the open() of Connection be done in the constructor?
		// The only reason it should is if connections can be reused (they aren't reused now).
		// FIXME! monitor is unused
		Connection connection = new Connection(this, method.createConnection(this, password));
		connection.open(monitor);
		return connection;
	}
	
	/*
	 * Dispose of the receiver by clearing any cached authorization information.
	 * This method shold only be invoked when the corresponding adapter is shut
	 * down or a connection is being validated.
	 */
	public void dispose() throws CVSException {
		flushCache();
	}
	
	/*
	 * Flush the keyring entry associated with the receiver
	 */
	private void flushCache() throws CVSException {
		try {
			Platform.flushAuthorizationInfo(FAKE_URL, getLocation(), AUTH_SCHEME);
		} catch (CoreException e) {
			// We should probably wrap the CoreException here!
			CVSProviderPlugin.log(e);
			throw new CVSException(IStatus.ERROR, IStatus.ERROR, Policy.bind("CVSRepositoryLocation.errorFlushing", getLocation()), e);//$NON-NLS-1$ 
		}
	}
	
	/*
	 * @see ICVSRepositoryLocation#getHost()
	 */
	public String getHost() {
		return host;
	}

	/*
	 * @see IRepositoryLocation#getLocation()
	 * 
	 * The username is included if it is fixed.
	 * The password is never included even if it is fixed.
	 * The port is included if it is not the default port.
	 */
	public String getLocation() {
		return COLON + method.getName() + COLON + 
			(userFixed?(user +
				(passwordFixed?(COLON + password):"")//$NON-NLS-1$ 
					+ HOST_SEPARATOR):"") +//$NON-NLS-1$ 
			host + COLON +
			((port == USE_DEFAULT_PORT)?"":(new Integer(port).toString())) + //$NON-NLS-1$ 
			root;
	}
	
	/*
	 * @see ICVSRepositoryLocation#getMethod()
	 */
	public IConnectionMethod getMethod() {
		return method;
	}
	
	public boolean setMethod(String methodName) {
		IConnectionMethod newMethod = getPluggedInConnectionMethod(methodName);
		if (newMethod == null)
			return false;
		method = newMethod;
		return true;
	}

	/*
	 * @see ICVSRepositoryLocation#getPort()
	 */
	public int getPort() {
		return port;
	}
	
	/*
	 * @see ICVSRepositoryLocation#members(CVSTag, boolean, IProgressMonitor)
	 */
	public ICVSRemoteResource[] members(CVSTag tag, boolean modules, IProgressMonitor progress) throws CVSException {
		try {
			if (modules) {
				return RemoteModule.getRemoteModules(this, tag, progress);
			} else {
				RemoteFolder root = new RemoteFolder(null, this, "", tag); //$NON-NLS-1$
				ICVSRemoteResource[] resources = (ICVSRemoteResource[])root.members(progress);
				// There is the off chance that there is a file in the root of the repository.
				// This is not supported by cvs so we need to make sure there are no files
				List folders = new ArrayList(resources.length);
				for (int i = 0; i < resources.length; i++) {
					ICVSRemoteResource remoteResource = resources[i];
					if (remoteResource.isContainer()) {
						folders.add(remoteResource);
					}
				}
				return (ICVSRemoteResource[]) folders.toArray(new ICVSRemoteResource[folders.size()]);
			}
		} catch(TeamException e) {
			throw new CVSException(e.getStatus());
		}
	}
	
	/*
	 * @see ICVSRepositoryLocation#getRemoteFolder(String, CVSTag)
	 */
	public ICVSRemoteFolder getRemoteFolder(String remotePath, CVSTag tag) {
		return new RemoteFolder(null, this, remotePath, tag);		
	}
	
	/*
	 * @see ICVSRepositoryLocation#getRemoteFile(String, CVSTag)
	 */
	public ICVSRemoteFile getRemoteFile(String remotePath, CVSTag tag) {
		IPath path = new Path(remotePath);
		RemoteFolderTree remoteFolder = new RemoteFolderTree(null, this, path.removeLastSegments(1).toString(), tag);
		RemoteFile remoteFile = new RemoteFile(remoteFolder, Update.STATE_ADDED_LOCAL, path.lastSegment(), tag);
		remoteFolder.setChildren(new ICVSRemoteResource[] { remoteFile });
		return remoteFile;
	}
	
	/*
	 * @see ICVSRepositoryLocation#getRootDirectory()
	 */
	public String getRootDirectory() {
		return root;
	}
	
	/*
	 * @see ICVSRepositoryLocation#getTimeout()
	 * 
	 * For the time being, the timeout value is a system wide value
	 * associated with the CVSPlugin singleton.
	 */
	public int getTimeout() {
		return CVSProviderPlugin.getPlugin().getTimeout();
	}
	
	/*
	 * @see ICVSRepositoryLocation#getUserInfo()
	 */
	public IUserInfo getUserInfo(boolean makeUsernameMutable) {
		return new UserInfo(getUsername(), password, makeUsernameMutable ? true : isUsernameMutable());
	}
	
	/*
	 * @see ICVSRepositoryLocation#getUsername()
	 * @see IUserInfo#getUsername()
	 */
	public String getUsername() {
		// If the username is mutable, get it from the cache if it's there
		if (user == null && isUsernameMutable()) {
			try {
				retrievePassword();
			} catch (CVSException e) {
			}
		}
		return user == null ? "" : user; //$NON-NLS-1$
	}
	
	/*
	 * @see IUserInfo#isUsernameMutable()
	 */
	public boolean isUsernameMutable() {
		return !userFixed;
	}

	/*
	 * Open a connection to the repository represented by the receiver.
	 * If the username or password are not fixed, openConnection will
	 * use the plugged-in authenticator to prompt for the username and/or
	 * password if one has not previously been provided or if the previously
	 * supplied username and password are invalid.
	 */
	public Connection openConnection(IProgressMonitor monitor) throws CVSException {
		
		try {
			// Allow two ticks in case of a retry
			monitor.beginTask(Policy.bind("CVSRepositoryLocation.openingConnection", getHost()), 2);//$NON-NLS-1$
			
			// If we have a username and password, use them to attempt a connection
			if ((user != null) && (password != null)) {
				return createConnection(password, monitor);
			}
			
			// Get the repository in order to ensure that the location is known by CVS.
			// (The get will record the location if it's not already recorded.
			CVSProviderPlugin.getPlugin().getRepository(getLocation());
			
			while (true) {
				try {
					// The following will throw an exception if authentication fails
					String password = retrievePassword();
					if (user == null) {
						// This is possible if the cache was cleared somehow for a location with a mutable username
						throw new CVSAuthenticationException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.usernameRequired"))); //$NON-NLS-1$
					}
					if (password == null)
						password = "";//$NON-NLS-1$ 
					return createConnection(password, monitor);
				} catch (CVSAuthenticationException ex) {
					String message = ex.getMessage();
					try {
						IUserAuthenticator authenticator = getAuthenticator();
						if (authenticator == null) {
							throw new CVSAuthenticationException(getLocation(), Policy.bind("Client.noAuthenticator"));//$NON-NLS-1$ 
						}
						authenticator.promptForUserInfo(this, this, message);
						updateCache();
					} catch (OperationCanceledException e) {
						throw new CVSAuthenticationException(new CVSStatus(CVSStatus.ERROR, message));
					}
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * Implementation of inherited toString()
	 */
	public String toString() {
		return getLocation();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof CVSRepositoryLocation)) return false;
		return getLocation().equals(((CVSRepositoryLocation)o).getLocation());
	}
	public int hashCode() {
		return getLocation().hashCode();
	}
	
	/*
	 * Return the cached password from the keyring. 
	 * Also, set the username of the receiver if the username is mutable
	 */
	private String retrievePassword() throws CVSException {
		Map map = Platform.getAuthorizationInfo(FAKE_URL, getLocation(), AUTH_SCHEME);
		if (map != null) {
			String username = (String) map.get(INFO_USERNAME);
			if (username != null && isUsernameMutable())
				setUsername(username);
			String password = (String) map.get(INFO_PASSWORD);
			if (password != null) {
				return password;
			}
		}
		return null;
	}
	/*
	 * @see IUserInfo#setPassword(String)
	 */
	public void setPassword(String password) {
		if (passwordFixed)
			throw new UnsupportedOperationException();
		// We set the password here but it will be cleared 
		// if the user info is cached using updateCache()
		this.password = password;
	}
	
	public void setUserInfo(IUserInfo userinfo) {
		user = userinfo.getUsername();
		password = ((UserInfo)userinfo).getPassword();
	}
	/*
	 * @see IUserInfo#setUsername(String)
	 */
	public void setUsername(String user) {
		if (userFixed)
			throw new UnsupportedOperationException();
		this.user = user;
	}
	
	public void setUserMuteable(boolean muteable) {
		userFixed = !muteable;
	}
	
	public void updateCache() throws CVSException {
		if (passwordFixed)
			return;
		updateCache(user, password, true);
		password = null;
		// Ensure that the receiver is known by the CVS provider
		CVSProviderPlugin.getPlugin().getRepository(getLocation());
	}
	
	/*
	 * Cache the user info in the keyring
	 */
	private void updateCache(String username, String password, boolean createIfAbsent) throws CVSException {
		// put the password into the Platform map
		Map map = Platform.getAuthorizationInfo(FAKE_URL, getLocation(), AUTH_SCHEME);
		if (map == null) {
			if ( ! createIfAbsent) return;
			map = new java.util.HashMap(10);
		}
		if (username != null)
			map.put(INFO_USERNAME, username);
		if (password != null)
			map.put(INFO_PASSWORD, password);
		try {
			Platform.addAuthorizationInfo(FAKE_URL, getLocation(), AUTH_SCHEME, map);
		} catch (CoreException e) {
			// We should probably wrap the CoreException here!
			CVSProviderPlugin.log(e);
			throw new CVSException(IStatus.ERROR, IStatus.ERROR, Policy.bind("CVSRepositoryLocation.errorCaching", getLocation()), e);//$NON-NLS-1$ 
		}
	}
	
	/*
	 * Validate that the receiver contains valid information for
	 * making a connection. If the receiver contains valid
	 * information, the method returns. Otherwise, an exception
	 * indicating the problem is throw.
	 */
	public void validateConnection(IProgressMonitor monitor) throws CVSException {
		try {
			ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot());
			Session.run(this, root, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					IStatus status = Command.VERSION.execute(null, CVSRepositoryLocation.this, monitor);
					// Log any non-ok status
					if (! status.isOK()) {
						CVSProviderPlugin.log(status);
					}
					if (getServerPlatform() == CVSNT_SERVER) {
						// check for the use of a repository prefix
						if (getRootDirectory().startsWith(Session.SERVER_SEPARATOR)) {
							// A prefix is in use. Log a warning
							CVSProviderPlugin.log(IStatus.WARNING, Policy.bind("CVSRepositoryLocation.cvsntPrefix", getLocation()), null); //$NON-NLS-1$
							throw new CVSAuthenticationException(new Status(IStatus.WARNING, CVSProviderPlugin.ID, 0,
								Policy.bind("CVSRepositoryLocation.cvsntPrefix", getLocation()), null)); //$NON-NLS-1$
						}
					}
				}
			}, monitor);
		} catch (CVSException e) {
			// If the validation failed, dispose of any cached info
			dispose();
			throw e;
		}
	}
	
	/**
	 * Return the server platform type. It will be one of the following:
	 *		UNDETERMINED_PLATFORM: The platform has not been determined
	 *		CVS_SERVER: The platform is regular CVS server
	 *		CVSNT_SERVER: The platform in CVSNT
	 * If UNDETERMINED_PLATFORM is returned, the platform can be determined
	 * using the Command.VERSION command.
	 */
	public int getServerPlatform() {
		return serverPlatform;
	}
	
	/**
	 * This method is called from Command.VERSION to set the platform type.
	 */
	public void setServerPlaform(IStatus status) {
		// OK means that its a regular cvs server
		if (status.isOK()) {
			serverPlatform = CVS_SERVER;
			return;
		}
		// Find the status that reports the CVS platform
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				IStatus iStatus = children[i];
				if (iStatus.getCode() == CVSStatus.SERVER_IS_CVSNT 
						|| iStatus.getCode() == CVSStatus.UNSUPPORTED_SERVER_VERSION
						|| iStatus.getCode() == CVSStatus.SERVER_IS_UNKNOWN) {
					status = iStatus;
					break;
				}
			}
		}
		// Second, check the code of the status itself to see if it is NT
		switch (status.getCode()) {
			case CVSStatus.SERVER_IS_CVSNT:
				serverPlatform = CVSNT_SERVER;
				break;
			case CVSStatus.UNSUPPORTED_SERVER_VERSION:
				serverPlatform = UNSUPPORTED_SERVER;
				break;
			case CVSStatus.SERVER_IS_UNKNOWN:
				serverPlatform = UNKNOWN_SERVER;
				break;
			default:
				// We had an error status with no info about the server.
				// Mark it as undetermined.
				serverPlatform = UNDETERMINED_PLATFORM;
		}
	}
	
	public static boolean validateConnectionMethod(String methodName) {
		IConnectionMethod[] methods = getPluggedInConnectionMethods();
		for (int i=0;i<methods.length;i++) {
			if (methodName.equals(methods[i].getName()))
				return true;
		}
		return false;
	}
	
	/*
	 * Create a repository location instance from the given properties.
	 * The supported properties are:
	 * 
	 *   connection The connection method to be used
	 *   user The username for the connection (optional)
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 */
	public static CVSRepositoryLocation fromProperties(Properties configuration) throws CVSException {
		// We build a string to allow validation of the components that are provided to us
		String connection = configuration.getProperty("connection");//$NON-NLS-1$ 
		if (connection == null)
			connection = "pserver";//$NON-NLS-1$ 
		IConnectionMethod method = getPluggedInConnectionMethod(connection);
		if (method == null)
			throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSRepositoryLocation.methods", new Object[] {getPluggedInConnectionMethodNames()}), null));//$NON-NLS-1$ 
		String user = configuration.getProperty("user");//$NON-NLS-1$ 
		if (user.length() == 0)
			user = null;
		String password = configuration.getProperty("password");//$NON-NLS-1$ 
		if (user == null)
			password = null;
		String host = configuration.getProperty("host");//$NON-NLS-1$ 
		if (host == null)
			throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSRepositoryLocation.hostRequired"), null));//$NON-NLS-1$ 
		String portString = configuration.getProperty("port");//$NON-NLS-1$ 
		int port;
		if (portString == null)
			port = ICVSRepositoryLocation.USE_DEFAULT_PORT;
		else
			port = Integer.parseInt(portString);
		String root = configuration.getProperty("root");//$NON-NLS-1$ 
		if (root == null)
			throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSRepositoryLocation.rootRequired"), null));//$NON-NLS-1$ 
		root = root.replace('\\', '/');

		return new CVSRepositoryLocation(method, user, password, host, port, root, user != null, false);
	}
	
	/*
	 * Parse a location string and return a CVSRepositoryLocation.
	 * 
	 * On failure, the status of the exception will be a MultiStatus
	 * that includes the original parsing error and a general status
	 * displaying the passed location and proper form. This form is
	 * better for logging, etc.
	 */
	public static CVSRepositoryLocation fromString(String location) throws CVSException {	
		try {
			return fromString(location, false);
		} catch (CVSException e) {
			// Parsing failed. Include a status that
			// shows the passed location and the proper form
			MultiStatus error = new MultiStatus(CVSProviderPlugin.ID, CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.invalidFormat", new Object[] {location}), null);//$NON-NLS-1$ 
			error.merge(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.locationForm")));//$NON-NLS-1$ 
			error.merge(e.getStatus());
			throw new CVSException(error);
		}
	}
	
	/*
	 * Parse a location string and return a CVSRepositoryLocation.
	 * 
	 * The valid format (from the cederqvist) is:
	 * 
	 * :method:[[user][:password]@]hostname[:[port]]/path/to/repository
	 * 
	 * However, this does not work with CVS on NT so we use the format
	 * 
	 * :method:[user[:password]@]hostname[#port]:/path/to/repository
	 * 
	 * Some differences to note:
	 *    The : after the host/port is not optional because of NT naming including device
	 *    e.g. :pserver:username:password@hostname#port:D:\cvsroot
	 * 
	 * If validateOnly is true, this method will always throw an exception.
	 * The status of the exception indicates success or failure. The status
	 * of the exception contains a specific message suitable for displaying
	 * to a user who has knowledge of the provided location string.
	 * @see CVSRepositoryLocation.fromString(String)
	 */
	public static CVSRepositoryLocation fromString(String location, boolean validateOnly) throws CVSException {
		String partId = null;
		try {
			// Get the connection method
			partId = "CVSRepositoryLocation.parsingMethod";//$NON-NLS-1$ 
			int start = location.indexOf(COLON);
			String methodName;
			int end;
			if (start == 0) {
				end = location.indexOf(COLON, start + 1);
				methodName = location.substring(start + 1, end);
				start = end + 1;
			} else {
				// this could be an alternate format for ext: username:password@host:path
				methodName = "ext"; //$NON-NLS-1$
				start = 0;
			}
			
			IConnectionMethod method = getPluggedInConnectionMethod(methodName);
			if (method == null)
				throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.methods", new Object[] {getPluggedInConnectionMethodNames()})));//$NON-NLS-1$ 
			
			// Get the user name and password (if provided)
			partId = "CVSRepositoryLocation.parsingUser";//$NON-NLS-1$ 
			
			end = location.indexOf(HOST_SEPARATOR, start);
			String user = null;;
			String password = null;
			// if end is -1 then there is no host separator meaning that the username is not present
			if (end != -1) {		
				// Get the optional user and password
				user = location.substring(start, end);
				// Separate the user and password (if there is a password)
				start = user.indexOf(COLON);
				if (start != -1) {
					partId = "CVSRepositoryLocation.parsingPassword";//$NON-NLS-1$ 
					password = user.substring(start+1);
					user = user.substring(0, start);	
				}
				// Set start to point after the host separator
				start = end + 1;
			}
			
			// Get the host (and port)
			partId = "CVSRepositoryLocation.parsingHost";//$NON-NLS-1$ 
			end= location.indexOf(COLON, start);
			String host = location.substring(start, end);
			int port = USE_DEFAULT_PORT;
			// Separate the port and host if there is a port
			start = host.indexOf(PORT_SEPARATOR);
			if (start != -1) {
				// Initially, we used a # between the host and port
				partId = "CVSRepositoryLocation.parsingPort";//$NON-NLS-1$ 
				port = Integer.parseInt(host.substring(start+1));
				host = host.substring(0, start);
			} else {
				// In the correct CVS format, the port follows the COLON
				partId = "CVSRepositoryLocation.parsingPort";//$NON-NLS-1$ 
				int index = end;
				char c = location.charAt(++index);
				String portString = new String();
				while (Character.isDigit(c)) {
					portString += c;
					c = location.charAt(++index);
				}
				if (portString.length() > 0) {
					end = index - 1;
					port = Integer.parseInt(portString);
				}
			}
			
			// Get the repository path (translating backslashes to slashes)
			partId = "CVSRepositoryLocation.parsingRoot";//$NON-NLS-1$ 
			start = end + 1;
			String root = location.substring(start).replace('\\', '/');
			
			if (validateOnly)
				throw new CVSException(new CVSStatus(CVSStatus.OK, Policy.bind("ok")));//$NON-NLS-1$ 
				
			return new CVSRepositoryLocation(method, user, password, host, port, root, (user != null), (password != null));
		}
		catch (IndexOutOfBoundsException e) {
			// We'll get here if anything funny happened while extracting substrings
			throw new CVSException(Policy.bind(partId));
		}
		catch (NumberFormatException e) {
			// We'll get here if we couldn't parse a number
			throw new CVSException(Policy.bind(partId));
		}
	}
	
	public static IUserAuthenticator getAuthenticator() {
		if (authenticator == null) {
			authenticator = getPluggedInAuthenticator();
		}
		return authenticator;
	}

	/*
	 * Return the connection method registered for the given name or null if none
	 * are registered
	 */
	private static IConnectionMethod getPluggedInConnectionMethod(String methodName) {
		IConnectionMethod[] methods = getPluggedInConnectionMethods();
		for(int i=0; i<methods.length; i++) {
			if(methodName.equals(methods[i].getName()))
				return methods[i];
		}
		return null;		
	}
	
	/*
	 * Return a string containing a list of all connection methods
	 */
	private static String getPluggedInConnectionMethodNames() {
		IConnectionMethod[] methods = getPluggedInConnectionMethods();
		StringBuffer methodNames = new StringBuffer();
		for(int i=0; i<methods.length; i++) {
			String name = methods[i].getName();
			if (i>0)
				methodNames.append(", ");//$NON-NLS-1$ 
			methodNames.append(name);
		}		
		return methodNames.toString();
	}
	
	public static IConnectionMethod[] getPluggedInConnectionMethods() {
		if(pluggedInConnectionMethods==null) {
			List connectionMethods = new ArrayList();
			
			if (STANDALONE_MODE) {				
				connectionMethods.add(new PServerConnectionMethod());
			} else {
				IExtension[] extensions = Platform.getPluginRegistry().getExtensionPoint(CVSProviderPlugin.ID, CVSProviderPlugin.PT_CONNECTIONMETHODS).getExtensions();
				for(int i=0; i<extensions.length; i++) {
					IExtension extension = extensions[i];
					IConfigurationElement[] configs = extension.getConfigurationElements();
					if (configs.length == 0) {
						CVSProviderPlugin.log(IStatus.ERROR, Policy.bind("CVSProviderPlugin.execProblem"), null);//$NON-NLS-1$ 
						continue;
					}
					try {
						IConfigurationElement config = configs[0];
						connectionMethods.add(config.createExecutableExtension("run"));//$NON-NLS-1$ 
					} catch (CoreException ex) {
						CVSProviderPlugin.log(IStatus.ERROR, Policy.bind("CVSProviderPlugin.execProblem"), ex);//$NON-NLS-1$ 
					}
				}
			}
			pluggedInConnectionMethods = (IConnectionMethod[])connectionMethods.toArray(new IConnectionMethod[0]);
		}
		return pluggedInConnectionMethods;
	}
	
	private static IUserAuthenticator getPluggedInAuthenticator() {
		IExtension[] extensions = Platform.getPluginRegistry().getExtensionPoint(CVSProviderPlugin.ID, CVSProviderPlugin.PT_AUTHENTICATOR).getExtensions();
		if (extensions.length == 0)
			return null;
		IExtension extension = extensions[0];
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0) {
			CVSProviderPlugin.log(IStatus.ERROR, Policy.bind("CVSAdapter.noConfigurationElement", new Object[] {extension.getUniqueIdentifier()}), null);//$NON-NLS-1$ 
			return null;
		}
		try {
			IConfigurationElement config = configs[0];
			return (IUserAuthenticator) config.createExecutableExtension("run");//$NON-NLS-1$ 
		} catch (CoreException ex) {
			CVSProviderPlugin.log(IStatus.ERROR, Policy.bind("CVSAdapter.unableToInstantiate", new Object[] {extension.getUniqueIdentifier()}), ex);//$NON-NLS-1$ 
			return null;
		}
	}
	
	/*
	 * Validate that the given string could ne used to succesfully create
	 * an instance of the receiver.
	 * 
	 * This method performs some initial checks to provide displayable
	 * feedback and also tries a more in-depth parse using fromString(String, boolean).
	 */
	public static IStatus validate(String location) {
		
		// Check some simple things that are not checked in creation
		if (location == null)
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.nullLocation"));//$NON-NLS-1$ 
		if (location.equals(""))//$NON-NLS-1$ 
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.emptyLocation"));//$NON-NLS-1$ 
		if (location.endsWith(" ") || location.endsWith("\t"))//$NON-NLS-1$  //$NON-NLS-2$ 
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.endWhitespace"));//$NON-NLS-1$ 
		if (!location.startsWith(":") || location.indexOf(COLON, 1) == -1)//$NON-NLS-1$ 
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSRepositoryLocation.startOfLocation"));//$NON-NLS-1$ 

		// Do some quick checks to provide geberal feedback
		String formatError = Policy.bind("CVSRepositoryLocation.locationForm");//$NON-NLS-1$ 
		int secondColon = location.indexOf(COLON, 1);
		int at = location.indexOf(HOST_SEPARATOR);
		if (at != -1) {
			String user = location.substring(secondColon + 1, at);
			if (user.equals(""))//$NON-NLS-1$ 
				return new CVSStatus(CVSStatus.ERROR, formatError);
		} else
			at = secondColon;
		int colon = location.indexOf(COLON, at + 1);
		if (colon == -1)
			return new CVSStatus(CVSStatus.ERROR, formatError);
		String host = location.substring(at + 1, colon);
		if (host.equals(""))//$NON-NLS-1$ 
				return new CVSStatus(CVSStatus.ERROR, formatError);
		String path = location.substring(colon + 1, location.length());
		if (path.equals(""))//$NON-NLS-1$ 
				return new CVSStatus(CVSStatus.ERROR, formatError);
				
		// Do a full parse and see if it passes
		try {
			fromString(location, true);
		} catch (CVSException e) {
			// An exception is always throw. Return the status
			return e.getStatus();
		}
				
		// Looks ok (we'll actually never get here because above 
		// fromString(String, boolean) will always throw an exception).
		return new CVSStatus(IStatus.OK, Policy.bind("ok"));//$NON-NLS-1$ 
	}
	/**
	 * @see ICVSRepositoryLocation#flushUserInfo()
	 */
	public void flushUserInfo() throws CVSException {
		flushCache();
	}
	
	/*
	 * Return the command string that is to be used by the EXT connection method.
	 */
	String[] getExtCommand(String password) throws IOException {
		// Get the user specified connection parameters
		String CVS_RSH = CVSProviderPlugin.getPlugin().getCvsRshCommand();
		String CVS_RSH_PARAMETERS = CVSProviderPlugin.getPlugin().getCvsRshParameters();
		String CVS_SERVER = CVSProviderPlugin.getPlugin().getCvsServer();
		if(CVS_RSH == null || CVS_SERVER == null) {
			throw new IOException(Policy.bind("EXTServerConnection.varsNotSet")); //$NON-NLS-1$
		}
		
		// If there is only one token, assume it is the command and use the default parameters and order
		if (CVS_RSH_PARAMETERS == null || CVS_RSH_PARAMETERS.length() == 0) {
			if (port != USE_DEFAULT_PORT)
				throw new IOException(Policy.bind("EXTServerConnection.invalidPort")); //$NON-NLS-1$
			return new String[] {CVS_RSH, host, "-l", user, CVS_SERVER, INVOKE_SVR_CMD}; //$NON-NLS-1$
		}

		// Substitute any variables for their appropriate values
		CVS_RSH_PARAMETERS = stringReplace(CVS_RSH_PARAMETERS, USER_VARIABLE, user);
		CVS_RSH_PARAMETERS = stringReplace(CVS_RSH_PARAMETERS, PASSWORD_VARIABLE, password);
		CVS_RSH_PARAMETERS = stringReplace(CVS_RSH_PARAMETERS, HOST_VARIABLE, host);
		CVS_RSH_PARAMETERS = stringReplace(CVS_RSH_PARAMETERS, PORT_VARIABLE, new Integer(port).toString());

		// Build the command list to be sent to the OS.
		List commands = new ArrayList();
		commands.add(CVS_RSH);
		StringTokenizer tokenizer = new StringTokenizer(CVS_RSH_PARAMETERS);
		while (tokenizer.hasMoreTokens()) {
			String next = tokenizer.nextToken();
			commands.add(next);
		}
		commands.add(CVS_SERVER);
		commands.add(INVOKE_SVR_CMD);
		return (String[]) commands.toArray(new String[commands.size()]);
	}

	/*
	 * Replace all occurances of oldString with newString
	 */
	private String stringReplace(String string, String oldString, String newString) {
		int index = string.toLowerCase().indexOf(oldString);
		if (index == -1) return string;
		return stringReplace(
			string.substring(0, index) + newString + string.substring(index + oldString.length()),
			oldString, newString);
	}

	/**
	 * Return the name of the cvs program on the remote server. 
	 * The default is "cvs" but it can be tailored for some servers
	 * @return String
	 */
	public String getRemoteCVSProgramName() {
		return remoteCVSProgramName;
	}

	/**
	 * Sets the remoteCVSProgramName.
	 * @param remoteCVSProgramName The remoteCVSProgramName to set
	 */
	public void setRemoteCVSProgramName(String remoteCVSProgramName) {
		this.remoteCVSProgramName = remoteCVSProgramName;
	}
		
	/**
	 * Return the server message with the prefix removed.
	 * Server aborted messages typically start with 
	 *    "cvs server: ..."
	 *    "cvs [server aborted]: ..."
	 *    "cvs rtag: ..."
	 */
	public String getServerMessageWithoutPrefix(String errorLine, String prefix) {
		String message = errorLine;
		String programName = getRemoteCVSProgramName();
		if (message.startsWith(programName)) {
			// remove the program name and the space
			message = message.substring(programName.length() + 1);
			if (message.startsWith(prefix)) {
				message = message.substring(prefix.length());
				if (message.charAt(0) == ' ') {
					message = message.substring(1);
				}
				return message;
			}
		}
		// This is not a server message with the desired prefix
		return null;
	}
}
