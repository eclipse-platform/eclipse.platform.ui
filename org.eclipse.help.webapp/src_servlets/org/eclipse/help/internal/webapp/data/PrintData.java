/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.search.HTMLDocParser;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;

/*
 * Used by the print jsp to access print-related data.
 */
public class PrintData extends RequestData {

	// for normalizing external links
	private static final Pattern PATTERN = Pattern.compile("(src|href)=\"(.*?\")", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	private final String HREF = request.getRequestURL().toString();
	private final String BASE_HREF = HREF.substring(0, HREF.lastIndexOf('/'));
	
	// html for section headings
	private final String SECTION_HTML_1 = "\n\n<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">\n<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><link rel=\"stylesheet\" href=\"" + BASE_HREF + "print.css\" charset=\"utf-8\" type=\"text/css\"></head><body dir=\"" + (UrlUtil.isRTL(request, response) ? "rtl" : "ltr") + "\"><div class=\"section\" id=\"section"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private final String SECTION_HTML_2 = "\"><h1>"; //$NON-NLS-1$
	private final String SECTION_HTML_3 = "</h1></div></body></html>\n\n"; //$NON-NLS-1$

	/*
	 * Constructs the print data for the given request.
	 */
	public PrintData(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		super(context, request, response);
	}

	/*
	 * Returns the overall topic's title.
	 */
	public String getTitle() {
		return getTopic().getLabel();
	}

	/*
	 * Returns the href of the toc containing the topic(s) to print.
	 */
	public String getTocHref() {
		return getToc().getHref();
	}
	
	/*
	 * Returns the href of the root topic to print.
	 */
	public String getTopicHref() {
		return getTopic().getHref();
	}

	/*
	 * Generates and outputs a table of contents div with links.
	 */
	public void generateToc(Writer out) throws IOException {
		out.write("<div id=\"toc\">"); //$NON-NLS-1$
		out.write("<h1>"); //$NON-NLS-1$
		out.write(getTitle());
		out.write("</h1>"); //$NON-NLS-1$
		out.write("<h2>"); //$NON-NLS-1$
		out.write(ServletResources.getString("PrintTocHeading", request)); //$NON-NLS-1$
		out.write("</h2>"); //$NON-NLS-1$
		out.write("<div id=\"toc_content\">"); //$NON-NLS-1$
		ITopic topic = getTopic();
		ITopic[] subtopics = topic.getSubtopics();
		for (int i=0;i<subtopics.length;++i) {
			generateToc(subtopics[i], String.valueOf(i + 1), out);
		}
		out.write("</div>"); //$NON-NLS-1$
		out.write("</div>"); //$NON-NLS-1$
	}

	/*
	 * Auxiliary method for recursively generating table of contents div.
	 */
	private void generateToc(ITopic topic, String sectionId, Writer out) throws IOException {
		out.write("<div class=\"toc_" + (sectionId.length() > 2 ? "sub" : "") + "entry\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		out.write(sectionId + ". " + "<a href=\"#section" + sectionId + "\">" + topic.getLabel() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ITopic[] subtopics = topic.getSubtopics();
		for (int i=0;i<subtopics.length;++i) {
			String subsectionId = sectionId + "." + (i + 1); //$NON-NLS-1$
			generateToc(subtopics[i], subsectionId, out);
		}
		out.write("</div>"); //$NON-NLS-1$
	}

	/*
	 * Generates the content to print (the merged topics).
	 */
	public void generateContent(Writer out) throws IOException {
		generateContent(getTopic(), null, out);
	}
	
	/*
	 * Auxiliary method for recursively generating print content.
	 */
	private void generateContent(ITopic topic, String sectionId, Writer out) throws IOException {
		String href = topic.getHref();
		if (href != null) {
			// get the topic content
			String pathHref = href.substring(0, href.lastIndexOf('/') + 1);
			String baseHref = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/topic" + pathHref;   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			String content = getContent(href, locale);
			
			// write the section heading
			out.write(SECTION_HTML_1);
			out.write(sectionId != null ? sectionId : ""); //$NON-NLS-1$
			out.write(SECTION_HTML_2);
			out.write(sectionId != null ? sectionId : ""); //$NON-NLS-1$
			out.write(SECTION_HTML_3);

			// normalize external links
			Matcher matcher = PATTERN.matcher(content);
			int prev = 0;
			while (matcher.find()) {
				out.write(content.substring(prev, matcher.start(2)));
				out.write(baseHref);
				out.write(matcher.group(2));
				prev = matcher.end();
			}
			out.write(content.substring(prev));
		}
		ITopic[] subtopics = topic.getSubtopics();
		for (int i=0;i<subtopics.length;++i) {
			String subsectionId = (sectionId != null ? sectionId + "." : "") + (i + 1); //$NON-NLS-1$ //$NON-NLS-2$
			generateContent(subtopics[i], subsectionId, out);
		}
	}
	
	/*
	 * Returns the string content of the referenced topic in UTF-8.
	 */
	private String getContent(String href, String locale) {
		InputStream in = HelpSystem.getHelpContent(href, locale);
		StringBuffer buf = new StringBuffer();
		if (in != null) {
			try {
				String charset = HTMLDocParser.getCharsetFromHTML(in);
				if (charset == null) {
					charset = "UTF-8"; //$NON-NLS-1$
				}
				in = HelpSystem.getHelpContent(href, locale);
				Reader reader = new BufferedReader(new InputStreamReader(in, charset));
				char[] cbuf = new char[4096];
				int num;
				while ((num = reader.read(cbuf)) > 0) {
					buf.append(cbuf, 0, num);
				}
			}
			catch (Exception e) {
				String msg = "Error retrieving print preview content for " + href; //$NON-NLS-1$
				HelpWebappPlugin.logError(msg, e);
			}
			finally {
				try {
					in.close();
				}
				catch (Exception e) {}
			}
		}
		return buf.toString();
	}

	/*
	 * Returns the toc containing the selected topic(s).
	 */
	private IToc getToc() {
		String tocParam = request.getParameter("toc"); //$NON-NLS-1$
		if (tocParam != null && tocParam.length() > 0) {
			return HelpPlugin.getTocManager().getToc(tocParam, getLocale());
		}
		String topicParam = request.getParameter("topic"); //$NON-NLS-1$
		if (topicParam != null && topicParam.length() > 0) {
			IToc[] tocs = HelpPlugin.getTocManager().getTocs(getLocale());
			for (int i=0;i<tocs.length;++i) {
				if (tocs[i].getTopic(topicParam) != null) {
					return tocs[i];
				}
			}
		}
		return null;
	}

	/*
	 * Returns the selected topic.
	 */
	private ITopic getTopic() {
		String topicParam = request.getParameter("topic"); //$NON-NLS-1$
		if (topicParam != null && topicParam.length() > 0) {
			IToc[] tocs = HelpPlugin.getTocManager().getTocs(getLocale());
			for (int i=0;i<tocs.length;++i) {
				ITopic topic = tocs[i].getTopic(topicParam);
				if (topic != null) {
					return topic;
				}
			}
			return null;
		}
		return getToc().getTopic(null);
	}
}
