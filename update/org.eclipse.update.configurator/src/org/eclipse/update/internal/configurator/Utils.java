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

import java.io.*;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.boot.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.framework.log.*;

public class Utils {
	static FrameworkLog log;
	
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
	
	public static void log(String message) {
		log(newStatus(message, null));
	}
	
	public static void log(IStatus status) {
		if (log != null) {
			log.log(new FrameworkLogEntry(ConfigurationActivator.PI_CONFIGURATOR, status.getMessage(), 0, status.getException(), null));
		} else {
			System.out.println(status.getMessage());
			if (status.getException() != null)
				status.getException().printStackTrace();
		}
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
			// TODO workaround bug in platform url resolution
			if (resolvedPlatformURL.getProtocol().equals("file"))
				resolvedPlatformURL = new File(resolvedPlatformURL.getFile()).toURL();
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
