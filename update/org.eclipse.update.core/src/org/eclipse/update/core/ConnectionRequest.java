package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2002, 2003
 *  All  Rights Reserved.
 */
import java.io.*;
import java.net.*;

/**
 * Connection Request.
 * 
 * Manages a connection to a specified URL. 
 * A connection request can be canceled. 
 * This class is not intented to be subclassed
 * 
 * @see ConnectionManager
 * @since 2.1
 */
public class ConnectionRequest{
	
	private Exception exception;
	private int status = STATUS_IDLE;
	private URL url;
	private URLConnection connection;
	private IConnectionRequestListener listener;
	private Thread runner;
	private ConnectionManager manager;

	/**
	 * The connection request is created
	 */
	public static final int STATUS_IDLE = 0;
	/**
	 * The connection request is waiting for the connection to the
	 * <code>URL</code>
	 */
	public static final int STATUS_WAITING = 1;
	/**
	 * The connection request is sucessfully connected to the <code>URL</code>
	 */	
	public static final int STATUS_OK = 2;
	/**
	 * The connection request has been canceled
	 */	
	public static final int STATUS_CANCELED = 3;
	/**
	 * An error has occured while connecting.
	 * @see ConnectionRequest:getException()
	 */	
	public static final int STATUS_ERROR = 4;

	class ConnectorThread extends Thread {
		private ConnectorRunnable runnable;
		private Throwable throwable;
		public ConnectorThread(ConnectorRunnable runnable) {
			this.runnable = runnable;
		}

		public Throwable getException() {
			return throwable;
		}

		public void run() {
			try {
				if (runnable != null)
					runnable.run();
			} catch (RuntimeException e) {
				throwable = e;
			} catch (ThreadDeath e) {
				// Make sure to propagate ThreadDeath, or threads will never fully terminate
				throw e;
			} catch (Error e) {
				throwable = e;
			} finally {
			}
		}
	}

	class ConnectorRunnable implements Runnable {
		public synchronized void run() {
			try {
				exception = null;
				connection = (HttpURLConnection) url.openConnection();
				status = STATUS_WAITING;
				connection.connect();
				if (status == STATUS_WAITING) {
					status = STATUS_OK;
					notifyRequestProcessed();
				}
			} catch (Exception e) {
				if (status != STATUS_CANCELED) {
					exception = e;
					status = STATUS_ERROR;
					connection = null;
					notifyRequestProcessed();
				}
			}
		}
	}

	private synchronized void notifyRequestProcessed() {
		if (listener != null) {
			listener.requestProcessed(this);
		}
		stopRunnerThread();
		manager.purge(this);
	}
	
	/**
	 * returns the <code>java.net.URL</code>
	 * @return URL the url to connect to
	 * @since 2.1
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Return the current status of the connection
	 * @see STATUS_IDLE
	 * @see STATUS_WAITING
	 * @see STATUS_OK
	 * @see STATUS_CANCELED
	 * @see STATUS_ERROR
	 * @return int the current status
	 * @since 2.1
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * returns the exception if an error occured or <code>null</code> if no
	 * exception occured
	 * @return Throwable
	 * @since 2.1
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Returns the <code>InputStream</code> of the <code>URL</code> or
	 * <code>null</code> if there is none.
	 * @return InputStream Returns the <code>InputStream</code> of the
	 * <code>URL</code> or <code>null</code> if there is none.
	 * @throws IOException thrown if an error occurs opening the URL inputStream
	 * @since 2.1
	 */
	public synchronized InputStream getInputStream() throws IOException {
		if (connection != null)
			return connection.getInputStream();
		return null;
	}
	/**
	 * Returns the <code>URLConnection</code> of the <code>URL</code> or
	 * <code>null</code> if there is none.
	 * @return URLConnection Returns the <code>URLConnection</code> of the
	 * <code>URL</code> or <code>null</code> if there is none.
	 * @since 2.1
	 */	
	public synchronized URLConnection getURLConnection(){
		return connection;
	}

	void open(IConnectionRequestListener listener) {
		this.listener = listener;
		exception = null;
		runner = new ConnectorThread(new ConnectorRunnable());
		runner.start();
	}
	
	/**
	 * Closes a connection request
	 * @since 2.1
	 */
	public void close() {
		if (status != STATUS_IDLE) {
			stopRunnerThread();
			if (connection != null) {
				if (connection instanceof HttpURLConnection)	
					((HttpURLConnection)connection).disconnect();
				connection = null;
				status = STATUS_IDLE;
				exception = null;
			}
		}
	}
	
	/**
	 * returns <code>true</code> if the connection can be canceled,
	 * <code>false</code> otherwise.
	 * @return boolean
	 */
	public boolean isCancelable() {
		return isHttpProtocol();
	}
	
	private boolean isHttpProtocol() {
		String protocol = url.getProtocol();
		return protocol.equalsIgnoreCase("http");
	}

	void cancel() {
		if (isCancelable()==false) return;
		if (connection != null && status == STATUS_WAITING) {
			if (connection instanceof HttpURLConnection)	
				((HttpURLConnection)connection).disconnect();
			connection = null;
			status = STATUS_CANCELED;
			notifyRequestProcessed();
		}
	}

	private void stopRunnerThread() {
		if (runner != null) {
			runner.interrupt();
			runner = null;
		}
	}

	ConnectionRequest(URL url) {
		this.url = url;
	}
	
	void setManager(ConnectionManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Cancels the connection if the connection is cancelable.
	 * When the connection is canceled, the connectionListener will be notified
	 * and the Status of the ConnectionRequest will be STATUS_CANCELED.
	 * @see IConnectionListener
	 */
	public void cancelConnection() {
		manager.cancelConnection(this);
	}
}