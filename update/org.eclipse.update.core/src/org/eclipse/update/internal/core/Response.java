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

public class Response {
	
	protected URL url;
	protected InputStream in;
	protected URLConnection connection;
	protected long lastModified;
	
	/**
	 * 
	 */
	public Response(InputStream in) {
		super();
		this.in = in;
	}

	/**
	 * 
	 */
	public Response(URL url) {
		super();
		this.url = url;
	}
	
	/**
	 * Method getInputStream.
	 * @return InputStream
	 */
	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
			connection = url.openConnection();
			this.in = connection.getInputStream();
		}
		return in;
	}	
	
	public InputStream getInputStream(IProgressMonitor monitor) throws IOException, CoreException {
		return getInputStream();
	}
	
	/**
	 * Method getContentLength.
	 * @return long
	 */
	public long getContentLength() {
		if (connection != null)
			return connection.getContentLength();
		return 0;
	}

	/**
	 * Method getStatusCode.
	 * @return int
	 */
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

	/**
	 * Method getStatusMessage.
	 * @return String
	 */
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
	
	/**
	 * Returns the timestamp of last modification to the resource
	 * @return
	 */
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
}
