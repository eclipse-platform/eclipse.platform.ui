package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Implements a connection method which invokes an external tool to 
 * establish the connection to the cvs server. Authentication and starting
 * of the cvs server are the responsibility of the external connection
 * tool.
 */
public class ExtConnection implements IServerConnection {

	// command to start remote cvs in server mode
	private static final String INVOKE_SVR_CMD = "server"; //$NON-NLS-1$
	
	// The default port for rsh
	private static final int DEFAULT_PORT = 9999;

	// cvs format for the repository (e.g. :extssh:user@host:/home/cvs/repo)
	private ICVSRepositoryLocation location;

	// incoming from remote host
	InputStream inputStream;

	// outgoing to remote host
	OutputStream outputStream;
	
	// Process spawn to run the command
	Process process;
	
	protected ExtConnection(ICVSRepositoryLocation location, String password) {
		this.location = location;
		// passwork not needed, authentication performed by external tool
	}
	
	/**
	 * Closes the connection.
	 */
	public void close() throws IOException {
		inputStream.close();
		outputStream.close();
		process.destroy();
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
	public void open(IProgressMonitor monitor) throws IOException {
		String hostname = location.getHost();
		String username = location.getUsername();
		
		String CVS_RSH = CVSProviderPlugin.getPlugin().getCvsRshCommand();
		String CVS_SERVER = CVSProviderPlugin.getPlugin().getCvsServer();
		String[] command = new String[] {CVS_RSH, "-l", username, hostname, CVS_SERVER, INVOKE_SVR_CMD}; //$NON-NLS-1$
		
		int port = location.getPort();
		if (port == location.USE_DEFAULT_PORT)
			port = DEFAULT_PORT;
		try {
			// The command line doesn't support the use of a port
			if (port != DEFAULT_PORT)
				throw new IOException(Policy.bind("EXTServerConnection.invalidPort")); //$NON-NLS-1$
					
			if(CVS_RSH == null || CVS_SERVER == null) {
				throw new IOException(Policy.bind("EXTServerConnection.varsNotSet"));				 //$NON-NLS-1$
			}		
				
			process =	Runtime.getRuntime().exec(command);

			inputStream = process.getInputStream();
			outputStream = process.getOutputStream();
		} catch (IOException ex) {
			try {
				close();
			} finally {
				throw new IOException(Policy.bind("EXTServerConnection.ioError", CVS_RSH)); //$NON-NLS-1$
			}
		} catch (RuntimeException e) {
			try {
				close();
			} finally {
				throw new IOException(Policy.bind("EXTServerConnection.ioError", CVS_RSH)); //$NON-NLS-1$
			}
		}
	}
}