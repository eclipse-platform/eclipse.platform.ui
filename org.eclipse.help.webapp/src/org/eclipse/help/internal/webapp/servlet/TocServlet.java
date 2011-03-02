/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import javax.xml.transform.TransformerException;

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.dynamic.DocumentWriter;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.toc.TocContribution;
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
	private DocumentWriter writer;
	private static boolean clearCache;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// set the character-set to UTF-8 before calling resp.getWriter()
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		resp.getWriter().write(processRequest(req, resp));
	}

	protected String processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BaseHelpSystem.checkMode();
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		
		if (clearCache){
			responseByLocale = new WeakHashMap();
			clearCache = false;
		}
		
		if (responseByLocale == null) {
			responseByLocale = new WeakHashMap();
		}
		String response = (String)responseByLocale.get(locale);
		if (response == null) {
			TocContribution[] contributions = HelpPlugin.getTocManager().getTocContributions(locale);
			try {
				response = serialize(contributions, locale);
			}
			catch (TransformerException e) {
				throw new ServletException(e);
			}
			responseByLocale.put(locale, response);
		}
		
		return (response != null) ? response : ""; //$NON-NLS-1$
	}
		
	protected String serialize(TocContribution[] contributions, String locale) throws TransformerException {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<tocContributions>\n"); //$NON-NLS-1$
		if (writer == null) {
			writer = new DocumentWriter();
		}
		for (int i = 0; i < contributions.length; ++i) {
			TocContribution contrib = contributions[i];
			if (!contrib.isSubToc()) {
				buf.append("<tocContribution"); //$NON-NLS-1$
				if (contrib.getCategoryId() != null) {
					buf.append("\n      categoryId=\"" + contrib.getCategoryId() + '"'); //$NON-NLS-1$
				}
				if (contrib.getContributorId() != null) {
					buf.append("\n      contributorId=\"" + contrib.getContributorId() + '"'); //$NON-NLS-1$
				}
				buf.append("\n      id=\"" + contrib.getId() + '"'); //$NON-NLS-1$
				buf.append("\n      locale=\"" + contrib.getLocale() + '"'); //$NON-NLS-1$
				buf.append("\n      isPrimary=\"" + contrib.isPrimary() + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append(writer.writeString((Toc)contrib.getToc(), false));
				String[] hrefs = contrib.getExtraDocuments();
				for (int j=0;j<hrefs.length;++j) {
					buf.append("   <extraDocument href=\"" + hrefs[j] + "\"/>\n");  //$NON-NLS-1$//$NON-NLS-2$
				}
				buf.append("</tocContribution>\n"); //$NON-NLS-1$
			}
		}
		buf.append("</tocContributions>\n"); //$NON-NLS-1$
		return buf.toString();
	}
	
	public static void clearCache()
	{
		clearCache = true;
	}
}
