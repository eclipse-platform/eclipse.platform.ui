/* -*-mode:java; c-basic-offset:2; -*- */
/*******************************************************************************
 * Copyright (c) 2003, Atsuhiko Yamanaka, JCraft,Inc. and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Atsuhiko Yamanaka, JCraft,Inc. - initial API and
 * implementation.
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;
import org.eclipse.team.internal.ccvs.ssh.SSHServerConnection;
import org.eclipse.team.internal.core.streams.PollingInputStream;
import org.eclipse.team.internal.core.streams.PollingOutputStream;
import org.eclipse.team.internal.core.streams.TimeoutInputStream;
import org.eclipse.team.internal.core.streams.TimeoutOutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * SSH2 connection method. Has the property of defaulting to SSH1 if the server
 * doesn't support SSH2. 
 */
public class CVSSSH2ServerConnection implements IServerConnection {
	private static final String COMMAND = "cvs server"; //$NON-NLS-1$
	private ICVSRepositoryLocation location;
	private String password;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Session session;
	private Channel channel;
	private IServerConnection ssh1;
	
	protected CVSSSH2ServerConnection(ICVSRepositoryLocation location, String password) {
		this.location = location;
		this.password = password;
	}
	public void close() throws IOException {
		if (ssh1 != null) {
			ssh1.close();
			ssh1 = null;
			return;
		}
		try {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Ignore I/O Exception on close
				}
			}
		} finally {
			try {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						// Ignore I/O Exception on close
					}
				}
			} finally {
				if (channel != null)
					channel.disconnect();
			}
		} 
	}
	public InputStream getInputStream() {
		if (ssh1 != null) {
			return ssh1.getInputStream();
		}
		return inputStream;
	}
	public OutputStream getOutputStream() {
		if (ssh1 != null) {
			return ssh1.getOutputStream();
		}
		return outputStream;
	}
	public void open(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
		if (ssh1 != null) {
			ssh1.open(monitor);
			return;
		}
		monitor.subTask(Policy.bind("CVSSSH2ServerConnection.open", location.getHost())); //$NON-NLS-1$
		monitor.worked(1);
		connectResponsively(monitor);
	}
	/**
	 * @param monitor
	 * @throws IOException
	 * @throws CVSAuthenticationException
	 */
	private void internalOpen(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
		try {
			String hostname = location.getHost();
			String username = location.getUsername();
			int port = location.getPort();
			if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT)
				port = 0;
			int retry = 1;
			OutputStream channel_out;
			InputStream channel_in;
			while (true) {
				session = JSchSession.getSession(location, username, password, hostname, port, new JSchSession.SimpleSocketFactory());
				channel = session.openChannel("exec"); //$NON-NLS-1$
				((ChannelExec) channel).setCommand(COMMAND);
				channel_out = channel.getOutputStream();
				channel_in = channel.getInputStream();
				try {
					channel.connect();
				} catch (JSchException ee) {
				  retry--;
				  if(retry<0){
				    throw new CVSAuthenticationException(Policy.bind("CVSSSH2ServerConnection.3"), CVSAuthenticationException.NO_RETRY); //$NON-NLS-1$
				  }
				  if(session.isConnected()){
				    session.disconnect();
				  }
				  continue;
				}
				break;
			}
			int timeout = location.getTimeout();
			inputStream = new PollingInputStream(new TimeoutInputStream(new FilterInputStream(channel_in) {
						public void close() throws IOException {
							// Don't close the underlying stream as it belongs to the session
						}
					},
					8192 /*bufferSize*/, 1000 /*readTimeout*/, -1 /*closeTimeout*/, true /* growWhenFull */), timeout > 0 ? timeout : 1, monitor);
			outputStream = new PollingOutputStream(new TimeoutOutputStream(new FilterOutputStream(channel_out) {
						public void close() throws IOException {
							// Don't close the underlying stream as it belongs to the session
						}
					},
					8192 /*buffersize*/, 1000 /*writeTimeout*/, 1000 /*closeTimeout*/), timeout > 0 ? timeout : 1, monitor);
		} catch (JSchException e) {
			if (e.toString().indexOf("invalid server's version string") == -1) { //$NON-NLS-1$
				throw new CVSAuthenticationException(e.toString(), CVSAuthenticationException.NO_RETRY);
			}
			ssh1 = new SSHServerConnection(location, password);
			if (ssh1 == null) {
				throw new CVSAuthenticationException(e.toString(), CVSAuthenticationException.NO_RETRY);
			}
			ssh1.open(monitor);
		}
	}
	
	/**
	 * Helper method that will time out when making a connection.
	 * This is required because there is no way to provide a timeout value
	 * when creating a socket and in some instances, they don't seem to
	 * timeout at all.
	 * @throws CVSAuthenticationException
	 * @throws IOException
	 */
	private void connectResponsively(final IProgressMonitor monitor) throws CVSAuthenticationException, IOException {
		// Start a thread to open a connection
		final Object lock = new Object();
		final Exception[] exception = new Exception[] {null };
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					// Attemp to make a connection
					internalOpen(monitor);
					// Ensure that we were not cancelled while waiting
					synchronized (lock) {
						if (Thread.interrupted()) {
							try {
								// we we're either cancelled or timed out so just close
								close();
							} catch (IOException e) {
								// Ignore close exception
							}
						}
					}
				} catch (CVSAuthenticationException e) {
					exception[0] = e;
				} catch (IOException e) {
					exception[0] = e;
				}
			}
		});
		thread.start();
		
		// Wait the appropriate number of seconds
		int timeout = CVSProviderPlugin.getPlugin().getTimeout();
		if (timeout == 0) timeout = CVSProviderPlugin.DEFAULT_TIMEOUT;
		for (int i = 0; i < timeout; i++) {
			try {
				// wait for the thread to complete or 1 second, which ever comes first
				thread.join(1000);
			} catch (InterruptedException e) {
				// I think this means the thread was interupted but not necessarily timed out
				// so we don't need to do anything
			}
			synchronized (lock) {
				// if the user cancelled, clean up before preempting the operation
				if (monitor.isCanceled()) {
					if (thread.isAlive()) {
						thread.interrupt();
					}
					if (session != null) {
						try {
							close();
						} catch (IOException e) {
							// Ignore close exception
						}
					}
					// this method will throw the proper exception
					Policy.checkCanceled(monitor);
				}
			}
		}
		// If the thread is still running (i.e. we timed out) signal that it is too late
		synchronized (lock) {
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}
		if (exception[0] != null) {
			if (exception[0] instanceof CVSAuthenticationException)
				throw (CVSAuthenticationException)exception[0];
			else
				throw (IOException)exception[0];
		}
		if (session == null) {
			throw new InterruptedIOException(Policy.bind("Util.timeout", location.getHost())); //$NON-NLS-1$
		}
	}
}
