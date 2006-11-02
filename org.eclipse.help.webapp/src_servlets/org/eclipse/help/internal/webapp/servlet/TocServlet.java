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

import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.dynamic.NodeWriter;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/*
 * Sends all toc contributions available on this host in XML form. The toc
 * is not send in assembled form, but instead fragments, because complete books
 * may be distributed between remote and local.
 * 
 * This is called on infocenters by client workbenches configured for remote
 * help in order to gather all the toc fragments and assemble them into a
 * complete toc.
 */
public class TocServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Map responseByLocale;
	private NodeWriter writer;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		
		if (responseByLocale == null) {
			responseByLocale = new WeakHashMap();
		}
		String response = (String)responseByLocale.get(locale);
		if (response == null) {
			TocContribution[] contributions = HelpPlugin.getTocManager().getTocContributions(locale);
			response = serialize(contributions, locale);
			responseByLocale.put(locale, response);
		}
		resp.getWriter().write(response);
	}
		
	private String serialize(TocContribution[] contributions, String locale) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<tocContributions>\n"); //$NON-NLS-1$
		for (int i = 0; i < contributions.length; ++i) {
			if (writer == null) {
				writer = new NodeWriter();
			}
			writer.write(contributions[i], buf, true, "   ", false); //$NON-NLS-1$
		}
		buf.append("</tocContributions>\n"); //$NON-NLS-1$
		return buf.toString();
	}
}
