package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.eclipse.help.internal.util.Logger;

/**
 * Thread server class to handle clients.
 */
class HelpHttpConnection extends Thread {
	Socket client; // client socket
	// Pass the socket as a argument to the constructor
	HelpHttpConnection(Socket client) throws SocketException {
		this.client = client;
		// Set the thread priority down so that the ServerSocket
		// will be responsive to new clients.
		setPriority(NORM_PRIORITY - 1);
		setName("Connection");
	}
	/**
	 * Handles a connection request.
	 * NOTE: move some of the constants to properties and do better
	 *       handling of requests, including SECURITY issues (originating machine, etc.)
	 **/
	public void handleRequest() {
		try {
			HelpHttpRequest request = new HelpHttpRequest(client.getInputStream());
			HelpHttpResponse response =
				new HelpHttpResponse(client.getOutputStream(), request);
			request.processRequest(response);
			client.close(); // close the client socket
		} catch (IOException e) {
			Logger.logDebugMessage(
				"HelpHttpConnection",
				"connection failed: " + e.getMessage());
		}

	}
	/**
	 * Handles a connection request.
	 **/
	public void run() {
		handleRequest();
	}
}
