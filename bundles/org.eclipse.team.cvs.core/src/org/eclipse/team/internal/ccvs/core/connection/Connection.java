package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.*;

/**
 * A connection to talk to a cvs server. The life cycle of a connection is
 * as follows:
 * <ul>
 *	<li> constructor: creates a new connection object that wraps the given
 *       repository location and connection method.
 *	<li> open: opens a connection.
 *	<li> send a request: use write* method or use the request stream directly.
 *	     <code>GetRequestStream</code> returns an output stream to directly
 *	     talk to the server.
 *	<li> read responses: use read* methods or use the response stream directly.
 *	     <code>GetResponseStream</code> returns an input stream to directly
 *	     read output from the server.
 *	<li> close: closes the connection. A closed connection can be reopened by
 *	     calling open again.
 * </ul>
 */
public class Connection {
	
	//private static final boolean DEBUG= System.getProperty("cvsclient.debug")!=null;
	private static final boolean DEBUG=true;
	
	public static final byte NEWLINE= 0xA;
	
	private IServerConnection serverConnection;
	
	private ICVSRepositoryLocation fCVSRoot;
	private String fCVSRootDirectory;
	private boolean fIsEstablished;
	private BufferedInputStream fResponseStream;
	private char fLastUsedTokenDelimiter;

	boolean closed = false;

	public Connection(ICVSRepositoryLocation cvsroot, IServerConnection serverConnection) {
		fCVSRoot = cvsroot;
		this.serverConnection = serverConnection;
	}
	
	private static byte[] append(byte[] buffer, int index, byte b) {
		if (index >= buffer.length) {
			byte[] newBuffer= new byte[index * 2];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
			buffer= newBuffer;
		}
		buffer[index]= b;
		return buffer;
	}
	/**
	 * Closes the connection.
	 */
	public void close() throws CVSException {
		if (!isEstablished())
			return;
		try {
			// Perhaps it should be left to the connection to deal with reading pending input!
			readPendingInput();
			serverConnection.close();
		} catch (IOException ex) {
			throw new CVSCommunicationException(Policy.bind("Connection.cannotClose"), ex);
		} finally {
			fResponseStream= null;
			fIsEstablished= false;
		}
	}
	/**
	 * Flushes the request stream.
	 */
	public void flush() throws CVSException {
		if (!isEstablished())
			return;
		try {
			getRequestStream().flush();	
		} catch(IOException e) {
			throw new CVSCommunicationException(e);
		}
	}
	//---- CVS root management -------------------------------------------------------
	
	/**
	 * Returns the CVS root.
	 */ 
	public ICVSRepositoryLocation getCVSRoot() {
		return fCVSRoot;
	}

	/**
	 * Returns the last delimiter character used to read a token.
	 */
	public char getLastUsedDelimiterToken() {
		return fLastUsedTokenDelimiter;
	}

	/**
	 * Returns the <code>OutputStream</code> used to send requests
	 * to the server.
	 */
	public OutputStream getRequestStream() throws CVSException {
		if (!isEstablished())
			return null;
		return serverConnection.getOutputStream();
	}
	/**
	 * Returns the <code>InputStream</code> used to read responses from
	 * the server.
	 */
	public InputStream getResponseStream() throws CVSException {
		if (!isEstablished())
			return null;
		if (fResponseStream == null)
			fResponseStream= new BufferedInputStream(serverConnection.getInputStream());
		return fResponseStream;	
	}

	public String getRootDirectory() throws CVSException {
		return getCVSRoot().getRootDirectory();
	}			

	/**
	 * Returns <code>true</code> if the connection is established;
	 * otherwise <code>false</code>.
	 */
	public boolean isEstablished() {
		return fIsEstablished;
	}
	//--- Helper to read strings from server -----------------------------------------
	
	/**
	 * Is input available in the response stream.
	 */
	// NIK: is not used
	public boolean isInputAvailable() {
		if (!isEstablished())
			return false;
		try {
			return getResponseStream().available() != 0;
		} catch (CVSException e) {
			return false;
		} catch (IOException e) {
			return false;
		}	
	}
	public boolean isClosed() {
		return closed;
	}
	/**
	 * Creates a blank separated string from the given string array.
	 */
	private String makeString(String[] s) {
		StringBuffer buffer= new StringBuffer();
		for (int i= 0; i < s.length; i++) {
			if (i != 0)
				buffer.append(' ');
			buffer.append(s[i]);
		}
		return buffer.toString();
	}
	/**
	 * Opens the connection.
	 */	
	public void open() throws CVSException {
		if (isEstablished())
			return;
		try {
			serverConnection.open();
		} catch (IOException e) {
			throw new CVSCommunicationException(e);
		}
		fIsEstablished= true;
	}
	/**
	 * Reads a line from the response stream.
	 */
	public String readLine() throws CVSException {
		return readLineOrUntil(-1);
	}
	
static String readLine(InputStream in) throws IOException {
	byte[] buffer = new byte[256];
	int index = 0;
	int r;
	while ((r = in.read()) != -1) {
		if (r == NEWLINE)
			break;
		buffer = append(buffer, index++, (byte) r);
	}
	String result = new String(buffer, 0, index);
	if (DEBUG)
		System.out.println(result);
	return result;
}

/**
 * Low level method to read a token.
 */
private String readLineOrUntil(int end) throws CVSException {
	if (!isEstablished())
		throw new CVSCommunicationException(Policy.bind("Connection.readUnestablishedConnection"));
	byte[] buffer = new byte[256];
	InputStream in = getResponseStream();
	int index = 0;
	int r;
	try {
		while ((r = in.read()) != -1) {
			if (r == NEWLINE || (end != -1 && r == end))
				break;
			buffer = append(buffer, index++, (byte) r);
		}
		switch (r) {
			case -1 :
				closed = true;
			case NEWLINE :
				fLastUsedTokenDelimiter = '\n';
				break;
			default :
				fLastUsedTokenDelimiter = (char) r;
		}
		String result = new String(buffer, 0, index);
		if (DEBUG)
			System.out.print(result + fLastUsedTokenDelimiter);
		return result;
	} catch (IOException e) {
		throw new CVSCommunicationException(e);
	}
}
	/**
	 * Reads any pending input from the response stream so that
	 * the stream can savely be closed.
	 */
	protected void readPendingInput() throws CVSException {
		byte[] buffer= new byte[2048];
		InputStream in= getResponseStream();
		OutputStream out= getRequestStream();
		try {
			while (true) {
				int available = in.available();
				if (available < 1) break;
				if (available > buffer.length) available = buffer.length;
				if (in.read(buffer, 0, available) < 1) break;
			}
			out.flush();
			while (true) {
				int available = in.available();
				if (available < 1) break;
				if (available > buffer.length) available = buffer.length;
				if (in.read(buffer, 0, available) < 1) break;
			}
		} catch (IOException e) {
			throw new CVSCommunicationException(e);
		}	
	}
	/**
	 * Reads a token from the response stream.
	 */
	public String readToken() throws CVSException {
		return readLineOrUntil(' ');
	}
	/**
	 * Sends the given array of strings to the server. The array's strings
	 * are concatenated using a blank.
	 */
	public void write(String[] a) throws CVSException {
		write(makeString(a), false);
	}
	//---- Helper to send strings to the server ----------------------------
	
	/**
	 * Sends the given string to the server.
	 */
	public void write(String s) throws CVSException {
		write(s, false);
	}
	/**
	 * Sends the given two strings separated by a blank to the
	 * server.
	 */
	public void write(String s1, String s2) throws CVSException {
		write(s1 + ' ' + s2, false);
	}
	/**
	 * Low level method to write a string to the server. All write* methods are
	 * funneled through this method.
	 */
	void write(String s, boolean newline) throws CVSException {
		if (!isEstablished())
			throw new CVSCommunicationException(Policy.bind("Connection.writeUnestablishedConnection"));
			
		if (DEBUG)
			System.out.print(s + (newline ? "\n" : ""));
	
		try {
			OutputStream out= getRequestStream();
			out.write(s.getBytes());
			if (newline)
				out.write(NEWLINE);
			out.flush();
			
		} catch (IOException e) {
			throw new CVSCommunicationException(e);
		}
	}
	/**
	 * Sends the given array of strings to the server. The array's strings
	 * are concatenated using a blank. Additionally a newline is sent.
	 */
	public void writeLine(String[] a) throws CVSException {
		write(makeString(a), true);
	}
	/**
	 * Sends the given string and a newline to the server. 
	 */
	public void writeLine(String s) throws CVSException {
		write(s, true);
	}
	/**
	 * Sends the given two strings separated by a blank to the
	 * server. Additionally a newline is sent.
	 */
	public void writeLine(String s1, String s2) throws CVSException {
		write(s1 + ' ' + s2, true);
	}
}
