package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IUserAuthenticator;
import org.eclipse.team.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * An authenticator that prompts the user for authentication info,
 * and stores the results in the Platform's authentication keyring.
 */
public class WorkbenchUserAuthenticator implements IUserAuthenticator {

	public static final String INFO_PASSWORD = "org.eclipse.team.ccvs.ui.password";
	public static final String INFO_USERNAME = "org.eclipse.team.ccvs.ui.username";
	public static final String AUTH_SCHEME = "";
	public static final URL FAKE_URL;
	
	static {
		URL temp = null;
		try {
			temp = new URL("http://org.eclipse.team.ccvs.ui");
		} catch (MalformedURLException e) {
		}
		FAKE_URL = temp;
	} 
	/**
	 * WorkbenchUserAuthenticator constructor.
	 */
	public WorkbenchUserAuthenticator() {
		super();
	}
	/**
	 * @see IUserAuthenticator#authenticateUser
	 */
	public boolean authenticateUser(final ICVSRepositoryLocation location, final IUserInfo userinfo, final boolean retry, final String message) throws CVSException {
	
		// first check to see if there is a cached username and password
		if ((!retry) && (retrievePassword(location, userinfo))) {
			return true;
		}
	
		// ask the user for a password
		final String[] result = new String[2];
		Display display = Display.getCurrent();
		if (display != null) {
			promptForPassword(location, userinfo.getUsername(), message, userinfo.isUsernameMutable(), result);
		} else {
			// sync exec in default thread
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					promptForPassword(location, userinfo.getUsername(), message, userinfo.isUsernameMutable(), result);
				}
			});
		}
	
		if (result[0] == null) {
			throw new OperationCanceledException(Policy.bind("WorkbenchUserAuthenticator.cancelled"));
		}
	
		updateAndCache(location, userinfo, result[0], result[1]);
		return true;
	}
	/**
	 * Asks the user to enter a password. Places the
	 * results in the supplied string[].  result[0] must
	 * contain the username, result[1] must contain the password.
	 * If the user canceled, both values must be zero.
	 * 
	 * @param location  the location to obtain the password for
	 * @param username  the username
	 * @param message  a message to display to the user
	 * @param userMutable  whether the user can be changed in the dialog
	 * @param result  a String array of length two in which to put the result
	 */
	private void promptForPassword(final ICVSRepositoryLocation location, final String username, final String message, final boolean userMutable, final String[] result) {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				Shell shell = new Shell(display);
				UserValidationDialog dialog = new UserValidationDialog(shell, location.getLocation(), (username==null)?"":username, message);
				dialog.setUsernameMutable(userMutable);
				dialog.open();
	
				shell.dispose();
				result[0] = dialog.getUsername();
				result[1] = dialog.getPassword();
			}
		});
	}
	/**
	 * @see IUserAuthenticator#cachePassword
	 */
	public void cachePassword(ICVSRepositoryLocation location, IUserInfo userinfo, String password) throws CVSException {
		updateAndCache(location, userinfo, userinfo.getUsername(), password);
	}
	/**
	 * @see IUserAuthenticator#retrievePassword
	 */
	public boolean retrievePassword(ICVSRepositoryLocation location, IUserInfo userinfo) throws CVSException {
		Map map = Platform.getAuthorizationInfo(FAKE_URL, location.getLocation(), AUTH_SCHEME);
		if (map != null) {
			String username = (String) map.get(INFO_USERNAME);
			String password = (String) map.get(INFO_PASSWORD);
			if (password != null) {
				if (userinfo.isUsernameMutable())
					userinfo.setUsername(username);
				userinfo.setPassword(password);
				return true;
			}
		}
		return false;
	}
	/**
	 * @see IUserAuthenticator#dispose(IRepositoryLocation)
	 */
	public void dispose(ICVSRepositoryLocation location) throws CVSException {
		try {
			Platform.flushAuthorizationInfo(FAKE_URL, location.getLocation(), AUTH_SCHEME);
		} catch (CoreException e) {
			// We should probably wrap the CoreException here!
			CVSUIPlugin.log(e.getStatus());
			throw new CVSException(IStatus.ERROR, IStatus.ERROR, Policy.bind("WorkbenchUserAuthenticator.errorFlushing", location.getLocation()), e);
		}
	}
	/**
	 * Updates the pasword in the platform keyring.
	 * 
	 * @param location  the repository location
	 * @param userinfo  the user information
	 * @param username  the name of the user
	 * @param password  the password
	 * @throws CVSException if a CVS error occurs
	 */
	public void updateAndCache(ICVSRepositoryLocation location, IUserInfo userinfo, String username, String password) throws CVSException {
		// put the password into the Platform map
		Map map = Platform.getAuthorizationInfo(FAKE_URL, location.getLocation(), AUTH_SCHEME);
		if (map == null) {
			map = new java.util.HashMap(10);
		}
		map.put(INFO_USERNAME, username);
		map.put(INFO_PASSWORD, password);
		try {
			Platform.addAuthorizationInfo(FAKE_URL, location.getLocation(), AUTH_SCHEME, map);
		} catch (CoreException e) {
			// We should probably wrap the CoreException here!
			CVSUIPlugin.log(e.getStatus());
			throw new CVSException(IStatus.ERROR, IStatus.ERROR, Policy.bind("WorkbenchUserAuthenticator.errorSaving", location.getLocation()), e);
		}
		if (userinfo.isUsernameMutable()) {
			userinfo.setUsername(username);
		}
		userinfo.setPassword(password);;
	}
}
