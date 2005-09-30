/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.internal.core.IStatusCodes;
import org.eclipse.update.internal.core.UpdateCore;

public class OtherResponse implements IResponse {
	private static final long POLLING_INTERVAL = 200;
	protected URL url;
	protected InputStream in;
	protected URLConnection connection;
	protected long lastModified;

	protected OtherResponse(URL url) throws IOException {
		this.url = url;
//		connection = url.openConnection();
	}

	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
            if (connection == null)
                connection = url.openConnection();
			in = connection.getInputStream();
			this.lastModified = connection.getLastModified();
		}
		return in;
	}
	/**
	 * @see IResponse#getInputStream(IProgressMonitor)
	 */
	public InputStream getInputStream(IProgressMonitor monitor)
		throws IOException, CoreException {
		if (in == null && url != null) {
            if (connection == null)
                connection = url.openConnection();

			if (monitor != null) {
				this.in =
					openStreamWithCancel(connection, monitor);
			} else {
				this.in = connection.getInputStream();
			}
			if (in != null) {
				this.lastModified = connection.getLastModified();
			}
		}
		return in;
	}

	public long getContentLength() {
		if (connection != null)
			return connection.getContentLength();
		return 0;
	}

	public int getStatusCode() {
		return IStatusCodes.HTTP_OK;
	}

	public String getStatusMessage() {
		return ""; //$NON-NLS-1$
	}

	public long getLastModified() {
		if (lastModified == 0 && connection != null) {
			lastModified = connection.getLastModified();
		}
		return lastModified;
	}
	
	private InputStream openStreamWithCancel(
			URLConnection urlConnection,
			IProgressMonitor monitor)
			throws IOException, CoreException {
		ConnectionThreadManager.StreamRunnable runnable =
			new ConnectionThreadManager.StreamRunnable(urlConnection);
		Thread t = ConnectionThreadManagerFactory.getConnectionManager().getConnectionThread(
				runnable);
		t.start();
		InputStream is = null;
		try {
			for (;;) {
				if (monitor.isCanceled()) {
					runnable.disconnect();
                    connection = null;
					break;
				}
				if (runnable.getInputStream() != null) {
					is = runnable.getInputStream();
					break;
				}
				if (runnable.getIOException() != null) 
					throw runnable.getIOException();
				if (runnable.getException() != null) 
						throw new CoreException(new Status(IStatus.ERROR,
															UpdateCore.getPlugin().getBundle().getSymbolicName(), 
															IStatus.OK,
															runnable.getException().getMessage(), 
															runnable.getException()));
				}
				t.join(POLLING_INTERVAL);
		} catch (InterruptedException e) {
		}
		return is;
	}
}
