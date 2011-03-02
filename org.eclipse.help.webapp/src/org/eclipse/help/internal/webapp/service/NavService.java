/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.webapp.utils.Utils;

/**
 * Generates navigation HTML page where topic is not present in the table
 * of contents for the selected toc passed as request path info. Displays
 * links to the direct child topics.
 * 
 * <p>This servlet is called on infocenters by client workbenches
 * configured for remote help in order to generate the navigation pages.
 * 
 * <p>Passes the request to {@link org.eclipse.help.internal.webapp.servlet.NavServlet}
 * servlet.
 *  
 * @return	An html page having the links to the direct child topics for
 * 			the selected toc
 * 
 * @version	$Version$
 * 
 **/
public class NavService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
		
		String baseURL = req.getRequestURL().toString();
		String navURL = baseURL.replaceFirst(Utils.SERVICE_CONTEXT, ""); //$NON-NLS-1$
		String query = req.getQueryString();
		if (query != null)
			navURL += '?' + query;
		URL url = new URL(navURL);
		String response = Utils.convertStreamToString(url.openStream());
		response = Utils.updateResponse(response);
		
		OutputStream out = resp.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8")); //$NON-NLS-1$
		writer.write(response);
		writer.close();
	}

}
