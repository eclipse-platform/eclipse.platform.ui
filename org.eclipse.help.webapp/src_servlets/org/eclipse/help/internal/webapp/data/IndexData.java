/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.IHelpResource;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.index.Index;
import org.eclipse.help.internal.index.IndexEntry;
import org.eclipse.help.internal.index.IIndexTopic;

/**
 * Helper class for Index view initialization
 */
public class IndexData extends ActivitiesData {
	private Index index;

	// images directory
	private String imagesDirectory;

	// plus/minus image file name
	private String plusMinusImage;

	// name of expand/collapse class for IMG, UL tags
	private String expandedCollapsed;

	// use or not expand/collapse feature
	private boolean usePlusMinus;

	// expand all by default flag
	private boolean expandAll;

	// flag right-to-left direction of text
	private boolean isRTL;

	// global writer for private generate...() methods
	private Writer out;

	/**
	 * Constructs the data for the index page.
	 * @param context
	 * @param request
	 */
	public IndexData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);

		imagesDirectory = preferences.getImagesDirectory();
		usePlusMinus = preferences.isIndexPlusMinus();
		expandAll = preferences.isIndexExpandAll();
		plusMinusImage = expandAll ? "/minus.gif" : "/plus.gif"; //$NON-NLS-1$ //$NON-NLS-2$
		expandedCollapsed = expandAll ? "expanded" : "collapsed"; //$NON-NLS-1$ //$NON-NLS-2$
		isRTL = UrlUtil.isRTL(request, response);

		loadIndex();
	}

	/**
	 * Loads help index
	 */
	private void loadIndex() {
		index = extractEnabled(HelpPlugin.getIndexManager().getIndex(getLocale()));
	}

	/**
	 * Generates values for array of ids of list items
	 * avaliable to be navigated through typein feature.
	 *
	 * Currently only first level items can be navigated.
	 *
	 * @param out
	 * @throws IOException
	 */
	public void generateIds(Writer out) throws IOException {
		boolean first = true;
		Iterator iter = index.getEntryMap().values().iterator();
		while (iter.hasNext()) {
			IndexEntry entry = (IndexEntry)iter.next();
			if (entry != null) {
				if (first) {
					first = false;
				} else {
					out.write(",\n"); //$NON-NLS-1$
				}
				out.write("\""); //$NON-NLS-1$
				out.write(entry.getKeyword());
				out.write("\""); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Generates the HTML code (a list) for the index.
	 *
	 * @param out
	 * @throws IOException
	 */
	public void generateIndex(Writer out) throws IOException {
		this.out = out;

		Iterator iter = index.getEntryMap().values().iterator();
		while(iter.hasNext()) {
			IndexEntry entry = (IndexEntry)iter.next();
			generateEntry(entry, 0);
		}
	}

	/**
	 * Generates the HTML code for an index entry.
	 *
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * For advanced UI:
	 *   <li>[ plus_image ]<a ...>...</a>
	 *   [<ul>list of topics</ul>]
	 *   [<ul>nested entries</ul>]
	 *   </li>
	 *
	 * For basic UI:
	 *   <li><a ...>...</a>
	 *   [<ul>
	 *     list of topics
	 *     nested entries
	 *   </ul>]
	 *   </li>
	 */
	private void generateEntry(IndexEntry entry, int level) throws IOException {
		IHelpResource[] topics = entry.getTopics();
		IIndexEntry[] subentries = entry.getSubentries();
		boolean multipleTopics = topics.length > 1;
		boolean singleTopic = topics.length == 1;

		out.write("<li>"); //$NON-NLS-1$
		if (usePlusMinus && advancedUI) generatePlusImage(multipleTopics);
		generateAnchor(singleTopic, entry, level);
		if (multipleTopics || subentries.length > 0) {
			if (!advancedUI) {
				out.write("<ul>\n"); //$NON-NLS-1$
			}
			if (multipleTopics) generateTopicList(entry);
			generateSubentries(entry, level + 1);
			if (!advancedUI) {
				out.write("</ul>\n"); //$NON-NLS-1$
			}
		}
		out.write("</li>\n"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code (a list) for the index.
	 * Basic UI version.
	 *
	 * @param out
	 * @throws IOException
	 */
	public void generateBasicIndex(Writer out) throws IOException {
		this.out = out;

		Iterator iter = index.getEntryMap().values().iterator();
		while(iter.hasNext()) {
			IndexEntry entry = (IndexEntry)iter.next();
			generateBasicEntry(entry, 0);
		}
	}

	/**
	 * Generates the HTML code for an index entry.
	 * Basic UI version.
	 *
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * <tr><td align={ "left" | "right" } nowrap>
	 *   <a ...>...</a>
	 * </td></tr>
	 * [<tr><td align={ "left" | "right" } nowrap><ul>
	 *   list of topics
	 *   nested entries
	 * </ul></td></tr>]
	 */
	private void generateBasicEntry(IndexEntry entry, int level) throws IOException {
		IHelpResource[] topics = entry.getTopics();
		IIndexEntry[] subentries = entry.getSubentries();
		boolean multipleTopics = topics.length > 1;
		boolean singleTopic = topics.length == 1;

		out.write("<tr><td align=\""); //$NON-NLS-1$
		out.write(isRTL ? "right" : "left"); //$NON-NLS-1$ //$NON-NLS-2$
		out.write("\" nowrap>\n"); //$NON-NLS-1$
		generateAnchor(singleTopic, entry, level);
		out.write("</td></tr>\n"); //$NON-NLS-1$
		if (multipleTopics || subentries.length > 0) {
			out.write("<tr><td align=\""); //$NON-NLS-1$
			out.write(isRTL ? "right" : "left"); //$NON-NLS-1$ //$NON-NLS-2$
			out.write("\" nowrap><ul>\n"); //$NON-NLS-1$
			if (multipleTopics) generateTopicList(entry);
			generateSubentries(entry, level + 1);
			out.write("</ul></td></tr>\n"); //$NON-NLS-1$
		}
	}

	/**
	 * Generates the HTML code for the plus/minus image.
	 *
	 * @param multipleTopics
	 * @throws IOException
	 */
	/*
	 * <img scr="images/plus.gif" class={ "collapsed" | "expanded" | "h" } alt="...">
	 */
	private void generatePlusImage(boolean multipleTopics) throws IOException {
		out.write("<img src=\""); //$NON-NLS-1$
		out.write(imagesDirectory);
		out.write(plusMinusImage);
		out.write("\" class=\""); //$NON-NLS-1$
		if (multipleTopics) {
			out.write(expandedCollapsed);
		} else {
			out.write("h"); //$NON-NLS-1$
		}
		out.write("\" alt=\""); //$NON-NLS-1$
		if (multipleTopics) {
			if (expandAll) {
				out.write(ServletResources.getString("collapseTopicTitles", request)); //$NON-NLS-1$
			} else {
				out.write(ServletResources.getString("expandTopicTitles", request)); //$NON-NLS-1$
			}
		}
		out.write("\">"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code for an index entry anchor tag.
	 *
	 * @param singleTopic
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * For advanced UI:
	 *   <a [ id="..." ] [ class="nolink" ] href="...">...</a>
	 *
	 * For basic UI:
	 *   <a href="...">...</a>
	 */
	private void generateAnchor(boolean singleTopic, IndexEntry entry, int level) throws IOException {
		out.write("<a "); //$NON-NLS-1$
		if (level == 0 && advancedUI) {
			out.write("id=\""); //$NON-NLS-1$
			out.write(entry.getKeyword());
			out.write("\" "); //$NON-NLS-1$
		}
		if (singleTopic) {
			out.write("href=\""); //$NON-NLS-1$
			out.write(UrlUtil.getHelpURL(((IIndexTopic)entry.getTopicList().get(0)).getHref()));
			out.write("\">"); //$NON-NLS-1$
		} else {
			if (advancedUI) {
				out.write("class=\"nolink\" "); //$NON-NLS-1$
			}
			out.write("href=\"about:blank\">"); //$NON-NLS-1$
		}
		out.write(UrlUtil.htmlEncode(entry.getKeyword()));
		out.write("</a>\n"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code for a list of topics.
	 *
	 * @param entry
	 * @throws IOException
	 */
	/*
	 * For advanced UI:
	 *   <ul class={"collapsed" | "expanded"}>
	 *   <li><img class="h" src="images/plus.gif" alt=""><a href="..."><img src="images/topic.gif" alt="">...</a></li>
	 *   <li>...
	 *   </ul>
	 *
	 * For basic UI:
	 *   <li><a href="..."><img src="images/topic.gif" border=0 alt="">...</a></li>
	 *   <li>...
	 */
	private void generateTopicList(IndexEntry entry) throws IOException {
		List topics = entry.getTopicList();
		int size = topics.size();

		if (advancedUI) {
			out.write("\n<ul class=\""); //$NON-NLS-1$
			out.write(expandedCollapsed);
			out.write("\">\n"); //$NON-NLS-1$
		}
		for (int i = 0; i < size; ++i) {
			IIndexTopic topic = (IIndexTopic)topics.get(i); 

			out.write("<li>"); //$NON-NLS-1$
			if (usePlusMinus && advancedUI) {
				out.write("<img class=\"h\" src=\""); //$NON-NLS-1$
				out.write(imagesDirectory);
				out.write(plusMinusImage);
				out.write("\" alt=\"\">"); //$NON-NLS-1$
			}
			out.write("<a href=\""); //$NON-NLS-1$
			out.write(UrlUtil.getHelpURL(topic.getHref())); 
			out.write("\"><img src=\""); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/topic.gif\" "); //$NON-NLS-1$
			if (!advancedUI) {
				out.write("border=0 "); //$NON-NLS-1$
			}
			out.write("alt=\"\">"); //$NON-NLS-1$
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a></li>\n"); //$NON-NLS-1$
		}
		if (advancedUI) {
			out.write("</ul>\n"); //$NON-NLS-1$
		}
	}

	/**
	 * Generates the HTML for nested index entries.
	 *
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * For advanced UI:
	 *   <ul class="expanded">
	 *   entries...
	 *   </ul>
	 *
	 * For basic UI:
	 *   entries...
	 */
	private void generateSubentries(IndexEntry entry, int level) throws IOException {
		Iterator iter = entry.getEntryMap().values().iterator();
		if (iter.hasNext()) {
			if (advancedUI) {
				out.write("<ul class=\"expanded\">\n"); //$NON-NLS-1$
			}
			do {
				IndexEntry childEntry = (IndexEntry)iter.next();
				generateEntry(childEntry, level);
			} while (iter.hasNext());
			if (advancedUI) {
				out.write("</ul>\n"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Get index built from the given containing not filtered entries.
	 * 
	 * @param index
	 * @return
	 */
	private Index extractEnabled(Index index) {
		if (!advancedUI) {
			// activities never filtered for basic browsers
			return index;
		}
		List enabledEntries = new ArrayList();
		Iterator iter = index.getEntryMap().values().iterator();
		while (iter.hasNext()) {
			IndexEntry entry = extractEnabled((IndexEntry)iter.next());
			if (entry != null)
				enabledEntries.add(entry);
		}
		return new Index(enabledEntries);
	}

	/**
	 * Get index entry built from the given containing not filtered
	 * topics and subentries. Returns null if all topics and subentries
	 * are filtered.
	 * 
	 * @param entry
	 * @return
	 */
	private IndexEntry extractEnabled(IndexEntry entry) {
		List enabledTopics = new ArrayList();
		List enabledSubentries = new ArrayList();

		List topics = entry.getTopicList();
		if (topics != null) {
			Iterator iter = topics.iterator();
			while (iter.hasNext()) {
				IIndexTopic topic = (IIndexTopic)iter.next(); 
				if (isEnabled(topic)) {
					enabledTopics.add(topic);
				}
			}
		}

		Map subentries = entry.getEntryMap();
		if (subentries != null) {
			Iterator iter = subentries.values().iterator();
			while (iter.hasNext()) {
				IndexEntry subentry = extractEnabled((IndexEntry)iter.next()); 
				if (subentry != null) {
					enabledSubentries.add(subentry);
				}
			}
		}

		if (enabledTopics.isEmpty() && enabledSubentries.isEmpty())
			return null;

		return new IndexEntry(entry.getKeyword(),
				enabledTopics, enabledSubentries);
	}

	/**
	 * Checks if topic matches an enabled activity.
	 * 
	 * @param topic
	 * @return
	 */
	private boolean isEnabled(IIndexTopic topic) {
		return HelpBasePlugin.getActivitySupport().isEnabled(topic.getHref());
	}
}
