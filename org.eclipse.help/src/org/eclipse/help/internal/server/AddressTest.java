package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.net.*;
import org.eclipse.help.internal.util.Resources;

/**
 * Test the local addresses to find out where one can connect
 * to the help server
 */
public class AddressTest {
	long timeout;

	// The connection class creates the socket.
	class Connection extends Thread {
		long timeout;
		int port;
		InetAddress inetAddress;
		String stringAddress;

		public Connection(String host, int port, long timeout) {
			this.stringAddress = host;
			this.port = port;
			this.timeout = timeout;
			setDaemon(true);
		}

		public void run() {
			if (socket != null)
				return;

			try {
				if (inetAddress != null) {
					socket = new Socket(inetAddress, port);
					HelpServer.setHost(inetAddress.getHostName());
				} else {
					socket = new Socket(stringAddress, port);
					//System.out.println("USING " + stringAddress + " " + port);
					HelpServer.setHost(stringAddress);
				}
				socket.setTcpNoDelay(true);
			} catch (IOException e) {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException ex) {
				}
				socket = null;
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException ex) {
				}
			}
		}
	};

	Connection connection;

	// The socket, or null if it couldn't be created.
	static Socket socket;
	// Constructor.  This addressTest will use Socket (String, int) to create
	// the socket.
	public AddressTest(String host, int port, long timeout) {
		connection = new Connection(host, port, timeout);
	}
	public static void main(String[] args) {
		try {
			int portNumber = 81;
			// 500 ms is a good number, don't use a number that is too small or you'll get wrong result
			int Timeout = 500;

			String host = InetAddress.getLocalHost().getHostName();
			System.out.println(Resources.getString("host_", host));
			InetAddress[] addr = InetAddress.getAllByName(host);
			System.out.println(addr.length + Resources.getString("address(es)_retrieved"));

			for (int i = 0; i < addr.length; i++) {
				System.out.println(Resources.getString("address_1", addr[i].getHostAddress()));
				testConnect(addr[i].getHostAddress(), portNumber, Timeout);
				// test all addresses retrieved from your system
			}

			System.out.println("\n");
			testConnect("127.0.0.1", portNumber, Timeout); // test 127.0.0.1 

			System.out.println("\n");
			testConnect("9.9.9.9", portNumber, Timeout); // test an obviously invalid IP 

		} catch (IOException e) {
			System.out.println(Resources.getString("Error") + e);
		}
	}
	// Make a test connection.

	static void testConnect(String address, int port, long timeout) {
		// the connection starts in a separate thread
		AddressTest test = new AddressTest(address, port, timeout);
		test.connection.start();

		// after the timeout expires, wakeup, interrupt the connection (if established)
		// and check the result.
		try {
			test.connection.join(timeout);
		} catch (InterruptedException e) {
		}
		test.connection.interrupt();

	}
}
