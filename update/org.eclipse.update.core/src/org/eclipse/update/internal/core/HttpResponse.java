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
import org.eclipse.core.runtime.IProgressMonitor;

public class HttpResponse extends Response {
	private static final long POLLING_INTERVAL = 200;

	/**
	 * 
	 */
	public HttpResponse(URL url) {
		//super(IStatusCodes.HTTP_OK,"", context, in);
		super(url);
	}

	/**
	 * A special version of 'getInputStream' that can
	 * be canceled if connection is HttpURLConnection.
	 * A monitor thread checks the state of the monitor
	 * and disconnects the connection if 'isCanceled()'
	 * is detected.  
	 * @param monitor the progress monitor
	 * @return InputStream an opened stream or null if failed.
	 * @throws IOException if there are problems
	 * @throws CoreException if no more connection threads are available
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
			} else
				this.in = connection.getInputStream();
		}
		return in;
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
