/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelp;
import org.eclipse.help.internal.*;
/**
 * Servlet to control Eclipse helpApplication from standalone application.
 * Accepts the following paramters:
 *  command=displayHelp | shutdown
 *  href - may be provided if comand==displayHelp
 */
public class ControlServlet extends HttpServlet {
	private static final String HELP_KEY = "org.eclipse.ui.help";
	private static final String HELP_SYSTEM_EXTENSION_ID =
		"org.eclipse.help.support";
	private static final String HELP_SYSTEM_CLASS_ATTRIBUTE = "class";
	private IHelp helpSupport = null;
	private boolean shuttingDown = false;

	/**
	 * Called by the servlet container to indicate to a servlet
	 * that the servlet is being placed into service.
	 */
	public void init() throws ServletException {
		super.init();
		if (HelpSystem.getMode() == HelpSystem.MODE_STANDALONE) {
			IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
			IExtensionPoint point =
				pluginRegistry.getExtensionPoint(HELP_SYSTEM_EXTENSION_ID);
			if (point != null) {
				IExtension[] extensions = point.getExtensions();
				if (extensions.length != 0) {
					// There should only be one extension/config element so we just take the first
					IConfigurationElement[] elements =
						extensions[0].getConfigurationElements();
					if (elements.length != 0) { // Instantiate the app server
						try {
							helpSupport =
								(IHelp) elements[0].createExecutableExtension(
									HELP_SYSTEM_CLASS_ATTRIBUTE);
						} catch (CoreException e) {
							// may need to change this
							HelpPlugin.getDefault().getLog().log(e.getStatus());
						}
					}
				}
			}
		}
	}

	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		processRequest(req, resp);
	}
	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a POST request. 
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		processRequest(req, resp);
	}
	private void processRequest(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");

		resp.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
		resp.setHeader("Pragma", "no-cache"); //HTTP 1.0
		resp.setDateHeader("Expires", 0);
		//prevents caching at the proxy server

		if (!"/helpControl".equals(req.getContextPath())
			|| !"/control.html".equals(req.getServletPath())) {
			// do not allow arbitrary URLs to execute this servlet
			resp.sendError(resp.SC_FORBIDDEN, "");
			return;
		}

		if (shuttingDown) {
			return;
		}

		String command = req.getParameter("command");
		if (command == null) {
			resp.getWriter().print("No command.");
			return;
		}

		if ("shutdown".equalsIgnoreCase(command)) {
			shutdown();
		} else if ("displayHelp".equalsIgnoreCase(command)) {
			if (HelpSystem.getMode() == HelpSystem.MODE_STANDALONE) {
				displayHelp(req);
			}
		} else {
			resp.getWriter().print("Unrecognized command.");
		}
	}
	/**
	 * Shuts-down Eclipse helpApplication.
	 */
	private void shutdown() {
		shuttingDown = true;
		HelpApplication.stop();
	}

	/**
	 * Displays help.
	 * @param req HttpServletRequest that might contain
	 * href parameter, which is the resource to display
	 */
	private void displayHelp(HttpServletRequest req) {
		String href = req.getParameter("href");
		if (href != null) {
			helpSupport.displayHelpResource(href);
		} else {
			helpSupport.displayHelp();
		}
	}
}