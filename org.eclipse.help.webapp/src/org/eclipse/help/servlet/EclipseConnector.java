/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

/**
 * Performs transfer of data from eclipse to a jsp/servlet
 */
public class EclipseConnector {
	private ServletContext context;

	/**
	 * Constructor.
	 */
	public EclipseConnector(ServletContext context) {
		this.context = context;
	}

	public InputStream openStream(String url) {
		try {
			URLConnection con = openConnection(url);
			return con.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}

	public void transfer(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {

		try {
			String url = getURL(req);
			URLConnection con = openConnection(url);
			resp.setContentType(con.getContentType());
			resp.setHeader(
				"Cache-Control",
				"max-age=" + (con.getExpiration() - System.currentTimeMillis()));
			InputStream is = con.getInputStream();
			if (is != null) {
				OutputStream os = resp.getOutputStream();
				byte buf[] = new byte[4096];
				int n = is.read(buf);
				while (n > -1) {
					if (n > 0)
						os.write(buf, 0, n);
					n = is.read(buf);
				}
				os.flush();
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets content from the named url (this could be and eclipse defined url)
	 */
	private URLConnection openConnection(String url) throws Exception {
		//System.out.println("help content for: " + url);

		Eclipse eclipse =
			(Eclipse) context.getAttribute("org.eclipse.help.servlet.eclipse");

		URLConnection con = null;
		if (eclipse != null) {
			con = eclipse.openConnection(url);
		} else {
			URL helpURL = new URL(url);
			con = helpURL.openConnection();
		}

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
		for (Enumeration params = req.getParameterNames(); params.hasMoreElements();) {
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