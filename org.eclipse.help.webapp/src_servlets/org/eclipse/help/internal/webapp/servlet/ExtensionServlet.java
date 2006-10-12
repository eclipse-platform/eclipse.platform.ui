/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

import org.eclipse.help.IContentExtension;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/*
 * Sends all topic extensions available on this host in XML form.
 * 
 * This is called on infocenters by client workbenches configured for remote
 * help in order to gather all the pieces of a document.
 */
public class ExtensionServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static Map locale2Response = new WeakHashMap();

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		
		String response = (String)locale2Response.get(locale);
		if (response == null) {
			IContentExtension[] extensions = HelpPlugin.getContentExtensionManager().getExtensions(locale);
			response = ExtensionSerializer.serialize(extensions);
			locale2Response.put(locale, response);
		}
		resp.getWriter().write(response);
	}
}
