/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.base.BaseHelpSystem;
/**
 * Servlet to interface client with remote Eclipse
 */
public class ContentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
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
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a GET request.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		BaseHelpSystem.checkMode();
		if (connector != null) {
			connector.transfer(req, resp);
		}
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
		if (connector != null)
			connector.transfer(req, resp);
	}
}
