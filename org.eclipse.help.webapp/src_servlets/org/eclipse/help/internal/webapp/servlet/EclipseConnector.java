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
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.webapp.data.*;

/**
 * Performs transfer of data from eclipse to a jsp/servlet
 */
public class EclipseConnector {
	private ServletContext context;
	private static final String errorPageBegin =
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"
			+ "<html><head>\n"
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
			+ "</head>\n"
			+ "<body><p>\n";
	private static final String errorPageEnd = "</p></body></html>";
	private static final IFilter filters[] =
		new IFilter[] { new FramesetFilter(), new HighlightFilter()};

	/**
	 * Constructor.
	 */
	public EclipseConnector(ServletContext context) {
		this.context = context;
	}

	public InputStream openStream(String url, HttpServletRequest request) {
		try {
			URLConnection con = openConnection(url, request);
			return con.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}

	public void transfer(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {

		try {

			String url = getURL(req);
			if (url == null)
				return;
			if (url.toLowerCase().startsWith("file:/")
				|| url.toLowerCase().startsWith("jar:file:/")) {
				int i = url.indexOf('?');
				if (i != -1)
					url = url.substring(0, i);
				// ensure the file is only accessed from a local installation
				if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER
					|| !UrlUtil.isLocalRequest(req)) {
					return;
				}
			} else
				url = "help:" + url;

			URLConnection con = openConnection(url, req);
			resp.setContentType(con.getContentType());

			long maxAge = 0;
			try {
				// getExpiration() throws NullPointerException when URL is jar:file:...
				long expiration = con.getExpiration();
				maxAge = expiration - System.currentTimeMillis();
				if (maxAge < 0 )
					maxAge = 0;
			} catch (Exception e) {
			}
			resp.setHeader("Cache-Control", "max-age=" + maxAge);

			InputStream is;
			try {
				is = con.getInputStream();
			} catch (IOException ioe) {
				if (url.toLowerCase().endsWith("htm")
					|| url.toLowerCase().endsWith("html")) {
					String error =
						errorPageBegin
							+ ServletResources.getString("noTopic", req)
							+ errorPageEnd;
					is = new ByteArrayInputStream(error.getBytes("UTF8"));
				} else {
					return;
				}
			}

			OutputStream out = resp.getOutputStream();
			for (int i = 0; i < filters.length; i++) {
				out = filters[i].filter(req, out);
			}

			transferContent(is, out);
			out.flush();
			is.close();

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * Write the body to the response
	 */
	private void transferContent(InputStream inputStream, OutputStream out)
		throws IOException {
		try {
			// Prepare the input stream for reading
			BufferedInputStream dataStream =
				new BufferedInputStream(inputStream);

			// Create a fixed sized buffer for reading.
			// We could create one with the size of availabe data...
			byte[] buffer = new byte[4096];
			int len = 0;
			while (true) {
				len = dataStream.read(buffer); // Read file into the byte array
				if (len == -1)
					break;
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Gets content from the named url (this could be and eclipse defined url)
	 */
	private URLConnection openConnection(
		String url,
		HttpServletRequest request)
		throws Exception {
		//System.out.println("help content for: " + url);

		URLConnection con = null;
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER) {
			// it is an infocentre, add client locale to url
			String locale = UrlUtil.getLocale(request);
			if (url.indexOf('?') >= 0) {
				url = url + "&lang=" + locale;
			} else {
				url = url + "?lang=" + locale;
			}
		}
		URL helpURL = new URL(url);
		String protocol = helpURL.getProtocol();
		if (!("help".equals(protocol)
			|| "file".equals(protocol)
			|| "jar".equals(protocol))) {
			throw new IOException();
		}
		con = helpURL.openConnection();

		con.setAllowUserInteraction(false);
		con.setDoInput(true);
		con.connect();
		return con;
	}

	/**
	 * Extracts the url from a request
	 */
	private String getURL(HttpServletRequest req) {
		String query = "";
		boolean firstParam = true;
		for (Enumeration params = req.getParameterNames();
			params.hasMoreElements();
			) {
			String param = (String) params.nextElement();
			String[] values = req.getParameterValues(param);
			if (values == null)
				continue;
			for (int i = 0; i < values.length; i++) {
				if (firstParam) {
					query += "?" + param + "=" + values[i];
					firstParam = false;
				} else
					query += "&" + param + "=" + values[i];
			}
		}

		// the request contains the eclipse url help: or search:
		String url = req.getPathInfo() + query;
		if (url.startsWith("/"))
			url = url.substring(1);
		return url;
	}

}
