package org.eclipse.update.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.Platform;import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.core.UpdateManagerPlugin;import org.eclipse.webdav.http.client.IAuthenticator;
import java.net.*;
import java.util.*;

/**
 * A transient database that remembers information, such as usernames and
 * passwords.  The information is stored in memory only and is discarted
 * when the Plaform shuts down.
 */
public class AuthorizationDatabase implements IAuthenticator{
	
	/**
	 * The Map containing the userid and password
	 */
	private Map result = new Hashtable();
	
/**
 * 
 */
public void addAuthenticationInfo(URL serverUrl,String realm,String scheme, Map info) {
	try {
		Platform.addAuthorizationInfo(serverUrl,realm,scheme,info);
	} catch (CoreException e) {
		UpdateManagerPlugin.getPluginInstance().getLog().log(e.getStatus());
	}
}

/**
 * 
 */
public void addProtectionSpace(URL resourceUrl, String realm){
	try {
		Platform.addProtectionSpace(resourceUrl,realm);
	} catch (CoreException e) {
		UpdateManagerPlugin.getPluginInstance().getLog().log(e.getStatus());
	}

}
/**
 *
 */
public Map getAuthenticationInfo(URL serverUrl, String realm, String scheme) {

	return Platform.getAuthorizationInfo(serverUrl,realm,scheme);
}
/**
 * 
 */
public String getProtectionSpace(URL resourceUrl){
	return Platform.getProtectionSpace(resourceUrl);
}
/**
 * 
 */
public Map requestAuthenticationInfo(final URL resourceUrl, final String realm, final String scheme) {
	
	result = new Hashtable();
	if (scheme.equalsIgnoreCase("Basic")) {
				
		Display disp = Display.getCurrent();
		if (disp != null) {
			promptForPassword(resourceUrl, realm, result);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {	
					promptForPassword(resourceUrl, realm, result);
					}
			});
		};
	}
	return result;
}

/**
 *
 */
private void promptForPassword(URL resourceUrl, String realm, Map info){
		
		Shell shell = new Shell();
		UserValidationDialog ui = new UserValidationDialog(shell,resourceUrl,realm,"");
		ui.setUsernameMutable(true);
		ui.setBlockOnOpen(true);
		ui.open();			
		
		if (ui.getReturnCode() != ui.CANCEL) {
			info.put("username", ui.getUserid());
			info.put("password", ui.getPassword());
		}
		shell.dispose();
	
}
}
