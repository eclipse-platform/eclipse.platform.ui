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
package org.eclipse.team.tests.ccvs.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;

/**
 * @author Administrator
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestConnection implements IServerConnection {

	public static TestConnection currentConnection;
	
	public static List previousLines;
	public static StringBuffer currentLine;
	
	
	private ByteArrayInputStream serverResponse;
	
	private static final String VALID_SERVER_REQUESTS = "Valid-requests Root Valid-responses valid-requests Repository Directory Max-dotdot Static-directory Sticky Checkin-prog Update-prog Entry Kopt Checkin-time Modified Is-modified UseUnchanged Unchanged Notify Questionable Case Argument Argumentx Global_option Gzip-stream wrapper-sendme-rcsOptions Set Kerberos-encrypt Gssapi-encrypt Gssapi-authenticate expand-modules ci co update diff log rlog add remove update-patches gzip-file-contents status rdiff tag rtag import admin export history release watch-on watch-off watch-add watch-remove watchers editors init annotate rannotate noop version";

	public static IServerConnection createConnection(ICVSRepositoryLocation location, String password) {
		currentConnection = new TestConnection();
		return currentConnection;
	}
	
	public static String getLastLine() {
		if (previousLines.isEmpty()) return null;
		return (String)previousLines.get(previousLines.size() - 1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IServerConnection#open(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void open(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
		resetStreams();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IServerConnection#close()
	 */
	public void close() throws IOException {
		resetStreams();
	}

	/**
	 * 
	 */
	private void resetStreams() {
		currentLine = new StringBuffer();
		previousLines = new ArrayList();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IServerConnection#getInputStream()
	 */
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return new InputStream() {
			public int read() throws IOException {
				if (serverResponse == null) {
					throw new IOException("Not prepared to make a response");
				} else {
					return serverResponse.read();
				}	
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IServerConnection#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return new OutputStream() {
			public void write(int output) throws IOException {
				byte b = (byte)output;
				if (b == '\n') {
					String sentLine = currentLine.toString();
					previousLines.add(sentLine);
					currentLine = new StringBuffer();
					respondToSentLine(sentLine);
				} else {
					currentLine.append((char)b);
				}
			}
		};
	}

	/**
	 * @param sentLine
	 */
	protected void respondToSentLine(String sentLine) {
		if (sentLine.equals("valid-requests")) {
			serverResponse = new ByteArrayInputStream((VALID_SERVER_REQUESTS + "\nok\n").getBytes());
		}
	}

}
