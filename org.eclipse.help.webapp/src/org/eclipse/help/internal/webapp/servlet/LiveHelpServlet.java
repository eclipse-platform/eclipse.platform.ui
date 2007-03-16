/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.webapp.data.WebappPreferences;

/**
 * Servlet to handle live help action requests
 */
public class LiveHelpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 */
	public void init() throws ServletException {
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER) {
			throw new ServletException();
		}
	}

	/**
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a GET request.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER) {
			return;
		}
		if (!new WebappPreferences().isActiveHelp()) {
			return;
		}
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		String pluginID = req.getParameter("pluginID"); //$NON-NLS-1$
		if (pluginID == null)
			return;
		String className = req.getParameter("class"); //$NON-NLS-1$
		if (className == null)
			return;
		String arg = req.getParameter("arg"); //$NON-NLS-1$
		BaseHelpSystem.runLiveHelp(pluginID, className, arg);
	}
	/**
	 * 
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a POST request.
	 * 
	 * Handle the search requests,
	 *  
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
