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
package org.eclipse.update.internal.operations;


import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.Utilities;

/**
 * A transient database that remembers information, such as usernames and
 * passwords.  The information is stored in memory only and is discarted
 * when the Plaform shuts down.
 */
public class UpdateManagerAuthenticator extends Authenticator {

	// fields needed for caching the password
	public static final String INFO_PASSWORD = "password"; //$NON-NLS-1$ 
	public static final String INFO_USERNAME = "username"; //$NON-NLS-1$ 
	public static final String AUTH_SCHEME = ""; //$NON-NLS-1$ 

	private InetAddress requestingSite;
	private int requestingPort;
	private String requestingProtocol;
	private String requestingPrompt;
	private String requestingScheme;
	private Map savedPasswordAuthentication;

	/**
	 * The Map containing the userid and password
	 * HashMap allows <code>null</code> as the value
	 */
	private Map result = new HashMap();

	/**
	 *  
	 */
	public void addAuthenticationInfo(URL serverUrl, String realm, String scheme, Map info) {
		try {
			Platform.addAuthorizationInfo(serverUrl, realm, scheme, info);
		} catch (CoreException e) {
			UpdateManager.logException(e);
		}
	}

	/**
	 *
	 */
	public Map getAuthenticationInfo(URL serverUrl, String realm, String scheme) {
		return Platform.getAuthorizationInfo(serverUrl, realm, scheme);
	}

	/**
	 * 
	 */
	public Map requestAuthenticationInfo(URL resourceUrl, String realm, String scheme) {
		// already called by retrieve
		//if (!equalsPreviousRequest(resourceUrl, realm, scheme)) {
			// save state
			InetAddress ip = null;
			try {
				ip = InetAddress.getByName(resourceUrl.getHost());
			} catch (UnknownHostException e) {
				UpdateManager.logException(e, false);
			}

			this.requestingPort = resourceUrl.getPort();
			this.requestingPrompt = realm;
			this.requestingProtocol = resourceUrl.getProtocol();
			this.requestingScheme = scheme;
			this.requestingSite = ip;

			// try to get the password info from the in-memory database first
			Map map = Platform.getAuthorizationInfo(resourceUrl, requestingPrompt, requestingScheme);
// TODO prompt for password
//			if (map == null) {
//				map = retrievePasswordAuthentication(resourceUrl, requestingPrompt, requestingScheme);
//			}
			savedPasswordAuthentication = map;
		//}

		// we must return a valid Map while we internally manage Cancel dialog (map==null -> dialog cancelled)
		return savedPasswordAuthentication;
	}

	/**
	 * @see org.eclipse.update.internal.core.net.http.client.IAuthenticator#addProtectionSpace(URL, String)
	 */
	public void addProtectionSpace(URL resourceUrl, String realm) {
		try {
			Platform.addProtectionSpace(resourceUrl, realm);
		} catch (CoreException e) {
			UpdateManager.logException(e, false);
		}
	}

	/**
	 * @see org.eclipse.update.internal.core.net.http.client.IAuthenticator#getProtectionSpace(URL)
	 */
	public String getProtectionSpace(URL resourceUrl) {
		return Platform.getProtectionSpace(resourceUrl);
	}

	/*
	 * forces a refresh
	 */
	public void reset() {
		requestingPort = 0;
		requestingPrompt = null;
		requestingProtocol = null;
		requestingScheme = null;
		requestingSite = null;
	}


	/*
	 * returns true if this request is the same as the saved one
	 * used to prevent double dialog if user cancelled or entered wrong userid/password
	 */
	private boolean equalsPreviousRequest(URL url, String realm, String scheme) {

		if (requestingPort != url.getPort())
			return false;

		if (requestingPrompt != null && !requestingPrompt.equals(realm))
			return false;
		if (requestingPrompt == null && realm != null)
			return false;

		if (requestingProtocol != null && !requestingProtocol.equals(url.getProtocol()))
			return false;
		if (requestingProtocol == null && url.getProtocol() != null)
			return false;

		if (requestingScheme != null && !requestingScheme.equals(scheme))
			return false;
		if (requestingScheme == null && scheme != null)
			return false;

		InetAddress ip = null;
		try {
			ip = InetAddress.getByName(url.getHost());
		} catch (UnknownHostException e) {
			UpdateManager.logException(e, false);
		}

		if (requestingSite != null && !requestingSite.equals(ip))
			return false;
		if (requestingSite == null && ip != null)
			return false;

		return true;
	}


	/*
	 * @see Authenticator#getPasswordAuthentication()
	 */
	protected PasswordAuthentication getPasswordAuthentication() {

		try {
			URL url = new URL(getRequestingProtocol(), getRequestingSite().getHostName(), getRequestingPort(), ""); //$NON-NLS-1$
			Map map = retrievePasswordAuthentication(url);

			String username = null;
			String password = null;

			if (map != null) {
				username = (String) map.get(INFO_USERNAME);
				password = (String) map.get(INFO_PASSWORD);
			}

			if (username != null && password != null) {
				return new PasswordAuthentication(username, password.toCharArray());
			} else {
				return null;
			}

		} catch (MalformedURLException e) {
			IStatus status = Utilities.newCoreException("", e).getStatus();
			UpdateManager.log(status, false);
		}
		return new PasswordAuthentication("", new char[] { ' ' }); //$NON-NLS-1$
	}

	/*
	 * 
	 */
	private Map retrievePasswordAuthentication(URL url) {

		if (equalsPreviousRequest(url,getRequestingPrompt(),getRequestingScheme())) {
			// same request, the userid/password was wrong
			// or user cancelled. force a refresh
			if (savedPasswordAuthentication != null)
				// only prompt if the user didn't cancel
				savedPasswordAuthentication = requestAuthenticationInfo(url, requestingPrompt, requestingScheme);
		} else {
			// save state
			requestingPort = getRequestingPort();
			requestingPrompt = getRequestingPrompt();
			requestingProtocol = getRequestingProtocol();
			requestingScheme = getRequestingScheme();
			requestingSite = getRequestingSite();
			savedPasswordAuthentication = Platform.getAuthorizationInfo(url, requestingPrompt, requestingScheme);
			if (savedPasswordAuthentication == null) {
				savedPasswordAuthentication = requestAuthenticationInfo(url, requestingPrompt, requestingScheme);
			}
		}
		return savedPasswordAuthentication;
	}

}
