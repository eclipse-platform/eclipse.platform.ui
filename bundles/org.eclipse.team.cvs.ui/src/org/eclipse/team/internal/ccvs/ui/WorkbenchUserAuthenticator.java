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

	/**
	 * WorkbenchUserAuthenticator constructor.
	 */
	public WorkbenchUserAuthenticator() {
		super();
	}
	/**
	 * @see IUserAuthenticator#authenticateUser
	 */
	public void promptForUserInfo(final ICVSRepositoryLocation location, final IUserInfo userinfo, final String message) throws CVSException {
	
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
			throw new OperationCanceledException(Policy.bind("WorkbenchUserAuthenticator.cancelled")); //$NON-NLS-1$
		}
		
		if (userinfo.isUsernameMutable())
			userinfo.setUsername(result[0]);
		userinfo.setPassword(result[1]);
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
		Display display = Display.getCurrent();
		Shell shell = new Shell(display);
		UserValidationDialog dialog = new UserValidationDialog(shell, location.getLocation(), (username==null)?"":username, message);//$NON-NLS-1$
		dialog.setUsernameMutable(userMutable);
		dialog.open();
		shell.dispose();
		
		result[0] = dialog.getUsername();
		result[1] = dialog.getPassword();
	}
}
