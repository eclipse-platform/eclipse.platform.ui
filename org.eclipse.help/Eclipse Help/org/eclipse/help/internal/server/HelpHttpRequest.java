package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.navigation.HelpNavigationManager;
import org.eclipse.help.internal.util.Resources;

/**
 * Handles a request
 */
public class HelpHttpRequest {
	private static final int NONE = 0;
	private static final int GET = 1;
	private static final int POST = 2;
	private static final int HEAD = 3;

	BufferedReader in; // request input reader
	int method; // request method: GET, POST...
	String version; // protocol version

	HelpURL helpURL; // the help URL

	InputStream contentStream; // stream of data to be sent to client

	protected int contentLength = 0;
	private static String errorStringBytes =
		"<html><head><META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>" + Resources.getString("topicNotAvailable") + "</body></html>";

	/**
	 * HelpRequest constructor comment.
	 */
	public HelpHttpRequest(InputStream inputStream) {
		super();

		try {
			// Use the client socket to obtain an input stream from it.
			InputStream sockInput = inputStream;

			// For text input we wrap an InputStreamReader around 
			// the raw input stream and set ASCII character encoding. 
			InputStreamReader isr = new InputStreamReader(sockInput, "8859_1");

			// Finally, use a BufferReader wrapper to obtain 
			// buffering and higher order read methods.
			in = new BufferedReader(isr);
		} catch (UnsupportedEncodingException e) {
			Logger.logDebugMessage("HelpHttpRequest", "encoding error: " + e.getMessage());
		} catch (Exception e) {
			Logger.logDebugMessage("HelpHttpRequest", "request failed: " + e.getMessage());
		}
	}
	public HelpURL getURL() {
		return helpURL;
	}
	public boolean isHTTP() {
		return (version != null && version.indexOf("HTTP/") != -1);
	}
	/**
	 */
	public void processRequest(HelpHttpResponse response) {
		readHeader();
		if (method == POST) {
			readBody();
		}

		if (method == GET || method == POST) {
			try {
				// Open a stream to the file.
				if (helpURL == null)
					return;

				// Prepare the output stream to the browser
				OutputStream out = response.getOutputStream();

				//Info.log(helpURL);
				InputStream inputStream = helpURL.openStream();
				if (inputStream == null) {
					helpURL =
						HelpURLFactory.createHelpURL(
							"/org.eclipse.help/" + Resources.getString("notopic.html"));
					inputStream = helpURL.openStream();
				}
				if (inputStream == null) {
					out.write(errorStringBytes.getBytes("UTF-8"));
					out.flush();
					return;
				}

				// Send header first
				response.sendHeader();

				// Fill the response body
				HelpContentManager.fillInResponse(helpURL, inputStream, out);

				// Flush all the buffered data before closing the stream
				out.flush();
				inputStream.close();
			} catch (IOException e) {
				Logger.logDebugMessage(
					"HelpHttpRequest",
					"processRequest failed: " + e.getMessage());
			}
		} else
			Logger.logInfo(
				Resources.getString("request")
					+ method
					+ Resources.getString("url_1")
					+ helpURL);
	}
	/**
	 * 
	 */
	protected void readBody() {
		try {
			if (contentLength > 0) {
				char[] inputLine = new char[contentLength];
				in.read(inputLine); // slurp all remaining content
				helpURL.addQuery(String.valueOf(inputLine));
			}

		} catch (SocketException e) {
			Logger.logDebugMessage("HelpHttpRequest", "readBody failed: " + e.getMessage());
		} catch (IOException e) {
			Logger.logDebugMessage("HelpHttpRequest", "readBody failed: " + e.getMessage());
		}
	}
	/**
	 * 
	 */
	protected void readHeader() {
		try {
			String inputLine = in.readLine();
			if (inputLine == null)
				return;
			// empty request. Note: do we need to write and end of line in the output stream??

			StringTokenizer st = new StringTokenizer(inputLine);
			String methodString = new String(st.nextToken());
			if (methodString.equals("GET"))
				this.method = GET;
			else
				if (methodString.equals("POST"))
					this.method = POST;
				else
					if (methodString.equals("HEAD"))
						this.method = HEAD;
			this.helpURL = HelpURLFactory.createHelpURL(st.nextToken());
			this.version = st.nextToken();
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.length() == 0)
					break;
				if (inputLine.startsWith("Content-Length")) {
					contentLength =
						Integer.parseInt(inputLine.substring(inputLine.lastIndexOf(" ") + 1), 10);
				}
			}

		} catch (SocketException e) {
			Logger.logDebugMessage(
				"HelpHttpRequest",
				"readHeader failed: " + e.getMessage());
		} catch (IOException e) {
			Logger.logDebugMessage(
				"HelpHttpRequest",
				"readHeader failed: " + e.getMessage());
		}
	}
}
