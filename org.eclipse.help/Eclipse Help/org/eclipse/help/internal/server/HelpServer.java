package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.net.*;
import org.eclipse.help.internal.util.*;

/**
 * Lightweight web server for help content.
 * It listens on a local address at a system generated port.
 */
public class HelpServer extends Thread {

	static int port = 0; // let the system select a port
	ServerSocket ssocket = null;
	static HelpServer _this = null;
	public static int timeout = 1000;
	static Object lock = new Object();
	static String host = null;

	public HelpServer() {
		super();

		setDaemon(true);
		setName("HelpServer");
		try {
			ssocket = new ServerSocket(port); 
			this.port = ssocket.getLocalPort();
		} catch (Exception e1) {
			ssocket = null;
			Logger.logError(e1.getMessage(), e1);
			return;
		}
		int timeout = 0;
		try {
			timeout = ssocket.getSoTimeout();
			ssocket.setSoTimeout(1); // quick accept
			ssocket.accept();
			ssocket.setSoTimeout(timeout);
		} catch (IOException exc) {
			try {
				ssocket.setSoTimeout(timeout);
			} catch (SocketException sockEx) {
			}
		}
	}
	public void close() {
		// close the zips
		PluginURL.clear();
		// close the communication channel
		try {
			if (ssocket != null)
				ssocket.close();
		} catch (IOException e) {
			Logger.logDebugMessage("HelpServer", e.getMessage());
		} finally {
			ssocket = null;
		}
	}
	public static void discoverServerAddress() {
		try {
			// First try the localhost address
			AddressTest.testConnect("127.0.0.1", port, timeout);
			if (HelpServer.host != null)
				return;
			String host = InetAddress.getLocalHost().getHostName();
			InetAddress[] addr = InetAddress.getAllByName(host);
			for (int i = 0; i < addr.length; i++) {
				//System.out.println("address=" + addr[i].getHostAddress());
				// test all addresses retrieved from your system
				AddressTest.testConnect(addr[i].getHostAddress(), port, timeout);

				if (host != null)
					return;
			}

			/*
			// Try the localhost by name
			AddressTest.testConnect("localhost", port, timeout);
			if (HelpServer.goodAddress != null) return;
			*/
		} catch (IOException e) {
			System.out.println(Resources.getString("Error_1") + e);
		} finally {
			// If all fails, use localhost
			if (HelpServer.host == null)
				HelpServer.host = "localhost";
		}
	}
	public void finalize() {
		close();
	}
	/**
	 * Returns the first valid internet address for this machine.
	 */
	public static URL getAddress() {
		try {
			return new URL("http", HelpServer.host, port, "");
		} catch (MalformedURLException e) {
			return null;
		}
	}
	public static HelpServer instance() {
		if (_this == null) {
			_this = new HelpServer();
			//_this.setPriority(Thread.currentThread().getPriority()+1);
			_this.start();

			// Discover the address on which the server listens
			if (host == null || host.length() == 0)
				discoverServerAddress();
		}
		return _this;
	}
	public void run() {
		try {

			// Here we loop indefinitely, just waiting for 
			// clients to connect
			while (true) {
				if(ssocket==null)
					return;
				// accept() does not return until a client 
				// requests a connection.
				Socket sock = ssocket.accept();

				sock.setTcpNoDelay(true); // local transfer, so maximize troughput

				// Now that a client has arrived, create an instance 
				// of our special thread subclass to respond to it.
				HelpHttpConnection connection = new HelpHttpConnection(sock);
				connection.start();
			} // Now loop back around and wait 
			// for next client to be accepted. 
		} catch (IOException e) {
			// commenting out for now to avoid platform not initialized problem.
			//Logger.logInfo(e.getMessage());
		} finally {
		}
	}
	public static void setAddress(String hostValue, String portValue) {
		String oldHost = host;
		int oldPort = port;
		
		if (hostValue != null && hostValue.trim().length() > 0)
			setHost(hostValue.trim());
		else
			setHost(null);
		try {
			int port = Integer.parseInt(portValue);
			if (0 <= port && port < 0xffff)
				setPort(port);
			else
				setPort(0);
		} catch (Exception e) {
			setPort(0);
		}

		// Restart the server the local address has changed

		// nothing to do when the server does not exists
		if (_this == null)
			return;

		// test for changess:
		// if the ports and the hosts are the same, nothing to do
		if ( oldPort == port)
			if (oldHost == null && host == null)
				return;
			else if (oldHost != null && oldHost.equals(host))
				return;

		// restart the server
		_this.close();
		_this = null;
		yield();
		_this = instance();
			
	}
	public static void setHost(String hostValue) {
		host = hostValue;
	}
	public static void setPort(int portValue) {
		port = portValue;
	}
}
