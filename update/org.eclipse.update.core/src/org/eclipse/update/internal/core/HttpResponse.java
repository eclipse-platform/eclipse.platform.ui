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

	public HttpResponse(URL url) {
		this.url = url;
	}

	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
			connection = url.openConnection();
			in = connection.getInputStream();
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

			if (monitor != null && connection instanceof HttpURLConnection) {
				this.in =
					openStreamWithCancel(
						(HttpURLConnection) connection,
						monitor);
			} else {
				this.in = connection.getInputStream();
			}
			// this can also be run inside a monitoring thread, but it is safe
			// to
			// just call it now, if the input stream has already been obtained
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
		if (connection != null) {
			if (connection instanceof HttpURLConnection)
				try {
					return ((HttpURLConnection) connection).getResponseCode();
				} catch (IOException e) {
					UpdateCore.warn("", e);
				}
		}
		return IStatusCodes.HTTP_OK;
	}

	public String getStatusMessage() {
		if (connection != null) {
			if (connection instanceof HttpURLConnection)
				try {
					return ((HttpURLConnection) connection)
						.getResponseMessage();
				} catch (IOException e) {
					UpdateCore.warn("", e);
				}
		}
		return "";
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
		HttpURLConnection urlConnection,
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
				if (runnable.getIOException() != null) {
					throw runnable.getIOException();
				}
				t.join(POLLING_INTERVAL);
			}
		} catch (InterruptedException e) {
		}
		return is;
	}
}
