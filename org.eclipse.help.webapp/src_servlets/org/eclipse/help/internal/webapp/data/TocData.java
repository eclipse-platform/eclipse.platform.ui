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
package org.eclipse.help.internal.webapp.data;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.internal.webapp.servlet.*;

/**
 * Helper class for tocView.jsp initialization
 */
public class TocData extends RequestData {
	// maximum number of topics in a book for generating all topics at once
	private static int loadBookAtOnceLimit;
	// suggested number of topic levels for large books
	private static int dynamicLoadDepths;
	// maximum number of topics generated when loading levels dynamically
	// above which dynamicLoadDepths is ignored, the rest of branches will be 1 deep 
	private static int honorLevelsLimit;

	// Request parameters
	private String tocHref;
	private String topicHref;

	// help form of selected topic href
	private String topicHelpHref;
	// Selected TOC
	private int selectedToc;
	// path from TOC to the root topic of the TOC fragment
	private int[] rootPath = null;
	// path from TOC to the selected topic, excluding TOC;
	private ITopic[] topicPath = null;
	// Number of topics generated so far
	private int topicsGenerated = 0;

	// List of TOC's
	private IToc[] tocs;

	// images directory
	private String imagesDirectory;

	/**
	 * Constructs the xml data for the contents page.
	 * @param context
	 * @param request
	 */
	public TocData(ServletContext context, HttpServletRequest request) {
		super(context, request);
		if (dynamicLoadDepths < 1) {
			WebappPreferences pref = new WebappPreferences();
			loadBookAtOnceLimit = pref.getBookAtOnceLimit();
			dynamicLoadDepths = pref.getLoadDepth();
			honorLevelsLimit = loadBookAtOnceLimit / 4;
		}

		this.tocHref = request.getParameter("toc");
		this.topicHref = request.getParameter("topic");
		if (tocHref != null && tocHref.length() == 0)
			tocHref = null;
		if (topicHref != null && topicHref.length() == 0)
			topicHref = null;
		// initialize rootPath
		String pathStr = request.getParameter("path");
		if (pathStr != null && pathStr.length() > 0) {
			// TODO use Java 1.4 API to do this
			String[] paths = CookieUtil.split(pathStr, '_');
			int[] indexes = new int[paths.length];
			boolean indexesOK = true;
			for (int i = 0; i < paths.length; i++) {
				try {
					indexes[i] = Integer.parseInt(paths[i]);
				} catch (NumberFormatException nfe) {
					indexesOK = false;
					break;
				}
				if (indexesOK) {
					rootPath = indexes;
				}
			}
		}

		imagesDirectory = preferences.getImagesDirectory();

		loadTocs();
	}

	// Accessor methods to avoid exposing help classes directly to JSP.
	// Note: this seems ok for now, but maybe we need to reconsider this 
	//       and allow help classes in JSP's.

	public int getTocCount() {
		return tocs.length;
	}

	public String getTocLabel(int i) {
		return tocs[i].getLabel();
	}

	public String getTocHref(int i) {
		return tocs[i].getHref();
	}

	public String getTocDescriptionTopic(int i) {
		return UrlUtil.getHelpURL(tocs[i].getTopic(null).getHref());
	}

	/**
	 * Returns the selected TOC
	 * @return int
	 */
	public int getSelectedToc() {
		return selectedToc;
	}

	/**
	 * Returns the topic to display.
	 * If there is a TOC, return its topic description.
	 * Return null if no topic is specified and there is no toc description.
	 * @return String
	 */
	public String getSelectedTopic() {
		if (topicHref != null && topicHref.length() > 0)
			return UrlUtil.getHelpURL(topicHref);
		else {
			if (selectedToc == -1)
				return null;
			IToc toc = tocs[selectedToc];
			ITopic tocDescription = toc.getTopic(null);
			if (tocDescription != null)
				return UrlUtil.getHelpURL(tocDescription.getHref());
			else
				return UrlUtil.getHelpURL(null);
		}
	}

	/**
	 * Returns a list of all the TOC's as xml elements.
	 * Individual TOC's are not loaded yet.
	 * @return Element[]
	 */
	public IToc[] getTocs() {
		return tocs;
	}

	private void loadTocs() {
		tocs = HelpSystem.getTocManager().getTocs(getLocale());
		// Find the requested TOC
		selectedToc = -1;
		if (tocHref != null && tocHref.length() > 0) {
			tocs = getTocs();
			for (int i = 0; selectedToc == -1 && i < tocs.length; i++) {
				if (tocHref.equals(tocs[i].getHref())) {
					selectedToc = i;
				}
			}
		} else {
			// try obtaining the TOC from the topic
			selectedToc = findTocContainingTopic(topicHref);

			ITopic topic = findTopic();
			if (topic != null
				&& topic instanceof org.eclipse.help.internal.toc.Topic) {
				topicPath =
					((org.eclipse.help.internal.toc.Topic) topic).getPathInToc(
						tocs[selectedToc]);
			}
		}
	}

	/**
	 * Finds a TOC that contains specified topic
	 * @param topic the topic href
	 */
	private int findTocContainingTopic(String topic) {
		if (topic == null || topic.equals(""))
			return -1;

		int index = topic.indexOf("/topic/");
		if (index != -1)
			topic = topic.substring(index + 6);
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals(""))
			return -1;

		tocs = getTocs();
		for (int i = 0; i < tocs.length; i++)
			if (tocs[i].getTopic(topic) != null)
				return i;

		// nothing found
		return -1;
	}
	/**
	 * Finds topic in a TOC
	 * @return ITopic or null
	 */
	private ITopic findTopic() {
		String topic = getSelectedTopic();
		if (topic == null || topic.equals(""))
			return null;

		int index = topic.indexOf("/topic/");
		if (index != -1)
			topic = topic.substring(index + 6);
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals(""))
			return null;

		if (getSelectedToc() < 0)
			return null;
		IToc selectedToc = getTocs()[getSelectedToc()];
		if (selectedToc == null)
			return null;
		return selectedToc.getTopic(topic);
	}

	/**
	 * Generates the HTML code (a tree) for a TOC.
	 * @param toc
	 * @param out
	 * @throws IOException
	 */
	public void generateToc(int toc, Writer out) throws IOException {
		ITopic[] topics = tocs[toc].getTopics();
		tocs[toc].getTopics();

		int maxLevels = dynamicLoadDepths;
		if (tocs[toc] instanceof Toc
			&& ((Toc) tocs[toc]).size() <= loadBookAtOnceLimit) {
			maxLevels = -1;
		}
		// Construct ID of subtree root
		StringBuffer id = new StringBuffer();
		if (rootPath != null) {
			// navigate to root topic, skipping parents
			for (int p = 0; p < rootPath.length; p++) {
				if (id.length() > 0) {
					id.append('_');
				}
				topics = topics[rootPath[p]].getSubtopics();
				id.append(rootPath[p]);
			}
			out.write("<ul class='expanded' id=\"" + id.toString() + "\">\n");
		}

		for (int i = 0; i < topics.length; i++) {
			String idPrefix = id.toString();
			if (idPrefix.length() > 0) {
				idPrefix = idPrefix + "_" + Integer.toString(i);
			} else {
				idPrefix = Integer.toString(i);
			}

			generateTopic(
				topics[i],
				out,
				idPrefix,
				maxLevels,
				rootPath == null ? 0 : rootPath.length);
		}

		if (rootPath != null) {
			out.write("</ul>\n");
		}

	}

	/**
	 * 
	 * @param topic
	 * @param out
	 * @param maxLevels relative number of topic levels to generate (pass <0 for inifinite), 1 generates this topic as last level topic
	 * @param currentLevel current level of topic, 0 is first Level under TOC
	 * @throws IOException
	 */
	private void generateTopic(
		ITopic topic,
		Writer out,
		String id,
		int maxLevels,
		int currentLevel)
		throws IOException {
		if (maxLevels == 0) {
			return;
		}

		topicsGenerated++;
		if (maxLevels > 1 && topicsGenerated > honorLevelsLimit) {
			maxLevels = 1;
		}

		boolean hasNodes = topic.getSubtopics().length > 0;

		if (hasNodes) {
			out.write("<li>");
			out.write("<img src='");
			out.write(imagesDirectory);
			out.write("/plus.gif' class='collapsed' >");
			out.write("<a href='" + UrlUtil.getHelpURL(topic.getHref()) + "'>");
			out.write("<img src='");
			out.write(imagesDirectory);
			out.write("/container_obj.gif'>");
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>");

			// is it ancestor of topic to reveal
			boolean isAncestor =
				topicPath != null
					&& topicPath.length > currentLevel + 1
					&& topicPath[currentLevel] == topic;

			if (maxLevels != 1 || isAncestor) {
				out.write("<ul class='collapsed'>\n");
			} else {
				// children will not be generated
				out.write("<ul class='collapsed' id=\"" + id + "\">\n");
			}

			ITopic[] topics = topic.getSubtopics();
			if (1 <= maxLevels
				&& maxLevels <= dynamicLoadDepths
				&& isAncestor) {
				// ignore max levels, show children
				for (int i = 0; i < topics.length; i++) {
					generateTopic(
						topics[i],
						out,
						id + "_" + i,
						dynamicLoadDepths,
						currentLevel + 1);
				}
			} else {
				for (int i = 0; i < topics.length; i++) {
					generateTopic(
						topics[i],
						out,
						id + "_" + i,
						maxLevels - 1,
						currentLevel + 1);
				}
			}

			out.write("</ul>\n");
		} else {
			out.write("<li>");
			out.write("<img src='");
			out.write(imagesDirectory);
			out.write("/plus.gif' class='h'>");
			out.write("<a href='" + UrlUtil.getHelpURL(topic.getHref()) + "'>");
			out.write("<img src='");
			out.write(imagesDirectory);
			out.write("/topic.gif'>");
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>");
		}

		out.write("</li>\n");
	}

	/**
	 * Generates the HTML code (a tree) for a TOC.
	 * @param toc
	 * @param out
	 * @throws IOException
	 */
	public void generateBasicToc(int toc, Writer out) throws IOException {
		ITopic[] topics = tocs[toc].getTopics();
		for (int i = 0; i < topics.length; i++) {
			generateBasicTopic(topics[i], out);
		}

	}

	private void generateBasicTopic(ITopic topic, Writer out)
		throws IOException {

		out.write("<li>");
		boolean hasNodes = topic.getSubtopics().length > 0;
		if (hasNodes) {
			out.write("<nobr>");
			out.write("<a ");
			if (getSelectedTopicHelpHref().equals(topic.getHref())) {
				out.write("name=\"selectedItem\" ");
			}
			out.write("href='" + UrlUtil.getHelpURL(topic.getHref()) + "'>");
			out.write("<img src='");
			out.write(imagesDirectory);
			out.write("/container_obj.gif' border=0>&nbsp;");
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>");
			out.write("</nobr>");

			out.write("<ul>\n");

			ITopic[] topics = topic.getSubtopics();
			for (int i = 0; i < topics.length; i++) {
				generateBasicTopic(topics[i], out);
			}

			out.write("</ul>\n");
		} else {
			out.write("<nobr>");
			out.write("<a ");
			if (getSelectedTopicHelpHref().equals(topic.getHref())) {
				out.write("name=\"selectedItem\" ");
			}
			out.write("href='" + UrlUtil.getHelpURL(topic.getHref()) + "'>");
			out.write("<img src='");
			out.write(imagesDirectory);
			out.write("/topic.gif' border=0>&nbsp;");
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>");
			out.write("</nobr>");
		}

		out.write("</li>\n");
	}
	/**
	 * @return String - help form of selected topic URL, or ""
	 */
	private String getSelectedTopicHelpHref() {
		if (topicHelpHref == null) {
			String topic = getSelectedTopic();
			if (topic == null || topic.length() == 0) {
				topicHelpHref = "";
				return topicHelpHref;
			}
			int index = topic.indexOf("/topic/");
			if (index != -1)
				topic = topic.substring(index + 6);
			index = topic.indexOf('?');
			if (index != -1)
				topic = topic.substring(0, index);
			topicHelpHref = topic;
			if (topic == null) {
				topicHelpHref = "";
			}
		}
		return topicHelpHref;
	}
}
