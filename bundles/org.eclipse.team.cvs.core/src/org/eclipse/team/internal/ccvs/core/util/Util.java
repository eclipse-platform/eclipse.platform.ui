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
package org.eclipse.team.internal.ccvs.core.util;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Session;

/**
 * Unsorted static helper-methods 
 */
public class Util {
	
	/**
	 * Return the last segment of the given path
	 * @param path
	 * @return String
	 */
	public static String getLastSegment(String path) {
		int index = path.lastIndexOf(Session.SERVER_SEPARATOR);
		if (index == -1)
			return path;
		else
			return path.substring(index + 1);
		
	}
	
	/**
	 * Return the the given path with the last segment removed
	 * @param path
	 * @return String
	 */
	public static String removeLastSegment(String path) {
		int index = path.lastIndexOf(Session.SERVER_SEPARATOR);
		if (index == -1)
			return ""; //$NON-NLS-1$
		else
			return path.substring(0, index);

	}
	/**
	 * Return the path without a trailing /
	 * @param path
	 * @return String
	 */
	public static String asPath(String path) {
		if (path.endsWith(Session.SERVER_SEPARATOR)) {
			return path.substring(0, path.length() - Session.SERVER_SEPARATOR.length());
		}
		return path;
	}
	/*
	 * *
	 * Get the extention of the path of resource
	 * relative to the path of root
	 * 
	 * @throws CVSException if root is not a root-folder of resource
	 */
	public static String getRelativePath(String rootName, String resourceName) 
		throws CVSException {

		if (!resourceName.startsWith(rootName) || rootName.length() > resourceName.length()) {
			throw new CVSException(Policy.bind("Util.Internal_error,_resource_does_not_start_with_root_3")); //$NON-NLS-1$
		}
		
		// Otherwise we would get an ArrayOutOfBoundException
		// in case of two equal Resources
		if (rootName.length() == resourceName.length()) {
			return ""; //$NON-NLS-1$
		}
		
		// Remove leading slash if there is one
		String result = resourceName.substring(rootName.length()).replace('\\', '/');
		if (result.startsWith("/")) { //$NON-NLS-1$
			result = result.substring(1);
		}
		return result;
	}
	
	/**
	 * Append the prefix and suffix to form a valid CVS path.
	 */
	public static String appendPath(String prefix, String suffix) {
		if (prefix.length() == 0 || prefix.equals(Session.CURRENT_LOCAL_FOLDER)) {
			return suffix;
		} else if (prefix.endsWith(Session.SERVER_SEPARATOR)) {
			if (suffix.startsWith(Session.SERVER_SEPARATOR))
				return prefix + suffix.substring(1);
			else
				return prefix + suffix;
		} else if (suffix.startsWith(Session.SERVER_SEPARATOR))
			return prefix + suffix;
		else
			return prefix + Session.SERVER_SEPARATOR + suffix;
	}

	public static void logError(String message, Throwable throwable) {
		CVSProviderPlugin.log(new Status(IStatus.ERROR, CVSProviderPlugin.ID, IStatus.ERROR, message, throwable));
	}

	/**
	 * If the number of segments in the relative path of <code>resource</code> to <code>root</code> is 
	 * greater than <code>split</code> then the returned path is truncated to <code>split</code> number
	 * of segments and '...' is shown as the first segment of the path.
	 */
	public static String toTruncatedPath(ICVSResource resource, ICVSFolder root, int split) {
		try {
			String stringPath = resource.getRelativePath(root);
			if (stringPath.equals(Session.CURRENT_LOCAL_FOLDER)) {
				return resource.getName();
			}
			return toTruncatedPath(stringPath, split);
		} catch(CVSException e) {
			return resource.getName();
		}
	}

	public static String toTruncatedPath(String stringPath, int split) {
		// Search backwards until split separators are found
		int count = 0;
		int index = stringPath.length();
		while (count++ < split && index != -1) {
			index = stringPath.lastIndexOf(Session.SERVER_SEPARATOR, index - 1);
		}
		if (index == -1) {
			return stringPath;
		} else {
			return Policy.bind("Util.truncatedPath", stringPath.substring(index)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Helper method that will time out when making a socket connection.
	 * This i
	 * s required because there is no way to provide a timeout value
	 * when creating a socket and in some instances, they don't seem to
	 * timeout at all.
	 */
	public static Socket createSocket(final String host, final int port, IProgressMonitor monitor) throws UnknownHostException, IOException {
		
		// Start a thread to open a socket
		final Socket[] socket = new Socket[] { null };
		final Exception[] exception = new Exception[] {null };
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					Socket newSocket = new Socket(host, port);
					synchronized (socket) {
						if (Thread.interrupted()) {
							// we we're either cancelled or timed out so just close the socket
							newSocket.close();
						} else {
							socket[0] = newSocket;
						}
					}
				} catch (UnknownHostException e) {
					exception[0] = e;
				} catch (IOException e) {
					exception[0] = e;
				}
			}
		});
		thread.start();
		
		// Wait the appropriate number of seconds
		int timeout = CVSProviderPlugin.getPlugin().getTimeout();
		if (timeout == 0) timeout = CVSProviderPlugin.DEFAULT_TIMEOUT;
		for (int i = 0; i < timeout; i++) {
			try {
				// wait for the thread to complete or 1 second, which ever comes first
				thread.join(1000);
			} catch (InterruptedException e) {
				// I think this means the thread was interupted but not necessarily timed out
				// so we don't need to do anything
			}
			synchronized (socket) {
				// if the user cancelled, clean up before preempting the operation
				if (monitor.isCanceled()) {
					if (thread.isAlive()) {
						thread.interrupt();
					}
					if (socket[0] != null) {
						socket[0].close();
					}
					// this method will throw the proper exception
					Policy.checkCanceled(monitor);
				}
			}
		}
		// If the thread is still running (i.e. we timed out) signal that it is too late
		synchronized (socket) {
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}
		if (exception[0] != null) {
			if (exception[0] instanceof UnknownHostException)
				throw (UnknownHostException)exception[0];
			else
				throw (IOException)exception[0];
		}
		if (socket[0] == null) {
			throw new InterruptedIOException(Policy.bind("Util.timeout", host)); //$NON-NLS-1$
		}
		return socket[0];
	}
	
	public static String[] parseIntoSubstrings(String string, String delimiter) {
		List result = new ArrayList();
		int start = 0;
		int index = string.indexOf(delimiter);
		String next;
		while (index != -1) {
			next = string.substring(start, index);
			result.add(next);
			start = index + 1;
			index = string.indexOf(delimiter, start);
		}
		if (start >= string.length()) {
			next = "";//$NON-NLS-1$
		} else {
			next = string.substring(start);
		}
		result.add(next);
		return (String[]) result.toArray(new String[result.size()]);
	}
	
	/**
	 * Return the substring at the given index (starting at 0) where each
	 * element is delimited by the provided delimiter.
	 * 
	 * @param bytes
	 * @param delimiter
	 * @param index
	 * @param includeRest
	 * @return String
	 */
	public static String getSubstring(byte[] bytes, byte delimiter, int index, boolean includeRest) {
		return new String(getBytesForSlot(bytes, delimiter, index, includeRest));
	}
	
	/**
	 * Return the offset the the Nth delimeter from the given start index.
	 * @param bytes
	 * @param delimiter
	 * @param start
	 * @param n
	 * @return int
	 */
	public static int getOffsetOfDelimeter(byte[] bytes, byte delimiter, int start, int n) {
		int count = 0;
		for (int i = start; i < bytes.length; i++) {
			if (bytes[i] == delimiter) count++;
			if (count == n) return i;
		}
		// the Nth delimeter was not found
		return -1;
	}
	
	/**
	 * Method getBytesForSlot.
	 * @param syncBytes
	 * @param SEPARATOR_BYTE
	 * @param i
	 * @param b
	 * @return byte[]
	 */
	public static byte[] getBytesForSlot(byte[] bytes, byte delimiter, int index, boolean includeRest) {
		// Find the starting index
		int start;
		if (index == 0) {
			// make start -1 so that end determination will start at offset 0.
			start = -1;
		} else {
			start = getOffsetOfDelimeter(bytes, delimiter, 0, index);
			if (start == -1) return null;
		}
		// Find the ending index
		int end = getOffsetOfDelimeter(bytes, delimiter, start + 1, 1);
		// Calculate the length
		int length;
		if (end == -1 || includeRest) {
			length = bytes.length - start - 1;
		} else {
			length = end - start - 1;
		}
		byte[] result = new byte[length];
		System.arraycopy(bytes, start + 1, result, 0, length);
		return result;
	}
	
	/**
	 * Method equals.
	 * @param syncBytes
	 * @param oldBytes
	 * @return boolean
	 */
	public static boolean equals(byte[] syncBytes, byte[] oldBytes) {
		if (syncBytes.length != oldBytes.length) return false;
		for (int i = 0; i < oldBytes.length; i++) {
			if (oldBytes[i] != syncBytes[i]) return false;
		}
		return true;
	}
}
