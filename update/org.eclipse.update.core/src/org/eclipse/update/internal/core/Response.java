package org.eclipse.update.internal.core;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
public class Response {
	
	protected URL url;
	protected InputStream in;
	protected URLConnection connection;

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

}
