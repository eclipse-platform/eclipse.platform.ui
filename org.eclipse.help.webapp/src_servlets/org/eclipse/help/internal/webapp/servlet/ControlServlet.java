/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.webapp.data.*;
/**
 * Servlet to control Eclipse helpApplication from standalone application.
 * Accepts the following paramters: command=displayHelp | shutdown href - may be
 * provided if comand==displayHelp
 */
public class ControlServlet extends HttpServlet {

	private HelpDisplay helpDisplay = null;
	private boolean shuttingDown = false;

	/**
	 * Called by the servlet container to indicate to a servlet that the servlet
	 * is being placed into service.
	 */
	public void init() throws ServletException {
		super.init();
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
			helpDisplay = BaseHelpSystem.getHelpDisplay();
		}
	}

	/**
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a GET request.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}
	/**
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a POST request.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}
	private void processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

		// for HTTP 1.1
		resp.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
		// for HTTP 1.0
		resp.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
		resp.setDateHeader("Expires", 0); //$NON-NLS-1$
		//prevents caching at the proxy server

		if (!UrlUtil.isLocalRequest(req)) {
			// do not allow remote clients to execute this servlet
			return;
		}
		if (!"/helpControl".equals(req.getContextPath()) //$NON-NLS-1$
				|| !"/control.html".equals(req.getServletPath())) { //$NON-NLS-1$
			// do not allow arbitrary URLs to execute this servlet
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, ""); //$NON-NLS-1$
			return;
		}

		if (shuttingDown) {
			return;
		}

		String command = req.getParameter("command"); //$NON-NLS-1$
		if (command == null) {
			// this should never happen and is invisible to the user
			resp.getWriter().print("No command."); //$NON-NLS-1$
			return;
		}

		if ("shutdown".equalsIgnoreCase(command)) { //$NON-NLS-1$
			shutdown();
		} else if ("displayHelp".equalsIgnoreCase(command)) { //$NON-NLS-1$
			if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
				displayHelp(req);
			}
		} else {
			// this should never happen and is invisible to the user
			resp.getWriter().print("Unrecognized command."); //$NON-NLS-1$
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
	 * 
	 * @param req
	 *            HttpServletRequest that might contain href parameter, which is
	 *            the resource to display
	 */
	private void displayHelp(HttpServletRequest req) {
		String href = req.getParameter("href"); //$NON-NLS-1$
		if (href != null) {
			helpDisplay.displayHelpResource(href, false);
		} else {
			helpDisplay.displayHelp(false);
		}
	}
}
