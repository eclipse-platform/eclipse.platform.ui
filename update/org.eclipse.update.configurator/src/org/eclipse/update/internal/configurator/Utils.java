/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.boot.PlatformURLBaseConnection;
import org.eclipse.core.runtime.*;

public class Utils {
	private static final String PLUGIN_PATH = ".plugin-path"; //$NON-NLS-1$
	private static ILog log;

	/*
	 * This method is retained for R1.0 compatibility because it is defined as API.
	 * It's function matches the API description (returns <code>null</code> when
	 * argument URL is <code>null</code> or cannot be read).
	 */
	public static URL[] getPluginPath(URL pluginPathLocation /*R1.0 compatibility*/
	) {
		InputStream input = null;
		// first try and see if the given plugin path location exists.
		if (pluginPathLocation == null)
			return null;
		try {
			input = pluginPathLocation.openStream();
		} catch (IOException e) {
			//fall through
		}

		// if the given path was null or did not exist, look for a plugin path
		// definition in the install location.
		if (input == null)
			try {
				URL url = new URL(PlatformURLBaseConnection.PLATFORM_URL_STRING + PLUGIN_PATH);
				input = url.openStream();
			} catch (MalformedURLException e) {
				//fall through
			} catch (IOException e) {
				//fall through
			}

		// nothing was found at the supplied location or in the install location
		if (input == null)
			return null;
		// if we found a plugin path definition somewhere so read it and close the location.
		URL[] result = null;
		try {
			try {
				result = readPluginPath(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			//let it return null on failure to read
		}
		return result;
	}

	private static URL[] readPluginPath(InputStream input) {
		Properties ini = new Properties();
		try {
			ini.load(input);
		} catch (IOException e) {
			return null;
		}
		Vector result = new Vector(5);
		for (Enumeration groups = ini.propertyNames(); groups.hasMoreElements();) {
			String group = (String) groups.nextElement();
			for (StringTokenizer entries = new StringTokenizer(ini.getProperty(group), ";"); entries.hasMoreElements();) { //$NON-NLS-1$
				String entry = (String) entries.nextElement();
				if (!entry.equals("")) //$NON-NLS-1$
					try {
						result.addElement(new URL(entry));
					} catch (MalformedURLException e) {
						//intentionally ignore bad URLs
						System.err.println(Messages.getString("ignore.plugin", entry)); //$NON-NLS-1$
					}
			}
		}
		return (URL[]) result.toArray(new URL[result.size()]);
	}
	
	public static void debug(String s) {
		if (ConfigurationActivator.DEBUG)
			System.out.println("PlatformConfig: " + s); //$NON-NLS-1$
	}
	
	/**
	 * Creates a CoreException from some other exception.
	 * The type of the CoreException is <code>IStatus.ERROR</code>
	 * If the exception passed as a parameter is also a CoreException,
	 * the new CoreException will contain all the status of the passed
	 * CoreException.
	 * 
	 * @see IStatus#ERROR
	 * @param s exception string
	 * @param e actual exception being reported
	 * @return a CoreException
	 * @since 2.0
	 */
	public static CoreException newCoreException(String s, Throwable e) {

		// check the case of a multistatus
		IStatus status;
		if (e instanceof CoreException) {
			if (s == null)
				s = "";
			status = new MultiStatus("org.eclipse.update.configurator", 0, s, e);
			IStatus childrenStatus = ((CoreException) e).getStatus();
			((MultiStatus) status).add(childrenStatus);
			((MultiStatus) status).addAll(childrenStatus);
		} else {
			StringBuffer completeString = new StringBuffer("");
			if (s != null)
				completeString.append(s);
			if (e != null) {
				completeString.append(" [");
				String msg = e.getLocalizedMessage();
				completeString.append(msg!=null?msg:e.toString());
				completeString.append("]");
			}
			status = newStatus(completeString.toString(), e);
		}
		return new CoreException(status); //$NON-NLS-1$
	}

	public static IStatus newStatus(String message, Throwable e) {
		return new Status(IStatus.ERROR, "org.eclipse.update.configurator", IStatus.OK, message, e);
	}
	
	public static void setLog(ILog log) {
		Utils.log = log;
	}
	
	public static void log(String message) {
		log(newStatus(message, null));
	}
	
	public static void log(IStatus status) {
//		if (log != null)
//			log.log(status);
		System.out.println(status.getMessage());
		if (status.getException() != null)
			status.getException().printStackTrace();
	}
	
	/**
	 * Returns the url as a platform:/ url, if possible, else leaves it unchanged
	 * @param url
	 * @return
	 */
	public static URL asPlatformURL(URL url) {
		try {
			URL platformURL = new URL(PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + "base" + "/"); //$NON-NLS-1$ //$NON-NLS-2$ // try using platform-relative URL
			URL resolvedPlatformURL = Platform.asLocalURL(platformURL);
			String platformURLAsString = resolvedPlatformURL.toExternalForm();
			String urlAsString = url.toExternalForm();
			if (urlAsString.startsWith(platformURLAsString))
				return new URL(platformURL.toExternalForm() + urlAsString.substring(platformURLAsString.length()) );
			else
				return url;
		} catch (Exception e) {
			return url;
		}
	}
	

	/**
	 * 
	 */
	public static boolean isValidEnvironment(String os, String ws, String arch) {
		if (os!=null && !isMatching(os, Platform.getOS())) return false;
		if (ws!=null && !isMatching(ws, Platform.getWS())) return false;
		if (arch!=null && !isMatching(arch, Platform.getOSArch())) return false;
		return true;
	}

	/**
	 * 
	 */	
	private static boolean isMatching(String candidateValues, String siteValues) {
		if (siteValues==null) return false;
		if ("*".equalsIgnoreCase(candidateValues)) return true;
		siteValues = siteValues.toUpperCase();		
		StringTokenizer stok = new StringTokenizer(candidateValues, ",");
		while (stok.hasMoreTokens()) {
			String token = stok.nextToken().toUpperCase();
			if (siteValues.indexOf(token)!=-1) return true;
		}
		return false;
	}
}
