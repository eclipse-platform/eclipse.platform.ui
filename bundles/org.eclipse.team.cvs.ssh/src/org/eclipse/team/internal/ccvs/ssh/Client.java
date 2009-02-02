/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

/**
 * An SSH 1.5 client..
 */

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.internal.core.streams.PollingInputStream;
import org.eclipse.team.internal.core.streams.PollingOutputStream;
import org.eclipse.team.internal.core.streams.TimeoutOutputStream;

public class Client {
	// client identification string
	private static final String clientId = "SSH-1.5-Java 1.2.2\n"; //$NON-NLS-1$

	// server identification string
	private static String serverId = null;

	// maximum outgoing packet size
	private static final int MAX_CLIENT_PACKET_SIZE = 1024;

	// packet types
	private static final int SSH_MSG_DISCONNECT = 1;
	private static final int SSH_SMSG_PUBLIC_KEY = 2;
	private static final int SSH_CMSG_SESSION_KEY = 3;
	private static final int SSH_CMSG_USER = 4;
	private static final int SSH_CMSG_AUTH_PASSWORD = 9;
	private static final int SSH_CMSG_REQUEST_PTY = 10;
	private static final int SSH_CMSG_EXEC_SHELL = 12;
	private static final int SSH_CMSG_EXEC_CMD = 13;
	private static final int SSH_SMSG_SUCCESS = 14;
	private static final int SSH_SMSG_FAILURE = 15;
	private static final int SSH_CMSG_STDIN_DATA = 16;
	private static final int SSH_SMSG_STDOUT_DATA = 17;
	private static final int SSH_SMSG_STDERR_DATA = 18;
	private static final int SSH_SMSG_EXITSTATUS = 20;
	private static final int SSH_CMSG_EXIT_CONFIRMATION = 33;
	private static final int SSH_MSG_DEBUG = 36;

	// cipher names
	private static String[] cipherNames = { "None", "IDEA", "DES", "3DES", "TSS", "RC4", "Blowfish" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	// cipher types
	private static int SSH_CIPHER_BLOWFISH = 6;

	// preferred cipher types
	private int[] preferredCipherTypes = { SSH_CIPHER_BLOWFISH };

	private String host;
	private int port;
	private String username;
	private String password;
	private String command;

	private Socket socket;
	/* package */ InputStream socketIn;
	private PollingOutputStream socketOut;
	private InputStream is;
	private OutputStream os;
	private boolean connected = false;
	private int timeout = -1;

	private Cipher cipher = null;

	class StandardInputStream extends InputStream {
		private ServerPacket packet = null;
		private InputStream buffer = null;
		private boolean atEnd = false;
		private boolean closed = false;

		public int available() throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed); 
			}

			int available = buffer == null ? 0 : buffer.available();

			if (available == 0 && socketIn.available() > 0) {
				fill();
				if (atEnd) {
					return 0;
				}
				available = buffer.available();
			}

			return available;
		}

		public void close() throws IOException {
			if (!closed) {
				closed = true;
				if (packet != null) {
					packet.close(false);
					buffer = null;
					packet = null;
				}
			}
		}

		public int read() throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			if (atEnd) {
				return -1;
			}

			if (buffer == null || buffer.available() == 0) {
				fill();
				if (atEnd) {
					return -1;
				}
			}

			return buffer.read();
		}

		public int read(byte b[], int off, int len) throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			if (atEnd) {
				return -1;
			}

			if (buffer == null || buffer.available() == 0) {
				fill();
				if (atEnd) {
					return -1;
				}
			}

			return buffer.read(b, off, len);
		}

		private void fill() throws IOException {
			if (buffer != null) {
				buffer.close();
			}

			packet = skip_SSH_MSG_DEBUG();
			int packetType = packet.getType();

			switch (packetType) {
				case SSH_SMSG_STDOUT_DATA :
				case SSH_SMSG_STDERR_DATA :
				case SSH_MSG_DEBUG :
					buffer = packet.getInputStream();
					Misc.readInt(buffer);
					break;
				case SSH_SMSG_EXITSTATUS :
					buffer = null;
					atEnd = true;
					InputStream pis = packet.getInputStream();
					Misc.readInt(pis);
					pis.close();
					send(SSH_CMSG_EXIT_CONFIRMATION, null);
					break;
				case SSH_MSG_DISCONNECT :
					buffer = null;
					atEnd = true;
					handleDisconnect(packet.getInputStream());
					break;
				default :
					throw new IOException(NLS.bind(CVSSSHMessages.Client_packetType, (new Object[] {new Integer(packetType)})));
			}
		}
		
		private void handleDisconnect(InputStream in) throws IOException {
			String description = null;
			try {
				description = Misc.readString(in);
			} catch (IOException e) {
				// ignore
			} finally {
				in.close();
			}
			
			// Log the description provided by the server
			if (description == null) {
				description = CVSSSHMessages.Client_noDisconnectDescription; 
			}
			
			// Throw an IOException with the proper text
			throw new IOException(NLS.bind(CVSSSHMessages.Client_disconnectDescription, (new Object[] {description})));
		}
	}

	class StandardOutputStream extends OutputStream {
		private int MAX_BUFFER_SIZE = MAX_CLIENT_PACKET_SIZE;
		private byte[] buffer = new byte[MAX_BUFFER_SIZE];
		private int bufpos = 0;
		private boolean closed = false;

		public void close() throws IOException {
			if (!closed) {
				try {
					flush();
				} finally {
					closed = true;
				}
			}
		}

		public void flush() throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			if (bufpos > 0) {
				send(SSH_CMSG_STDIN_DATA, buffer, 0, bufpos);
				bufpos = 0;
			}
		}

		public void write(int b) throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			buffer[bufpos++] = (byte) b;

			if (bufpos == MAX_BUFFER_SIZE) {
				flush();
			}
		}

		public void write(byte b[], int off, int len) throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed); 
			}

			int bytesWritten = 0;
			int totalBytesWritten = 0;

			if (bufpos > 0) {
				bytesWritten = Math.min(MAX_BUFFER_SIZE - bufpos, len);
				System.arraycopy(b, off, buffer, bufpos, bytesWritten);
				bufpos += bytesWritten;
				totalBytesWritten += bytesWritten;

				if (bufpos == MAX_BUFFER_SIZE) {
					flush();
				}
			}

			while (len - totalBytesWritten >= MAX_BUFFER_SIZE) {
				send(SSH_CMSG_STDIN_DATA, b, off + totalBytesWritten, MAX_BUFFER_SIZE);
				totalBytesWritten += MAX_BUFFER_SIZE;
			}

			if (totalBytesWritten < len) {
				bytesWritten = len - totalBytesWritten;
				System.arraycopy(b, off + totalBytesWritten, buffer, 0, bytesWritten);
				bufpos += bytesWritten;
			}
		}
	}
public Client(String host, int port, String username, String password) {
	this.host = host;
	this.port = port;
	this.username = username;
	this.password = password;
}
public Client(String host, int port, String username, String password, String command) {
	this(host, port, username, password);
	this.command = command;
}
public Client(String host, int port, String username, String password, String command, int timeout) {
	this(host, port, username, password, command);
	this.timeout = timeout;
}
/**
 * Close all streams and sockets.
 */
private void cleanup() throws IOException {
	try {
		if (is != null)
			is.close();
	} finally {
		try {
			if (os != null)
				os.close();
		} finally {
			try {
				if (socketIn != null)
					socketIn.close();
			} finally {
				try {
					if (socketOut != null)
						socketOut.close();
				} finally {
					try {
						if (socket != null)
							socket.close();
					} finally {
						socket = null;
					}
				}
			}
		}
	}
}
/**
 * Connect to the remote server. If an exception is thrown, the caller
 * can asssume that all streams and sockets are closed.
 */
public void connect(IProgressMonitor monitor) throws IOException, CVSAuthenticationException {
	// If we're already connected, just ignore the invokation
	if (connected || monitor.isCanceled())
		return;
		
	// Otherwise, set up the connection
	try {
		PollingInputStream pollingInputStream = null;
		// Create the socket (the socket should always be null here)
		if (socket == null) {
			try {
				socket = Util.createSocket(host, port, monitor);
				// Bug 36351: disable buffering and send bytes immediately
				socket.setTcpNoDelay(true);
			} catch (InterruptedIOException e) {
				// If we get this exception, chances are the host is not responding
				throw new InterruptedIOException(NLS.bind(CVSSSHMessages.Client_socket, (new Object[] {host})));

			}
			if (timeout >= 0) {
				socket.setSoTimeout(1000);
			}
			pollingInputStream = new PollingInputStream(socket.getInputStream(),
				timeout > 0 ? timeout : 1, monitor);
			socketIn = new BufferedInputStream(pollingInputStream);
			
			socketOut = new PollingOutputStream(new TimeoutOutputStream(
				socket.getOutputStream(), 8192 /*bufferSize*/, 1000 /*writeTimeout*/, 1000 /*closeTimeout*/),
				timeout > 0 ? timeout : 1, monitor);
		}

		// read the ssh server id. The socket creation may of failed if the
		// server cannot accept our connection request. We don't expect the
		// socket to be closed at this point.
		// Don't allow cancellation during the initial handshake and login since this
		// can potentially cause the SSH server to think that it is being hacked and
		// disable the account.		
		socketOut.setIsCancellable(false /* don't allow cancellation */);
		pollingInputStream.setIsCancellable(false);
		StringBuffer buf = new StringBuffer();
		int c;
		while ((c = socketIn.read()) != '\n') {
			if (c == -1)
				throw new IOException(CVSSSHMessages.Client_socketClosed);
			buf.append((char) c);
		}
		serverId = buf.toString();
		
		if (Policy.DEBUG_SSH_PROTOCOL) {
			System.out.println("SSH > server ID: " + serverId); //$NON-NLS-1$
			System.out.println("SSH > client ID: " + clientId); //$NON-NLS-1$
		}
		
		if (!serverId.startsWith("SSH-1.")) { //$NON-NLS-1$
			String sshVersion = (serverId.startsWith("SSH-")? serverId:""); //$NON-NLS-1$ //$NON-NLS-2$
			throw new IOException(NLS.bind(CVSSSHMessages.Client_sshProtocolVersion, new String[] { sshVersion }));
		} 
		
		// send our id.
		socketOut.write(clientId.getBytes());
		socketOut.flush();

		login();

		socketOut.setIsCancellable(true /* allow cancellation */);
		pollingInputStream.setIsCancellable(true);
		
		// start a shell and enter interactive session or start by
		// executing the given command.
		if( command == null ) {
			startShell();
		} else {
			executeCommand();
		}

		is = new StandardInputStream();
		os = new StandardOutputStream();
		connected = true;
	// If an exception occurs while connected, make sure we disconnect before passing the exception on
	} finally {
		if (! connected) cleanup();
	}
}
/**
 * Terminate the connection to the server.
 */
public void disconnect() throws IOException {
	if (Policy.DEBUG_SSH_PROTOCOL) {
		System.out.println("Disconnecting."); //$NON-NLS-1$
	}	
	if (connected) {
		connected = false;
		try {
			send(SSH_MSG_DISCONNECT, null);
		} finally {
			cleanup();
		}
	}
}
public InputStream getInputStream() throws IOException {
	if (!connected) {
		throw new IOException(CVSSSHMessages.Client_notConnected);
	}

	return is;
}
public OutputStream getOutputStream() throws IOException {
	if (!connected) {
		throw new IOException(CVSSSHMessages.Client_notConnected);
	}

	return os;
}

private void startShell() throws IOException {
	ServerPacket packet = null;
	int packetType;

	send_SSH_CMSG_REQUEST_PTY();

	try {
		packet = skip_SSH_MSG_DEBUG();
		packetType = packet.getType();

		if (packetType != SSH_SMSG_SUCCESS) {
			throw new IOException(NLS.bind(CVSSSHMessages.Client_packetType, (new Object[] {new Integer(packetType)})));
		}
	} finally {
		if (packet != null) {
			packet.close(true /*perform crc check*/);
		}
	}

	send(SSH_CMSG_EXEC_SHELL, null);
}

private void executeCommand() throws IOException {	
	send(SSH_CMSG_EXEC_CMD, command);
}

private void login() throws IOException, CVSAuthenticationException {
	ServerPacket packet = null;
	int packetType;

	try {
		packet = skip_SSH_MSG_DEBUG();
		packetType = packet.getType();

		if (packetType != SSH_SMSG_PUBLIC_KEY) {
			throw new IOException(NLS.bind(CVSSSHMessages.Client_packetType, (new Object[] {new Integer(packetType)})));
		}

		receive_SSH_SMSG_PUBLIC_KEY(packet);
	} finally {
		if (packet != null) {
			packet.close(true);
		}
	}

	try {
		packet = skip_SSH_MSG_DEBUG();
		packetType = packet.getType();

		if (packetType != SSH_SMSG_SUCCESS) {
			throw new IOException(NLS.bind(CVSSSHMessages.Client_packetType, (new Object[] {new Integer(packetType)})));
		}
	} finally {
		if (packet != null) {
			packet.close(true);
		}
	}

	send(SSH_CMSG_USER, username);

	try {
		packet = skip_SSH_MSG_DEBUG();
		packetType = packet.getType();

		if (packetType != SSH_SMSG_FAILURE) {
			throw new IOException(NLS.bind(CVSSSHMessages.Client_packetType, (new Object[] {new Integer(packetType)})));
		}
	} finally {
		if (packet != null) {
			packet.close(true);
		}
	}

	send(SSH_CMSG_AUTH_PASSWORD, password);

	try {
		packet = skip_SSH_MSG_DEBUG();
		packetType = packet.getType();

		if (packetType == SSH_SMSG_FAILURE) {
			throw new CVSAuthenticationException(CVSSSHMessages.Client_authenticationFailed, CVSAuthenticationException.RETRY);
		}

		if (packetType != SSH_SMSG_SUCCESS) {
			throw new IOException(NLS.bind(CVSSSHMessages.Client_packetType, (new Object[] {new Integer(packetType)})));
		}
	} finally {
		if (packet != null) {
			packet.close(true);
		}
	}
}
private void receive_SSH_SMSG_PUBLIC_KEY(ServerPacket packet) throws IOException, CVSAuthenticationException {
	InputStream pis = packet.getInputStream();

	byte[] anti_spoofing_cookie = new byte[8];
	Misc.readFully(pis, anti_spoofing_cookie);

	byte[] server_key_bits = new byte[4];
	Misc.readFully(pis, server_key_bits);

	byte[] server_key_public_exponent = Misc.readMpInt(pis);
	byte[] server_key_public_modulus = Misc.readMpInt(pis);

	byte[] host_key_bits = new byte[4];
	Misc.readFully(pis, host_key_bits);

	byte[] host_key_public_exponent = Misc.readMpInt(pis);
	byte[] host_key_public_modulus = Misc.readMpInt(pis);

	byte[] protocol_flags = new byte[4];
	Misc.readFully(pis, protocol_flags);

	byte[] supported_ciphers_mask = new byte[4];
	Misc.readFully(pis, supported_ciphers_mask);

	byte[] supported_authentications_mask = new byte[4];
	Misc.readFully(pis, supported_authentications_mask);

	pis.close();

	send_SSH_CMSG_SESSION_KEY(anti_spoofing_cookie, host_key_bits, server_key_public_modulus, host_key_public_modulus, supported_ciphers_mask, server_key_public_exponent, host_key_public_exponent);
}
void send(int packetType, String s) throws IOException {
	byte[] data = s == null ? new byte[0] : s.getBytes("UTF-8"); //$NON-NLS-1$
	send(packetType, data, 0, data.length);
}
void send(int packetType, byte[] data, int off, int len) throws IOException {
	data = data == null ? null : Misc.lengthEncode(data, off, len);
	ClientPacket packet = new ClientPacket(packetType, data, cipher);
	socketOut.write(packet.getBytes());
	socketOut.flush();
}
private void send_SSH_CMSG_REQUEST_PTY() throws IOException {
	byte packet_type = SSH_CMSG_REQUEST_PTY;

	byte[] termType = Misc.lengthEncode("dumb".getBytes(), 0, 4);//$NON-NLS-1$
	byte[] row = {0, 0, 0, 0};
	byte[] col = {0, 0, 0, 0};
	byte[] XPixels = {0, 0, 0, 0};
	byte[] YPixels = {0, 0, 0, 0};
	byte[] terminalModes = {0};

	byte[] data = new byte[termType.length + row.length + col.length + XPixels.length + YPixels.length + terminalModes.length];

	int offset = 0;
	System.arraycopy(termType, 0, data, offset, termType.length);

	offset += termType.length;
	System.arraycopy(row, 0, data, offset, row.length);

	offset += row.length;
	System.arraycopy(col, 0, data, offset, col.length);

	offset += col.length;
	System.arraycopy(XPixels, 0, data, offset, XPixels.length);

	offset += XPixels.length;
	System.arraycopy(YPixels, 0, data, offset, YPixels.length);

	offset += YPixels.length;
	System.arraycopy(terminalModes, 0, data, offset, terminalModes.length);

	ClientPacket packet = new ClientPacket(packet_type, data, cipher);
	socketOut.write(packet.getBytes());
	socketOut.flush();
}
private void send_SSH_CMSG_SESSION_KEY(byte[] anti_spoofing_cookie, byte[] host_key_bits, byte[] server_key_public_modulus, byte[] host_key_public_modulus, byte[] supported_ciphers_mask, byte[] server_key_public_exponent, byte[] host_key_public_exponent) throws IOException, CVSAuthenticationException {
	byte packet_type = SSH_CMSG_SESSION_KEY;

	// session_id
	byte[] session_id = new byte[host_key_public_modulus.length + server_key_public_modulus.length + anti_spoofing_cookie.length];

	int offset = 0;
	System.arraycopy(host_key_public_modulus, 0, session_id, offset, host_key_public_modulus.length);

	offset += host_key_public_modulus.length;
	System.arraycopy(server_key_public_modulus, 0, session_id, offset, server_key_public_modulus.length);

	offset += server_key_public_modulus.length;
	System.arraycopy(anti_spoofing_cookie, 0, session_id, offset, anti_spoofing_cookie.length);

	session_id = Misc.md5(session_id);

	// cipher_type
	byte cipher_type = 0;
	boolean foundSupportedCipher = false;

	for (int i = 0; i < preferredCipherTypes.length && !foundSupportedCipher; ++i) {
		cipher_type = (byte) preferredCipherTypes[i];
		foundSupportedCipher = (supported_ciphers_mask[3] & (byte) (1 << cipher_type)) != 0;
	}

	if (!foundSupportedCipher) {
		throw new IOException(CVSSSHMessages.Client_cipher);
	}

	// session_key
	byte[] session_key = new byte[32];
	byte[] session_key_xored = new byte[32];
	byte[] session_key_encrypted = null;

	Misc.random(session_key, 0, session_key.length, true);
	System.arraycopy(session_key, 0, session_key_xored, 0, session_key.length);
	Misc.xor(session_key_xored, 0, session_id, 0, session_key_xored, 0, session_id.length);

	BigInteger host_e = new BigInteger(1, host_key_public_exponent);
	BigInteger host_n = new BigInteger(1, host_key_public_modulus);
	if (!new KnownHosts().verifyKey(host, host_key_bits, host_e, host_n)) {
		throw new CVSAuthenticationException(CVSSSHMessages.Client_hostIdChanged, CVSAuthenticationException.NO_RETRY); 
	}
	byte[] result;
	if (new BigInteger(1,server_key_public_modulus).compareTo(host_n) == -1) {
		result = Misc.encryptRSAPkcs1(session_key_xored, server_key_public_exponent, server_key_public_modulus);
		result = Misc.encryptRSAPkcs1(result, host_key_public_exponent, host_key_public_modulus);
	} else {
		result = Misc.encryptRSAPkcs1(session_key_xored, host_key_public_exponent, host_key_public_modulus);
		result = Misc.encryptRSAPkcs1(result, server_key_public_exponent, server_key_public_modulus);	 
	}

	session_key_encrypted = new byte[result.length + 2];
	session_key_encrypted[1] = (byte) ((8 * result.length) & 0xff);
	session_key_encrypted[0] = (byte) (((8 * result.length) >> 8) & 0xff);

	for (int i = 0; i < result.length; i++) {
		session_key_encrypted[i + 2] = result[i];
	}

	// protocol_flags
	byte[] protocol_flags = {0, 0, 0, 0};

	// data
	byte[] data = new byte[1 + anti_spoofing_cookie.length + session_key_encrypted.length + protocol_flags.length];

	offset = 0;
	data[offset++] = cipher_type;

	System.arraycopy(anti_spoofing_cookie, 0, data, offset, anti_spoofing_cookie.length);

	offset += anti_spoofing_cookie.length;
	System.arraycopy(session_key_encrypted, 0, data, offset, session_key_encrypted.length);

	offset += session_key_encrypted.length;
	System.arraycopy(protocol_flags, 0, data, offset, protocol_flags.length);

	// cipher
	cipher = Cipher.getInstance(cipherNames[cipher_type]);
	cipher.setKey(session_key);

	// packet
	ClientPacket packet = new ClientPacket(packet_type, data, null);
	socketOut.write(packet.getBytes());
	socketOut.flush();
}

ServerPacket skip_SSH_MSG_DEBUG() throws IOException {
	ServerPacket packet = new ServerPacket(socketIn, cipher);
	while (packet.getType() == SSH_MSG_DEBUG) {
		packet.close(true);
		packet = new ServerPacket(socketIn, cipher);
	}
	
	return packet;
}
}
