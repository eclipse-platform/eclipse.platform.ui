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
	protected URL url;
	protected InputStream in;
	protected URLConnection connection;
	protected long lastModified;

	public OtherResponse(URL url) throws IOException {
		this.url = url;
		connection = url.openConnection();
	}

	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
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
		return getInputStream();
	}

	public long getContentLength() {
		return connection.getContentLength();
	}

	public int getStatusCode() {
		return IStatusCodes.HTTP_OK;
	}

	public String getStatusMessage() {
		return ""; //$NON-NLS-1$
	}

	public long getLastModified() {
		if (lastModified == 0) {
			lastModified = connection.getLastModified();
		}
		return lastModified;
	}
}
