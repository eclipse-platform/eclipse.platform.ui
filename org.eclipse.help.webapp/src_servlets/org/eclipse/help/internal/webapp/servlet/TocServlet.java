/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.internal.webapp.data.*;
/**
 * URL-like description of help table of contents.
 * <ul>
 * <li>toc/pluginid/tocfile.xml: the toc defined by the specified toc xml</li>
 * <li>toc/: all the toc's</li>
 * <li>toc/?topic=/pluginid/topic.html: a list of toc that contain the
 * specified topic</li>
 * </ul>
 */
public class TocServlet extends HttpServlet {
	private String locale;

	/**
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a GET request.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		resp.setHeader("Cache-Control", "max-age=10000"); //$NON-NLS-1$ //$NON-NLS-2$

		if ("/".equals(req.getPathInfo())) { //$NON-NLS-1$
			if (req.getParameter("topic") == null) //$NON-NLS-1$
				serializeTocs(resp);
			else
				serializeTocs(
						findTocContainingTopic(req.getParameter("topic")), //$NON-NLS-1$
						resp);
		} else {
			serializeToc(req.getPathInfo(), resp);
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
		doGet(req, resp);
	}

	/**
	 * XML representation of TOC
	 */
	private void serializeToc(String tocID, HttpServletResponse resp)
			throws ServletException, IOException {
		IToc toc = HelpPlugin.getTocManager().getToc(tocID, locale);
		serializeToc(toc, resp);
	}
	/**
	 * XML representation of TOC
	 */
	private void serializeToc(IToc toc, HttpServletResponse resp)
			throws ServletException, IOException {
		if (toc == null)
			throw new ServletException();

		TocWriter tocWriter = new TocWriter(resp.getWriter());
		tocWriter.generate(toc, true);
		tocWriter.close();
	}

	/**
	 * XML representation of TOC list
	 */
	private void serializeTocs(HttpServletResponse resp)
			throws ServletException, IOException {
		TocManager tocManager = HelpPlugin.getTocManager();
		IToc[] tocs = tocManager.getTocs(locale);

		TocWriter gen = new TocWriter(resp.getWriter());
		gen.println("<tocs>"); //$NON-NLS-1$
		gen.pad++;
		for (int i = 0; i < tocs.length; i++) {
			gen.printPad();
			gen.generate(tocs[i], false);
		}
		gen.pad--;
		gen.println("</tocs>"); //$NON-NLS-1$
		gen.close();
	}

	private void serializeTocs(IToc toc, HttpServletResponse resp)
			throws ServletException, IOException {
		if (toc == null)
			throw new ServletException();

		TocWriter gen = new TocWriter(resp.getWriter());
		gen.println("<tocs>"); //$NON-NLS-1$
		gen.pad++;
		gen.printPad();
		gen.generate(toc, false);
		gen.pad--;
		gen.println("</tocs>"); //$NON-NLS-1$
		gen.close();
	}

	/**
	 * Finds a TOC that contains specified topic
	 * 
	 * @param topic
	 *            the topic href
	 */
	private IToc findTocContainingTopic(String topic) {
		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return null;

		int index = topic.indexOf("/topic/"); //$NON-NLS-1$
		if (index != -1)
			topic = topic.substring(index + 6);
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return null;

		IToc[] tocs = HelpPlugin.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++)
			if (tocs[i].getTopic(topic) != null)
				return tocs[i];

		// nothing found
		return null;
	}

	/**
	 * This generates the XML file for the help navigation.
	 */
	private static class TocWriter extends XMLGenerator {
		/**
		 * @param writer
		 *            java.io.Writer
		 */
		public TocWriter(Writer writer) {
			super(writer);
		}

		/**
		 * Writes out xml data for a toc
		 */
		public void generate(IToc toc, boolean genTopics) {
			// get the topic description
			String topicDescription = ""; //$NON-NLS-1$
			ITopic topic = toc.getTopic(null);
			if (topic != null)
				topicDescription = topic.getHref();

			println("<toc label=\"" //$NON-NLS-1$
					+ xmlEscape(toc.getLabel()) + "\" href=\"" //$NON-NLS-1$
					+ reduceURL(toc.getHref()) + "\" topic=\"" //$NON-NLS-1$
					+ reduceURL(topicDescription) + "\">"); //$NON-NLS-1$
			if (genTopics) {
				ITopic[] topics = toc.getTopics();
				for (int i = 0; i < topics.length; i++) {
					generate(topics[i]);
				}
			}
			println("</toc>"); //$NON-NLS-1$
		}

		/**
		 * Generates part of navigation for a given Topic and it children Topic
		 */
		protected void generate(ITopic topic) {
			pad++;
			printPad();
			String href = topic.getHref();
			print("<topic label=\"" //$NON-NLS-1$
					+ xmlEscape(topic.getLabel()) + "\"" //$NON-NLS-1$
					+ (href != null ? " href=\"" + reduceURL(href) + "\"" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITopic[] subtopics = topic.getSubtopics();
			if (subtopics.length > 0) {
				println(">"); //$NON-NLS-1$
				for (int i = 0; i < subtopics.length; i++) {
					generate(subtopics[i]);
				}
				printPad();
				println("</topic>"); //$NON-NLS-1$
			} else {
				println(" />"); //$NON-NLS-1$
			}
			pad--;
		}

		/**
		 * Simplifies url path by removing "string/.." from the path
		 * 
		 * @return reduced url String
		 * @param url
		 *            String
		 */
		protected static String reduceURL(String url) {
			if (url == null)
				return url;
			while (true) {
				int index = url.indexOf("/..", 1); //$NON-NLS-1$
				if (index <= 0)
					break;
				//there is no "/.." or nothing before "/.." to simplify
				String part1 = url.substring(0, index);
				String part2 = url.substring(index + "/..".length()); //$NON-NLS-1$
				index = part1.lastIndexOf("/"); //$NON-NLS-1$
				if (index >= 0)
					url = part1.substring(0, index) + part2;
				else
					url = part2;
			}
			return url;
		}
	}
}
