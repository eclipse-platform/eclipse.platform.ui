package org.eclipse.update.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.swt.widgets.*;
import org.eclipse.webdav.client.URLTool;
import org.eclipse.webdav.http.client.IAuthenticator;
import java.net.*;
import java.util.*;

/**
 * A transient database that remembers information, such as usernames and
 * passwords.  The information is stored in memory only and is discarted
 * when the Plaform shuts down.
 */
public class AuthorizationDatabase implements IAuthenticator{
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
/**
 * 
 */
public void addAuthenticationInfo(
	URL serverUrl,
	String realm,
	String scheme,
	java.util.Map info) {
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

	authSchemeToInfo.put(scheme.toLowerCase(), info);
}
/**
 * 
 */
public void addProtectionSpace(URL resourceUrl, String realm){

	String file = resourceUrl.getFile();
	if(!file.endsWith("/")){
		resourceUrl = URLTool.getParent(resourceUrl);
	} 

	String oldRealm = getProtectionSpace(resourceUrl);
	if(oldRealm != null && oldRealm.equals(realm)){
		return;
	}

	String url1 = resourceUrl.toString();
	Enumeration urls = protectionSpace.keys();
	while(urls.hasMoreElements()){
		String url2 = (String)urls.nextElement();
		if(url1.startsWith(url2) || url2.startsWith(url1)){
			protectionSpace.remove(url2);
			break;
		}
	}

	protectionSpace.put(url1, realm);
}
/**
 *
 */
public Map getAuthenticationInfo(URL serverUrl, String realm, String scheme) {
	Hashtable realmToAuthScheme = (Hashtable)authorizationInfo.get(serverUrl.toString());
	if(realmToAuthScheme == null){
		return null;
	}

	Hashtable authSchemeToInfo = (Hashtable)realmToAuthScheme.get(realm);
	if(authSchemeToInfo == null){
		return null;
	}

	return (Map)authSchemeToInfo.get(scheme.toLowerCase());
}
/**
 * 
 */
public String getProtectionSpace(URL resourceUrl){
	while(resourceUrl != null){
		String realm = (String)protectionSpace.get(resourceUrl.toString());
		if(realm != null){
			return realm;
		}
		resourceUrl = URLTool.getParent(resourceUrl);
	}

	return null;
}
/**
 * 
 */
public java.util.Map requestAuthenticationInfo(java.net.URL resourceUrl, java.lang.String realm, java.lang.String scheme) {
	Shell shell = new Shell(Display.getCurrent());

	if (scheme.equalsIgnoreCase("Basic")) {
		UserValidationDialog ui = new UserValidationDialog(shell,resourceUrl.getHost(), "");
		ui.setUsernameMutable(true);
		ui.setBlockOnOpen(true);
		ui.open();

		if (ui.getReturnCode() == ui.CANCEL)
			return null;
		else {
			Map info = new Hashtable();
			info.put("username", ui.getUserid());
			info.put("password", ui.getPassword());
			return info;
		}
	} else
		return null;
}
}
