package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.Date;

/**
 * Response for a request
 */
public class HelpHttpResponse {
	private OutputStream out; // the output stream back to the client
	private HelpHttpRequest request; // the request for which this response is for
	/**
	 * HelpResponse constructor comment.
	 */
	public HelpHttpResponse(OutputStream out, HelpHttpRequest req) {
		super();
		this.out = out;
		this.request = req;
	}
	public OutputStream getOutputStream() {
		return out;
	}
	public void sendHeader() {
		try {
			if (request.isHTTP()) {
				out.write("HTTP/1.1 200 OK\r\n".getBytes());
				Date now = new Date();
				out.write(("Date: " + now + "\r\n").getBytes());
				out.write("Server: HelpServer 1.0\r\n".getBytes());
				if (request.getURL().isCacheable())
					out.write("Cache-Control: max-age=10000\r\n".getBytes());
				else
					out.write("Cache-Control: no-cache\r\n".getBytes());

				//out.write(
				//  ("Content-Length: " + request.getURL().getContentSize() + "\r\n").getBytes());
				out.write(
					("Content-Type: " + request.getURL().getContentType() + "\r\n\r\n").getBytes());
			}
		} catch (IOException e) {
		}
	}
}
