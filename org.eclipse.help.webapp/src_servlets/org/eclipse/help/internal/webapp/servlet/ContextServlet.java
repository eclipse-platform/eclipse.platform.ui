/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;

/**
 * Returns context sensitive help information.
 * http://server/help/context/plugin.id.context.id
 */
public class ContextServlet extends HttpServlet {
	private String locale;

	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		locale = UrlUtil.getLocale(req);
		req.setCharacterEncoding("UTF-8");

		resp.setContentType("application/xml; charset=UTF-8");
		resp.setHeader("Cache-Control", "max-age=0");
	
		String contextId = req.getPathInfo();
		if (contextId == null || contextId.length() < 2)
			throw new ServletException();
		contextId = contextId.substring(1);
		IContext context = HelpSystem.getContextManager().getContext(contextId);
		if (context == null)
			throw new ServletException();
		
		ContextWriter resultsWriter = new ContextWriter(resp.getWriter(), locale);
		resultsWriter.generate(contextId, context, resp);
		resultsWriter.close();
	}
	/**
	 *
	 * Called by the server (via the <code>service</code> method)
	 * to allow a servlet to handle a POST request.
	 *
	 * Handle the search requests,
	 *
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		doGet(req, resp);
	}

	/**
	 * This generates the XML file for the help navigation.
	 */
	private static class ContextWriter extends XMLGenerator {
		private String locale;
		public ContextWriter(Writer writer, String locale) {
			super(writer);
			this.locale = locale;
		}

		/** 
		 * XML representation of context info.
		 */
		public void generate(String contextId, IContext context, HttpServletResponse resp) {

			println("<context id=\""+ contextId +"\">");
			pad++;
			printPad();
			print("<description>");
			print(context.getText());
			println("</description>");
			
			IHelpResource[] links = context.getRelatedTopics();
			if (links == null)
				links = new IHelpResource[0];

			for (int i = 0; i < links.length; i++) {
				printPad();
				print(
					"<topic label=\""
						+ xmlEscape(links[i].getLabel())
						+ "\""
						+ " href=\""
						+ links[i].getHref()
						+ "\"");
				IToc toc = findTocForTopic(links[i].getHref());
				if (toc != null) {
					print(
						" toc=\""
							+ toc.getHref()
							+ "\""
							+ " toclabel=\""
							+ toc.getLabel()
							+ "\"");
				}
				print(" />");
			}
			pad--;
			println("</context>");
		}
		

		/**
		 * Finds a topic in a bookshelf
		 * or within a scope if specified
		 */
		IToc findTocForTopic(String href) {
			IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
			for (int i = 0; i < tocs.length; i++) {
				ITopic topic = tocs[i].getTopic(href);
				if (topic != null)
					return tocs[i];
			}
			return null;
		}
	}
}
