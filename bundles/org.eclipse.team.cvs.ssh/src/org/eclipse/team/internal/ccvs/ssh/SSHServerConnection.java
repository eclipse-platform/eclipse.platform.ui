/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;

public class SSHServerConnection implements IServerConnection {
	
	// command to start remote cvs in server mode
	private static final String INVOKE_SVR_CMD = "cvs server"; //$NON-NLS-1$
	
	private static final int DEFAULT_PORT = 22;
	
	// cvs format for the repository (e.g. :extssh:user@host:/home/cvs/repo)
	private ICVSRepositoryLocation location;
	
	// password for user specified in repository location string
	private String password;
	
	// incoming from remote host
	InputStream inputStream;
	
	// outgoing to remote host
	OutputStream outputStream;
	
	// ssh client 
	Client client;

	public SSHServerConnection(ICVSRepositoryLocation location, String password) {
		if (password == null) {
			password = ""; //$NON-NLS-1$
		}
		this.location = location;
		this.password = password;
	}

	public void close() throws IOException {
		client.disconnect();
	}
	/**
	 * Returns the <code>InputStream</code> used to read data from the
	 * server.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	/**
	 * Returns the <code>OutputStream</code> used to send data to the
	 * server.
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * Opens the connection and invokes cvs in server mode.
	 *
	 * @see Connection.open()
	 */
	public void open(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
		monitor.subTask(CVSSSHMessages.SSHServerConnection_authenticating); 
		monitor.worked(1);
		String hostname = location.getHost();
		String username = location.getUsername();
		int port = location.getPort();
		if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT)
			port = DEFAULT_PORT;
		// create the connection using host, username, and password
		client = new Client(hostname, port, username, password, INVOKE_SVR_CMD, location.getTimeout());	
		client.connect(monitor);
		inputStream = client.getInputStream();
		outputStream = client.getOutputStream();
	}
}
