/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

/**
 * Performs transfer of data from eclipse to a jsp/servlet
 */
public class EclipseConnector {
	private ServletContext context;
	private static IFilter[] noFilters = new IFilter[0];
	private static CSSFilter cssFilter = new CSSFilter();
	private static IFilter[] basicFilters = new IFilter[] { cssFilter };

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
			URLConnection con = openConnection(url, req);
			resp.setContentType(con.getContentType());
			resp.setHeader(
				"Cache-Control",
				"max-age=" + (con.getExpiration() - System.currentTimeMillis()));
			InputStream is = con.getInputStream();
			if (is == null)
				return;
			OutputStream os = resp.getOutputStream();

			IFilter[] filters = getFilters(req);
			if (filters.length == 0)
				transferContent(is, os);
			else {
				ByteArrayOutputStream tempOut = new ByteArrayOutputStream(4096);
				transferContent(is, tempOut);
				byte[] tempBuffer = tempOut.toByteArray();
				for (int i = 0; i < filters.length; i++) {
					tempBuffer = filters[i].filter(tempBuffer);
				}
				ByteArrayInputStream tempIn = new ByteArrayInputStream(tempBuffer);
				transferContent(tempIn, os);
			}
			os.flush();
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
			BufferedInputStream dataStream = new BufferedInputStream(inputStream);

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
	private URLConnection openConnection(String url, HttpServletRequest request)
		throws Exception {
		//System.out.println("help content for: " + url);

		Eclipse eclipse =
			(Eclipse) context.getAttribute("org.eclipse.help.servlet.eclipse");

		URLConnection con = null;
		if (eclipse != null) {
			// it is an infocentre, add client locale to url
			if (url.indexOf('?') >= 0) {
				url = url + "&lang=" + request.getLocale().toString();
			} else {
				url = url + "?lang=" + request.getLocale().toString();
			}
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

	/**
	 * Returns the filters for this url, if any
	 * @return array of IFilter
	 */
	private IFilter[] getFilters(HttpServletRequest req) {
		String uri = req.getRequestURI();
		String agent = req.getHeader("User-Agent").toLowerCase(Locale.US);
		boolean ie = (agent.indexOf("msie") != -1);
		// we only insert css for ie
		if (ie) {
			if (uri != null && (uri.endsWith("html") || uri.endsWith("htm"))) {
				if (UrlUtil.getRequestParameter(req, "resultof") != null)
					return new IFilter[] {
						cssFilter,
						new HighlightFilter(UrlUtil.getRequestParameter(req, "resultof"))};
				else
					return basicFilters;
			} else
				return noFilters;
		} else {
			if (UrlUtil.getRequestParameter(req, "resultof") != null)
				return new IFilter[] { new HighlightFilter(UrlUtil.getRequestParameter(req, "resultof"))};
			else
				return noFilters;
		}
	}
}