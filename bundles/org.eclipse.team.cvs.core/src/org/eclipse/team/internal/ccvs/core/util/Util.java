package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.core.target.SynchronizedTargetProvider;

/**
 * Unsorted static helper-methods 
 */
public class Util {
	/**
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
			IPath path = new Path(resource.getRelativePath(root));
			int segments = path.segmentCount();
			if(segments>split) {				
				IPath last = path.removeFirstSegments(segments - split);
				return "..." + path.SEPARATOR + last.toString(); //$NON-NLS-1$
			}
			return path.toString();
		} catch(CVSException e) {
			return resource.getName();
		}
	}
	
	/**
	 * Helper method that will time out when making a socket connection.
	 * This is required because there is no way to provide a timeout value
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
						if (Thread.currentThread().interrupted()) {
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
			throw new InterruptedIOException("A timeout occured connecting to " + host);
		}
		return socket[0];
	}
}