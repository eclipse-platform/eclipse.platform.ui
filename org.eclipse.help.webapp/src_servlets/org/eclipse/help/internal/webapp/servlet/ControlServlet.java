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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.help.IHelp;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.webapp.data.*;
/**
 * Servlet to control Eclipse helpApplication from standalone application.
 * Accepts the following paramters:
 *  command=displayHelp | shutdown
 *  href - may be provided if comand==displayHelp
 */
public class ControlServlet extends HttpServlet {

	private IHelp helpSupport = null;
	private boolean shuttingDown = false;

	/**
	 * Called by the servlet container to indicate to a servlet
	 * that the servlet is being placed into service.
	 */
	public void init() throws ServletException {
		super.init();
		if (HelpSystem.getMode() == HelpSystem.MODE_STANDALONE) {
			helpSupport = HelpSystem.getHelpSupport();
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

		if (!UrlUtil.isLocalRequest(req)) {
			// do not allow remote clients to execute this servlet
			return;
		}
		if (!"/helpControl".equals(req.getContextPath())
			|| !"/control.html".equals(req.getServletPath())) {
			// do not allow arbitrary URLs to execute this servlet
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "");
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
