package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.URL;
import java.net.URLConnection;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
public class Response {

	private URL url;
	private InputStream in;
	private URLConnection connection;

	public class CancelConnection implements Runnable {

		private URL url;

		public CancelConnection(URL url) {
			this.url = url;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				connection = url.openConnection();
				System.out.println("Connection Opened");
			} catch (IOException e) {
				throw new RuntimeException(e.toString());
			}
		}

		public synchronized URLConnection getConnection() {
			return connection;
		}

	}

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
			CancelConnection cancel = new CancelConnection(url);
			Thread t = new Thread(cancel, "cancellable url connection");

			try {
				t.start();
				while (connection == null) {
					connection = cancel.getConnection();
					Thread.sleep(2000);
				}
				t.interrupt();
			} catch (InterruptedException e){
				UpdateManagerPlugin.warn("Interrupted", e);
				throw new IOException();				
			} 
			this.in = connection.getInputStream();
		}

		return in;
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
					UpdateManagerPlugin.warn("", e);
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
					return ((HttpURLConnection) connection).getResponseMessage();
				} catch (IOException e) {
					UpdateManagerPlugin.warn("", e);
				}
		}
		return "";
	}

}
