/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

import java.io.*;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Servlet to handle live help action requests
 */
public class LiveHelpServlet extends HttpServlet {
	private EclipseConnector connector;

	/**
	 */
	public void init() throws ServletException {
		try {
			connector = new EclipseConnector(getServletContext());
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		Eclipse eclipse =
			(Eclipse) getServletContext().getAttribute("org.eclipse.help.servlet.eclipse");
		if (connector != null && eclipse == null) {
			// it is not an infocentre
			String query = req.getQueryString();
			// Correct encoding of parameters encoded in Javascript 1.3
			String agent = req.getHeader("User-Agent").toLowerCase(Locale.US);
			boolean ie = (agent.indexOf("msie") != -1);
			boolean mozilla = (!ie && (agent.indexOf("mozilla/5") != -1));
			if (!mozilla) {
				query = UrlUtil.changeParameterEncoding(query, "arg", "arg");
			}
			//
			String url = "livehelp:?" + query;
			InputStream is = connector.openStream(url, req);
			is.close();
		}
		resp.getOutputStream().close();
	}
	/**
	 *
	 * Called by the server (via the <code>service</code> method)
	 * to allow a servlet to handle a POST request.
	 *
	 * Handle the search requests,
	 *
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req, resp);
	}
}