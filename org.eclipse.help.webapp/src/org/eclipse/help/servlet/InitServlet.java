package org.eclipse.help.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.*;
import java.util.ResourceBundle;

import javax.servlet.*;
import javax.servlet.http.*;
import org.w3c.dom.Element;

/**
 * Servlet to initialize eclipse, tocs, etc.
 * There is no doGet() or doPost().
 * NOTE: when moving to Servlet 2.3 we should use the
 * application lifecyle events and get rid of this servlet.
 */
public class InitServlet extends HttpServlet {
	private static final String RESOURCE_BUNDLE = InitServlet.class.getName();
	private ResourceBundle resBundle;
	private boolean initialized = false;
	private Eclipse eclipse;
	private Tocs tocs;
	private Search search;

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
			resBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
			
			// In infocentre mode, initialize and save the eclipse app
			if (isInfocentre()) {
				eclipse = new Eclipse(context);
				context.setAttribute("org.eclipse.help.servlet.eclipse", eclipse);
			}

			// initialize and save the tocs
			tocs = new Tocs(context);
			context.setAttribute("org.eclipse.help.tocs", tocs);
			
			// initialize and save the search
			search = new Search(context);
			context.setAttribute("org.eclipse.help.search", search);
			
		} catch (Throwable e) {
			log(resBundle.getString("problemInit"), e);
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

	/**
	 * Servlet destroy method shuts down Eclipse.
	 */
	public void destroy() {
		if (isInfocentre()) {
			if (eclipse != null)
				eclipse.shutdown();

		}
	}

	/**
	 * Returns true if running in infocentre mode
	 */
	private boolean isInfocentre() {
		// check if running in standalone mode
		//String mode = getInitParameter("mode");
		String mode = getServletContext().getInitParameter("mode");
		//System.out.println("mode="+mode);
		return "infocentre".equals(mode);
	}

}