package org.eclipse.help.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet to initialize eclipse, tocs, etc.
 * There is no doGet() or doPost().
 * NOTE: when moving to Servlet 2.3 we should use the
 * application lifecyle events and get rid of this servlet.
 */
public class InitServlet extends HttpServlet {
	private WebappResources resBundle;
	private boolean initialized = false;
	/**
	 * Initializes eclipse
	 */
	public void init() throws ServletException {
		//System.out.println("*** init server");
		if (initialized)
			return;

		ServletContext context = getServletContext();
		try {
			// initializes string resources
			resBundle = new WebappResources(context);

			// initialize help preferences
			WebappPreferences prefs = new WebappPreferences(context);
			context.setAttribute("WebappPreferences", prefs);

		} catch (Throwable e) {
			if (resBundle != null)
				log(resBundle.getString("problemInit", null), e);
			else
				log("Problem occured initializing Eclipse", e);
			throw new ServletException(e);
		} finally {
			// Note: should we set it to false if something failed?
			initialized = true;
		}
	}
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		//System.out.println("do get...");
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	}

}