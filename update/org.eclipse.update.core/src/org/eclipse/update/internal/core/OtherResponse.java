/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;

public class OtherResponse implements Response {
	private static final long POLLING_INTERVAL = 200;
	protected URL url;
	protected InputStream in;
	protected URLConnection connection;
	protected long lastModified;

	public OtherResponse(URL url) throws IOException {
		this.url = url;
//		connection = url.openConnection();
	}

	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
			connection = url.openConnection();
			in = connection.getInputStream();
			this.lastModified = connection.getLastModified();
		}
		return in;
	}
	/**
	 * @see Response#getInputStream(IProgressMonitor)
	 */
	public InputStream getInputStream(IProgressMonitor monitor)
		throws IOException, CoreException {
		if (in == null && url != null) {
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
			Thread t =
				UpdateCore.getPlugin().getConnectionManager().createThread(
					runnable);
			t.start();
			InputStream is = null;
			try {
				for (;;) {
					if (monitor.isCanceled()) {
						runnable.disconnect();
						break;
					}
					if (runnable.getInputStream() != null) {
						is = runnable.getInputStream();
						break;
					}
					if (runnable.getException() != null) {
						if (runnable.getException() instanceof IOException)
							throw (IOException) runnable.getException();
						else
							throw new CoreException(new Status(IStatus.ERROR,
									UpdateCore.getPlugin().getBundle()
											.getSymbolicName(), IStatus.OK,
									runnable.getException().getMessage(), runnable
											.getException()));
					}
					t.join(POLLING_INTERVAL);
				}
			} catch (InterruptedException e) {
			}
			return is;
		}
}
