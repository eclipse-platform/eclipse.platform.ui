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
import java.io.*;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;
import org.eclipse.team.internal.ccvs.ssh.SSHServerConnection;
import org.eclipse.team.internal.core.streams.*;

import com.jcraft.jsch.*;

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
		internalOpen(monitor);
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
				session = JSchSession.getSession(location, username, password, hostname, port, new JSchSession.ResponsiveSocketFacory(monitor));
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
			    String message = e.getMessage();
			    if (message.equals("Auth fail")) { //$NON-NLS-1$
			        message = Policy.bind("CVSSSH2ServerConnection.0"); //$NON-NLS-1$
			        // Could possibly retry below but wont just in case
			    } else if (message.startsWith("Session.connect: ")) { //$NON-NLS-1$
			        // Jsh has messages formatted like "Session.connect: java.net.NoRouteToHostException: ..."
			        // Strip of the exception and try to convert it to a more meaningfull string
			        int start = message.indexOf(": ") + 1; //$NON-NLS-1$
			        if (start != -1) {
				        int end = message.indexOf(": ", start); //$NON-NLS-1$
				        if (end != -1) {
				            String exception = message.substring(start, end).trim();
				            if (exception.indexOf("NoRouteToHostException") != -1) { //$NON-NLS-1$
				                message = Policy.bind("CVSSSH2ServerConnection.1", location.getHost()); //$NON-NLS-1$
				                throw new NoRouteToHostException(message);
				            } else if (exception.indexOf("java.net.UnknownHostException") != -1) { //$NON-NLS-1$
				                throw new UnknownHostException(location.getHost());
				            } else {
				                message = message.substring(end + 1).trim();
				            }
				        }
			        }
			    }
				throw new CVSAuthenticationException(message, CVSAuthenticationException.NO_RETRY);
			}
			ssh1 = new SSHServerConnection(location, password);
			if (ssh1 == null) {
				throw new CVSAuthenticationException(e.toString(), CVSAuthenticationException.NO_RETRY);
			}
			ssh1.open(monitor);
		}
	}
}
