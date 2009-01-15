/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.HTMLDocParser;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;

/*
 * Used by the print jsp to access print-related data.
 */
public class PrintData extends RequestData {

	// where to inject the section numbers
	private static final Pattern PATTERN_HEADING = Pattern.compile("<body.*?>[\\s]*?([\\w])", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	// to normalize external links to new base href
	private static final Pattern PATTERN_LINK = Pattern.compile("(src|href)=\"(.*?\")", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

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
		out.write(ServletResources.getString("TocHeading", request)); //$NON-NLS-1$
		out.write("</h2>"); //$NON-NLS-1$
		out.write("<div id=\"toc_content\">"); //$NON-NLS-1$
		ITopic topic = getTopic();
		ITopic[] subtopics = EnabledTopicUtils.getEnabled(topic.getSubtopics());
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
		ITopic[] subtopics = EnabledTopicUtils.getEnabled(topic.getSubtopics());
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
		generateContent(getTopic(), null, out, new HashSet());
	}
	
	/*
	 * Auxiliary method for recursively generating print content.
	 */
	private void generateContent(ITopic topic, String sectionId, Writer out, Set generated) throws IOException {
		String href = topic.getHref();
		if (href != null) {
			// get the topic content
			href = removeAnchor(href);
			String pathHref = href.substring(0, href.lastIndexOf('/') + 1);
			String baseHref = "../topic" + pathHref;   //$NON-NLS-1$
			String content;
			if (!generated.contains(href)) {
				generated.add(href);
				content = getContent(href, locale);
				// root topic doesn't have sectionId
				if (sectionId != null) {
					content = injectHeading(content, sectionId);
				}
				content = normalizeHrefs(content, baseHref);
				out.write(content);
			}				
		}
		ITopic[] subtopics = EnabledTopicUtils.getEnabled(topic.getSubtopics());
		for (int i=0;i<subtopics.length;++i) {
			String subsectionId = (sectionId != null ? sectionId + "." : "") + (i + 1); //$NON-NLS-1$ //$NON-NLS-2$
			generateContent(subtopics[i], subsectionId, out, generated);
		}
	}

	/*
	 * Injects the sectionId into the document heading.
	 */
	private String injectHeading(String content, String sectionId) {
		Matcher matcher = PATTERN_HEADING.matcher(content);
		if (matcher.find()) {
			String heading = "<a id=\"section" + sectionId + "\">" + sectionId + ". </a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return content.substring(0, matcher.start(1)) + heading + content.substring(matcher.start(1));
		}
		return content;
	}
	
	/*
	 * Normalizes all external links since we're not at the same base href as the
	 * topics we're printing.
	 */
	private String normalizeHrefs(String content, String baseHref) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = PATTERN_LINK.matcher(content);
		int prev = 0;
		while (matcher.find()) {
			buf.append(content.substring(prev, matcher.start(2)));
			buf.append(baseHref);
			buf.append(matcher.group(2));
			prev = matcher.end();
		}
		buf.append(content.substring(prev));
		return buf.toString();
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
				InputStream rawInput = HelpSystem.getHelpContent(href, locale);
				boolean filter = BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER;
				in = DynamicXHTMLProcessor.process(href, rawInput, locale, filter);
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
			if (topicParam.startsWith("/../nav/")) { //$NON-NLS-1$
				String navPath = topicParam.substring(8);
				StringTokenizer tok = new StringTokenizer(navPath, "_"); //$NON-NLS-1$
				int index = Integer.parseInt(tok.nextToken());
				return HelpPlugin.getTocManager().getTocs(getLocale())[index];
			}
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
		String anchorParam = request.getParameter("anchor"); //$NON-NLS-1$
		if (anchorParam!=null) {
			topicParam = topicParam + '#' + anchorParam;
		}
		if (topicParam != null && topicParam.length() > 0) {
			if (topicParam.startsWith("/../nav/")) { //$NON-NLS-1$
				String navPath = topicParam.substring(8);
				StringTokenizer tok = new StringTokenizer(navPath, "_"); //$NON-NLS-1$
				int index = Integer.parseInt(tok.nextToken());
				ITopic topic = HelpPlugin.getTocManager().getTocs(getLocale())[index].getTopic(null);
				while (tok.hasMoreTokens()) {
					index = Integer.parseInt(tok.nextToken());
					topic = topic.getSubtopics()[index];
				}
				return topic;
			}
			else {
				IToc[] tocs = HelpPlugin.getTocManager().getTocs(getLocale());
				for (int i=0;i<tocs.length;++i) {
					ITopic topic = tocs[i].getTopic(topicParam);
					if (topic != null) {
						return topic;
					}
					// Test for root node as topic
					topic = tocs[i].getTopic(null);
					if (topicParam.equals(topic.getHref())) {
						return topic;
					}
				}
			}
			return null;
		}
		return getToc().getTopic(null);
	}
	
	private static String removeAnchor(String href) {
		int index = href.indexOf('#');
		if (index != -1) {
			return href.substring(0, index);
		}
		return href;
	}
}
