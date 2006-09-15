/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/*
 * Returns a context help entry with the id specified in the id parameter.
 * 
 * This is called on infocenters by client workbenches configured for remote
 * help in order to retrieve context help stored on the remote help server.
 */
public class ContextServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String PARAMETER_ID = "id"; //$NON-NLS-1$
	private static Map localeAndId2Response = new WeakHashMap();
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		String id = req.getParameter(PARAMETER_ID);
		if (id != null) {
			String localeAndId = locale + id;
			String response = (String)localeAndId2Response.get(localeAndId);
			if (response == null) {
				IContext context = HelpSystem.getContext(id, locale);
				if (context != null) {
					response = ContextSerializer.serialize(context, id);
					localeAndId2Response.put(localeAndId, response);
				}
			}
			if (response != null) {
				resp.getWriter().write(response);
			}
			else {
				resp.sendError(404);
			}
		}
		else {
			resp.sendError(400); // bad request; missing parameter
		}
	}
}
