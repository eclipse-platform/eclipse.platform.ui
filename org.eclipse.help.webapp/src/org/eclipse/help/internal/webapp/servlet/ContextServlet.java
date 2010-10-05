/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.IContext;
import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.context.Context;
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
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BaseHelpSystem.checkMode();
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		String id = req.getParameter(PARAMETER_ID);
		if (id != null) {
			IContext context = getContext(locale, id);
			if (context != null) {
				serialize(context, resp.getWriter());
			}
			else {
				resp.sendError(404);
			}
		}
		else {
			resp.sendError(400); // bad request; missing parameter
		}
	}

	protected IContext getContext(String locale, String id) {
		IContext context = HelpPlugin.getContextManager().getContext(id, locale);
		return context;
	}
	
	private void serialize(IContext context, Writer out) throws IOException {
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		out.write('<' + Context.NAME );
		if (context instanceof IContext2) {
			String title = ((IContext2)context).getTitle();
			if (title != null && title.length() > 0) {
				out.write(" title=\"" + title + "\""); //$NON-NLS-1$ //$NON-NLS-2$			
			}
		}
		out.write(">\n"); //$NON-NLS-1$
		String description = context.getText();
		if (description != null) {
			out.write("   <description>" + description + "</description>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		IHelpResource[] topics = context.getRelatedTopics();
		for (int i=0;i<topics.length;++i) {
			out.write("   <" + Topic.NAME); //$NON-NLS-1$
			if (topics[i].getLabel() != null) {
				out.write("\n         " + Topic.ATTRIBUTE_LABEL + "=\"" + topics[i].getLabel() + '"'); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (topics[i].getHref() != null) {
				out.write("\n         " + Topic.ATTRIBUTE_HREF + "=\"" + topics[i].getHref() + '"'); //$NON-NLS-1$ //$NON-NLS-2$
			}
			out.write(">\n   </topic>"); //$NON-NLS-1$
		}
		out.write("</" + Context.NAME + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
