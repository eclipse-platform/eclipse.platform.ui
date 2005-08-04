/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;
import java.io.*;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
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
	private final class SSH2IOException extends IOException {
        private static final long serialVersionUID = 1L;

        private final JSchException e;

        private SSH2IOException(String s, JSchException e) {
            super(s);
            this.e = e;
        }

        public Throwable getCause() {
            return e;
        }
    }
    private static final String COMMAND = "cvs server"; //$NON-NLS-1$
	private ICVSRepositoryLocation location;
	private String password;
	private InputStream inputStream;
	private OutputStream outputStream;
	private JSchSession session;
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
		monitor.subTask(NLS.bind(CVSSSH2Messages.CVSSSH2ServerConnection_open, new String[] { location.getHost() })); 
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
			OutputStream channel_out = null;
			InputStream channel_in = null;
            boolean firstTime = true;
            boolean tryAgain = false;
			while (firstTime || tryAgain) {
                tryAgain = false; // reset the try again flag
				session = JSchSession.getSession(location, location.getUsername(), password, location.getHost(), location.getPort(), monitor);
				channel = session.getSession().openChannel("exec"); //$NON-NLS-1$
				((ChannelExec) channel).setCommand(COMMAND);
				channel_out = channel.getOutputStream();
				channel_in = channel.getInputStream();
				try {
					channel.connect();
				} catch (JSchException ee) {
                    // This strange logic is here due to how the JSch client shares sessions.
                    // It is possible that we have obtained a session that thinks it is connected
                    // but is not. Channel connection only works if the session is connected so the
                    // above channel connect may fail because the session is down. For this reason,
                    // we want to retry if the connection fails.
                    try {
                        if (firstTime && (isSessionDownError(ee) || isChannelNotOpenError(ee))) {
                            tryAgain = true;
                        }
                        if (!tryAgain) {
                            throw ee;
                        }
                    } finally {
                        // Always dispose of the current session when a failure occurs so we can start from scratch
                        session.dispose();
                    }
				}
                firstTime = false; // the first time is done
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
		} catch (final JSchException e) {
			if (isSSH2Unsupported(e)) {
                ssh1 = new SSHServerConnection(location, password);
                if (ssh1 == null) {
                    throw new SSH2IOException(e.toString(), e);
                }
                ssh1.open(monitor);
            } else {
			    String message = e.getMessage();
			    if (JSchSession.isAuthenticationFailure(e)) {
                    // Do not retry as the Jsh library has it's own retry logic
                    throw new CVSAuthenticationException(CVSSSH2Messages.CVSSSH2ServerConnection_0, CVSAuthenticationException.NO_RETRY, e); 
			    } else if (message.startsWith("Session.connect: ")) { //$NON-NLS-1$
			        // Jsh has messages formatted like "Session.connect: java.net.NoRouteToHostException: ..."
			        // Strip of the exception and try to convert it to a more meaningfull string
			        int start = message.indexOf(": ") + 1; //$NON-NLS-1$
			        if (start != -1) {
				        int end = message.indexOf(": ", start); //$NON-NLS-1$
				        if (end != -1) {
				            String exception = message.substring(start, end).trim();
				            if (exception.indexOf("NoRouteToHostException") != -1) { //$NON-NLS-1$
				                message = NLS.bind(CVSSSH2Messages.CVSSSH2ServerConnection_1, new String[] { location.getHost() }); 
				                throw new NoRouteToHostException(message);
				            } else if (exception.indexOf("java.net.UnknownHostException") != -1) { //$NON-NLS-1$
				                throw new UnknownHostException(location.getHost());
				            } else {
				                message = message.substring(end + 1).trim();
				            }
				        }
			        }
			    }
 				throw new SSH2IOException(message, e);
			}
		}
	}
    
    private boolean isChannelNotOpenError(JSchException ee) {
        return ee.getMessage().indexOf("channel is not opened") != -1; //$NON-NLS-1$
    }
    private boolean isSessionDownError(JSchException ee) {
        return ee.getMessage().equals("session is down"); //$NON-NLS-1$
    }
    private boolean isSSH2Unsupported(JSchException e) {
        return e.toString().indexOf("invalid server's version string") != -1; //$NON-NLS-1$
    }
}
