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

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.context.Context;
import org.eclipse.help.internal.dynamic.NodeWriter;
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
	private Map responseByLocaleAndId;
	private NodeWriter writer;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		String id = req.getParameter(PARAMETER_ID);
		if (id != null) {
			String localeAndId = locale + id;
			if (responseByLocaleAndId == null) {
				responseByLocaleAndId = new WeakHashMap();
			}
			String response = (String)responseByLocaleAndId.get(localeAndId);
			if (response == null) {
				Context context = HelpPlugin.getContextManager().getContext(id, locale);
				if (context != null) {
					response = serialize(context, id);
					responseByLocaleAndId.put(localeAndId, response);
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
	
	private String serialize(Context context, String id) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		if (writer == null) {
			writer = new NodeWriter();
		}
		writer.write(context, buf, true, "   ", true); //$NON-NLS-1$
		return buf.toString();
	}
}
