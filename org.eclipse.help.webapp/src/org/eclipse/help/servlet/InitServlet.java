package org.eclipse.help.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.ResourceBundle;

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
	private Eclipse eclipse;

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
			
			// In infocentre mode, initialize and save the eclipse app
			if (isInfocentre()) {
				eclipse = new Eclipse(context);
				context.setAttribute("org.eclipse.help.servlet.eclipse", eclipse);
			}		
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
	 * Returns true if running in infocentre mode.
	 * Assumptions:
	 * - in eclipse we run with catalina.home set to the tomcat plugin.
	 */
	private boolean isInfocentre() {
		ServletContext context = getServletContext();
		String base = context.getRealPath("/");
		String catalina_home = System.getProperty("catalina.home");
		// if this variable was not set, we are not running inside eclipse
		if (catalina_home == null) 
			return true;
		
		// Check if both paths have the same parent
		File f1 = new File(base);
		File f2 = new File(catalina_home);
		String p1 = f1.getParent();
		String p2 = f2.getParent();
		return !p1.equals(p2);
		
		/*
		// check if running in standalone mode
		String mode = getServletContext().getInitParameter("mode");
		return "infocentre".equals(mode);
		*/
	}

}