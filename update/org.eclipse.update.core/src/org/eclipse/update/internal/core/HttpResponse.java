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

public class HttpResponse implements Response {
	private static final long POLLING_INTERVAL = 200;
	protected URL url;
	protected InputStream in;
	protected URLConnection connection;
	protected long lastModified;
	protected long offset;

	public HttpResponse(URL url) {
		this.url = url;
	}

	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
			connection = url.openConnection();
			if (offset > 0)
				connection.setRequestProperty("Range", "bytes=" + offset + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			in = connection.getInputStream();
			checkOffset();
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
			if (offset > 0)
				connection.setRequestProperty("Range", "bytes=" + offset + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (monitor != null) {
				this.in =
					openStreamWithCancel(connection, monitor);
			} else {
				this.in = connection.getInputStream();
			}
			// this can also be run inside a monitoring thread, but it is safe
			// to
			// just call it now, if the input stream has already been obtained
			checkOffset();
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
		if (connection == null)
			try {
				connection = url.openConnection();
			} catch (IOException e) {
			}
		if (connection != null) {
			try {
				return ((HttpURLConnection) connection).getResponseCode();
			} catch (IOException e) {
				UpdateCore.warn("", e); //$NON-NLS-1$
			}
		}
		return IStatusCodes.HTTP_OK;
	}

	public String getStatusMessage() {
		if (connection != null) {
			try {
				return ((HttpURLConnection) connection).getResponseMessage();
			} catch (IOException e) {
				UpdateCore.warn("", e); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}

	public long getLastModified() {
		if (lastModified == 0) {
			if (connection == null)
				try {
					connection = url.openConnection();
				} catch (IOException e) {
				}
			if (connection != null)
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
	public void setOffset(long offset) {
		this.offset = offset;
	}
	private void checkOffset() throws IOException {
		if (offset == 0)
			return;
		String range = connection.getHeaderField("Content-Range"); //$NON-NLS-1$
		//System.out.println("Content-Range=" + range);
		if (range == null) {
			//System.err.println("Server does not support ranges");
			throw new IOException(Policy.bind("HttpResponse.rangeExpected")); //$NON-NLS-1$
		} else if (!range.startsWith("bytes " + offset + "-")) { //$NON-NLS-1$ //$NON-NLS-2$
			//System.err.println("Server returned wrong range");
			throw new IOException(Policy.bind("HttpResponse.wrongRange")); //$NON-NLS-1$
		}
	}
}
